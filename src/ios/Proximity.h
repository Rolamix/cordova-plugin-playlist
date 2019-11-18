@interface Proximity : NSObject

- (void)addAudioRouteObserver;
- (void)removeAudioRouteObserver;
- (void)setPlaying:(BOOL) _isPlaying;
- (void)setSpeakerEnabled:(BOOL) _isSpeakerEnabled;

@end