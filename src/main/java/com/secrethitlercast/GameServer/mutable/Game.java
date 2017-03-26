package com.secrethitlercast.GameServer.mutable;

import static com.secrethitlercast.GameServer.utils.Utils.toShuffledList;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import lombok.Synchronized;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterables;
import com.secrethitlercast.GameServer.domain.GameResult;
import com.secrethitlercast.GameServer.domain.GameState;
import com.secrethitlercast.GameServer.domain.GameState.GameStateBuilder;
import com.secrethitlercast.GameServer.domain.HiddenData;
import com.secrethitlercast.GameServer.domain.User;
import com.secrethitlercast.GameServer.domain.enums.ExecutivePower;
import com.secrethitlercast.GameServer.domain.enums.GameBoard;
import com.secrethitlercast.GameServer.domain.enums.GovernmentRole;
import com.secrethitlercast.GameServer.domain.enums.Party;
import com.secrethitlercast.GameServer.domain.enums.SecretRole;
import com.secrethitlercast.GameServer.domain.enums.State;
import com.secrethitlercast.GameServer.domain.enums.Vote;
import com.secrethitlercast.GameServer.exceptions.GameRuleException;
import com.secrethitlercast.GameServer.questions.Answer;
import com.secrethitlercast.GameServer.questions.ChancellorLegislationQuestion;
import com.secrethitlercast.GameServer.questions.ChancellorQuestion;
import com.secrethitlercast.GameServer.questions.ElectionQuestion;
import com.secrethitlercast.GameServer.questions.ExecutionQuestion;
import com.secrethitlercast.GameServer.questions.ExecutiveInvestigationResultQuestion;
import com.secrethitlercast.GameServer.questions.ExecutiveInvestigationTargetQuestion;
import com.secrethitlercast.GameServer.questions.PlayerReadyQuestion;
import com.secrethitlercast.GameServer.questions.PolicyPeekQuestion;
import com.secrethitlercast.GameServer.questions.PresidentialLegislationQuestion;
import com.secrethitlercast.GameServer.questions.PresidentialVetoQuestion;
import com.secrethitlercast.GameServer.questions.Question;
import com.secrethitlercast.GameServer.questions.SpecialElectionQuestion;
import com.secrethitlercast.GameServer.utils.OutputGenerator;

public class Game {
  private static final ImmutableList<Party> STARTING_POLICY_DECK = ImmutableMultiset
      .<Party>builder().addCopies(Party.LIBERAL, 6).addCopies(Party.FASCIST, 11).build().asList();

  private final String code;
  private volatile GameState state;
  private volatile ImmutableMap<String, String> outputByUsername;

  public Game(String code) {
    this.code = code;
    state = GameState.builder().state(State.WAITING_FOR_PLAYERS)
        .randomPlayerOffset(ThreadLocalRandom.current().nextInt(state.getPlayers().size())).build();
    updateOutputByUsername();
  }

  public ImmutableList<User> getPlayers() {
    return state.getPlayers();
  }

  public String getCode() {
    return code;
  }

  public String getOutputFor(User user) {
    return outputByUsername.get(user.getName());
  }

  @Synchronized
  public void joinGame(User user) throws GameRuleException {
    GameRuleException.check(state.getState() == State.WAITING_FOR_PLAYERS,
        "Game has already started");
    GameRuleException.check(state.getPlayers().size() < 10, "Game is full");
    GameRuleException.check(
        state.getPlayers().stream().map(User::getName).noneMatch(isEqual(user.getName())),
        "Somebody in the game already has your name");
    state = state.toBuilder().player(user).updateHiddenDataFor(user,
        gameStateBuilder -> gameStateBuilder.question(new PlayerReadyQuestion())).build();
    updateOutputByUsername();
  }

