package com.secrethitlercast.GameServer.generators;

import static java.util.stream.Collectors.joining;

import java.security.SecureRandom;

import com.google.inject.Inject;

public class CodeGenerator {
  private static final String[] codeChars = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
      "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
  private final SecureRandom generator;

  @Inject
  public CodeGenerator(SecureRandom generator) {
    this.generator = generator;
  }

  public String getCode() {
    return generator.ints(4, 0, codeChars.length).mapToObj(i -> codeChars[i]).collect(joining());
  }

}
