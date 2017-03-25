package com.secrethitlercast.GameServer.domain.enums;

import com.secrethitlercast.GameServer.questions.ElectionQuestion;

public enum Vote {
  YES, NO;

  public static Vote fromAnswer(int answerId) {
    switch (answerId) {
      case ElectionQuestion.YES_ID:
        return YES;
      case ElectionQuestion.NO_ID:
        return NO;
      default:
        throw new IllegalStateException("Answer ID is neither yes nor no");
    }
  }
}
