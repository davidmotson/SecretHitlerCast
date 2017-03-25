package com.secrethitlercast.GameServer.managers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import lombok.Synchronized;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.secrethitlercast.GameServer.domain.User;
import com.secrethitlercast.GameServer.exceptions.GameRuleException;
import com.secrethitlercast.GameServer.generators.CodeGenerator;
import com.secrethitlercast.GameServer.mutable.Game;

@Singleton
public class GameManager {
  private final Cache<String, Game> gamesByCode;
  private final ConcurrentHashMap<User, Game> gamesByUser;
  private final CodeGenerator codeGenerator;

  @Inject
  public GameManager(CodeGenerator codeGenerator) {
    this.codeGenerator = codeGenerator;
    gamesByUser = new ConcurrentHashMap<User, Game>();
    gamesByCode =
        CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
            .<String, Game>removalListener(this::cleanGamesByUser)
            .build();
  }
  
  @Synchronized
  public Game createGame() {
    GameRuleException.check(gamesByCode.size() < 100, "Too many games currently running, try again later");
    while(true) {
      String code = codeGenerator.getCode();
      Game game = gamesByCode.getIfPresent(code);
      if(game == null) {
        game = new Game(code);
        gamesByCode.put(code, game);
        return game;
      }
    }
  }
  
  public Game getGameByUser(User user) {
    Game game = GameRuleException.checkNotNull(gamesByUser.get(user), "game");
    gamesByCode.getIfPresent(game.getCode());
    return game;
  }

  public Game joinGameWithCode(String code, User user) {
    Game game = GameRuleException.checkNotNull(gamesByCode.getIfPresent(code), "game");
    game.joinGame(user);
    gamesByUser.put(user, game);
    return game;
  }
  
  public void joinGameAsHost(Game game, User hostUser) {
    gamesByUser.put(hostUser, game);
  }

  private void cleanGamesByUser(RemovalNotification<String, Game> notification) {
    notification.getValue().getPlayers().stream().forEach(gamesByUser::remove);
  }
}
