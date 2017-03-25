package com.secrethitlercast.GameServer.questions;

import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.secrethitlercast.GameServer.domain.User;
import com.secrethitlercast.GameServer.domain.enums.State;

public class SpecialElectionQuestion implements Question {
  public static final int ID = 7;

  private final ImmutableList<Answer> answers;

  public SpecialElectionQuestion(User user, ImmutableList<User> allPlayers) {
    this.answers =
        IntStream.range(0, allPlayers.size())
            .mapToObj(i -> new Answer(i, allPlayers.get(i).getName()))
            .filter(answer -> !answer.getAnswer().equals(user.getName()))
            .collect(ImmutableList.toImmutableList());
  }


  @Override
  public String getQuestion() {
    return "You call a special election! Name your successor!";
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
    return State.WAITING_FOR_SPECIAL_ELECTION;
  }

}
