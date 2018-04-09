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

public enum RmxAudioStatusMessage {
  RMXSTATUS_NONE(0),
  RMXSTATUS_REGISTER(1),
  RMXSTATUS_INIT(2),
  RMXSTATUS_ERROR(5),

  RMXSTATUS_LOADING(10),
  RMXSTATUS_CANPLAY(11),
  RMXSTATUS_LOADED(15),
  RMXSTATUS_STALLED(20),
  RMXSTATUS_BUFFERING(25),
  RMXSTATUS_PLAYING(30),
  RMXSTATUS_PAUSE(35),
  RMXSTATUS_PLAYBACK_POSITION(40),
  RMXSTATUS_SEEK(45),
  RMXSTATUS_COMPLETED(50),
  RMXSTATUS_DURATION(55),
  RMXSTATUS_STOPPED(60),

  RMX_STATUS_SKIP_FORWARD(90),
  RMX_STATUS_SKIP_BACK(95),
  RMXSTATUS_TRACK_CHANGED(100),
  RMXSTATUS_PLAYLIST_COMPLETED(105),
  RMXSTATUS_ITEM_ADDED(110),
  RMXSTATUS_ITEM_REMOVED(115),
  RMXSTATUS_PLAYLIST_CLEARED(120),

  RMXSTATUS_VIEWDISAPPEAR(200); // just for testing

  private final int id;
  RmxAudioStatusMessage(int id) { this.id = id; }
  public int getValue() { return id; }
};

public interface RmxConstants {
  public static final String DOCUMENTS_SCHEME_PREFIX  = "documents://";
  public static final String HTTP_SCHEME_PREFIX       = "http://";
  public static final String HTTPS_SCHEME_PREFIX      = "https://";
  public static final String CDVFILE_PREFIX           = "cdvfile://";

  // Playlist item management
  public static final String INITIALIZE             = "initialize";
  public static final String STORE_CHANNEL          = "storeMessageChannel";
  public static final String SET_PLAYLIST_ITEMS     = "setPlaylistItems";
  public static final String ADD_PLAYLIST_ITEM      = "addItem";
  public static final String ADD_PLAYLIST_ITEMS     = "addAllItems";
  public static final String REMOVE_PLAYLIST_ITEM   = "removeItem";
  public static final String REMOVE_PLAYLIST_ITEMS  = "removeItems";
  public static final String CLEAR_PLAYLIST_ITEMS   = "clearAllItems";

  // Playback
  public static final String PLAY                   = "play";
  public static final String PLAY_BY_INDEX          = "playTrackByIndex";
  public static final String PLAY_BY_ID             = "playTrackById";
  public static final String PAUSE                  = "pause";
  public static final String SKIP_FORWARD           = "skipForward";
  public static final String SKIP_BACK              = "skipBack";
  public static final String SEEK                   = "seekTo";
  public static final String SEEK_TO_QUEUE_POSITION = "seekToQueuePosition";
  public static final String SET_PLAYBACK_RATE      = "setPlaybackRate";
  public static final String SET_PLAYBACK_VOLUME    = "setPlaybackVolume";
  public static final String SET_LOOP_ALL           = "setLoopAll";

  // Getters, should almost always be unneeded since the status is continually reported.
  public static final String GET_PLAYBACK_RATE      = "getPlaybackRate";
  public static final String GET_PLAYBACK_VOLUME    = "getPlaybackVolume";
  public static final String GET_PLAYBACK_POSITION  = "getPlaybackPosition";
  public static final String GET_BUFFER_STATUS      = "getCurrentBuffer";
  public static final String GET_TOTAL_DURATION     = "getTotalDuration";
  public static final String GET_QUEUE_POSITION     = "getQueuePosition";

  public static final String RELEASE = "release";
}
