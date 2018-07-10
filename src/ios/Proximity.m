#import <AVFoundation/AVFoundation.h>

@interface Proximity : NSObject {
  BOOL isPlaying;
}
@end

@implementation Proximity
- (void)initialize {
  isPlaying = false;
}

- (void)setPlaying:(BOOL) _isPlaying {
  isPlaying = _isPlaying;
  [self changeSensorState];
}

- (void)addAudioRouteObserver {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                          selector:@selector(didSessionRouteChange:)
                                          name:AVAudioSessionRouteChangeNotification object:nil];
}

- (void)removeAudioRouteObserver {
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                          name:AVAudioSessionRouteChangeNotification
                                          object:nil];
}

// ヘッドフォンが繋がれていたら近接センサーの無効化
// 繋がれていない場合で、かつ再生中なら有効化
// 繋がれていない場合で、再生中でないなら無効化
- (void)changeSensorState {
    if ([self isHeadsetPluggedIn]) {
        [self inactivateSensor];
    } else if (isPlaying == YES) {
        [self activateSensor];
    } else {
        [self inactivateSensor];
    }
}

- (void)activateSensor {
    [UIDevice currentDevice].proximityMonitoringEnabled = YES;
}
- (void)inactivateSensor {
    [UIDevice currentDevice].proximityMonitoringEnabled = NO;
}

- (void)didSessionRouteChange:(NSNotification *)notification {
    NSDictionary *interuptionDict = notification.userInfo;
    NSInteger routeChangeReason = [[interuptionDict valueForKey:AVAudioSessionRouteChangeReasonKey] integerValue];
    NSLog(@"AVAudioSessionRouteChangeReasonKey: %zd", routeChangeReason);

    switch (routeChangeReason) {
        // 取り外し
        case AVAudioSessionRouteChangeReasonOldDeviceUnavailable: {
            // 取り外しの際は変更しない（再生中にヘッドフォンを抜くと、その瞬間
            // iOSの仕組み上再生が停止するにも関わらず、状態として再生中になっている、
            // そのため取り外しの瞬間の再生ステータスを見ても無意味なため無視する
            //[self changeSensorState];
        }
            break;
            
        // 新規接続
        case AVAudioSessionRouteChangeReasonNewDeviceAvailable: {
            [self changeSensorState];
        }
            
        default:
            break;
    }
}

- (BOOL)isHeadsetPluggedIn {
    // headphone と定義する output
    NSSet *headphoneStrings = [NSSet setWithObjects:AVAudioSessionPortHeadphones,
                             AVAudioSessionPortBluetoothA2DP,
                             nil];
    
    AVAudioSessionRouteDescription* route = [[AVAudioSession sharedInstance] currentRoute];
    for (AVAudioSessionPortDescription* desc in [route outputs]) {
        if ([headphoneStrings containsObject:[desc portType]]) {
            return YES;
        }
    }
    return NO;
}
@end
