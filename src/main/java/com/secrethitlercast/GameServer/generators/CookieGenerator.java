package com.secrethitlercast.GameServer.generators;

import static java.util.stream.Collectors.joining;

import java.security.SecureRandom;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CookieGenerator {
  private static final String[] cookieChars = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "a",
      "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
      "t", "u", "v", "w", "x", "y", "z"};
  private final SecureRandom generator;

  @Inject
  public CookieGenerator(SecureRandom generator) {
    this.generator = generator;
  }

  public String getCookie() {
    return generator.ints(16, 0, cookieChars.length).mapToObj(i -> cookieChars[i])
        .collect(joining());
  }
}
