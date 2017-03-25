package com.secrethitlercast.GameServer.domain;

import com.secrethitlercast.GameServer.exceptions.GameRuleException;

import lombok.Value;

@Value
public class JoinGameRequest {
  String name;
  String code;
  
  public JoinGameRequest verify() {
    GameRuleException.checkNotNull(name, "name");
    GameRuleException.checkNotNull(code, "code");
    GameRuleException.check(code.length() == 4, "Code needs to be four letters long");
    return this;
  }
}
