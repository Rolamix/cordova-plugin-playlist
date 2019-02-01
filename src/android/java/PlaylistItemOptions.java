package com.rolamix.plugins.audioplayer;

import org.json.JSONException;
import org.json.JSONObject;

public class PlaylistItemOptions {
  private JSONObject options;

  private boolean retainPosition = false;
  private long playFromPosition = -1L;
  private String playFromId = null;
  private boolean startPaused = true;

  PlaylistItemOptions(JSONObject optionsObj) {
    this.options = optionsObj;
    if (this.options == null) {
      this.options = new JSONObject();
    }

    this.retainPosition = this.options.optBoolean("retainPosition", false);
    this.startPaused = this.options.optBoolean("startPaused", true);
    this.playFromId = this.options.optString("playFromId", null);

    try {
      playFromPosition = (long)this.options.getDouble("playFromPosition") * 1000L;
    } catch (JSONException ex) {
      playFromPosition = -1L;
    }
  }

  PlaylistItemOptions(boolean retainPosition, long playFromPosition, boolean startPaused) {
    this.startPaused = startPaused;
    this.retainPosition = retainPosition;
    this.playFromPosition = playFromPosition;
  }

  public boolean getStartPaused() {
    return startPaused;
  }

  public boolean getRetainPosition() {
    return retainPosition;
  }

  public long getPlayFromPosition() {
    return playFromPosition;
  }

  public String getPlayFromId() {
    return playFromId;
  }
}
