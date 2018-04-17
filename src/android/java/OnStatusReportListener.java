package com.rolamix.plugins.audioplayer;
import org.json.JSONObject;

public interface OnStatusReportListener {
  void onError(RmxAudioErrorType errorCode, String trackId, String message);
  void onStatus(RmxAudioStatusMessage what, String trackId, JSONObject param);
}
