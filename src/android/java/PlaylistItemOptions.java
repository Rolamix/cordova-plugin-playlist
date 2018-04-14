package com.rolamix.plugins.audioplayer;

import org.json.JSONException;
import org.json.JSONObject;

public class PlaylistItemOptions {
  private JSONObject options;

  PlaylistItemOptions(JSONObject optionsObj) {
    this.options = optionsObj;
    if (this.options == null) {
      this.options = new JSONObject();
    }
  }

  public boolean getRetainPosition() {
    return this.options.optBoolean("retainPosition", false);
  }

  public long getPlayFromPosition() {
    try {
      return (long)this.options.getDouble("playFromPosition") * 1000L;
    } catch (JSONException ex) {
      return -1L;
    }
  }
}