  @Synchronized
  public void answerQuestion(User player, int questionId, int answerId) {
    HiddenData data =
        GameRuleException.checkNotNull(state.getHiddenData().get(player), "Hidden Data");
    Question question = GameRuleException.checkNotNull(data.getQuestion(), "Question");
    GameRuleException.check(question.getState() == state.getState(),
        "The question is for a different state than the one the game is in. %s != %s",
        question.getState(), state.getState());
    GameRuleException.check(questionId == question.getId(),
        "This question is not the one you are being asked right now");
    GameRuleException.check(question.getValidAnswerIds().contains(answerId),
        "This answer doesn't match the question");
    Answer answer =
        question.getAnswers().stream().filter(ans -> answerId == ans.getId()).findFirst().get();
    GameStateBuilder gameStateBuilder = state.toBuilder();
    gameStateBuilder.updateHiddenDataFor(player,
        hiddenDataBuilder -> hiddenDataBuilder.question(null));
    switch (questionId) {
      case PlayerReadyQuestion.ID:
        playerReady(player, gameStateBuilder);
        break;

      case ChancellorQuestion.ID:
        User chancellorCandidate = state.getPlayers().stream()
            .filter(user -> answer.getAnswer().equals(user.getName())).findFirst().get();
        User presidentialCandidate = GameRuleException.checkNotNull(
            state.getCurrentCandidates().get(GovernmentRole.PRESIDENT), "Presidential Candidate");
        advanceStateToWaitingForElection(gameStateBuilder, chancellorCandidate,
            presidentialCandidate);
        break;

      case ElectionQuestion.ID:
        castVote(gameStateBuilder, player, Vote.fromAnswer(answerId));
        break;

      case PresidentialLegislationQuestion.ID:
        User chancellor = state.getCurrentGovernment().get(GovernmentRole.CHANCELLOR);
        List<Party> possibleAnswers = question.getAnswers().stream().map(Answer::getId)
            .map(Party::fromAnswer).collect(toList());
        Party removedAnswer = Party.fromAnswer(answerId);
        possibleAnswers.remove(removedAnswer);
        advanceStateToWaitingForChancellorLegislationQuestion(gameStateBuilder, chancellor,
            ImmutableList.copyOf(possibleAnswers), Optional.of(removedAnswer), false);
        break;

      case ChancellorLegislationQuestion.ID:
        possibleAnswers = question.getAnswers().stream().map(Answer::getId)
            .filter(answId -> answId != ChancellorLegislationQuestion.VETO_ANSWER.getId())
            .map(Party::fromAnswer).collect(toList());
        User president = state.getCurrentGovernment().get(GovernmentRole.PRESIDENT);
        if (answerId == ChancellorLegislationQuestion.VETO_ANSWER.getId()) {
          advanceStateToWaitingForPresidentialVeto(gameStateBuilder, president,
              ImmutableList.copyOf(possibleAnswers));
        } else {
          Party passedPolicy = Party.fromAnswer(answerId);
          possibleAnswers.remove(passedPolicy);
          Party discardedPolicy = Iterables.getOnlyElement(possibleAnswers);
          passPolicy(gameStateBuilder, passedPolicy, discardedPolicy);
        }
        break;

      case PresidentialVetoQuestion.ID:
        ImmutableList<Party> possiblyVetoedPolicies =
            ((PresidentialVetoQuestion) question).getVetoedPolicies();
        chancellor = state.getCurrentGovernment().get(GovernmentRole.CHANCELLOR);
        if (answer.getId() == PresidentialVetoQuestion.VETO_ANSWER.getId()) {
          veto(gameStateBuilder, possiblyVetoedPolicies);
        } else {
          advanceStateToWaitingForChancellorLegislationQuestion(gameStateBuilder, chancellor,
              possiblyVetoedPolicies, Optional.empty(), true);
        }
        break;

      case SpecialElectionQuestion.ID:
        User specialElectionPresidentialCandidate = state.getPlayers().stream()
            .filter(user -> answer.getAnswer().equals(user.getName())).findFirst().get();
        advanceStateToWaitingForCandidates(gameStateBuilder,
            Optional.of(specialElectionPresidentialCandidate));
        break;

      case ExecutionQuestion.ID:
        User playerToDie = state.getPlayers().stream()
            .filter(user -> answer.getAnswer().equals(user.getName())).findFirst().get();
        gameStateBuilder.deadPlayer(playerToDie);
        state = gameStateBuilder.build();
        advanceStateToWaitingForCandidates(gameStateBuilder, Optional.empty());
        break;

      case PolicyPeekQuestion.ID:
        advanceStateToWaitingForCandidates(gameStateBuilder, Optional.empty());
        break;

      case ExecutiveInvestigationTargetQuestion.ID:
        User playerToInvestigate = state.getPlayers().stream()
            .filter(user -> answer.getAnswer().equals(user.getName())).findFirst().get();
        advanceStateToWaitingForExecutiveInvestigationResult(gameStateBuilder, playerToInvestigate);
        break;

      case ExecutiveInvestigationResultQuestion.ID:
        advanceStateToWaitingForCandidates(gameStateBuilder, Optional.empty());
        break;
    }
    state = gameStateBuilder.build();
    checkFascistVictory(state).ifPresent(this::consumeVictory);
    checkLiberalVictory(state).ifPresent(this::consumeVictory);
    updateOutputByUsername();
  }

