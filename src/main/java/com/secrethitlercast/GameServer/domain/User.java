package com.secrethitlercast.GameServer.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class User {
	String name;
	String cookie;
}
