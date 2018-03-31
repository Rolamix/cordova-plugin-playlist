package com.rolamix.plugins;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
// import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.LOG;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
* The core Cordova interface for the audio player
*/
public class RmxAudioPlayerHandler extends CordovaPlugin {

  public static String TAG = "RmxAudioPlayerHandler";

  private CallbackContext statusCallback;
  private PowerManager.WakeLock wakeLock;
  private WifiManager.WifiLock wifiLock;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    // your init code here

  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

    if (action.equals("echo")) {
      String message = args.getString(0);
      this.echo(message, callbackContext);
      return true;
    }

    if (action.equals("storeMessageChannel")) {
      statusCallback = callbackContext;
      return true;
    }

    return false;
  }


















  void createErrorWithCode(RmxAudioErrorType code, String message) {
    JSONObject error = new JSONObject();
    error.put("code", errorCode);
    error.put("message", message ? message : "");
    return error;
  }

  void onStatus(RmxAudioStatusMessage what, String trackId, JSONObject param) {
    JSONObject status = new JSONObject(); // JSONArray jsonArray = new JSONArray(); jsonArray.put(item); e.g.
    status.put("msgType", what.getValue());
    status.put("trackId", trackId);
    status.put("value", param);

    JSONObject dict = new JSONObject();
    dict.put("action", "status");
    dict.put("status", status);

    LOG.e(TAG, "statusChanged:", dict);

    PluginResult result = new PluginResult(PluginResult.Status.OK, dict);
    result.setKeepCallback(true);
    if (statusCallback != null) {
      // statusCallback.success(dict);
      statusCallback.sendPluginResult(result);
    }
  }
}
