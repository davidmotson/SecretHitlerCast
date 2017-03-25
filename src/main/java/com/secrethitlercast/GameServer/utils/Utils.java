package com.secrethitlercast.GameServer.utils;

import java.util.Collections;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

public class Utils {
  private Utils() {};

  public static <T> Collector<T, ?, ImmutableList<T>> toShuffledList() {
    return Collectors.collectingAndThen(Collectors.toList(), list -> {
      Collections.shuffle(list);
      return ImmutableList.copyOf(list);
    });
  }
}
