package com.rolamix.plugins.audioplayer;

public interface OnStatusReportListener {
  void onError(RmxAudioErrorType errorCode, String trackId, String message);
  void onStatus(RmxAudioStatusMessage what, String trackId, JSONObject param);
}
