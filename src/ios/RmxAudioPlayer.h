//
// RmxAudioPlayer.h
// Music Controls Cordova Plugin
//
// Created by Juan Gonzalez on 12/16/16.
//

#ifndef RmxAudioPlayer_h
#define RmxAudioPlayer_h

#import <UIKit/UIKit.h>
#import <Cordova/CDVPlugin.h> // this already includes Foundation.h
#import <MediaPlayer/MediaPlayer.h>
#import <MediaPlayer/MPNowPlayingInfoCenter.h>
#import <MediaPlayer/MPMediaItem.h>
#import <AVFoundation/AVFoundation.h>

#import "Constants.h"
#import "AudioTrack.h"

@interface RmxAudioPlayer : CDVPlugin

// structural methods
- (void) pluginInitialize;
- (void) setOptions:(CDVInvokedUrlCommand*) command;
- (void) initialize:(CDVInvokedUrlCommand*) command;

// public API

// Item management
- (void) setPlaylistItems:(CDVInvokedUrlCommand *) command;
- (void) addItem:(CDVInvokedUrlCommand *) command;
- (void) addAllItems:(CDVInvokedUrlCommand *) command;
- (void) removeItem:(CDVInvokedUrlCommand *) command;
- (void) removeItems:(CDVInvokedUrlCommand *) command;
- (void) clearAllItems:(CDVInvokedUrlCommand*) command;

// Playback management
- (void) play:(CDVInvokedUrlCommand *) command;
- (void) playTrackByIndex:(CDVInvokedUrlCommand *) command;
- (void) playTrackById:(CDVInvokedUrlCommand *) command;
- (void) pause:(CDVInvokedUrlCommand *) command;
- (void) skipForward:(CDVInvokedUrlCommand *) command;
- (void) skipBack:(CDVInvokedUrlCommand *) command;
- (void) seekTo:(CDVInvokedUrlCommand *) command;
- (void) seekToQueuePosition:(CDVInvokedUrlCommand *) command;
- (void) setPlaybackRate:(CDVInvokedUrlCommand *) command;
- (void) setPlaybackVolume:(CDVInvokedUrlCommand *) command;
- (void) setLoopAll:(CDVInvokedUrlCommand *) command;

// Get accessors to manually update values. Note:
// these values are reported anyway via the onStatus event
// stream, you don't normally need to read these directly.
- (void) getPlaybackRate:(CDVInvokedUrlCommand *) command;
- (void) getPlaybackVolume:(CDVInvokedUrlCommand *) command;
- (void) getPlaybackPosition:(CDVInvokedUrlCommand *) command;
- (void) getCurrentBuffer:(CDVInvokedUrlCommand *) command;
- (void) getQueuePosition:(CDVInvokedUrlCommand *) command;

// Cleanup
- (void) release:(CDVInvokedUrlCommand*) command;

@end

#endif /* RmxAudioPlayer_h */
