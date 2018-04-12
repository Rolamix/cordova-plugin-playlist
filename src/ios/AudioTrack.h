//
//  AudioTrack.h
//  BackgroundAudioObjc
//
//  Created by Patrick Sears on 3/29/18.
//

#ifndef AudioTrack_h
#define AudioTrack_h

#import <AVFoundation/AVFoundation.h>

// AVPlayerItem already includes duration, error, status
@interface AudioTrack : AVPlayerItem

@property (nonatomic) BOOL isStream;
@property (nonatomic, strong) NSString* trackId;
@property (nonatomic, strong) NSURL* assetUrl;
@property (nonatomic, strong) NSURL* albumArt;
@property (nonatomic, strong) NSString* artist;
@property (nonatomic, strong) NSString* album;
@property (nonatomic, strong) NSString* title;

+(AudioTrack*)initWithDictionary:(NSDictionary*)trackInfo;
-(NSDictionary*)toDict;

@end

#endif /* AudioTrack_h */
