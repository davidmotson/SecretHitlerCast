package com.secrethitlercast.GameServer.domain;

import com.secrethitlercast.GameServer.domain.enums.Party;

import lombok.Value;

@Value
public class GameResult {
  Party winner;
  String winReason;
}
