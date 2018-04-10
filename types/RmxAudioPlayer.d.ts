import { RmxAudioStatusMessage } from './Constants';
import { AudioPlayerEventHandler, AudioPlayerEventHandlers, AudioPlayerOptions, AudioTrack, AudioTrackRemoval, OnStatusCallback, SuccessCallback, ErrorCallback } from './interfaces';
/**
 * AudioPlayer class implementation. A singleton of this class is exported for use by Cordova,
 * but nothing stops you from creating another instance. Keep in mind that the native players
 * are in fact singletons, so the only thing the separate instance gives you would be
 * separate onStatus callback streams.
 */
export declare class RmxAudioPlayer {
    handlers: AudioPlayerEventHandlers;
    options: AudioPlayerOptions | null;
    constructor();
    /**
     * Player interface
     */
    init: (successCallback: SuccessCallback, errorCallback: ErrorCallback, options: AudioPlayerOptions) => void;
    /**
     * Playlist item management
     */
    setPlaylistItems: (successCallback: SuccessCallback, errorCallback: ErrorCallback, items: AudioTrack[]) => void;
    addItem: (successCallback: SuccessCallback, errorCallback: ErrorCallback, trackItem: AudioTrack) => void;
    addAllItems: (successCallback: SuccessCallback, errorCallback: ErrorCallback, items: AudioTrack[]) => void;
    removeItem: (successCallback: SuccessCallback, errorCallback: ErrorCallback, removeItem: AudioTrackRemoval) => void;
    removeItems: (successCallback: SuccessCallback, errorCallback: ErrorCallback, items: AudioTrackRemoval[]) => void;
    clearAllItems: (successCallback: SuccessCallback, errorCallback: ErrorCallback) => void;
    /**
     * Playback management
     */
    play: (successCallback: SuccessCallback, errorCallback: ErrorCallback) => void;
    playTrackByIndex: (successCallback: SuccessCallback, errorCallback: ErrorCallback, index: number) => void;
    playTrackById: (successCallback: SuccessCallback, errorCallback: ErrorCallback, trackId: string) => void;
    pause: (successCallback: SuccessCallback, errorCallback: ErrorCallback) => void;
    skipForward: (successCallback: SuccessCallback, errorCallback: ErrorCallback) => void;
    skipBack: (successCallback: SuccessCallback, errorCallback: ErrorCallback) => void;
    seekTo: (successCallback: SuccessCallback, errorCallback: ErrorCallback, position: number) => void;
    seekToQueuePosition: (successCallback: SuccessCallback, errorCallback: ErrorCallback, position: number) => void;
    setPlaybackRate: (successCallback: SuccessCallback, errorCallback: ErrorCallback, rate: number) => void;
    setVolume: (successCallback: SuccessCallback, errorCallback: ErrorCallback, volume: number) => void;
    setLoop: (successCallback: SuccessCallback, errorCallback: ErrorCallback, loop: boolean) => void;
    /**
     * Get accessors
     */
    getPlaybackRate: (successCallback: SuccessCallback, errorCallback: ErrorCallback) => void;
    getVolume: (successCallback: SuccessCallback, errorCallback: ErrorCallback) => void;
    getPosition: (successCallback: SuccessCallback, errorCallback: ErrorCallback) => void;
    getCurrentBuffer: (successCallback: SuccessCallback, errorCallback: ErrorCallback) => void;
    getTotalDuration: (successCallback: SuccessCallback, errorCallback: ErrorCallback) => void;
    getQueuePosition: (successCallback: SuccessCallback, errorCallback: ErrorCallback) => void;
    /**
     * Status event handling
     */
    onStatus(trackId: string, type: RmxAudioStatusMessage, value: any): void;
    on(eventName: "status", callback: OnStatusCallback): void;
    off(eventName: string, handle: AudioPlayerEventHandler): void;
    emit(...args: any[]): boolean;
}
declare const playerInstance: RmxAudioPlayer;
export declare const AudioPlayer: RmxAudioPlayer;
export default playerInstance;
