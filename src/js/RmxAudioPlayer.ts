/* global cordova:false */
/* globals window */

import {
  RmxAudioStatusMessage,
  RmxAudioStatusMessageDescriptions,
} from './Constants';

import {
  AudioPlayerEventHandler,
  AudioPlayerEventHandlers,
  AudioPlayerOptions,
  AudioTrack,
  AudioTrackRemoval,
  OnStatusCallback,
  SuccessCallback,
  ErrorCallback,
  PlaylistItemOptions,
  OnStatusCallbackUpdateData,
  OnStatusTrackChangedData,
  OnStatusErrorCallbackData,
} from './interfaces';

/*!
 * Module dependencies.
 */

declare var cordova: any;
const exec = typeof cordova !== 'undefined' ? cordova.require('cordova/exec') : null;
// const channel = typeof cordova !== 'undefined' ? cordova.require('cordova/channel') : null;
const log = console;

const itemStatusChangeTypes = [
  RmxAudioStatusMessage.RMXSTATUS_PLAYBACK_POSITION, RmxAudioStatusMessage.RMXSTATUS_DURATION,
  RmxAudioStatusMessage.RMXSTATUS_BUFFERING, RmxAudioStatusMessage.RMXSTATUS_CANPLAY,
  RmxAudioStatusMessage.RMXSTATUS_LOADING, RmxAudioStatusMessage.RMXSTATUS_LOADED,
  RmxAudioStatusMessage.RMXSTATUS_PAUSE,
  RmxAudioStatusMessage.RMXSTATUS_COMPLETED,
  RmxAudioStatusMessage.RMXSTATUS_ERROR,
];

/**
 * AudioPlayer class implementation. A singleton of this class is exported for use by Cordova,
 * but nothing stops you from creating another instance. Keep in mind that the native players
 * are in fact singletons, so the only thing the separate instance gives you would be
 * separate onStatus callback streams.
 */
export class RmxAudioPlayer {
  handlers: AudioPlayerEventHandlers = {};
  options: AudioPlayerOptions = { verbose: false, resetStreamOnPause: true };

  private _inititialized: boolean = false;
  private _initPromise: Promise<boolean>;
  private _readyResolve: any;
  private _readyReject: any;

  private _currentState: 'unknown' | 'ready' | 'error' | 'playing' | 'loading' | 'paused' | 'stopped' = 'unknown';
  private _hasError: boolean = false;
  private _hasLoaded: boolean = false;
  private _currentItem: AudioTrack | null = null;

  /**
   * The current summarized state of the player, as a string. It is preferred that you use the 'isX' accessors,
   * because they properly interpret the range of these values, but this field is exposed if you wish to observe
   * or interrogate it.
   */
  get currentState() {
    return this._currentState;
  }

  /**
   * True if the plugin has been initialized. You'll likely never see this state; it is handled internally.
   */
  get isInitialized() {
    return this._inititialized;
  }

  get currentTrack(): AudioTrack | null {
    return this._currentItem;
  }

  /**
   * If the playlist is currently playling a track.
   */
  get isPlaying() {
    return this._currentState === 'playing';
  }

  /**
   * True if the playlist is currently paused
   */
  get isPaused() {
    return this._currentState === 'paused' || this._currentState === 'stopped';
  }

  /**
   * True if the plugin is currently loading its *current* track.
   * On iOS, many tracks are loaded in parallel, so this only reports for the *current item*, e.g.
   * the item that will begin playback if you press pause.
   * If you need track-specific data, it is better to watch the onStatus stream and watch for RMXSTATUS_LOADING,
   * which will be raised independently & simultaneously for every track in the playlist.
   * On Android, tracks are only loaded as they begin playback, so this value and RMXSTATUS_LOADING should always
   * apply to the same track.
   */
  get isLoading() {
    return this._currentState === 'loading';
  }

  /**
   * True if the *currently playing track* has been loaded and can be played (this includes if it is *currently playing*).
   */
  get hasLoaded() {
    return this._hasLoaded;
  }

  /**
   * True if the *current track* has reported an error. In almost all cases,
   * the playlist will automatically skip forward to the next track, in which case you will also receive
   * an RMXSTATUS_TRACK_CHANGED event.
   */
  get hasError() {
    return this._hasError;
  }


