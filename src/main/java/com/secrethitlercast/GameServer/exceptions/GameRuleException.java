package com.secrethitlercast.GameServer.exceptions;

public class GameRuleException extends RuntimeException {

  private static final long serialVersionUID = -8956013080914409617L;

  public GameRuleException(String message) {
    super(message);
  }

  public static void check(boolean condition, String message, Object... args) {
    if (!condition) {
      throw new GameRuleException(String.format(message, args));
    }
  }

  public static void fail(String message, Object... args) throws GameRuleException {
    throw new GameRuleException(String.format(message, args));
  }

  public static <T> T checkNotNull(T obj, String name) {
    if (obj == null) {
      throw new GameRuleException(String.format("%s is missing", name));
    }
    return obj;
  }

}
