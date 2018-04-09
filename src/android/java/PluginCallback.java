package com.rolamix.plugins.audioplayer;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.*;

public class PluginCallback {
    private CallbackContext callbackContext;

    public PluginCallback(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    public void sendError(String message) {
      JSONObject params = new JSONObject();
      params.put("message", message);
      send(PluginResult.Status.ERROR, params);
    }

    public void send(PluginResult.Status status) {
        send(status, false);
    }

    public void send(PluginResult.Status status, JSONObject message) {
        send(status, message, false);
    }

    public void send(PluginResult.Status status, boolean keepCallback) {
        PluginResult result = new PluginResult(status);
        send(result, keepCallback);
    }

    public void send(PluginResult.Status status, JSONObject message, boolean keepCallback) {
        PluginResult result = new PluginResult(status, message);
        send(result, keepCallback);
    }

    public void send(PluginResult result) {
        send(result, false);
    }

    public void send(PluginResult result, boolean keepCallback) {
        if (callbackContext == null) {
          return;
        }
        result.setKeepCallback(keepCallback);
        // callbackContext.success(dict);
        callbackContext.sendPluginResult(result);
    }
}
