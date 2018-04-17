package com.rolamix.plugins.audioplayer;

import org.json.JSONException;
import org.json.JSONObject;

public class PlaylistItemOptions {
  private JSONObject options;

  private boolean retainPosition = false;
  private long playFromPosition = -1L;

  PlaylistItemOptions(JSONObject optionsObj) {
    this.options = optionsObj;
    if (this.options == null) {
      this.options = new JSONObject();
    }

    this.retainPosition = this.options.optBoolean("retainPosition", false);
    try {
      playFromPosition = (long)this.options.getDouble("playFromPosition") * 1000L;
    } catch (JSONException ex) {
      playFromPosition = -1L;
    }
  }

  PlaylistItemOptions(boolean retainPosition, long playFromPosition) {
    this.retainPosition = retainPosition;
    this.playFromPosition = playFromPosition;
  }

  public boolean getRetainPosition() {
    return retainPosition;
  }

  public long getPlayFromPosition() {
    return playFromPosition;
  }
}
