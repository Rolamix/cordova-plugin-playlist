/**
 * This is an example Angular + Ionic 3 service to wrap the Cordova audio player plugin.
 * Simply drop this into your app, make sure you have the plugin installed,
 * add the service to your app.module.ts and import it wherever you need.
 *
 * An example of such usage might be:
 *
 * constructor(
 *   private cdvAudioPlayer: CordovaAudioPlayerService,
 *  ) {
 *   this.cdvAudioPlayer.init({ verbose: true })
 *     .then(() => {
 *       this.cdvAudioPlayer.setPlaylistItems([
 *         { trackId: '12345', assetUrl: testUrls[0], albumArt: testImgs[0], artist: 'Awesome', album: 'Test Files', title: 'Test 1' },
 *         { trackId: '678900', assetUrl: testUrls[1], albumArt: testImgs[1], artist: 'Awesome', album: 'Test Files', title: 'Test 2' },
 *         { trackId: 'a1b2c3d4', assetUrl: testUrls[2], albumArt: testImgs[2], artist: 'Awesome', album: 'Test Files', title: 'Test 3' },
 *         { trackId: 'a1bSTREAM', assetUrl: testUrls[3], albumArt: testImgs[3], artist: 'Awesome', album: 'Streams', title: 'The Stream', isStream: true },
 *       ])
 *       .then(() => {
 *         this.cdvAudioPlayer.play();
 *       }).catch((err) => console.log('YourService, cdvAudioPlayer setPlaylistItems error: ', err));
 *     }).catch((err) => console.log('YourService, cdvAudioPlayer init error: ', err));
 *
 *   this.cdvAudioPlayer.onStatus.subscribe((status) => {
 *     onsole.log('YourService: Got RmxAudioPlayer onStatus: ', status);
 *   });
 * }
 */

import { Injectable, NgZone } from '@angular/core';
import { Platform } from 'ionic-angular';
import { RmxAudioPlayer } from 'cordova-plugin-audio-player/types/RmxAudioPlayer';
import {
  AudioTrack, AudioTrackRemoval, AudioPlayerOptions, OnStatusCallbackData, OnStatusErrorCallbackData,
} from 'cordova-plugin-audio-player/types/interfaces';
import { ReplaySubject } from 'rxjs/ReplaySubject';

const production = false;
const Log = console;

@Injectable()
export class CordovaAudioPlayerService {

  private AudioPlayer: RmxAudioPlayer;
  private statusStream: ReplaySubject<OnStatusCallbackData | OnStatusErrorCallbackData> = new ReplaySubject(1);

  get onStatus() {
    return this.statusStream;
  }

  constructor(
    private platform: Platform,
    private zone: NgZone,
  ) {
    this.platform.ready().then(() => {
      if (this.platform.is('cordova')) {
        this.AudioPlayer = (<any>window).plugins.AudioPlayer.AudioPlayer;

        if (!this.AudioPlayer) {
          console.log('CordovaAudioPlayerService: Could not read `AudioPlayer` from `window.plugins`: ', (<any>window).plugins);
          throw new Error('CordovaAudioPlayerService: Could not read `AudioPlayer` from `window.plugins`');
        }

        this.AudioPlayer.on('status', (data: OnStatusCallbackData | OnStatusErrorCallbackData) => {
          this.statusStream.next(data);
        });
      }
    });
  }

  /**
   * Playlist item management
   */