  /**
   * Creates a new RmxAudioPlayer instance.
   */
  constructor() {
    this.handlers = {};
    this._initPromise = new Promise((resolve, reject) => {
      this._readyResolve = resolve;
      this._readyReject = reject;
    });
  }

  /**
   * Player interface
   */

  /**
   * Returns a promise that resolves when the plugin is ready.
   */
  ready = () => {
    return this._initPromise;
  }

  initialize = () => {
    // Initialize the plugin to send and receive messages
    // channel.createSticky('onRmxAudioPlayerReady');
    // channel.waitForInitialization('onRmxAudioPlayerReady');

    const onNativeStatus = (msg: any) => {
      // better or worse, we got an answer back from native, so we resolve.
      this._inititialized = true;
      this._readyResolve(true);
      if (msg.action === 'status') {
        this.onStatus(msg.status.trackId, msg.status.msgType, msg.status.value);
      } else {
        console.warn('Unknown audio player onStatus message:', msg.action);
      }
    };

    // channel.onCordovaReady.subscribe(() => {
    const error = (args: any) => {
      const message = 'CORDOVA RMXAUDIOPLAYER: Error storing message channel:';
      console.warn(message, args);
      this._readyReject({ message, args });
    };

    exec(onNativeStatus, error, 'RmxAudioPlayer', 'initialize', []);
    // channel.initializationComplete('onRmxAudioPlayerReady');
    // });

    return this._initPromise;
  }

  /**
   * Sets the player options. This can be called at any time and is not required before playback can be initiated.
   */
  setOptions = (successCallback: SuccessCallback, errorCallback: ErrorCallback, options: AudioPlayerOptions) => {
    this.options = { ...this.options, ...options };
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setOptions', [options]);
  }

  /**
   * Playlist item management
   */

