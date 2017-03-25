package com.secrethitlercast.GameServer.domain;

import lombok.Value;

@Value
public class AnswerQuestionRequest {
  int questionId;
  int answerId;
}
