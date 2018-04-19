package com.rolamix.plugins.audioplayer;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.*;
import android.util.Log;

public class OnStatusCallback extends PluginCallback {

  private static final String TAG = "OnStatusCallback";

  OnStatusCallback(CallbackContext callbackContext) {
    super(callbackContext);
  }

  public static JSONObject createErrorWithCode(RmxAudioErrorType code, String message) {
    JSONObject error = new JSONObject();
    try {
        error.put("code", code);
        error.put("message", message != null ? message : "");
    } catch (JSONException e) {
        Log.e(TAG, "Exception while raising onStatus: ", e);
    }
    return error;
  }

  public void onStatus(RmxAudioStatusMessage what, String trackId, JSONObject param) {
    // JSONArray jsonArray = new JSONArray(); jsonArray.put(item); e.g.
    JSONObject status = new JSONObject();
    JSONObject dict = new JSONObject();

    try {
        status.put("msgType", what.getValue()); // not .ordinal()
        status.put("trackId", trackId);
        status.put("value", param);

        dict.put("action", "status");
        dict.put("status", status);
    } catch (JSONException e) {
        Log.e(TAG, "Exception while raising onStatus: ", e);
    }

    Log.v(TAG, "statusChanged:" + dict.toString());
    send(PluginResult.Status.OK, dict, true);
  }

}
