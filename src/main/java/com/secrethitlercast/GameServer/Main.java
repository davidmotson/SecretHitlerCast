package com.secrethitlercast.GameServer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.secrethitlercast.GameServer.config.SecretHitlerModule;
import com.secrethitlercast.GameServer.interfaces.WebServer;

public class Main {
  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new SecretHitlerModule());
    WebServer server = injector.getInstance(WebServer.class);
    server.init();
  }
}
