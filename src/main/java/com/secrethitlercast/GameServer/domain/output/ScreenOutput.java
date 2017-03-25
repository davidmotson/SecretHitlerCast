package com.secrethitlercast.GameServer.domain.output;

import lombok.Builder;
import lombok.Value;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.secrethitlercast.GameServer.domain.enums.State;
import com.secrethitlercast.GameServer.domain.enums.Vote;

@Value
@Builder
public class ScreenOutput {
  String code;
  State state;
  String message;
  ImmutableList<String> players;
  ImmutableList<String> readyPlayers;
  ImmutableList<String> deadPlayers;
  Integer fascistPolicies;
  Integer liberalPolicies;
  Integer electionCounter;
  ImmutableList<String> playersVoted;
  ImmutableMap<String, Vote> votes;
  String currentPresident;
  String currentChancellor;
  String currentPresidentialCandidate;
  String currentChancellorCandidate;

}
