@interface Proximity : NSObject

- (void)addAudioRouteObserver;
- (void)removeAudioRouteObserver;
- (void)setPlaying:(BOOL) _isPlaying;

@end