package com.secrethitlercast.GameServer.domain.enums;

import java.util.Arrays;

import com.secrethitlercast.GameServer.questions.Answer;
import com.secrethitlercast.GameServer.questions.PresidentialLegislationQuestion;

public enum Party {
  FASCIST(PresidentialLegislationQuestion.FASCIST_ANSWER,
      "Get six fascist policies passed, or get Hitler elected chancelor after three policies have passed."), LIBERAL(
          PresidentialLegislationQuestion.LIBERAL_ANSWER,
          "Pass five Liberal Policies, or kill Hitler.");

  private String winCondition;
  private Answer policyAnswer;

  private Party(Answer policyAnswer, String winCondition) {
    this.winCondition = winCondition;
    this.policyAnswer = policyAnswer;
  }

  public String getWinCondition() {
    return winCondition;
  }

  public Answer getPolicyAnswer() {
    return policyAnswer;
  }

  public static Party fromAnswer(int answerId) {
    return Arrays.stream(values()).filter(party -> party.getPolicyAnswer().getId() == answerId)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Answer ID was neither fascist nor liberal"));
  }
}
