package com.secrethitlercast.GameServer.questions;

import com.google.common.collect.ImmutableList;
import com.secrethitlercast.GameServer.domain.enums.Party;
import com.secrethitlercast.GameServer.domain.enums.State;
import com.secrethitlercast.GameServer.exceptions.GameRuleException;

public class PresidentialLegislationQuestion implements Question {
  public static final int ID = 3;

  public static final Answer FASCIST_ANSWER = new Answer(1, "FASCIST");
  public static final Answer LIBERAL_ANSWER = new Answer(2, "LIBERAL");

  private final ImmutableList<Answer> answers;

  public PresidentialLegislationQuestion(ImmutableList<Party> policies) {
    GameRuleException.check(policies.size() == 3,
        "Somehow, there aren't three policies in a PresidentialLegislationQuestion");
    answers =
        policies.stream().map(Party::getPolicyAnswer).collect(ImmutableList.toImmutableList());
  }

  @Override
  public String getQuestion() {
    return "As the president, you get to pick the agenda. Pick a policy to discard before they get passed to the chancellor.";
  }

  @Override
  public ImmutableList<Answer> getAnswers() {
    return answers;
  }

  @Override
  public int getId() {
    return ID;
  }

  @Override
  public State getState() {
    return State.WAITING_FOR_PRESIDENTIAL_LEGISLATION;
  }

}
