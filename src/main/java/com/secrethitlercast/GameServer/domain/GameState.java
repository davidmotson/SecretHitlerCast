package com.secrethitlercast.GameServer.domain;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.secrethitlercast.GameServer.domain.HiddenData.HiddenDataBuilder;
import com.secrethitlercast.GameServer.domain.enums.GameBoard;
import com.secrethitlercast.GameServer.domain.enums.GovernmentRole;
import com.secrethitlercast.GameServer.domain.enums.Party;
import com.secrethitlercast.GameServer.domain.enums.State;
import com.secrethitlercast.GameServer.domain.enums.Vote;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class GameState {
  @Singular
  ImmutableList<User> players;
  @Singular
  ImmutableSet<User> readyPlayers;
  @Singular
  ImmutableList<User> investigatedPlayers;
  ImmutableMap<GovernmentRole, User> currentGovernment;
  ImmutableMap<GovernmentRole, User> previousGovernment;
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
  User lastOrganicPresidentialCandidate;
  State state;
  int fascistPolicies;
  int liberalPolicies;
  int electionCounter;
  GameResult gameResult;

  public User getPresidentialCandidate() {
    if (lastOrganicPresidentialCandidate == null) {
      return players.get(ThreadLocalRandom.current().nextInt(players.size()));
    }
    int currentIndex = players.indexOf(lastOrganicPresidentialCandidate);
    while (true) {
      currentIndex++;
      User player = players.get(currentIndex % players.size());
      if (!deadPlayers.contains(player)) {
        return player;
      }
    }
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
    if (previousGovernment == null) {
      return ImmutableSet.of();
    }
    ImmutableSet.Builder<User> termLimitedPlayers = ImmutableSet.builder();
    if (players.size() - deadPlayers.size() > 5) {
      termLimitedPlayers.add(currentGovernment.get(GovernmentRole.PRESIDENT));
    }
    termLimitedPlayers.add(currentGovernment.get(GovernmentRole.CHANCELLOR));
    return termLimitedPlayers.build();
  }

  public GameStateBuilder toBuilder() {
    return GameState.builder().players(players).readyPlayers(readyPlayers)
        .investigatedPlayers(investigatedPlayers).currentGovernment(currentGovernment)
        .previousGovernment(previousGovernment).currentCandidates(currentCandidates)
        .deadPlayers(deadPlayers).hiddenData(hiddenData).policyDeck(policyDeck)
        .discardPile(discardPile).votes(votes)
        .lastOrganicPresidentialCandidate(lastOrganicPresidentialCandidate).state(state)
        .fascistPolicies(fascistPolicies).liberalPolicies(liberalPolicies)
        .electionCounter(electionCounter).gameResult(gameResult);
  }

  public static class GameStateBuilder {

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
      HiddenDataBuilder hiddenDataBuilder = hiddenData != null && hiddenData.containsKey(user)
          ? hiddenData.get(user).toBuilder() : HiddenData.builder();
      updater.accept(hiddenDataBuilder);
      mapBuilder.put(user, hiddenDataBuilder.build());
      hiddenData(mapBuilder.build());
      return this;
    }
  }
}
