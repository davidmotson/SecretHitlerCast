package com.secrethitlercast.GameServer.domain.output;

import com.google.common.collect.ImmutableList;
import com.secrethitlercast.GameServer.questions.Answer;
import com.secrethitlercast.GameServer.questions.Question;

import lombok.Value;

@Value
public class QuestionOutput {
  int id;
  String question;
  ImmutableList<Answer> answers;

  public static QuestionOutput fromQuestion(Question question) {
    return new QuestionOutput(question.getId(), question.getQuestion(), question.getAnswers());
  }
}
