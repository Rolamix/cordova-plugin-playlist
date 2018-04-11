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

export const RmxAudioStatusMessageDescriptions = {
  0: 'No Status',
  1: 'Plugin Registered',
  2: 'Plugin Initialized',
  5: 'Error',

  10: 'Loading',
  11: 'CanPlay',
  15: 'Loaded',
  20: 'Stalled',
  25: 'Buffering',
  30: 'Playing',
  35: 'Paused',
  40: 'Playback Position Changed',
  45: 'Seeked',
  50: 'Playback Completed',
  55: 'Duration Changed',
  60: 'Stopped',

  90: 'Skip Forward',
  95: 'Skip Backward',
  100: 'Track Changed',
  105: 'Playlist Completed',
  110: 'Track Added',
  115: 'Track Removed',
  120: 'Playlist Cleared',

  200: 'DEBUG_View_Disappeared',
};