  public boolean isOver() {
    return state.getState() == State.GAME_OVER;
  }

  private void playerReady(User user, GameStateBuilder gameStateBuilder) throws GameRuleException {
    gameStateBuilder.readyPlayer(user);
    if (state.getPlayers().size() >= 5
        && state.getPlayers().size() == state.getReadyPlayers().size() + 1) {
      startGame(gameStateBuilder);
    }
  }

  private void startGame(GameStateBuilder gameStateBuilder) {
    GameBoard board = state.getGameBoard();
    ImmutableList<User> randomizedPlayers = state.getPlayers().stream().collect(toShuffledList());
    User hitler = randomizedPlayers.get(0);
    int fascistPartitionIndex = board.numOfFascists() + 1;
    ImmutableList<User> fascists = randomizedPlayers.subList(1, fascistPartitionIndex);
    ImmutableList<User> liberals =
        randomizedPlayers.subList(fascistPartitionIndex, randomizedPlayers.size());
    gameStateBuilder.updateHiddenDataFor(hitler, hiddenDataBuilder -> {
      hiddenDataBuilder.secretRole(SecretRole.HITLER);
      if (board.hitlerKnowsFascists()) {
        hiddenDataBuilder.knownFascists(fascists).knownLiberals(liberals);
      }
    });
    fascists.forEach(fascist -> gameStateBuilder.updateHiddenDataFor(fascist,
        hiddenDataBuilder -> hiddenDataBuilder.secretRole(SecretRole.FASCIST).knownHitler(hitler)
            .knownFascists(fascists).knownLiberals(liberals)));
    liberals.forEach(liberal -> gameStateBuilder.updateHiddenDataFor(liberal,
        hiddenDataBuilder -> hiddenDataBuilder.secretRole(SecretRole.LIBERAL)));
    gameStateBuilder.policyDeck(STARTING_POLICY_DECK.stream().collect(toShuffledList()));
    advanceStateToWaitingForCandidates(gameStateBuilder, Optional.empty());
  }

  private void castVote(GameStateBuilder gameStateBuilder, User player, Vote vote) {
    GameRuleException.check(!state.getVotes().containsKey(player), "You have already voted");
    gameStateBuilder.vote(player, vote);
    if (state.getVotes().entrySet().size() + 1 == state.getAlivePlayers().size()) {
      GameState protoState = gameStateBuilder.build();
      long yesVotes = protoState.getVotes().entrySet().stream()
          .filter(entry -> entry.getValue().equals(Vote.YES)).collect(counting());
      long noVotes = protoState.getVotes().entrySet().stream()
          .filter(entry -> entry.getValue().equals(Vote.NO)).collect(counting());

      if (yesVotes > noVotes) {
        gameStateBuilder.clearCurrentGovernment()
            .currentGovernment(protoState.getCurrentCandidates()).clearCurrentCandidates();
        advanceStateToWaitingForPresidentialLegislation(gameStateBuilder,
            protoState.getCurrentCandidates().get(GovernmentRole.PRESIDENT));
      } else {
        incrementElectionTracker(gameStateBuilder, protoState);
      }
    }
  }

