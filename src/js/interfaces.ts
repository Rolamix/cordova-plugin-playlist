import {
  RmxAudioErrorType,
  RmxAudioStatusMessage,
} from './Constants';

/**
 * Callback function for the on(eventName) handlers
 */
export declare type AudioPlayerEventHandler = (args?: any) => void;
/**
 * @internal
 * The collection of event handlers currently held by the plugin
 */
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

/**
 * Options governing how the items are managed when using setPlaylistItems
 * to update the playlist. This is typically useful if you are retaining items
 * that were in the previous list.
 */
export interface PlaylistItemOptions {
  /**
   * If true, the plugin will continue playback from the current playback position after
   * setting the items to the playlist.
   */
  retainPosition?: boolean;
  /**
   * If retainPosition is true, this value will tell the plugin the exact time to start from,
   * rather than letting the plugin decide based on current playback.
   */
  playFromPosition?: number;
  /**
   * If retainPosition is true, this value will tell the plugin the uid of the "current" item to start from,
   * rather than letting the plugin decide based on current playback.
   */
  playFromId?: number;
  /**
   * If playback should immediately begin when calling setPlaylistItems on the plugin.
   * Default is false;
   */
  startPaused?: boolean;
}

/**
 * An audio track for playback by the playlist.
 */
export interface AudioTrack {
  /**
   * This item is a streaming asset. Make sure this is set to true for stream URLs,
   * otherwise you will get odd behavior when the asset is paused.
   */
  isStream?: boolean;
  /**
   * trackId is optional and if not passed in, an auto-generated UUID will be used.
   */
  trackId?: string;
  /**
   * URL of the asset; can be local, a URL, or a streaming URL.
   * If the asset is a stream, make sure that isStream is set to true,
   * otherwise the plugin can't properly handle the item's buffer.
   */
  assetUrl: string;
  /**
   * The local or remote URL to an image asset to be shown for this track.
   * If this is null, the plugin's default image is used.
   * This field is not used on iOS (yet)
   */
  albumArt?: string;
  /**
   * The track's artist
   */
  artist: string;
  /**
   * Album the track belongs to
   */
  album: string;
  /**
   * Title of the track
   */
  title: string;
}

/**
 * Encapsulates the fields you can pass to the plugin to remove a track.
 * You can either remove a track by its ID if you know it, or by index if you know it;
 * Index will be preferred if both index and ID are passed.
 */
export interface AudioTrackRemoval {
  /**
   * The track ID to remove
   */
  trackId?: string;
  /**
   * The index of a track to remove.
   */
  trackIndex?: number;
}

/**
 * Encapsulates the data received by an onStatus callback
 */
export interface OnStatusCallbackData {
  /**
   * The ID of this track. If the track is null or has completed, this value is "NONE"
   * If the playlist is completed, this value is "INVALID"
   */
  trackId: string;
  /**
   * The type of status update
   */
  type: RmxAudioStatusMessage;
  /**
   * The status payload. For all updates except ERROR, the data package is described by OnStatusCallbackUpdateData.
   * For Errors, the data is shaped as OnStatusErrorCallbackData
   */
  value: OnStatusCallbackUpdateData | OnStatusTrackChangedData | OnStatusErrorCallbackData;
}

/**
 * Reports information about the playlist state when a track changes.
 * Includes the new track, its index, and the state of the playlist.
 */
export interface OnStatusTrackChangedData {
  /**
   * The new track that has been selected. May be null if you are at the end of the playlist,
   * or the playlist has been emptied.
   */
  currentItem: AudioTrack;
  /**
   * The 0-based index of the new track. If the playlist has ended or been cleared, this will be -1.
   */
  currentIndex: number;
  /**
   * Indicates whether the playlist is now currently at the last item in the list.
   */
  isAtEnd: boolean;
  /**
   * Indicates whether the playlist is now at the first item in the list
   */
  isAtBeginning: boolean;
  /**
   * Indicates if there are additional playlist items after the current item.
   */
  hasNext: boolean;
  /**
   * Indicates if there are any items before this one in the playlist.
   */
  hasPrevious: boolean;
}

/**
 * Contains the current track status as of the moment an onStatus update event is emitted.
 */
export interface OnStatusCallbackUpdateData {
  /**
   * The ID of this track corresponding to this event. If the track is null or has completed, this value is "NONE".
   * This will happen when skipping to the beginning or end of the playlist.
   * If the playlist is completed, this value is "INVALID"
   */
  trackId: string;
  /**
   * Boolean indicating whether this is a streaming track.
   */
  isStream: boolean;
  /**
   * The current index of the track in the playlist.
   */
  currentIndex: number;
  /**
   * The current status of the track, as a string. This is used
   * to summarize the various event states that a track can be in; e.g. "playing" is true for any number
   * of track statuses. The Javascript interface takes care of this for you; this field is here only for reference.
   */
  status: 'unknown' | 'ready' | 'error' | 'playing' | 'loading' | 'paused';
  /**
   * Current playback position of the reported track.
   */
  currentPosition: number;
  /**
   * The known duration of the reported track. For streams or malformed MP3's, this value will be 0.
   */
  duration: number;
  /**
   * Progress of track playback, as a percent, in the range 0 - 100
   */
  playbackPercent: number;
  /**
   * Buffering progress of the track, as a percent, in the range 0 - 100
   */
  bufferPercent: number;
  /**
   * The starting position of the buffering progress. For now, this is always reported as 0.
   */
  bufferStart: number;
  /**
   * The maximum position, in seconds, of the track buffer. For now, only the buffer with the maximum
   * playback position is reported, even if there are other segments (due to seeking, for example).
   * Practically speaking you don't need to worry about that, as in both implementations the
   * minor gaps are automatically filled in by the underlying players.
   */
  bufferEnd: number;
}

/**
 * Represents an error reported by the onStatus callback.
 */
export interface OnStatusErrorCallbackData {
  /**
   * Error code
   */
  code: RmxAudioErrorType;
  /**
   * The error, as a message
   */
  message: string;
}

/**
 * Function declaration for onStatus event handlers
 */
export declare type OnStatusCallback = (info: OnStatusCallbackData) => void;
/**
 * Function declaration for the successCallback fields of the Cordova functions
 */
export declare type SuccessCallback = (args?: any) => void;
/**
 * Function declaration for the errorCallback fields of the Cordova functions
 */
export declare type ErrorCallback = (error: any) => void;
