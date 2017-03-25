package com.secrethitlercast.GameServer.domain.output;

import lombok.Value;

@Value
public class PlayerOutput {
  String name;
  HiddenDataOutput hiddenData;
  QuestionOutput question;
}