  init(options?: AudioPlayerOptions) {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.init(this.getSuccessCb(resolve), this.getErrorCb(reject), options);
    });
  }

  setPlaylistItems = (items: AudioTrack[]) => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.setPlaylistItems(this.getSuccessCb(resolve), this.getErrorCb(reject), items);
    });
  }

  addItem = (trackItem: AudioTrack) => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.addItem(this.getSuccessCb(resolve), this.getErrorCb(reject), trackItem);
    });
  }

  addAllItems = (items: AudioTrack[]) => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.addAllItems(this.getSuccessCb(resolve), this.getErrorCb(reject), items);
    });
  }

  removeItem = (removeItem: AudioTrackRemoval) => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.removeItem(this.getSuccessCb(resolve), this.getErrorCb(reject), removeItem);
    });
  }

  removeItems = (items: AudioTrackRemoval[]) => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.removeItems(this.getSuccessCb(resolve), this.getErrorCb(reject), items);
    });
  }

  clearAllItems = () => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.clearAllItems(this.getSuccessCb(resolve), this.getErrorCb(reject));
    });
  }


  /**
   * Playback management
   */

  play = () => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.play(this.getSuccessCb(resolve), this.getErrorCb(reject));
    });
  }

  playTrackByIndex = (index: number) => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.playTrackByIndex(this.getSuccessCb(resolve), this.getErrorCb(reject), index);
    });
  }

  playTrackById = (trackId: string) => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.playTrackById(this.getSuccessCb(resolve), this.getErrorCb(reject), trackId);
    });
  }

  pause = () => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.pause(this.getSuccessCb(resolve), this.getErrorCb(reject));
    });
  }

  skipForward = () => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.skipForward(this.getSuccessCb(resolve), this.getErrorCb(reject));
    });
  }

  skipBack = () => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.skipBack(this.getSuccessCb(resolve), this.getErrorCb(reject));
    });
  }

  seekTo = (position: number) => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.seekTo(this.getSuccessCb(resolve), this.getErrorCb(reject), position);
    });
  }

  seekToQueuePosition = (position: number) => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.seekToQueuePosition(this.getSuccessCb(resolve), this.getErrorCb(reject), position);
    });
  }

  setPlaybackRate = (rate: number) => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.setPlaybackRate(this.getSuccessCb(resolve), this.getErrorCb(reject), rate);
    });
  }

  setVolume = (volume: number) => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.setVolume(this.getSuccessCb(resolve), this.getErrorCb(reject), volume);
    });
  }

  setLoop = (loop: boolean) => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.setLoop(this.getSuccessCb(resolve), this.getErrorCb(reject), loop);
    });
  }

  /**
   * Get accessors
   */

  getPlaybackRate = () => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.getPlaybackRate(this.getSuccessCb(resolve), this.getErrorCb(reject));
    });
  }

  getVolume = () => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.getVolume(this.getSuccessCb(resolve), this.getErrorCb(reject));
    });
  }

  getPosition = () => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.getPosition(this.getSuccessCb(resolve), this.getErrorCb(reject));
    });
  }

  getCurrentBuffer = () => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.getCurrentBuffer(this.getSuccessCb(resolve), this.getErrorCb(reject));
    });
  }

  getTotalDuration = () => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.getTotalDuration(this.getSuccessCb(resolve), this.getErrorCb(reject));
    });
  }

  getQueuePosition = () => {
    return this.wrapPromise((resolve, reject) => {
      this.AudioPlayer.getQueuePosition(this.getSuccessCb(resolve), this.getErrorCb(reject));
    });
  }


  /**
   * Private helper methods
   */

  private getSuccessCb = <T>(resolve: (value?: T | PromiseLike<T>) => void) => (data: any) => this.zone.run(() => resolve(data));
  private getErrorCb = (reject: (reason?: any) => void) => (data: any) => this.zone.run(() => reject(data));

  private async wrapPromise<T>(execFn: (resolve: (value?: T | PromiseLike<T>) => void, reject: (reason?: any) => void) => void) {
    await this.checkPlatform();
    if (!this.platform.is('cordova')) { return Promise.resolve(); }
    let executor = new Promise((resolve, reject) => execFn(resolve, reject));

    if (!production) {
      executor = executor.then((data) => {
        Log.log('AudioPlayerSuccessCb: ', data);
        return data;
      })
      .catch((err) => {
        Log.warn('AudioPlayerErrorCb: ', err);
        throw err;
      });
    }

    return executor;
  }

  private checkPlatform() {
    return this.platform.ready()
      .then(() => {
        if (!this.platform.is('cordova')) { return; }
        if (!this.AudioPlayer) {
          console.log('CordovaAudioPlayerService: Could not read `AudioPlayer` from `window.plugins`: ', (<any>window).plugins);
          throw new Error('CordovaAudioPlayerService: Could not read `AudioPlayer` from `window.plugins`');
        }
      });
  }

}
