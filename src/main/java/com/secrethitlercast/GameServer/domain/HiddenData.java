package com.secrethitlercast.GameServer.domain;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import com.google.common.collect.ImmutableSet;
import com.secrethitlercast.GameServer.domain.enums.SecretRole;
import com.secrethitlercast.GameServer.questions.Question;

@Value
@Builder
public class HiddenData {
  SecretRole secretRole;
  @Singular
  ImmutableSet<User> knownLiberals;
  @Singular
  ImmutableSet<User> knownFascists;
  User knownHitler;
  Question question;

  public HiddenDataBuilder toBuilder() {
    return builder().secretRole(secretRole).knownLiberals(knownLiberals)
        .knownFascists(knownFascists).knownHitler(knownHitler).question(question);
  }
}
