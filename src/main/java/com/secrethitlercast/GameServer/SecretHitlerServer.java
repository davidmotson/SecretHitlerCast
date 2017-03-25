package com.secrethitlercast.GameServer;

import static com.secrethitlercast.GameServer.exceptions.GameRuleException.checkNotNull;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.port;
import static spark.SparkBase.staticFileLocation;
import spark.Request;
import spark.Response;

import com.google.common.html.HtmlEscapers;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.secrethitlercast.GameServer.domain.AnswerQuestionRequest;
import com.secrethitlercast.GameServer.domain.JoinGameRequest;
import com.secrethitlercast.GameServer.domain.User;
import com.secrethitlercast.GameServer.exceptions.GameRuleException;
import com.secrethitlercast.GameServer.interfaces.WebServer;
import com.secrethitlercast.GameServer.managers.GameManager;
import com.secrethitlercast.GameServer.managers.UserManager;
import com.secrethitlercast.GameServer.mutable.Game;

@Singleton
public class SecretHitlerServer implements WebServer {

  private static final String COOKIE_NAME = "secret_hitler_identity";
  private static final int COOKIE_DURATION = 86400;
  private static final String SUCCESS_RESPONSE = "{\"status\": \"success\"}";

  private final GameManager gameManager;
  private final UserManager userManager;
  private final Gson gson;

  @Inject
  public SecretHitlerServer(GameManager gameManager, UserManager userManager) {
    this.gameManager = gameManager;
    this.userManager = userManager;
    this.gson = new Gson();
  }

  @Override
  public void init() {
    configure();
    route();
  }

  @Override
  public void stop() {

  }

  private void configure() {
    port(8080);
    staticFileLocation("/public");
    exception(GameRuleException.class, (exception, request, response) -> {
      response.body(generateErrorReponse(exception));
    });
  }

  private void route() {
    get("/create_game", this::createGame);
    post("/join_game", this::joinGame);
    post("/answer_question", this::answerQuestion);
    get("/get_status", this::getStatus);
  }

  private String createGame(Request request, Response response) {
    Game game = gameManager.createGame();
    User user = userManager.createUser("HOSTSCREEN" + game.getCode());
    gameManager.joinGameAsHost(game, user);
    response.cookie(COOKIE_NAME, user.getCookie(), COOKIE_DURATION);
    return getResponseForCode(game.getCode());
  }

  private String joinGame(Request request, Response response) {
    JoinGameRequest joinRequest = gson.fromJson(request.body(), JoinGameRequest.class).verify();
    User user = userManager.createUser(joinRequest.getName());
    gameManager.joinGameWithCode(joinRequest.getCode(), user);
    response.cookie(COOKIE_NAME, user.getCookie(), COOKIE_DURATION);
    return SUCCESS_RESPONSE;
  }

  private String answerQuestion(Request request, Response response) {
    AnswerQuestionRequest answerRequest =
        gson.fromJson(request.body(), AnswerQuestionRequest.class);
    String cookie = checkNotNull(request.cookie(COOKIE_NAME), "cookie");
    User user = userManager.getUser(cookie);
    Game game = gameManager.getGameByUser(user);
    game.answerQuestion(user, answerRequest.getQuestionId(), answerRequest.getAnswerId());
    return SUCCESS_RESPONSE;
  }

  private String getStatus(Request request, Response response) {
    String cookie = checkNotNull(request.cookie(COOKIE_NAME), "cookie");
    User user = userManager.getUser(cookie);
    return gameManager.getGameByUser(user).getOutputFor(user);
  }

  private String getResponseForCode(String code) {
    return "{\"code\":\"" + code + "\"}";
  }

  private String generateErrorReponse(Exception e) {
    return "{\"status\":\"error\",\"error\":\"" + HtmlEscapers.htmlEscaper().escape(e.getMessage())
        + "\"}";
  }
}
