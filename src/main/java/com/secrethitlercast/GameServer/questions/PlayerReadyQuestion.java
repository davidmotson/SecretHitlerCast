package com.secrethitlercast.GameServer.questions;

import com.google.common.collect.ImmutableList;
import com.secrethitlercast.GameServer.domain.enums.State;

public class PlayerReadyQuestion implements Question {
  public static final int ID = 11;

  public static final Answer READY = new Answer(1, "READY");

  @Override
  public String getQuestion() {
    return "Are you ready to start the game? Click ready when all players have joined.";
  }

  @Override
  public ImmutableList<Answer> getAnswers() {
    return ImmutableList.of(READY);
  }

  @Override
  public int getId() {
    return ID;
  }

  @Override
  public State getState() {
    return State.WAITING_FOR_PLAYERS;
  }

}
