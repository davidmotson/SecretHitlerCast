package com.secrethitlercast.GameServer.questions;

import com.google.common.collect.ImmutableList;
import com.secrethitlercast.GameServer.domain.User;
import com.secrethitlercast.GameServer.domain.enums.State;

public class ElectionQuestion implements Question {
  public static final int ID = 2;
  public static final int YES_ID = 1;
  public static final int NO_ID = 2;

  private static final ImmutableList<Answer> ANSWERS =
      ImmutableList.of(new Answer(YES_ID, "JA (yes)"), new Answer(NO_ID, "NEIN (no)"));

  private final User presidentialCandidate;
  private final User chancellorCandidate;


  public ElectionQuestion(User presidentialCandidate, User chancellorCandidate) {
    this.presidentialCandidate = presidentialCandidate;
    this.chancellorCandidate = chancellorCandidate;
  }

  @Override
  public String getQuestion() {
    return String.format("Vote on a new government! President: %s Chancellor: %s.",
        presidentialCandidate.getName(), chancellorCandidate.getName());
  }

  @Override
  public ImmutableList<Answer> getAnswers() {
    return ANSWERS;
  }

  @Override
  public int getId() {
    return ID;
  }

  @Override
  public State getState() {
    return State.WAITING_FOR_ELECTION;
  }

}
