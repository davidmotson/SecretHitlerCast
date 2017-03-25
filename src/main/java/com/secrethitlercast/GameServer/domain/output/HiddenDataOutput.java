package com.secrethitlercast.GameServer.domain.output;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.secrethitlercast.GameServer.domain.HiddenData;
import com.secrethitlercast.GameServer.domain.User;
import com.secrethitlercast.GameServer.domain.enums.SecretRole;

import lombok.Value;

@Value
public class HiddenDataOutput {
  SecretRole secretRole;
  String hitler;
  ImmutableList<String> liberals;
  ImmutableList<String> fascists;
  
  public static HiddenDataOutput fromHiddenData(HiddenData data) {
    return new HiddenDataOutput(data.getSecretRole(), Optional.ofNullable(data.getKnownHitler())
        .map(User::getName).orElse(null), data.getKnownLiberals().stream().map(User::getName)
        .collect(toImmutableList()), data.getKnownFascists().stream().map(User::getName)
        .collect(toImmutableList()));
  }

}
