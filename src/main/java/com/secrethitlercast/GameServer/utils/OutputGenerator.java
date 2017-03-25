package com.secrethitlercast.GameServer.utils;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.secrethitlercast.GameServer.domain.GameState;
import com.secrethitlercast.GameServer.domain.HiddenData;
import com.secrethitlercast.GameServer.domain.User;
import com.secrethitlercast.GameServer.domain.enums.GovernmentRole;
import com.secrethitlercast.GameServer.domain.enums.State;
import com.secrethitlercast.GameServer.domain.output.HiddenDataOutput;
import com.secrethitlercast.GameServer.domain.output.PlayerOutput;
import com.secrethitlercast.GameServer.domain.output.QuestionOutput;
import com.secrethitlercast.GameServer.domain.output.ScreenOutput;
import com.secrethitlercast.GameServer.domain.output.ScreenOutput.ScreenOutputBuilder;

public class OutputGenerator {
  private static final Gson gson = new Gson();
  private static final ImmutableSet<State> STATES_WITH_VOTES = ImmutableSet.of(
      State.WAITING_FOR_PRESIDENTIAL_LEGISLATION, State.WAITING_FOR_CHANCELLOR_LEGISLATION,
      State.WAITING_FOR_PRESIDENTIAL_VETO);
  
  private static final ImmutableSet<State> STATES_WITH_PLAYERS_VOTED = ImmutableSet
      .<State>builder().addAll(STATES_WITH_VOTES).add(State.WAITING_FOR_ELECTION).build();
  
  private static final ImmutableSet<State> STATES_WITH_GOVERNMENT = ImmutableSet.of(
      State.WAITING_FOR_PRESIDENTIAL_LEGISLATION, State.WAITING_FOR_CHANCELLOR_LEGISLATION,
      State.WAITING_FOR_PRESIDENTIAL_VETO, State.WAITING_FOR_EXECUTIVE_INVESTIGATION,
      State.WAITING_FOR_EXECUTIVE_INVESTIGATION_RESULT, State.WAITING_FOR_SPECIAL_ELECTION,
      State.WAITING_FOR_POLICY_PEEK, State.WAITING_FOR_EXECUTION);
  
  private static final ImmutableSet<State> STATES_WITH_CHANCELLOR_CANDIDATE = ImmutableSet
      .of(State.WAITING_FOR_ELECTION);
  
  private static final ImmutableSet<State> STATES_WITH_PRESIDENT_CANDIDATE = ImmutableSet
      .<State>builder().addAll(STATES_WITH_CHANCELLOR_CANDIDATE).add(State.WAITING_FOR_CANDIDATES)
      .build();


  private OutputGenerator() {}

  public static String generateOutputForScreen(GameState state, String code) {
    if (state.getState() == State.WAITING_FOR_PLAYERS) {
      return gson.toJson(ScreenOutput
          .builder()
          .state(state.getState())
          .code(code)
          .players(state.getPlayers().stream().map(User::getName).collect(toImmutableList()))
          .readyPlayers(
              state.getReadyPlayers().stream().map(User::getName).collect(toImmutableList()))
          .build());
    }
    ScreenOutputBuilder builder =
        ScreenOutput
            .builder()
            .state(state.getState())
            .message(getMessageForState(state))
            .players(state.getPlayers().stream().map(User::getName).collect(toImmutableList()))
            .deadPlayers(
                state.getDeadPlayers().stream().map(User::getName).collect(toImmutableList()))
            .fascistPolicies(state.getFascistPolicies())
            .liberalPolicies(state.getLiberalPolicies())
            .electionCounter(state.getElectionCounter());

    if (STATES_WITH_PLAYERS_VOTED.contains(state.getState())) {
      builder.playersVoted(state.getVotes().keySet().stream().map(User::getName)
          .collect(toImmutableList()));
    }

    if (STATES_WITH_VOTES.contains(state.getState())) {
      builder.votes(state.getVotes().entrySet().stream()
          .collect(toImmutableMap(entry -> entry.getKey().getName(), Map.Entry::getValue)));
    }

    if (STATES_WITH_GOVERNMENT.contains(state.getState())) {
      builder.currentChancellor(
          state.getCurrentGovernment().get(GovernmentRole.CHANCELLOR).getName()).currentPresident(
          state.getCurrentGovernment().get(GovernmentRole.PRESIDENT).getName());
    }

    if (STATES_WITH_PRESIDENT_CANDIDATE.contains(state.getState())) {
      builder.currentPresidentialCandidate(state.getCurrentCandidates()
          .get(GovernmentRole.PRESIDENT).getName());
    }

    if (STATES_WITH_CHANCELLOR_CANDIDATE.contains(state.getState())) {
      builder.currentChancellorCandidate(state.getCurrentCandidates()
          .get(GovernmentRole.CHANCELLOR).getName());
    }

    return gson.toJson(builder.build());
  }

