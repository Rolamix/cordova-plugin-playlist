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
*   this.cdvAudioPlayer.setOptions({ verbose: true, resetStreamOnPause: true })
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
*   this.cdvAudioPlayer.setOptions({ verbose: true, resetStreamOnPause: true });
*   this.cdvAudioPlayer.setVolume(0.5);
*
*   this.cdvAudioPlayer.onStatus.subscribe((status) => {
*     console.log('YourService: Got RmxAudioPlayer onStatus: ', status);
*   });
* }
*/
import { Injectable, NgZone } from '@angular/core';
import { Platform } from 'ionic-angular';
import { ReplaySubject } from 'rxjs/ReplaySubject';
import {
  RmxAudioPlayer,
  AudioTrack,
  AudioTrackRemoval,
  AudioPlayerOptions,
  OnStatusCallbackData,
  OnStatusErrorCallbackData,
  PlaylistItemOptions,
} from 'cordova-plugin-playlist';
export * from 'cordova-plugin-playlist'; // 'cordova-plugin-playlist/www/index.d'

@Injectable()
export class CordovaAudioPlayerService {

  private AudioPlayer: RmxAudioPlayer;
  private statusStream: ReplaySubject<OnStatusCallbackData | OnStatusErrorCallbackData> = new ReplaySubject(1);
  private Log = console;

  get onStatus() {
    return this.statusStream;
  }

  get currentState() {
    return this.AudioPlayer ? this.AudioPlayer.currentState : 'unknown';
  }

  get isInitialized() {
    return !!this.AudioPlayer && this.AudioPlayer.isInitialized;
  }

  get isLoading() {
    return !!this.AudioPlayer && this.AudioPlayer.isLoading;
  }

  get isPaused() {
    return !this.AudioPlayer || this.AudioPlayer.isPaused;
  }

  get isPlaying() {
    return !!this.AudioPlayer && this.AudioPlayer.isPlaying;
  }

  get hasLoaded() {
    return !!this.AudioPlayer && this.AudioPlayer.hasLoaded;
  }

  get hasError() {
    return !!this.AudioPlayer && this.AudioPlayer.hasError;
  }

  constructor(
    private platform: Platform,
    private zone: NgZone,
  ) {
    this.platform.ready().then(() => {
      if (this.platform.is('cordova')) {
        this.AudioPlayer = (<any>window).plugins.AudioPlayer.AudioPlayer;

        if (!this.AudioPlayer) {
          this.Log.warn('CordovaAudioPlayerService: Could not read `AudioPlayer` from `window.plugins`: ', (<any>window).plugins);
          throw new Error('CordovaAudioPlayerService: Could not read `AudioPlayer` from `window.plugins`');
        }

        this.AudioPlayer.on('status', (data: OnStatusCallbackData | OnStatusErrorCallbackData) => {
          this.statusStream.next(data);
        });

        // This returns a promise that you can wait for if you want.
        this.AudioPlayer.initialize().catch((ex) => console.warn(ex));
      }
    });
  }

  /**
   * Playlist item management
   */

  setOptions(options?: AudioPlayerOptions) {
    return this.wrapPromise('setOptions', (resolve, reject) => {
      this.AudioPlayer.setOptions(this.getSuccessCb(resolve), this.getErrorCb(reject), options);
    }).catch((ex) => console.warn(ex));
  }

  setPlaylistItems = (items: AudioTrack[], options?: PlaylistItemOptions) => {
    return this.wrapPromise('setPlaylistItems', (resolve, reject) => {
      this.AudioPlayer.setPlaylistItems(this.getSuccessCb(resolve), this.getErrorCb(reject), items, options || {});
    }).catch((ex) => console.warn(ex));
  }

  addItem = (trackItem: AudioTrack) => {
    return this.wrapPromise('addItem', (resolve, reject) => {
      this.AudioPlayer.addItem(this.getSuccessCb(resolve), this.getErrorCb(reject), trackItem);
    }).catch((ex) => console.warn(ex));
  }

  addAllItems = (items: AudioTrack[]) => {
    return this.wrapPromise('addAllItems', (resolve, reject) => {
      this.AudioPlayer.addAllItems(this.getSuccessCb(resolve), this.getErrorCb(reject), items);
    }).catch((ex) => console.warn(ex));
  }

  removeItem = (removeItem: AudioTrackRemoval) => {
    return this.wrapPromise('removeItem', (resolve, reject) => {
      this.AudioPlayer.removeItem(this.getSuccessCb(resolve), this.getErrorCb(reject), removeItem);
    }).catch((ex) => console.warn(ex));
  }

  removeItems = (items: AudioTrackRemoval[]) => {
    return this.wrapPromise('removeItems', (resolve, reject) => {
      this.AudioPlayer.removeItems(this.getSuccessCb(resolve), this.getErrorCb(reject), items);
    }).catch((ex) => console.warn(ex));
  }

  clearAllItems = () => {
    return this.wrapPromise('clearAllItems', (resolve, reject) => {
      this.AudioPlayer.clearAllItems(this.getSuccessCb(resolve), this.getErrorCb(reject));
    }).catch((ex) => console.warn(ex));
  }


  /**
   * Playback management
   */

  play = () => {
    return this.wrapPromise('play', (resolve, reject) => {
      this.AudioPlayer.play(this.getSuccessCb(resolve), this.getErrorCb(reject));
    }).catch((ex) => console.warn(ex));
  }

  playTrackByIndex = (index: number) => {
    return this.wrapPromise('playTrackByIndex', (resolve, reject) => {
      this.AudioPlayer.playTrackByIndex(this.getSuccessCb(resolve), this.getErrorCb(reject), index);
    }).catch((ex) => console.warn(ex));
  }