  /**
   * Sets the entire list of tracks to be played by the playlist.
   * This will clear all previous items from the playlist.
   * If you pass options.retainPosition = true, the current playback position will be
   * recorded and used when playback restarts. This can be used, for example, to set the
   * playlist to a new set of tracks, but retain the currently-playing item to avoid skipping.
   */
  setPlaylistItems = (successCallback: SuccessCallback, errorCallback: ErrorCallback, items: AudioTrack[], options?: PlaylistItemOptions) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setPlaylistItems', [this.validateTracks(items), options || {}]);
  }

  /**
   * Add a single track to the end of the playlist
   */
  addItem = (successCallback: SuccessCallback, errorCallback: ErrorCallback, trackItem: AudioTrack) => {
    const validTrackItem = this.validateTrack(trackItem);
    if (!validTrackItem) { return errorCallback(new Error('Provided track is null or not an audio track')); }
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'addItem', [validTrackItem]);
  }

  /**
   * Adds the list of tracks to the end of the playlist.
   */
  addAllItems = (successCallback: SuccessCallback, errorCallback: ErrorCallback, items: AudioTrack[]) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'addAllItems', [this.validateTracks(items)]);
  }

  /**
   * Removes a track from the playlist. If this is the currently playing item, the next item will automatically begin playback.
   */
  removeItem = (successCallback: SuccessCallback, errorCallback: ErrorCallback, removeItem: AudioTrackRemoval) => {
    if (!removeItem) { return errorCallback(new Error('Track removal spec is empty')); }
    if (!removeItem.trackId && !removeItem.trackIndex) { return errorCallback(new Error('Track removal spec is invalid')); }
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'removeItem', [removeItem.trackIndex, removeItem.trackId]);
  }

  /**
   * Removes all given tracks from the playlist; these can be specified either by trackId or trackIndex. If the removed items
   * include the currently playing item, the next available item will automatically begin playing.
   */
  removeItems = (successCallback: SuccessCallback, errorCallback: ErrorCallback, items: AudioTrackRemoval[]) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'removeItems', [items]);
  }

  /**
   * Clear the entire playlist. This will result in the STOPPED event being raised.
   */
  clearAllItems = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'clearAllItems', []);
  }

  /**
   * Playback management
   */

  /**
   * Begin playback. If no tracks have been added, this has no effect.
   */
  play = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'play', []);
  }

  /**
   * Play the track at the given index. If the track does not exist, this has no effect.
   */
  playTrackByIndex = (successCallback: SuccessCallback, errorCallback: ErrorCallback, index: number, position?: number) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'playTrackByIndex', [index, position || 0]);
  }

  /**
   * Play the track matching the given trackId. If the track does not exist, this has no effect.
   */
  playTrackById = (successCallback: SuccessCallback, errorCallback: ErrorCallback, trackId: string, position?: number) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'playTrackById', [trackId, position || 0]);
  }

  /**
   * Pause playback
   */
  pause = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'pause', []);
  }

  /**
   * Skip to the next track. If you are already at the end, and loop is false, this has no effect.
   * If you are at the end, and loop is true, playback will begin at the beginning of the playlist.
   */
  skipForward = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'skipForward', []);
  }

  /**
   * Skip to the previous track. If you are already at the beginning, this has no effect.
   */
  skipBack = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'skipBack', []);
  }

  /**
   * Seek to the given position in the currently playing track. If the value exceeds the track length,
   * the track will complete and playback of the next track will begin.
   */
  seekTo = (successCallback: SuccessCallback, errorCallback: ErrorCallback, position: number) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'seekTo', [position]);
  }

  /**
   * (iOS only): Seek to the given position in the *entire queue of songs*.
   * Not implemented on Android since the Android player does not load track durations until the item
   * begins playback. On the TODO list to implement.
   */
  seekToQueuePosition = (successCallback: SuccessCallback, errorCallback: ErrorCallback, position: number) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'seekToQueuePosition', [position]);
  }

  /**
   * Set the playback speed; a float value between [-1, 1] inclusive. If set to 0, this pauses playback.
   */
  setPlaybackRate = (successCallback: SuccessCallback, errorCallback: ErrorCallback, rate: number) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setPlaybackRate', [rate]);
  }

  /**
   * Set the playback volume. Float value between [0, 1] inclusive.
   * On both Android and iOS, this sets the volume of the media stream, which can be externally
   * controlled by setting the overall hardware volume.
   */
  setVolume = (successCallback: SuccessCallback, errorCallback: ErrorCallback, volume: number) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setPlaybackVolume', [volume]);
  }

  /**
   * Sets a flag indicating whether the playlist should loop back to the beginning once it reaches the end.
   */
  setLoop = (successCallback: SuccessCallback, errorCallback: ErrorCallback, loop: boolean) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setLoopAll', [!!loop]);
  }

  /**
   * Get accessors
   */

  /**
   * Reports the current playback rate.
   */
  getPlaybackRate = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getPlaybackRate', []);
  }

  /**
   * Reports the current playback volume
   */
  getVolume = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getPlaybackVolume', []);
  }

  /**
   * Reports the playback position of the current item. You are recommended to handle the onStatus events
   * rather than this value, as this value will be stale by the time you receive it.
   */
  getPosition = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getPlaybackPosition', []);
  }

  /**
   * Reports the buffer status of the current item. You are recommended to handle the onStatus events
   * rather than this value, as this value will be stale by the time you receive it.
   */
  getCurrentBuffer = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getCurrentBuffer', []);
  }

  /**
   * (iOS only): Gets the overall playback position in the entire queue, in seconds (e.g. 1047 seconds).
   * Not implemented on Android since durations are not known ahead of time.
   */
  getQueuePosition = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getQueuePosition', []);
  }


  /**
   * Status event handling
   */

  /**
   * @internal
   * Call this function to emit an onStatus event via the on('status') handler.
   * Internal use only, to raise events received from the native interface.
   */
  protected onStatus(trackId: string, type: RmxAudioStatusMessage, value: OnStatusCallbackUpdateData | OnStatusTrackChangedData | OnStatusErrorCallbackData) {
    const status = { type, trackId, value };
    if (this.options.verbose) {
      log.log(`RmxAudioPlayer.onStatus: ${RmxAudioStatusMessageDescriptions[type]}(${type}) [${trackId}]: `, value);
    }

    if (status.type === RmxAudioStatusMessage.RMXSTATUS_TRACK_CHANGED) {
      this._hasError = false;
      this._hasLoaded = false;
      this._currentState = 'loading';
      this._currentItem = (status.value as OnStatusTrackChangedData).currentItem;
    }

    // The plugin's status changes only in response to specific events.
    if (itemStatusChangeTypes.indexOf(status.type) >= 0) {
      // Only change the plugin's *current status* if the event being raised is for the current active track.
      if (this._currentItem && this._currentItem.trackId === trackId) {

        if (status.value && (<any>status.value).status) {
          this._currentState = (<any>status.value).status;
        }

        if (status.type === RmxAudioStatusMessage.RMXSTATUS_CANPLAY) {
          this._hasLoaded = true;
        }

        if (status.type === RmxAudioStatusMessage.RMXSTATUS_ERROR) {
          this._hasError = true;
        }
      }
    }

    this.emit('status', status);
  }

  /**
   * Subscribe to events raised by the plugin, e.g. on('status', (data) => { ... }),
   * For now, only 'status' is supported.
   *
   * @param eventName Name of event to subscribe to.
   * @param callback The callback function to receive the event data
   */
  on(eventName: "status", callback: OnStatusCallback): void;
  on(eventName: string, callback: AudioPlayerEventHandler) {
    if (!Object.prototype.hasOwnProperty.call(this.handlers, eventName)) {
      this.handlers[eventName] = [];
    }
    this.handlers[eventName].push(callback);
  }

  /**
   * Remove an event handler from the plugin
   * @param eventName The name of the event whose subscription is to be removed
   * @param handle The event handler to destroy. Ensure that this is the SAME INSTANCE as the handler
   * that was passed in to create the subscription!
   */
  off(eventName: string, handle: AudioPlayerEventHandler) {
    if (Object.prototype.hasOwnProperty.call(this.handlers, eventName)) {
      const handleIndex = this.handlers[eventName].indexOf(handle);
      if (handleIndex >= 0) {
        this.handlers[eventName].splice(handleIndex, 1);
      }
    }
  }

  /**
   * @internal
   * Raises an event via the corresponding event handler. Internal use only.
   * @param args Event args to pass through to the handler.
   */
  protected emit(...args: any[]) {
    const eventName: string = args.shift();
    if (!Object.prototype.hasOwnProperty.call(this.handlers, eventName)) {
      return false;
    }

    const handler = this.handlers[eventName];
    for (let i = 0; i < handler.length; i++) {
      const callback = this.handlers[eventName][i];
      if (typeof callback === 'function') {
        callback(...args);
      }
    }

    return true;
  }

  /**
   * Validates the list of AudioTrack items to ensure they are valid.
   * Used internally but you can call this if you need to :)
   *
   * @param items The AudioTrack items to validate
   */
  validateTracks = (items: AudioTrack[]) => {
    if (!items || !Array.isArray(items)) { return []; }
    return items.map(this.validateTrack).filter(x => x); // may produce an empty array!
  }

  /**
   * Validate a single track and ensure it is valid for playback.
   * Used internally but you can call this if you need to :)
   *
   * @param track The AudioTrack to validate
   */
  validateTrack = (track: AudioTrack) => {
    if (!track) { return null; }
    // For now we will rely on TS to do the heavy lifting, but we can add a validation here
    // that all the required fields are valid. For now we just take care of the unique ID.
    track.trackId = track.trackId || this.generateUUID();
    return track;
  }

  /**
   * Generate a v4 UUID for use as a unique trackId. Used internally, but you can use this to generate track ID's if you want.
   */
  generateUUID() { // Doesn't need to be perfect or secure, just good enough to give each item an ID.
    var d = new Date().getTime();
    if (typeof performance !== 'undefined' && typeof performance.now === 'function') {
      d += performance.now(); //use high-precision timer if available
    }
    // There are better ways to do this in ES6, we are intentionally avoiding the import
    // of an ES6 polyfill here.
    const template = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx';
    return (<string[]>[].slice.call(template)).map(function (c) {
      if (c === '-' || c === '4') { return c; }
      var r = (d + Math.random() * 16) % 16 | 0;
      d = Math.floor(d / 16);
      return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    }).join('');
  }
}

const playerInstance = new RmxAudioPlayer();

/*!
 * AudioPlayer Plugin instance.
 */

export const AudioPlayer = playerInstance;
export default playerInstance; // keep typescript happy
