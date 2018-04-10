import { RmxAudioErrorType, RmxAudioStatusMessage } from './Constants';
export declare type AudioPlayerEventHandler = (args?: any) => void;
export interface AudioPlayerEventHandlers {
    [key: string]: AudioPlayerEventHandler[];
}
export interface AudioPlayerOptions {
}
export interface AudioTrack {
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
