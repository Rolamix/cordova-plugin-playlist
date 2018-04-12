import {
  RmxAudioErrorType,
  RmxAudioStatusMessage,
} from './Constants';

export declare type AudioPlayerEventHandler = (args?: any) => void;
export interface AudioPlayerEventHandlers {
  [key: string]: AudioPlayerEventHandler[]
}

/**
 * Options governing the overall behavior of the audio player plugin
 */
export interface AudioPlayerOptions {
  /**
   * Should the plugin's javascript dump the status message stream to the javascript console?
   */
  verbose?: boolean;
  /**
   * If true, when pausing a live stream, play will continue from the LIVE POSITION (e.g. the stream
   * jumps forward to the current point in time, rather than picking up where it left off when you paused).
   * If false, the stream will continue where you paused. The drawback of doing this is that when the audio
   * buffer fills, it will jump forward to the current point in time, cause a disjoint in playback.
   *
   * Default is true.
   */
  resetStreamOnPause?: boolean;
}

export interface AudioTrack {
  isStream?: boolean;
  trackId: string;
  assetUrl: string;
  albumArt: string;
  artist: string;
  album: string;
  title: string;
}

export interface AudioTrackRemoval {
  trackId?: string;
  trackIndex?: number;
}

export interface OnStatusCallbackData {
  trackId: string;
  type: RmxAudioStatusMessage;
  value: any;
}

export interface OnStatusErrorCallbackData {
  code: RmxAudioErrorType;
  message: string;
}

export declare type OnStatusCallback = (info: OnStatusCallbackData | OnStatusErrorCallbackData) => void;
export declare type SuccessCallback = (args?: any) => void;
export declare type ErrorCallback = (error: any) => void;
