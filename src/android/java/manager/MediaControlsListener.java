package com.rolamix.plugins.audioplayer.manager;

import com.rolamix.plugins.audioplayer.data.AudioTrack;

/*
* Interface to enable the PlaylistManager to send these events out.
* We could add more like play/pause/toggle/stop, but right now there
* are other ways to get all the other information.
*/
public interface MediaControlsListener {
  void onNext(AudioTrack currentItem, int currentIndex);
  void onPrevious(AudioTrack currentItem, int currentIndex);
}
