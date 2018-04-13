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
} from './interfaces';

/*!
 * Module dependencies.
 */

declare var cordova: any;
const exec = cordova.require('cordova/exec');
const channel = cordova.require('cordova/channel');
const log = console;

/**
 * AudioPlayer class implementation. A singleton of this class is exported for use by Cordova,
 * but nothing stops you from creating another instance. Keep in mind that the native players
 * are in fact singletons, so the only thing the separate instance gives you would be
 * separate onStatus callback streams.
 */
export class RmxAudioPlayer {
  handlers: AudioPlayerEventHandlers = {};
  options: AudioPlayerOptions = { verbose: false, resetStreamOnPause: true };
  private _currentState: string = 'unknown';
  private _hasError: boolean = false;
  private _hasLoaded: boolean = false;

  get currentState() {
    return this._currentState;
  }

  get isInitialized() {
    return this._currentState !== 'unknown';
  }

  get isPlaying() {
    return this._currentState === 'playing';
  }

  get isPaused() {
    return this._currentState === 'paused' || this._currentState === 'stopped';
  }

  get isLoading() {
    return this._currentState === 'loading';
  }

  get hasLoaded() {
    return this._hasLoaded;
  }

  get hasError() {
    return this._hasError;
  }


  constructor() {
    this.handlers = {};
  }

  /**
   * Player interface
   */

  init = (successCallback: SuccessCallback, errorCallback: ErrorCallback, options: AudioPlayerOptions) => {
    this.options = {...this.options, ...options};
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'initialize', [options]);
  }

  /**
   * Playlist item management
   */

  setPlaylistItems = (successCallback: SuccessCallback, errorCallback: ErrorCallback, items: AudioTrack[]) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setPlaylistItems', [items]);
  }

  addItem = (successCallback: SuccessCallback, errorCallback: ErrorCallback, trackItem: AudioTrack) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'addItem', [trackItem]);
  }

  addAllItems = (successCallback: SuccessCallback, errorCallback: ErrorCallback, items: AudioTrack[]) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'addAllItems', [items]);
  }

  removeItem = (successCallback: SuccessCallback, errorCallback: ErrorCallback, removeItem: AudioTrackRemoval) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'removeItem', [removeItem.trackIndex, removeItem.trackId]);
  }

  removeItems = (successCallback: SuccessCallback, errorCallback: ErrorCallback, items: AudioTrackRemoval[]) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'removeItems', [items]);
  }

  clearAllItems = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'clearAllItems', []);
  }

  /**
   * Playback management
   */

  play = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'play', []);
  }

  playTrackByIndex = (successCallback: SuccessCallback, errorCallback: ErrorCallback, index: number) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'playTrackByIndex', [index]);
  }

  playTrackById = (successCallback: SuccessCallback, errorCallback: ErrorCallback, trackId: string) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'playTrackById', [trackId]);
  }

  pause = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'pause', []);
  }

  skipForward = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'skipForward', []);
  }

  skipBack = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'skipBack', []);
  }

  seekTo = (successCallback: SuccessCallback, errorCallback: ErrorCallback, position: number) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'seekTo', [position]);
  }

  seekToQueuePosition = (successCallback: SuccessCallback, errorCallback: ErrorCallback, position: number) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'seekToQueuePosition', [position]);
  }

  setPlaybackRate = (successCallback: SuccessCallback, errorCallback: ErrorCallback, rate: number) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setPlaybackRate', [rate]);
  }

  setVolume = (successCallback: SuccessCallback, errorCallback: ErrorCallback, volume: number) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setPlaybackVolume', [volume]);
  }

  setLoop = (successCallback: SuccessCallback, errorCallback: ErrorCallback, loop: boolean) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setLoopAll', [!!loop]);
  }

  /**
   * Get accessors
   */

  getPlaybackRate = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getPlaybackRate', []);
  }

  getVolume = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getPlaybackVolume', []);
  }

  getPosition = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getPlaybackPosition', []);
  }

  getCurrentBuffer = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getCurrentBuffer', []);
  }

  getTotalDuration = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getTotalDuration', []);
  }

  getQueuePosition = (successCallback: SuccessCallback, errorCallback: ErrorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getQueuePosition', []);
  }


  /**
   * Status event handling
   */

  onStatus(trackId: string, type: RmxAudioStatusMessage, value: any) {
    const status = { type, trackId, value };
    if (this.options.verbose) {
      log.log(`RmxAudioPlayer.onStatus: ${RmxAudioStatusMessageDescriptions[type]}(${type}) [${trackId}]: `, value);
    }

    if (status.value && status.value.status) {
      this._currentState = status.value.status;
    }

    if (status.type === RmxAudioStatusMessage.RMXSTATUS_ERROR) {
      this._hasError = true;
    }

    if (status.type === RmxAudioStatusMessage.RMXSTATUS_TRACK_CHANGED) {
      this._hasError = false;
      this._hasLoaded = false;
    }

    if (status.type === RmxAudioStatusMessage.RMXSTATUS_CANPLAY) {
      this._hasLoaded = true;
    }

    this.emit('status', status);
  }

  on(eventName: "status", callback: OnStatusCallback): void;
  on(eventName: string, callback: AudioPlayerEventHandler) {
    if (!Object.prototype.hasOwnProperty.call(this.handlers, eventName)) {
      this.handlers[eventName] = [];
    }
    this.handlers[eventName].push(callback);
  }

  off(eventName: string, handle: AudioPlayerEventHandler) {
    if (Object.prototype.hasOwnProperty.call(this.handlers, eventName)) {
      const handleIndex = this.handlers[eventName].indexOf(handle);
      if (handleIndex >= 0) {
        this.handlers[eventName].splice(handleIndex, 1);
      }
    }
  }

  emit(...args: any[]) {
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
}

const playerInstance = new RmxAudioPlayer();

// Initialize the plugin to send and receive messages

channel.createSticky('onRmxAudioPlayerReady');
channel.waitForInitialization('onRmxAudioPlayerReady');

function onNativeStatus(msg: any) {
  if (msg.action === 'status') {
    playerInstance.onStatus(msg.status.trackId, msg.status.msgType, msg.status.value);
  } else {
    throw new Error(`Unknown audio player action ${msg.action}`);
  }
}

channel.onCordovaReady.subscribe(() => {
  exec(onNativeStatus, undefined, 'RmxAudioPlayer', 'storeMessageChannel', []);
  channel.initializationComplete('onRmxAudioPlayerReady');
});

/*!
 * AudioPlayer Plugin instance.
 */

export const AudioPlayer = playerInstance;
export default playerInstance; // keep typescript happy
