package com.secrethitlercast.GameServer.domain.enums;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.secrethitlercast.GameServer.exceptions.GameRuleException;

public enum GameBoard {
	SMALL(1, true, ImmutableMap.of(3, ExecutivePower.POLICY_PEEK, 4, ExecutivePower.EXECUTION, 5, ExecutivePower.EXECUTION)),
	MEDIUM(2, false, ImmutableMap.of(2, ExecutivePower.INVESTIGATION, 3, ExecutivePower.SPECIAL_ELECTION, 4, ExecutivePower.EXECUTION, 5, ExecutivePower.EXECUTION)),
	LARGE(3, false, ImmutableMap.of(1, ExecutivePower.INVESTIGATION, 2, ExecutivePower.INVESTIGATION, 3, ExecutivePower.SPECIAL_ELECTION, 4, ExecutivePower.EXECUTION, 5, ExecutivePower.EXECUTION));
	
  private int numOfFascists;
  private boolean hitlerKnowsFascists;
  private ImmutableMap<Integer, ExecutivePower> fascistConsequences;

  private GameBoard(int numOfFascists, boolean hitlerKnowsFascists,
      ImmutableMap<Integer, ExecutivePower> fascistConsequences) {
    this.numOfFascists = numOfFascists;
    this.hitlerKnowsFascists = hitlerKnowsFascists;
    this.fascistConsequences = fascistConsequences;
  }

  public int numOfFascists() {
    return numOfFascists;
  }

  public boolean hitlerKnowsFascists() {
    return hitlerKnowsFascists;
  }

  public Optional<ExecutivePower> fascistConsequencesForPolicy(int policyNumber) {
    return Optional.ofNullable(fascistConsequences.get(policyNumber));
  }

  public static GameBoard gameBoardForNumberOfPlayers(int numOfPlayers) {
    switch (numOfPlayers) {
      case 5:
      case 6:
        return SMALL;
      case 7:
      case 8:
        return MEDIUM;
      case 9:
      case 10:
        return LARGE;
      default:
        throw new GameRuleException(
            String.format("Tried to fetch boardgame for %s players", numOfPlayers));
    }
  }
}
