"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = exports.AudioPlayer = exports.RmxAudioPlayer = void 0;

var _Constants = require("./Constants");

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

var exec = cordova.require('cordova/exec');

var channel = cordova.require('cordova/channel');
/**
 * AudioPlayer class implementation. A singleton of this class is exported for use by Cordova,
 * but nothing stops you from creating another instance. Keep in mind that the native players
 * are in fact singletons, so the only thing the separate instance gives you would be
 * separate onStatus callback streams.
 */


var RmxAudioPlayer =
/*#__PURE__*/
function () {
  function RmxAudioPlayer() {
    var _this = this;

    _classCallCheck(this, RmxAudioPlayer);

    Object.defineProperty(this, "handlers", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: {}
    });
    Object.defineProperty(this, "options", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: null
    });
    Object.defineProperty(this, "init", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback, options) {
        // we don't use this for now.
        _this.options = options || {};
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'initialize', [options]);
      }
    });
    Object.defineProperty(this, "setPlaylistItems", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback, items) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setPlaylistItems', [items]);
      }
    });
    Object.defineProperty(this, "addItem", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback, trackItem) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'addItem', [trackItem]);
      }
    });
    Object.defineProperty(this, "addAllItems", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback, items) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'addAllItems', [items]);
      }
    });
    Object.defineProperty(this, "removeItem", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback, removeItem) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'removeItem', [removeItem.trackIndex, removeItem.trackId]);
      }
    });
    Object.defineProperty(this, "removeItems", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback, items) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'removeItems', [items]);
      }
    });
    Object.defineProperty(this, "clearAllItems", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'clearAllItems', []);
      }
    });
    Object.defineProperty(this, "play", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'play', []);
      }
    });
    Object.defineProperty(this, "playTrackByIndex", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback, index) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'playTrackByIndex', [index]);
      }
    });
    Object.defineProperty(this, "playTrackById", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback, trackId) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'playTrackById', [trackId]);
      }
    });
    Object.defineProperty(this, "pause", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'pause', []);
      }
    });
    Object.defineProperty(this, "skipForward", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'skipForward', []);
      }
    });
    Object.defineProperty(this, "skipBack", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'skipBack', []);
      }
    });
    Object.defineProperty(this, "seekTo", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback, position) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'seekTo', [position]);
      }
    });
    Object.defineProperty(this, "seekToQueuePosition", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback, position) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'seekToQueuePosition', [position]);
      }
    });
    Object.defineProperty(this, "setPlaybackRate", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback, rate) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setPlaybackRate', [rate]);
      }
    });
    Object.defineProperty(this, "setVolume", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback, volume) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setPlaybackVolume', [volume]);
      }
    });
    Object.defineProperty(this, "setLoop", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback, loop) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'setLoopAll', [!!loop]);
      }
    });
    Object.defineProperty(this, "getPlaybackRate", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getPlaybackRate', []);
      }
    });
    Object.defineProperty(this, "getVolume", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getPlaybackVolume', []);
      }
    });
    Object.defineProperty(this, "getPosition", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getPlaybackPosition', []);
      }
    });
    Object.defineProperty(this, "getCurrentBuffer", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getCurrentBuffer', []);
      }
    });
    Object.defineProperty(this, "getTotalDuration", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getTotalDuration', []);
      }
    });
    Object.defineProperty(this, "getQueuePosition", {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function value(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'RmxAudioPlayer', 'getQueuePosition', []);
      }
    });
    this.handlers = {};
  }
  /**
   * Player interface
   */


  _createClass(RmxAudioPlayer, [{
    key: "onStatus",

    /**
     * Status event handling
     */
    value: function onStatus(trackId, type, value) {
      var status = {
        type,
        trackId,
        value
      };
      console.log(`RmxAudioPlayer.onStatus: ${_Constants.RmxAudioStatusMessageDescriptions[type]}(${type}) [${trackId}]: `, value);
      this.emit('status', status);
    }
  }, {
    key: "on",
    value: function on(eventName, callback) {
      if (!Object.prototype.hasOwnProperty.call(this.handlers, eventName)) {
        this.handlers[eventName] = [];
      }

      this.handlers[eventName].push(callback);
    }
  }, {
    key: "off",
    value: function off(eventName, handle) {
      if (Object.prototype.hasOwnProperty.call(this.handlers, eventName)) {
        var handleIndex = this.handlers[eventName].indexOf(handle);

        if (handleIndex >= 0) {
          this.handlers[eventName].splice(handleIndex, 1);
        }
      }
    }
  }, {
    key: "emit",
    value: function emit() {
      for (var _len = arguments.length, args = new Array(_len), _key = 0; _key < _len; _key++) {
        args[_key] = arguments[_key];
      }

      var eventName = args.shift();

      if (!Object.prototype.hasOwnProperty.call(this.handlers, eventName)) {
        return false;
      }

      var handler = this.handlers[eventName];

      for (var i = 0; i < handler.length; i++) {
        var _callback = this.handlers[eventName][i];

        if (typeof _callback === 'function') {
          _callback.apply(void 0, args);
        }
      }

      return true;
    }
  }]);

  return RmxAudioPlayer;
}();

exports.RmxAudioPlayer = RmxAudioPlayer;
var playerInstance = new RmxAudioPlayer(); // Initialize the plugin to send and receive messages

channel.createSticky('onRmxAudioPlayerReady');
channel.waitForInitialization('onRmxAudioPlayerReady');

function onNativeStatus(msg) {
  if (msg.action === 'status') {
    playerInstance.onStatus(msg.status.trackId, msg.status.msgType, msg.status.value);
  } else {
    throw new Error(`Unknown media action ${msg.action}`);
  }
}

channel.onCordovaReady.subscribe(function () {
  exec(onNativeStatus, undefined, 'RmxAudioPlayer', 'storeMessageChannel', []);
  channel.initializationComplete('onRmxAudioPlayerReady');
});
/*!
 * AudioPlayer Plugin instance.
 */

var AudioPlayer = playerInstance;
exports.AudioPlayer = AudioPlayer;
var _default = playerInstance; // keep typescript happy

exports.default = _default;