  private void incrementElectionTracker(GameStateBuilder gameStateBuilder, GameState protoState) {
    if (protoState.getElectionCounter() == 2) {
      gameStateBuilder.electionCounter(0);
      ImmutableList<Party> policyDeck = protoState.getPolicyDeck();
      if (policyDeck.isEmpty()) {
        policyDeck = protoState.getDiscardPile().stream().collect(toShuffledList());
        gameStateBuilder.clearDiscardPile();
      }
      Party autoPolicy = policyDeck.get(0);
      if (autoPolicy == Party.FASCIST) {
        gameStateBuilder.incrementFascistPolicies();
      } else {
        gameStateBuilder.incrementLiberalPolicies();
      }
      gameStateBuilder.policyDeck(policyDeck.subList(1, policyDeck.size()));
    } else {
      gameStateBuilder.incrementElectionTracker();
    }
    advanceStateToWaitingForCandidates(gameStateBuilder, Optional.empty());
  }

  private void passPolicy(GameStateBuilder gameStateBuilder, Party passedPolicy,
      Party discardedPolicy) {
    gameStateBuilder.discardPile(discardedPolicy);
    if (passedPolicy == Party.LIBERAL) {
      gameStateBuilder.incrementLiberalPolicies();
      advanceStateToWaitingForCandidates(gameStateBuilder, Optional.empty());
    } else {
      gameStateBuilder.incrementFascistPolicies();
      Optional<ExecutivePower> policyConsequence =
          state.getGameBoard().fascistConsequencesForPolicy(state.getFascistPolicies() + 1);
      if (!policyConsequence.isPresent()) {
        advanceStateToWaitingForCandidates(gameStateBuilder, Optional.empty());
      } else {
        switch (policyConsequence.get()) {
          case INVESTIGATION:
            advanceStateToWaitingForExecutiveInvestigation(gameStateBuilder);
            break;
          case SPECIAL_ELECTION:
            advanceStateToWaitingForSpecialElection(gameStateBuilder);
            break;
          case POLICY_PEEK:
            advanceStateToWaitingForPolicyPeek(gameStateBuilder);
            break;
          case EXECUTION:
            advanceStateToWaitingForExecution(gameStateBuilder);
            break;
        }
      }
    }
  }

  private void veto(GameStateBuilder gameStateBuilder,
      ImmutableList<Party> possiblyVetoedPolicies) {
    gameStateBuilder.discardPile(possiblyVetoedPolicies);
    incrementElectionTracker(gameStateBuilder, gameStateBuilder.build());
  }

  private void advanceStateToWaitingForCandidates(GameStateBuilder gameStateBuilder,
      Optional<User> specialCandidate) {
    if (specialCandidate.isPresent()) {
      gameStateBuilder.incrementNumOfSpecialElections();
    }
    User presidentialCandidate = specialCandidate.orElse(state.getPresidentialCandidate());
    gameStateBuilder.clearCurrentCandidates()
        .currentCandidate(GovernmentRole.PRESIDENT, presidentialCandidate).incrementRound()
        .clearVotes().state(State.WAITING_FOR_CANDIDATES).updateHiddenDataFor(presidentialCandidate,
            hiddenDataBuilder -> hiddenDataBuilder.question(new ChancellorQuestion(
                presidentialCandidate, state.getAlivePlayers(), state.getTermLimitedPlayers())));
  }

