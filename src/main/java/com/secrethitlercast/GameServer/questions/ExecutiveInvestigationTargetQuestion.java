package com.secrethitlercast.GameServer.questions;

import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.secrethitlercast.GameServer.domain.User;
import com.secrethitlercast.GameServer.domain.enums.State;

public class ExecutiveInvestigationTargetQuestion implements Question {
  public static final int ID = 5;

  private final ImmutableList<Answer> answers;

  public ExecutiveInvestigationTargetQuestion(User user, ImmutableList<User> allPlayers) {
    this.answers = IntStream.range(0, allPlayers.size())
        .mapToObj(i -> new Answer(i, allPlayers.get(i).getName()))
        .filter(answer -> !answer.getAnswer().equals(user.getName()))
        .collect(ImmutableList.toImmutableList());
  }

  @Override
  public String getQuestion() {
    return "You may investigate people to find their loyalties! Whom shall your spies target?";
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
    return State.WAITING_FOR_EXECUTIVE_INVESTIGATION;
  }

}