  private static String getMessageForState(GameState state) {
    switch (state.getState()) {
      case GAME_OVER:
        return state.getGameResult().getWinReason();

      case WAITING_FOR_CANDIDATES:
        return String.format("Presidential Candidate %s is picking their Chancellor Candidate",
            state.getCurrentCandidates().get(GovernmentRole.PRESIDENT));

      case WAITING_FOR_CHANCELLOR_LEGISLATION:
        return String.format("Chancellor %s is picking a policy to pass", state
            .getCurrentGovernment().get(GovernmentRole.CHANCELLOR));

      case WAITING_FOR_ELECTION: 
        return String.format("Election time, the ballot is President: %s Chancellor: %s", state
            .getCurrentCandidates().get(GovernmentRole.PRESIDENT), state.getCurrentCandidates()
            .get(GovernmentRole.CHANCELLOR));

      case WAITING_FOR_EXECUTION:
        return String.format("President %s will execute somebody.", state.getCurrentGovernment()
            .get(GovernmentRole.PRESIDENT));

      case WAITING_FOR_EXECUTIVE_INVESTIGATION:
        return String.format("President %s is investigating somebody's party loyalty.", state.getCurrentGovernment()
            .get(GovernmentRole.PRESIDENT));

      case WAITING_FOR_EXECUTIVE_INVESTIGATION_RESULT:
        return String.format("President %s has investigated %s", state.getCurrentGovernment()
            .get(GovernmentRole.PRESIDENT), Iterables.getLast(state.getInvestigatedPlayers()).getName());

      case WAITING_FOR_POLICY_PEEK:
        return String.format("President %s is looking at the upcoming agenda", state
            .getCurrentGovernment().get(GovernmentRole.PRESIDENT));

      case WAITING_FOR_PRESIDENTIAL_LEGISLATION:
        return String.format("President %s is picking the agenda", state.getCurrentGovernment()
            .get(GovernmentRole.PRESIDENT));

      case WAITING_FOR_PRESIDENTIAL_VETO:
        return String.format(
            "Chancellor %s wants to veto this agenda! Will President %s allow it?", state
                .getCurrentGovernment().get(GovernmentRole.CHANCELLOR), state
                .getCurrentGovernment().get(GovernmentRole.PRESIDENT));

      case WAITING_FOR_SPECIAL_ELECTION:
        return String.format(
            "President %s is picking his successor.", state
                .getCurrentGovernment().get(GovernmentRole.PRESIDENT));

      case WAITING_FOR_PLAYERS:
        return "Waiting for players";

    }
    throw new IllegalStateException("unknown state");
  }

  public static String generateOutputForUser(GameState state, User user) {
    Optional<HiddenData> hiddenData = Optional.ofNullable(state.getHiddenData().get(user));
    HiddenDataOutput hiddenOutput = hiddenData.map(HiddenDataOutput::fromHiddenData).orElse(null);
    QuestionOutput questionOutput =
        hiddenData.map(HiddenData::getQuestion).map(QuestionOutput::fromQuestion).orElse(null);
    return gson.toJson(new PlayerOutput(user.getName(), hiddenOutput, questionOutput));
  }
}
