/* global cordova:false */
/* globals window */

import {
  RmxAudioErrorType,
  RmxAudioErrorTypeDescriptions,
  RmxAudioStatusMessage,
  RmxAudioStatusMessageDescriptions,
} from './Constants';

/*!
 * Module dependencies.
 */

const exec = cordova.require('cordova/exec');
const channel = cordova.require('cordova/channel');

// for debug, add these immediately after initializing the class.
// "https://rolamix-usercontent.s3.us-east-2.amazonaws.com/band-audios/5a4eec1e7b9b120ef9c8d528/coYiT0UlNJtnSv.mp3"
// "https://rolamix-usercontent.s3.us-east-2.amazonaws.com/band-audios/5a5d72f3617acf0e90de39b3/qeQM8hfanV143Q.mp3"

class AudioPlayer {
  constructor() {
    this.handlers = {};
  }

  /**
   * Player interface
   */

  init = (successCallback, errorCallback, options) => {
    // we don't use this for now.
    this.options = options || null;
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'initialize', [options]);
  }

  /**
   * Playlist item management
   */

  setPlaylistItems = (successCallback, errorCallback, items) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setPlaylistItems', [items]);
  }

  addItem = (successCallback, errorCallback, trackItem) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'addItem', [trackItem]);
  }

  addAllItems = (successCallback, errorCallback, trackItems) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'addAllItems', [trackItems]);
  }

  removeItem = (successCallback, errorCallback, trackItem) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'removeItem', [trackItem]);
  }

  removeItems = (successCallback, errorCallback, trackItems) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'removeItems', [trackItems]);
  }

  clearAllItems = (successCallback, errorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'clearAllItems', []);
  }

  /**
   * Playback management
   */

  play = (successCallback, errorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'play', []);
  }

  playTrackByIndex = (successCallback, errorCallback, index) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'playTrackByIndex', [index]);
  }

  playTrackById = (successCallback, errorCallback, trackId) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'playTrackById', [trackId]);
  }

  pause = (successCallback, errorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'pause', []);
  }

  skipForward = (successCallback, errorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'skipForward', []);
  }

  skipBack = (successCallback, errorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'skipBack', []);
  }

  seekTo = (successCallback, errorCallback, position) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'seekTo', [position]);
  }

  seekToQueuePosition = (successCallback, errorCallback, position) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'seekToQueuePosition', [position]);
  }

  setPlaybackRate = (successCallback, errorCallback, rate) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setPlaybackRate', [rate]);
  }

  setVolume = (successCallback, errorCallback, volume) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setPlaybackVolume', [volume]);
  }

  setLoop = (successCallback, errorCallback, loop) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setLoopAll', [!!loop]);
  }

  /**
   * Get accessors
   */

  getPlaybackRate = (successCallback, errorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getPlaybackRate', []);
  }

  getVolume = (successCallback, errorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getPlaybackVolume', []);
  }

  getPosition = (successCallback, errorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getPlaybackPosition', []);
  }

  getCurrentBuffer = (successCallback, errorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getCurrentBuffer', []);
  }

  getTotalDuration = (successCallback, errorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getTotalDuration', []);
  }

  getQueuePosition = (successCallback, errorCallback) => {
    exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getQueuePosition', []);
  }


  /**
   * Status event handling
   */

  onStatus(trackId, type, value) {
    const status = { type, trackId, value };
    console.log(`RmxAudioPlayer.onStatus: ${RmxAudioStatusMessageDescriptions[type]}: ${value}`);
    this.emit('status', status);
  }

  on(eventName, callback) {
    if (!Object.prototype.hasOwnProperty.call(this.handlers, eventName)) {
      this.handlers[eventName] = [];
    }
    this.handlers[eventName].push(callback);
  }

  off(eventName, handle) {
    if (Object.prototype.hasOwnProperty.call(this.handlers, eventName)) {
      const handleIndex = this.handlers[eventName].indexOf(handle);
      if (handleIndex >= 0) {
        this.handlers[eventName].splice(handleIndex, 1);
      }
    }
  }

  emit(...args) {
    const eventName = args.shift();
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

const playerInstance = new AudioPlayer();

// Initialize the plugin to send and receive messages

channel.createSticky('onRmxAudioPlayerReady');
channel.waitForInitialization('onRmxAudioPlayerReady');

function onNativeStatus(msg) {
  if (msg.action === 'status') {
    playerInstance.onStatus(msg.status.trackId, msg.status.msgType, msg.status.value);
  } else {
    throw new Error(`Unknown media action ${msg.action}`);
  }
}

channel.onCordovaReady.subscribe(() => {
  exec(onNativeStatus, undefined, 'RmxAudioPlayer', 'storeMessageChannel', []);
  channel.initializationComplete('onRmxAudioPlayerReady');
});

/*!
 * AudioPlayer Plugin.
 */

module.exports = {

  /**
   * AudioPlayer instance.
   */
  AudioPlayer: playerInstance,
  AudioErrorType: RmxAudioErrorType,
  AudioErrorTypeDescriptions: RmxAudioErrorTypeDescriptions,
  AudioStatusMessage: RmxAudioStatusMessage,
  AudioStatusMessageDescriptions: RmxAudioStatusMessageDescriptions,
};
