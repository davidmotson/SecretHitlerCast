package com.secrethitlercast.GameServer.domain;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.secrethitlercast.GameServer.domain.HiddenData.HiddenDataBuilder;
import com.secrethitlercast.GameServer.domain.enums.GameBoard;
import com.secrethitlercast.GameServer.domain.enums.GovernmentRole;
import com.secrethitlercast.GameServer.domain.enums.Party;
import com.secrethitlercast.GameServer.domain.enums.State;
import com.secrethitlercast.GameServer.domain.enums.Vote;

@Value
@Builder
public class GameState {
  @Singular
  ImmutableList<User> players;
  @Singular
  ImmutableSet<User> readyPlayers;
  @Singular
  ImmutableList<User> investigatedPlayers;
  @Singular("currentGovernment")
  ImmutableMap<GovernmentRole, User> currentGovernment;
  @Singular
  ImmutableMap<GovernmentRole, User> currentCandidates;
  @Singular
  ImmutableList<User> deadPlayers;
  ImmutableMap<User, HiddenData> hiddenData;
  ImmutableList<Party> policyDeck;
  @Singular("discardPile")
  ImmutableList<Party> discardPile;
  @Singular
  ImmutableMap<User, Vote> votes;
  State state;
  int randomPlayerOffset;
  int round;
  int numOfSpecialElections;
  int fascistPolicies;
  int liberalPolicies;
  int electionCounter;
  GameResult gameResult;

  public User getPresidentialCandidate() {
    return getAlivePlayers().get((round - numOfSpecialElections + randomPlayerOffset) % (players.size() - deadPlayers.size()));
  }

  public boolean isVetoPowerEnabled() {
    return fascistPolicies >= 5;
  }

  public GameBoard getGameBoard() {
    return GameBoard.gameBoardForNumberOfPlayers(players.size());
  }
  
  public ImmutableList<User> getAlivePlayers() {
    return getPlayers().stream().filter(player -> !getDeadPlayers().contains(player))
        .collect(toImmutableList());
  }

  public ImmutableSet<User> getTermLimitedPlayers() {
    ImmutableSet.Builder<User> termLimitedPlayers = ImmutableSet.builder();
    if (players.size() - deadPlayers.size() > 5
        && currentGovernment.containsKey(GovernmentRole.PRESIDENT)) {
      termLimitedPlayers.add(currentGovernment.get(GovernmentRole.PRESIDENT));
    }
    if (currentGovernment.containsKey(GovernmentRole.CHANCELLOR)) {
      termLimitedPlayers.add(currentGovernment.get(GovernmentRole.CHANCELLOR));
    }
    return termLimitedPlayers.build();
  }

  public GameStateBuilder toBuilder() {
    return GameState.builder()
        .players(players)
        .readyPlayers(readyPlayers)
        .investigatedPlayers(investigatedPlayers)
        .currentGovernment(currentGovernment)
        .currentCandidates(currentCandidates)
        .deadPlayers(deadPlayers)
        .hiddenData(hiddenData)
        .policyDeck(policyDeck)
        .discardPile(discardPile)
        .votes(votes)
        .state(state)
        .randomPlayerOffset(randomPlayerOffset)
        .round(round)
        .numOfSpecialElections(numOfSpecialElections)
        .fascistPolicies(fascistPolicies)
        .liberalPolicies(liberalPolicies)
        .electionCounter(electionCounter)
        .gameResult(gameResult);
  }

  public static class GameStateBuilder {
    public GameStateBuilder incrementRound() {
      round++;
      return this;
    }

    public GameStateBuilder incrementNumOfSpecialElections() {
      numOfSpecialElections++;
      return this;
    }

    public GameStateBuilder incrementFascistPolicies() {
      fascistPolicies++;
      return this;
    }

    public GameStateBuilder incrementLiberalPolicies() {
      liberalPolicies++;
      return this;
    }

    public GameStateBuilder incrementElectionTracker() {
      electionCounter++;
      return this;
    }

    public GameStateBuilder updateHiddenDataFor(User user, Consumer<HiddenDataBuilder> updater) {
      ImmutableMap.Builder<User, HiddenData> mapBuilder = ImmutableMap.builder();
      if (hiddenData != null) {
        hiddenData.entrySet().stream().filter(entry -> !entry.getKey().equals(user))
            .forEach(mapBuilder::put);
      }
      HiddenDataBuilder hiddenDataBuilder =
          hiddenData != null && hiddenData.containsKey(user) ? hiddenData.get(user).toBuilder()
              : HiddenData.builder();
      updater.accept(hiddenDataBuilder);
      mapBuilder.put(user, hiddenDataBuilder.build());
      hiddenData(mapBuilder.build());
      return this;
    }
  }
}
