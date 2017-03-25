package com.secrethitlercast.GameServer.config;

import com.google.inject.AbstractModule;
import com.secrethitlercast.GameServer.SecretHitlerServer;
import com.secrethitlercast.GameServer.generators.CodeGenerator;
import com.secrethitlercast.GameServer.generators.CookieGenerator;
import com.secrethitlercast.GameServer.interfaces.WebServer;
import com.secrethitlercast.GameServer.managers.GameManager;
import com.secrethitlercast.GameServer.managers.UserManager;

public class SecretHitlerModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(CookieGenerator.class);
    bind(GameManager.class);
    bind(UserManager.class);
    bind(WebServer.class).to(SecretHitlerServer.class);
    bind(CookieGenerator.class);
    bind(CodeGenerator.class);
  }

}
