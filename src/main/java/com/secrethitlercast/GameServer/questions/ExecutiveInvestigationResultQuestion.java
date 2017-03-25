package com.secrethitlercast.GameServer.questions;

import com.google.common.collect.ImmutableList;
import com.secrethitlercast.GameServer.domain.User;
import com.secrethitlercast.GameServer.domain.enums.Party;
import com.secrethitlercast.GameServer.domain.enums.State;

public class ExecutiveInvestigationResultQuestion implements Question {
  public static final int ID = 6;

  private static final Answer OK = new Answer(1, "OK");

  private final Party investigationResult;
  private final User investigatedUser;

  public ExecutiveInvestigationResultQuestion(User investigatedUser, Party investigationResult) {
    this.investigatedUser = investigatedUser;
    this.investigationResult = investigationResult;
  }

  @Override
  public String getQuestion() {
    return String.format("Your spies have returned! %s is a %s!", investigatedUser.getName(),
        investigationResult.name());
  }

  @Override
  public ImmutableList<Answer> getAnswers() {
    return ImmutableList.of(OK);
  }

  @Override
  public int getId() {
    return ID;
  }

  @Override
  public State getState() {
    return State.WAITING_FOR_EXECUTIVE_INVESTIGATION_RESULT;
  }

}
