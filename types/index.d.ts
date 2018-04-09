// Type definitions for cordova-plugin-audio-player
// Project: https://github.com/Rolamix/cordova-plugin-audio-player
// Definitions by: codinronan <https://github.com/codinronan>

declare namespace RmxAudioPlayer {

  interface RmxAudioPlayerStatic {
		init(options: InitOptions): AudioPlayer
		new (options: InitOptions): AudioPlayer
  }

}

interface Window {
	RmxAudioPlayer: RmxAudioPlayer.AudioPlayer
}
declare var RmxAudioPlayer: RmxAudioPlayer.AudioPlayer;