  playTrackById = (trackId: string) => {
    return this.wrapPromise('playTrackById', (resolve, reject) => {
      this.AudioPlayer.playTrackById(this.getSuccessCb(resolve), this.getErrorCb(reject), trackId);
    }).catch((ex) => console.warn(ex));
  }

  pause = () => {
    return this.wrapPromise('pause', (resolve, reject) => {
      this.AudioPlayer.pause(this.getSuccessCb(resolve), this.getErrorCb(reject));
    }).catch((ex) => console.warn(ex));
  }

  skipForward = () => {
    return this.wrapPromise('skipForward', (resolve, reject) => {
      this.AudioPlayer.skipForward(this.getSuccessCb(resolve), this.getErrorCb(reject));
    }).catch((ex) => console.warn(ex));
  }

  skipBack = () => {
    return this.wrapPromise('skipBack', (resolve, reject) => {
      this.AudioPlayer.skipBack(this.getSuccessCb(resolve), this.getErrorCb(reject));
    }).catch((ex) => console.warn(ex));
  }

  seekTo = (position: number) => {
    return this.wrapPromise('seekTo', (resolve, reject) => {
      this.AudioPlayer.seekTo(this.getSuccessCb(resolve), this.getErrorCb(reject), position);
    }).catch((ex) => console.warn(ex));
  }

  seekToQueuePosition = (position: number) => {
    return this.wrapPromise('seekToQueuePosition', (resolve, reject) => {
      this.AudioPlayer.seekToQueuePosition(this.getSuccessCb(resolve), this.getErrorCb(reject), position);
    }).catch((ex) => console.warn(ex));
  }

  setPlaybackRate = (rate: number) => {
    return this.wrapPromise('setPlaybackRate', (resolve, reject) => {
      this.AudioPlayer.setPlaybackRate(this.getSuccessCb(resolve), this.getErrorCb(reject), rate);
    }).catch((ex) => console.warn(ex));
  }

  setVolume = (volume: number) => {
    return this.wrapPromise('setVolume', (resolve, reject) => {
      this.AudioPlayer.setVolume(this.getSuccessCb(resolve), this.getErrorCb(reject), volume);
    }).catch((ex) => console.warn(ex));
  }

  setLoop = (loop: boolean) => {
    return this.wrapPromise('setLoop', (resolve, reject) => {
      this.AudioPlayer.setLoop(this.getSuccessCb(resolve), this.getErrorCb(reject), loop);
    }).catch((ex) => console.warn(ex));
  }

  /**
   * Get accessors
   */

  getPlaybackRate = () => {
    return this.wrapPromise('getPlaybackRate', (resolve, reject) => {
      this.AudioPlayer.getPlaybackRate(this.getSuccessCb(resolve), this.getErrorCb(reject));
    }).catch((ex) => console.warn(ex));
  }

  getVolume = () => {
    return this.wrapPromise('getVolume', (resolve, reject) => {
      this.AudioPlayer.getVolume(this.getSuccessCb(resolve), this.getErrorCb(reject));
    }).catch((ex) => console.warn(ex));
  }

  getPosition = () => {
    return this.wrapPromise('getPosition', (resolve, reject) => {
      this.AudioPlayer.getPosition(this.getSuccessCb(resolve), this.getErrorCb(reject));
    }).catch((ex) => console.warn(ex));
  }

  getCurrentBuffer = () => {
    return this.wrapPromise('getCurrentBuffer', (resolve, reject) => {
      this.AudioPlayer.getCurrentBuffer(this.getSuccessCb(resolve), this.getErrorCb(reject));
    }).catch((ex) => console.warn(ex));
  }

  getQueuePosition = () => {
    return this.wrapPromise('getQueuePosition', (resolve, reject) => {
      this.AudioPlayer.getQueuePosition(this.getSuccessCb(resolve), this.getErrorCb(reject));
    }).catch((ex) => console.warn(ex));
  }


  /**
   * Private helper methods
   */

  private getSuccessCb = <T>(resolve: (value?: T | PromiseLike<T>) => void) => (data: any) => this.zone.run(() => resolve(data));
  private getErrorCb = (reject: (reason?: any) => void) => (data: any) => this.zone.run(() => reject(data));

  private async wrapPromise<T>(name: string, execFn: (resolve: (value?: T | PromiseLike<T>) => void, reject: (reason?: any) => void) => void) {
    try {
      await this.checkPlatform();
      if (!this.platform.is('cordova')) { return Promise.resolve(); }
      if (!this.AudioPlayer.isInitialized) {
        throw new Error(`cordova-plugin-playlist could not be initialized (calling [${name}])`);
      }

      let executor = new Promise((resolve, reject) => execFn(resolve, reject));

      if (this.AudioPlayer.options.verbose) {
        return executor.then((data) => {
          this.Log.log(`AudioPlayerSuccessCb [${name}]:`, data);
          return data;
        })
          .catch((err) => {
            this.Log.warn(`AudioPlayerSuccessCb [${name}]:`, err);
            throw err;
          });
      }

      return executor;
    } catch (ex) {
      return Promise.reject(ex);
    }
  }

  private checkPlatform() {
    return this.platform.ready()
      .then(() => {
        if (!this.platform.is('cordova')) { return; }
        if (!this.AudioPlayer) {
          this.Log.warn('CordovaAudioPlayerService: Could not read `AudioPlayer` from `window.plugins`: ', (<any>window).plugins);
          throw new Error('CordovaAudioPlayerService: Could not read `AudioPlayer` from `window.plugins`');
        }
        return this.AudioPlayer.ready();
      });
  }

}
