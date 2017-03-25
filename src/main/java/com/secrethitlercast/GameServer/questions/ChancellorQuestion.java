package com.secrethitlercast.GameServer.questions;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.secrethitlercast.GameServer.domain.User;
import com.secrethitlercast.GameServer.domain.enums.State;

public class ChancellorQuestion implements Question {
  public static final int ID = 1;


  private final ImmutableList<Answer> answers;

  public ChancellorQuestion(User user, ImmutableList<User> allPlayers,
      ImmutableSet<User> termLimitedPlayers) {
    ImmutableSet<String> termLimitedPlayerNames =
        termLimitedPlayers.stream().map(User::getName).collect(toImmutableSet());
    this.answers =
        IntStream.range(0, allPlayers.size())
            .mapToObj(i -> new Answer(i, allPlayers.get(i).getName()))
            .filter(answer -> !answer.getAnswer().equals(user.getName()))
            .filter(answer -> !termLimitedPlayerNames.contains(answer.getAnswer()))
            .collect(ImmutableList.toImmutableList());
  }

  @Override
  public String getQuestion() {
    return "Who do you want to be your chancelor candidate?";
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
    return State.WAITING_FOR_CANDIDATES;
  }

}
