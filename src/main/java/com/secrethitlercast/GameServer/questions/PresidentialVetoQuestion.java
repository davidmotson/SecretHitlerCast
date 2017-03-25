package com.secrethitlercast.GameServer.questions;

import com.google.common.collect.ImmutableList;
import com.secrethitlercast.GameServer.domain.enums.Party;
import com.secrethitlercast.GameServer.domain.enums.State;

public class PresidentialVetoQuestion implements Question {
  public static final int ID = 10;

  public static final Answer VETO_ANSWER = new Answer(1, "Yes, Veto");

  public static final Answer FORCE_LEGISLATION_ANSWER = new Answer(2, "No, Force Legislation");

  private static final ImmutableList<Answer> ANSWERS =
      ImmutableList.of(VETO_ANSWER, FORCE_LEGISLATION_ANSWER);

  private final ImmutableList<Party> possiblyVetoedPolicies;

  public PresidentialVetoQuestion(ImmutableList<Party> possiblyVetoedPolicies) {
    this.possiblyVetoedPolicies = possiblyVetoedPolicies;
  }

  @Override
  public String getQuestion() {
    return "Your chancellor wishes to veto this agenda. Do you agree?";
  }

  @Override
  public ImmutableList<Answer> getAnswers() {
    return ANSWERS;
  }

  @Override
  public int getId() {
    return ID;
  }

  @Override
  public State getState() {
    return State.WAITING_FOR_PRESIDENTIAL_VETO;
  }

  public ImmutableList<Party> getVetoedPolicies() {
    return possiblyVetoedPolicies;
  }
}
