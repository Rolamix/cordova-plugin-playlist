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
    // NSString* albumArt = trackInfo[@"albumArt"];

    if (trackId == nil || [trackId isEqualToString:@""]) { return nil; }
    if (assetUrl == nil) { return nil; }

    NSURL* assetUrlObj = [self getUrlForAsset:assetUrl];
    AudioTrack* track = [AudioTrack playerItemWithURL:assetUrlObj];

    track.trackId = trackId;
    track.artist = trackInfo[@"artist"];
    track.album = trackInfo[@"album"];
    track.title = trackInfo[@"title"];

    return track;
}

// We create a wrapper function for this so that we can properly handle web, file, cdv, and document urls.
+(NSURL*)getUrlForAsset:(NSString*)assetUrl
{
    return [NSURL URLWithString:assetUrl];
}

- (void)dealloc {

}

@end