  private void advanceStateToWaitingForElection(GameStateBuilder gameStateBuilder,
      User chancellorCandidate, User presidentialCandidate) {
    gameStateBuilder.currentCandidate(GovernmentRole.CHANCELLOR, chancellorCandidate)
        .state(State.WAITING_FOR_ELECTION);
    state.getAlivePlayers()
        .forEach(player -> gameStateBuilder.updateHiddenDataFor(player,
            hiddenDataBuilder -> hiddenDataBuilder
                .question(new ElectionQuestion(presidentialCandidate, chancellorCandidate))));
  }

  private void advanceStateToWaitingForPresidentialLegislation(GameStateBuilder gameStateBuilder,
      User president) {
    ImmutableList<Party> policyDeck = state.getPolicyDeck();
    if (policyDeck.size() < 3) {
      policyDeck = ImmutableList.<Party>builder().addAll(policyDeck)
          .addAll(state.getDiscardPile().stream().collect(toShuffledList())).build();
      gameStateBuilder.clearDiscardPile();
    }
    ImmutableList<Party> policiesForPresident = policyDeck.subList(0, 3);
    ImmutableList<Party> newPolicyDeck = policyDeck.subList(3, policyDeck.size());
    gameStateBuilder.electionCounter(0).policyDeck(newPolicyDeck)
        .state(State.WAITING_FOR_PRESIDENTIAL_LEGISLATION)
        .updateHiddenDataFor(president, hiddenDataBuilder -> hiddenDataBuilder
            .question(new PresidentialLegislationQuestion(policiesForPresident)));
  }

  private void advanceStateToWaitingForChancellorLegislationQuestion(
      GameStateBuilder gameStateBuilder, User chancellor,
      ImmutableList<Party> policiesForChancellor, Optional<Party> presidentiallyRemovedPolicy,
      boolean vetoOverride) {
    presidentiallyRemovedPolicy.ifPresent(policy -> gameStateBuilder.discardPile(policy));
    gameStateBuilder.state(State.WAITING_FOR_CHANCELLOR_LEGISLATION).updateHiddenDataFor(chancellor,
        hiddenDataBuilder -> hiddenDataBuilder.question(new ChancellorLegislationQuestion(
            policiesForChancellor, vetoOverride ^ state.isVetoPowerEnabled())));
  }


  private void advanceStateToWaitingForPresidentialVeto(GameStateBuilder gameStateBuilder,
      User president, ImmutableList<Party> possibleAnswers) {
    gameStateBuilder.state(State.WAITING_FOR_PRESIDENTIAL_VETO).updateHiddenDataFor(president,
        hiddenDataBuilder -> hiddenDataBuilder
            .question(new PresidentialVetoQuestion(possibleAnswers)));
  }

  private void advanceStateToWaitingForSpecialElection(GameStateBuilder gameStateBuilder) {
    User president = state.getCurrentGovernment().get(GovernmentRole.PRESIDENT);
    gameStateBuilder.state(State.WAITING_FOR_SPECIAL_ELECTION).updateHiddenDataFor(president,
        hiddenDataBuilder -> hiddenDataBuilder
            .question(new SpecialElectionQuestion(president, state.getAlivePlayers())));
  }

  private void advanceStateToWaitingForExecution(GameStateBuilder gameStateBuilder) {
    User president = state.getCurrentGovernment().get(GovernmentRole.PRESIDENT);
    gameStateBuilder.state(State.WAITING_FOR_EXECUTION).updateHiddenDataFor(president,
        hiddenDataBuilder -> hiddenDataBuilder
            .question(new ExecutionQuestion(president, state.getAlivePlayers())));
  }

  private void advanceStateToWaitingForPolicyPeek(GameStateBuilder gameStateBuilder) {
    User president = state.getCurrentGovernment().get(GovernmentRole.PRESIDENT);
    ImmutableList<Party> policyDeck = state.getPolicyDeck();
    if (policyDeck.size() < 3) {
      policyDeck = ImmutableList.<Party>builder().addAll(policyDeck)
          .addAll(state.getDiscardPile().stream().collect(toShuffledList())).build();
      gameStateBuilder.clearDiscardPile();
    }
    ImmutableList<Party> peekedPolicies = policyDeck.subList(0, 3);
    gameStateBuilder.state(State.WAITING_FOR_POLICY_PEEK).policyDeck(policyDeck)
        .updateHiddenDataFor(president, hiddenDataBuilder -> hiddenDataBuilder
            .question(new PolicyPeekQuestion(peekedPolicies)));
  }


