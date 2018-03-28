# cordova-plugin-audio-player
A Cordova plugin for Android and iOS with native support for audio playlists, background support, and lock screen controls

## 0. Index

1. [Background](#1-background)
2. [Installation](#2-installation)
3. [Usage](#3-usage)
4. [Todo](#4-todo)
5. [Credits](#5-credits)
6. [License](#6-license)

## 1. Background

I have worked with [cordova-plugin-media](https://github.com/apache/cordova-plugin-media) for quite a long time and have pushed it pretty far. In the end it's not designed for certain use cases and I ultimately decided to apply what I had learned, and issues I have seen others have, to create a plugin that better addresses those use cases.

Both Android and iOS have special support for playlist-based playback, and the native implementation provides a superior user experience over attempting to implement a playlist out of an interface designed for single-item playback. Also, it is not possible to implement continuous playback using `cordova-plugin-media` on iOS, since the space between songs in the background will stop playback. This plugin addresses that, in addition to including support for command center and lock screen controls.

* Playlist support is implemented in native code
* Native playlists mean better support for continual playback in the background
* Background playback is optional - opt-in via runtime config and config.xml flags
* Includes support for lock-screen controls
* Works just as well for a single item as it does for a playlist
* Compatible with [Cordova Plugman](https://github.com/apache/cordova-plugman).
* For Android and iOS

**NOTE**: This plugin does NOT display track cover art on the lock screen controls on iOS. Usage of the media image object on iOS is historically buggy and can cause memory leaks. See the [Todo](#4-todo) section. This is fully support on Android, however.

### On *Android*, utilizes a wrapper over ExoPlayer called ExoMedia. ExoPlayer is a powerful, high-quality player for Android provided by Google
### On iOS, utilizes a customized AVQueuePlayer in order to provide feedback about track changes, buffering, etc.

## 2. Installation

As with most cordova plugins...

```
cordova plugin add cordova-plugin-audio-player
```

Rather than oblige all developers to include background permissions, add the following to your config.xml if you wish to support continuing to play audio in the background:

### Android - inside `<platform name="android">`:
```
<config-file target="AndroidManifest.xml" parent="/*">
  <uses-permission android:name="android.permission.WAKE_LOCK" />
</config-file>
```

### iOS - inside `<platform name="ios">`:
```
<config-file target="*-Info.plist" parent="UIBackgroundModes">
  <array>
    <string>audio</string>
  </array>
</config-file>
```

Android normally will give you ~2-3 minutes of background playback before killing your audio. Adding the WAKE_LOCK permission allows the plugin to utilize additional permissions to continue playing. 

iOS will immediately stop playback when the app goes into the background if you do not include the `audio` `UIBackgroundMode`. iOS has an additional requirement that audio playback must never stop; when it does, the audio session will be terminated and playback cannot continue without user interaction.

## 3. Usage
Coming...

## 4. Todo

There's so much more to do on this plugin. Some items I would like to see added if anyone wants to help:
* [iOS] Safely implement cover art for cover images displayed on the command/lock screen controls
* [iOS] Write this plugin in Swift instead of Objective-C. I didn't have time to learn Swift when I needed this.
* [iOS] Utilize [AudioPlayer](https://github.com/delannoyk/AudioPlayer) instead of directly implementing AVQueuePlayer. `AudioPlayer` includes some smart network recovery features
* [iOS, Android] Add support for single-item repeat
* [iOS, Android] Add a full example

## 5. Credits

There are several plugins that are similar to this one, but all are focused on aspects of the media management experience. This plugin takes inspiration from:
* [cordova-plugin-media](https://github.com/apache/cordova-plugin-media)
* [ExoMedia](https://github.com/brianwernick/ExoMedia)
* [PlaylistCore](https://github.com/brianwernick/PlaylistCore) (provides player controls on top of ExoMedia)
* [Bi-Directional AVQueuePlayer proof of concept](https://github.com/jrtaal/AVBidirectionalQueuePlayer)
* [cordova-music-controls-plugin](https://github.com/homerours/cordova-music-controls-plugin)


## 6. License

[The MIT License (MIT)](http://www.opensource.org/licenses/mit-license.html)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
