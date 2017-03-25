package com.secrethitlercast.GameServer.managers;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.secrethitlercast.GameServer.domain.User;
import com.secrethitlercast.GameServer.exceptions.GameRuleException;
import com.secrethitlercast.GameServer.generators.CookieGenerator;

@Singleton
public class UserManager {
  private final Cache<String, User> userCache;
  private final CookieGenerator generator;

  @Inject
  public UserManager(CookieGenerator generator) {
    this.userCache =
        CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES).concurrencyLevel(3).build();
    this.generator = generator;
  }

  public User getUser(String cookie) {
    return GameRuleException.checkNotNull(userCache.getIfPresent(cookie), "user");
  }

  public User createUser(String name) {
    String cookie = generator.getCookie();
    User user = User.builder().name(name).cookie(cookie).build();
    userCache.put(cookie, user);
    return user;
  }

}
