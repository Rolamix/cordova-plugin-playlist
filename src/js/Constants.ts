export enum RmxAudioErrorType {
  RMXERR_NONE_ACTIVE = 0,
  RMXERR_ABORTED = 1,
  RMXERR_NETWORK = 2,
  RMXERR_DECODE = 3,
  RMXERR_NONE_SUPPORTED = 4,
};

export const RmxAudioErrorTypeDescriptions = [
  'No Active Sources',
  'Aborted',
  'Network',
  'Failed to Decode',
  'No Supported Sources',
];

export enum RmxAudioStatusMessage {
  RMXSTATUS_NONE = 0,
  RMXSTATUS_REGISTER = 1,
  RMXSTATUS_INIT = 2,
  RMXSTATUS_ERROR = 5,

  RMXSTATUS_LOADING = 10,
  RMXSTATUS_CANPLAY = 11,
  RMXSTATUS_LOADED = 15,
  RMXSTATUS_STALLED = 20,
  RMXSTATUS_BUFFERING = 25,
  RMXSTATUS_PLAYING = 30,
  RMXSTATUS_PAUSE = 35,
  RMXSTATUS_PLAYBACK_POSITION = 40,
  RMXSTATUS_SEEK = 45,
  RMXSTATUS_COMPLETED = 50,
  RMXSTATUS_DURATION = 55,
  RMXSTATUS_STOPPED = 60,

  RMX_STATUS_SKIP_FORWARD = 90,
  RMX_STATUS_SKIP_BACK = 95,
  RMXSTATUS_TRACK_CHANGED = 100,
  RMXSTATUS_PLAYLIST_COMPLETED = 105,
  RMXSTATUS_ITEM_ADDED = 110,
  RMXSTATUS_ITEM_REMOVED = 115,
  RMXSTATUS_PLAYLIST_CLEARED = 120,

  RMXSTATUS_VIEWDISAPPEAR = 200, // just for testing
};

export const RmxAudioStatusMessageDescriptions = [
  'No Status',
  'Plugin Registered',
  'Plugin Initialized',
  'Error',

  'Loading',
  'CanPlay',
  'Loaded',
  'Stalled',
  'Buffering',
  'Playing',
  'Paused',
  'Playback Position Changed',
  'Seeked',
  'Playback Completed',
  'Duration Changed',
  'Stopped',

  'Skip Forward',
  'Skip Backward',
  'Track Changed',
  'Playlist Completed',
  'Track Added',
  'Track Removed',
  'Playlist Cleared',

  'DEBUG_View_Disappeared',
];
