package com.secrethitlercast.GameServer.questions;

import com.google.common.collect.ImmutableList;
import com.secrethitlercast.GameServer.domain.enums.State;

public class ElectionFailureQuestion implements Question {
  private static final ImmutableList<Answer> ANSWERS = ImmutableList.of(new Answer(1, "OK"));
  public static final int ID = 12;
  private final String question;
  
  public ElectionFailureQuestion(boolean electionTrackerMaxed) {
    StringBuilder questionBuilder = new StringBuilder();
    questionBuilder.append("Your government didn't pass. The election tracker has moved up.");
    if (electionTrackerMaxed) {
      questionBuilder.append(" A new policy will be passed at random.");
    }
    question = questionBuilder.toString();
  }

  @Override
  public String getQuestion() {
    return question;
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
    return State.WAITING_FOR_ELECTION_FAILURE;
  }

}
