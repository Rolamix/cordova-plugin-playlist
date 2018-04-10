package com.rolamix.plugins.audioplayer;

public enum RmxAudioErrorType {
  RMXERR_NONE_ACTIVE(0),
  RMXERR_ABORTED(1),
  RMXERR_NETWORK(2),
  RMXERR_DECODE(3),
  RMXERR_NONE_SUPPORTED(4);

  private final int id;
  RmxAudioErrorType(int id) { this.id = id; }
  public int getValue() { return id; }
};
