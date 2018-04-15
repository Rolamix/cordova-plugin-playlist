//
//  AudioTrack.m
//  RmxAudioPlayer
//
//  Created by codinronan on 3/29/18.
//

#import "AudioTrack.h"

@interface AudioTrack() {

}
// other properties
@end

@implementation AudioTrack

+(AudioTrack*)initWithDictionary:(NSDictionary*)trackInfo
{
    NSString* trackId = trackInfo[@"trackId"];
    NSString* assetUrl = trackInfo[@"assetUrl"];
    NSString* isStreamStr = trackInfo[@"isStream"];
    NSString* albumArt = trackInfo[@"albumArt"];

    if (trackId == nil || [trackId isEqualToString:@""]) { return nil; }
    if (assetUrl == nil) { return nil; }

    NSURL* assetUrlObj = [self getUrlForAsset:assetUrl];
    AudioTrack* track = [AudioTrack playerItemWithURL:assetUrlObj];

    BOOL isStream = NO;
    if (isStreamStr != nil && [isStreamStr boolValue]) {
        isStream = YES;
    }

    track.isStream = isStream;
    track.trackId = trackId;
    track.assetUrl = assetUrlObj;
    track.albumArt = albumArt != nil ? [self getUrlForAsset:albumArt] : nil;
    track.artist = trackInfo[@"artist"];
    track.album = trackInfo[@"album"];
    track.title = trackInfo[@"title"];

    if (isStream && [track respondsToSelector:@selector(setCanUseNetworkResourcesForLiveStreamingWhilePaused:)]) {
        track.canUseNetworkResourcesForLiveStreamingWhilePaused = YES;
    }

    return track;
}

// We create a wrapper function for this so that we can properly handle web, file, cdv, and document urls.
+(NSURL*)getUrlForAsset:(NSString*)assetUrl
{
    return [NSURL URLWithString:assetUrl];
}

-(NSDictionary*)toDict {
  NSDictionary* info = @{
    @"isStream": @(self.isStream),
    @"trackId": self.trackId,
    @"assetUrl": [self.assetUrl absoluteString],
    @"albumArt": self.albumArt != nil ? [self.albumArt absoluteString] : @"",
    @"artist": self.artist,
    @"album": self.album,
    @"title": self.title
  };

  return info;
}

- (void)dealloc {

}

@end
