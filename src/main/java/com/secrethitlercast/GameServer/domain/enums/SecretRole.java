package com.secrethitlercast.GameServer.domain.enums;

public enum SecretRole {
  FASCIST(Party.FASCIST), HITLER(Party.FASCIST), LIBERAL(Party.LIBERAL);

  private Party party;

  private SecretRole(Party party) {
    this.party = party;
  }

  public Party getParty() {
    return party;
  }
}
