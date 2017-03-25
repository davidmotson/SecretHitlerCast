package com.secrethitlercast.GameServer.questions;

import com.google.common.collect.ImmutableList;
import com.secrethitlercast.GameServer.domain.enums.Party;
import com.secrethitlercast.GameServer.domain.enums.State;
import com.secrethitlercast.GameServer.exceptions.GameRuleException;

public class ChancellorLegislationQuestion implements Question {
  public static final int ID = 4;

  public static final Answer VETO_ANSWER = new Answer(3, "VETO");

  private final ImmutableList<Answer> answers;

  public ChancellorLegislationQuestion(ImmutableList<Party> policies, boolean vetoPower) {
    GameRuleException.check(policies.size() == 2,
        "Somehow, there aren't two policies in a ChancellorLegislationQuestion");

    ImmutableList.Builder<Answer> answerBuilder = ImmutableList.builder();
    policies.stream().map(Party::getPolicyAnswer).forEach(answerBuilder::add);
    if (vetoPower) {
      answerBuilder.add(VETO_ANSWER);
    }
    answers = answerBuilder.build();
  }

  @Override
  public String getQuestion() {
    return "As the chancellor, you pass laws. Choose a law to pass.";
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
    return State.WAITING_FOR_CHANCELLOR_LEGISLATION;
  }

}
