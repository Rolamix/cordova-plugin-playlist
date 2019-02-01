package com.rolamix.plugins.audioplayer;

public interface RmxConstants {
  String DOCUMENTS_SCHEME_PREFIX  = "documents://";
  String HTTP_SCHEME_PREFIX       = "http://";
  String HTTPS_SCHEME_PREFIX      = "https://";
  String CDVFILE_PREFIX           = "cdvfile://";

  // Playlist item management
  String SET_OPTIONS            = "setOptions";
  String INITIALIZE             = "initialize";
  String SET_PLAYLIST_ITEMS     = "setPlaylistItems";
  String ADD_PLAYLIST_ITEM      = "addItem";
  String ADD_PLAYLIST_ITEMS     = "addAllItems";
  String REMOVE_PLAYLIST_ITEM   = "removeItem";
  String REMOVE_PLAYLIST_ITEMS  = "removeItems";
  String CLEAR_PLAYLIST_ITEMS   = "clearAllItems";

  // Playback
  String PLAY                   = "play";
  String PLAY_BY_INDEX          = "playTrackByIndex";
  String PLAY_BY_ID             = "playTrackById";
  String PAUSE                  = "pause";
  String SKIP_FORWARD           = "skipForward";
  String SKIP_BACK              = "skipBack";
  String SEEK                   = "seekTo";
  String SEEK_TO_QUEUE_POSITION = "seekToQueuePosition";
  String SET_PLAYBACK_RATE      = "setPlaybackRate";
  String SET_PLAYBACK_VOLUME    = "setPlaybackVolume";
  String SET_LOOP_ALL           = "setLoopAll";

  // Getters, should almost always be unneeded since the status is continually reported.
  String GET_PLAYBACK_RATE      = "getPlaybackRate";
  String GET_PLAYBACK_VOLUME    = "getPlaybackVolume";
  String GET_PLAYBACK_POSITION  = "getPlaybackPosition";
  String GET_BUFFER_STATUS      = "getCurrentBuffer";
  String GET_QUEUE_POSITION     = "getQueuePosition";

  String RELEASE = "release";
}
