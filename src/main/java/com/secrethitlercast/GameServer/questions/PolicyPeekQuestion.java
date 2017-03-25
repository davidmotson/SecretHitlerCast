package com.secrethitlercast.GameServer.questions;

import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableList;
import com.secrethitlercast.GameServer.domain.enums.Party;
import com.secrethitlercast.GameServer.domain.enums.State;
import com.secrethitlercast.GameServer.exceptions.GameRuleException;

public class PolicyPeekQuestion implements Question {
  public static final int ID = 8;

  private static final Answer OK = new Answer(1, "OK");

  private final ImmutableList<Party> upcomingPolicies;

  public PolicyPeekQuestion(ImmutableList<Party> upcomingPolicies) {
    GameRuleException.check(upcomingPolicies.size() == 3,
        "Too many policies in the policy peek! %s policies instead of three",
        upcomingPolicies.size());
    this.upcomingPolicies = upcomingPolicies;
  }

  @Override
  public String getQuestion() {
    return String.format("You peek at the next session's agenda. The upcoming policies are: %s",
        upcomingPolicies.stream().map(Party::name).collect(joining(", ")));
  }

  @Override
  public ImmutableList<Answer> getAnswers() {
    return ImmutableList.of(OK);
  }

  @Override
  public int getId() {
    return ID;
  }

  @Override
  public State getState() {
    return State.WAITING_FOR_POLICY_PEEK;
  }

}
