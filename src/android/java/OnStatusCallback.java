package com.rolamix.plugins.audioplayer;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.*;

public class OnStatusCallback extends PluginCallback {

  public OnStatusCallback(CallbackContext callbackContext) {
    super(callbackContext);
  }

  public static JSONObject createErrorWithCode(RmxAudioErrorType code, String message) {
    JSONObject error = new JSONObject();
    error.put("code", errorCode);
    error.put("message", message ? message : "");
    return error;
  }

  public void onStatus(RmxAudioStatusMessage what, String trackId, JSONObject param) {
    // JSONArray jsonArray = new JSONArray(); jsonArray.put(item); e.g.
    JSONObject status = new JSONObject();
    status.put("msgType", what.getValue()); // not .ordinal()
    status.put("trackId", trackId);
    status.put("value", param);

    JSONObject dict = new JSONObject();
    dict.put("action", "status");
    dict.put("status", status);

    LOG.e(TAG, "statusChanged:", dict);
    send(PluginResult.Status.OK, dict, true);
  }

}