  private void advanceStateToWaitingForExecutiveInvestigation(GameStateBuilder gameStateBuilder) {
    User president = state.getCurrentGovernment().get(GovernmentRole.PRESIDENT);
    gameStateBuilder.state(State.WAITING_FOR_EXECUTIVE_INVESTIGATION).updateHiddenDataFor(president,
        hiddenDataBuilder -> hiddenDataBuilder.question(
            new ExecutiveInvestigationTargetQuestion(president, state.getAlivePlayers())));
  }

  private void advanceStateToWaitingForExecutiveInvestigationResult(
      GameStateBuilder gameStateBuilder, User investigatedUser) {
    User president = state.getCurrentGovernment().get(GovernmentRole.PRESIDENT);
    Party investigationResult =
        state.getHiddenData().get(investigatedUser).getSecretRole().getParty();
    gameStateBuilder.state(State.WAITING_FOR_EXECUTIVE_INVESTIGATION_RESULT)
        .investigatedPlayer(investigatedUser).updateHiddenDataFor(president, hiddenDataBuilder -> {
          hiddenDataBuilder.question(
              new ExecutiveInvestigationResultQuestion(investigatedUser, investigationResult));
          if (investigationResult == Party.FASCIST) {
            hiddenDataBuilder.knownFascist(investigatedUser);
          } else {
            hiddenDataBuilder.knownLiberal(investigatedUser);
          }
        });
  }


  private static Optional<GameResult> checkFascistVictory(GameState gameState) {
    if (gameState.getFascistPolicies() == 6) {
      return Optional.of(new GameResult(Party.FASCIST,
          "The Fascists have passed 6 fascist policies, destroying Democracy."));
    }
    if (gameState.getFascistPolicies() >= 3
        && gameState.getState() == State.WAITING_FOR_PRESIDENTIAL_LEGISLATION
        && gameState.getCurrentGovernment().containsKey(GovernmentRole.CHANCELLOR)
        && gameState.getHiddenData()
            .get(gameState.getCurrentGovernment().get(GovernmentRole.CHANCELLOR))
            .getSecretRole() == SecretRole.HITLER) {
      return Optional.of(new GameResult(Party.FASCIST, "Hitler has been elected chancellor"));
    }
    return Optional.empty();
  }

  private static Optional<GameResult> checkLiberalVictory(GameState gameState) {
    if (gameState.getLiberalPolicies() == 5) {
      return Optional.of(new GameResult(Party.LIBERAL,
          "The liberals have maintained control of government and forced out fascism."));
    }
    if (gameState.getDeadPlayers().stream()
        .map(deadPlayer -> gameState.getHiddenData().get(deadPlayer)).map(HiddenData::getSecretRole)
        .anyMatch(role -> role == SecretRole.HITLER)) {
      return Optional.of(new GameResult(Party.LIBERAL, "The liberals have killed Hitler!"));
    }
    return Optional.empty();
  }

  private void consumeVictory(GameResult result) {
    GameStateBuilder builder = state.toBuilder().state(State.GAME_OVER).gameResult(result);
    state.getPlayers().forEach(player -> builder.updateHiddenDataFor(player,
        hiddenDataBuilder -> hiddenDataBuilder.question(null)));
    state = builder.build();
  }

  private void updateOutputByUsername() {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    builder.put("HOSTSCREEN" + code, OutputGenerator.generateOutputForScreen(state, code));
    state.getPlayers().forEach(
        user -> builder.put(user.getName(), OutputGenerator.generateOutputForUser(state, user)));
    outputByUsername = builder.build();
  }
}
