package com.secrethitlercast.GameServer.questions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.secrethitlercast.GameServer.domain.enums.State;

public interface Question {
  String getQuestion();

  ImmutableList<Answer> getAnswers();

  int getId();

  State getState();

  default ImmutableSet<Integer> getValidAnswerIds() {
    return getAnswers().stream().map(Answer::getId).collect(ImmutableSet.toImmutableSet());
  }
}
