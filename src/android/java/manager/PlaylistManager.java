package com.rolamix.plugins.audioplayer.manager;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ArrayList;
import android.app.Application;
import android.util.Log;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import com.devbrackets.android.playlistcore.api.PlaylistItem;
import com.devbrackets.android.playlistcore.data.MediaProgress;
import com.devbrackets.android.playlistcore.manager.ListPlaylistManager;
import com.devbrackets.android.playlistcore.manager.BasePlaylistManager;
import com.devbrackets.android.playlistcore.api.MediaPlayerApi;
import com.devbrackets.android.exomedia.listener.OnErrorListener;

import com.rolamix.plugins.audioplayer.PlaylistItemOptions;
import com.rolamix.plugins.audioplayer.TrackRemovalItem;
import com.rolamix.plugins.audioplayer.data.AudioTrack;
import com.rolamix.plugins.audioplayer.playlist.AudioApi;
import com.rolamix.plugins.audioplayer.service.MediaService;

/**
 * A PlaylistManager that extends the {@link ListPlaylistManager} for use with the
 * {@link MediaService} which extends {@link com.devbrackets.android.playlistcore.service.BasePlaylistService}.
 */
public class PlaylistManager extends ListPlaylistManager<AudioTrack> implements OnErrorListener {

    private static final String TAG = "PlaylistManager";
    private List<AudioTrack> AudioTracks = new ArrayList<>();

    private boolean mediaServiceStarted = false;
    private float volumeLeft = 1.0f;
    private float volumeRight = 1.0f;
    private float playbackSpeed = 1.0f;
    private boolean loop = false;
    private boolean shouldStopPlaylist = false;
    private boolean previousInvoked = false;
    private boolean nextInvoked = false;
    private AudioTrack currentErrorTrack;

    // Really need a way to propagate the settings through the app
    private boolean resetStreamOnPause = true;

    private WeakReference<MediaControlsListener> mediaControlsListener = new WeakReference<>(null);
    private WeakReference<OnErrorListener> errorListener = new WeakReference<>(null);
    private WeakReference<MediaPlayerApi<AudioTrack>> currentMediaPlayer = new WeakReference<>(null);

    public PlaylistManager(Application application) {
        super(application, MediaService.class);
        this.setParameters(AudioTracks, -1);
    }

    public void onMediaServiceInit(boolean hasInit) {
        mediaServiceStarted = hasInit; // this now implies that this.getPlaylistHandler() is not null.
    }

    public void onMediaPlayerChanged(MediaPlayerApi<AudioTrack> currentMediaPlayer) {
        if (this.currentMediaPlayer.get() != null) {
            this.currentMediaPlayer.clear();
            this.currentMediaPlayer = null;
        }
        this.currentMediaPlayer = new WeakReference<>(currentMediaPlayer);
        if (mediaServiceStarted) {
            setVolume(volumeLeft, volumeRight);
            setPlaybackSpeed(playbackSpeed); // dunno bout this one. probably should be independent.
        }
    }

    public void setOnErrorListener(OnErrorListener listener) {
      errorListener = new WeakReference<>(listener);
    }

    public void setMediaControlsListener(MediaControlsListener listener) {
      mediaControlsListener = new WeakReference<>(listener);
    }

    public boolean getResetStreamOnPause() {
      return resetStreamOnPause;
    }

    public void setResetStreamOnPause(boolean val) {
      resetStreamOnPause = val;
    }

    public AudioTrack getCurrentErrorTrack() {
      return currentErrorTrack;
    }

    public void setCurrentErrorTrack(@Nullable PlaylistItem errorItem) {
        currentErrorTrack = (AudioTrack)errorItem;
    }

    public boolean isPlaying() {
      return getPlaylistHandler() != null && getPlaylistHandler().getCurrentMediaPlayer() != null && getPlaylistHandler().getCurrentMediaPlayer().isPlaying();
    }

    @Override
    public boolean onError(Exception e) {
        Log.i(TAG, "onError: " + e.toString());
        if (errorListener.get() != null) {
          errorListener.get().onError(e);
        }
        return true;
    }

    private boolean isShouldStopPlaylist() {
      return shouldStopPlaylist;
    }

    public void setShouldStopPlaylist(boolean shouldStopPlaylist) {
        this.shouldStopPlaylist = shouldStopPlaylist;
    }

    /*
     * isNextAvailable, getCurrentItem, and next() are overridden because there is
     * a glaring bug in playlist core where when an item completes, isNextAvailable and
     * getCurrentItem return wildly contradictory things, resulting in endless repeat
     * of the last item in the playlist.
     */

    @Override
    public boolean isNextAvailable() {
        boolean isAtEnd = getCurrentPosition() + 1 >= getItemCount();
        boolean isConstrained = getCurrentPosition() + 1 >= 0 && getCurrentPosition() + 1 < getItemCount();

        if (isAtEnd) {
            return loop;
        }
        return isConstrained;
    }

    @Override
    public AudioTrack getCurrentItem() {
        boolean isAtEnd = getCurrentPosition() + 1 == getItemCount();
        boolean isConstrained = getCurrentPosition() >= 0 && getCurrentPosition() < getItemCount();

        if (isAtEnd && isShouldStopPlaylist()) {
            return null;
        }
        if (isConstrained) {
            return getItem(getCurrentPosition());
        }
        return null;
    }

    @Override
    public AudioTrack previous() {
        setCurrentPosition(Math.max(0, getCurrentPosition() -1));
        AudioTrack prevItem = getCurrentItem();

        if (!previousInvoked) { // this command came from the notification, not the user
            Log.i(TAG, "PlaylistManager.previous: invoked via service.");
            if (mediaControlsListener.get() != null) {
              mediaControlsListener.get().onPrevious(prevItem, getCurrentPosition());
            }
        }

        previousInvoked = false;
        return prevItem;
    }

    @Override
    public AudioTrack next() {
        if (isNextAvailable()) {
            setCurrentPosition(Math.min(getCurrentPosition() + 1, getItemCount()));
        } else {
            if (loop) {
              setCurrentPosition(BasePlaylistManager.INVALID_POSITION);
            } else {
              setShouldStopPlaylist(true);
              raiseAndCheckOnNext();
              return null;
            }
        }

        raiseAndCheckOnNext();
        return getCurrentItem();
    }

    private void raiseAndCheckOnNext() {
        AudioTrack nextItem = getCurrentItem();
        if (!nextInvoked) { // this command came from the notification, not the user
            Log.i(TAG, "PlaylistManager.next: invoked via service.");
            if (mediaControlsListener.get() != null) {
              mediaControlsListener.get().onNext(nextItem, getCurrentPosition());
            }
        }
        nextInvoked = false;
    }


    /*
     * List management
     */

    public void setAllItems(List<AudioTrack> items, PlaylistItemOptions options) {
      clearItems();
      addAllItems(items);
      setCurrentPosition(0);

      // If the options said to start from a specific position, do so.
      long seekStart = 0;
      if (options.getRetainPosition()) {
        if (options.getPlayFromPosition() > 0) {
          seekStart = options.getPlayFromPosition();
        } else {
          MediaProgress progress = getCurrentProgress();
          if (progress != null) {
            seekStart = progress.getPosition();
          }
        }
      }

      // If the options said to start from a specific id, do so.
      String idStart = null;
      if (options.getRetainPosition()) {
          if (options.getPlayFromId() != null) {
              idStart = options.getPlayFromId();
          }
      }
      if (idStart != null && !"".equals((idStart))) {
          int code = idStart.hashCode();
          setCurrentItem(code);
      }

      // We assume that if the playlist is fully loaded in one go,
      // that the next thing to happen will be to play. So let's start
      // paused, which will allow the player to pre-buffer until the
      // user says Go.
      beginPlayback(seekStart, options.getStartPaused());
    }

    public void addItem(AudioTrack item) {
        if (item == null) { return; }
        AudioTracks.add(item);
        setItems(AudioTracks);
    }

    public void addAllItems(List<AudioTrack> items) {
        AudioTrack currentItem = getCurrentItem(); // may be null
        AudioTracks.addAll(items);
        setItems(AudioTracks); // not *strictly* needed since they share the reference, but for good measure..
        setCurrentPosition(AudioTracks.indexOf(currentItem));
    }

    public AudioTrack removeItem(int index, @Nullable String itemId) {
      boolean wasPlaying = this.isPlaying();
      if (this.getPlaylistHandler() != null) {
          this.getPlaylistHandler().pause(true);
      }
      int currentPosition = getCurrentPosition();
      AudioTrack currentItem = getCurrentItem(); // may be null
      AudioTrack foundItem = null;
      boolean removingCurrent = false;

      // If isPlaying is true, and currentItem is not null,
      // that implies that currentItem is the currently playing item.
      // If removingCurrent gets set to true, we are removing the currently playing item,
      // and we need to restart playback once we do.

      int resolvedIndex = resolveItemPosition(index, itemId);
      if (resolvedIndex >= 0) {
          foundItem = AudioTracks.get(resolvedIndex);
          if (foundItem == currentItem) {
              removingCurrent = true;
          }
          AudioTracks.remove(resolvedIndex);
      }

      setItems(AudioTracks);
      setCurrentPosition(removingCurrent ? currentPosition : AudioTracks.indexOf(currentItem));
      this.beginPlayback(0, !wasPlaying);

      return foundItem;
    }

    public ArrayList<AudioTrack> removeAllItems(ArrayList<TrackRemovalItem> items) {
      ArrayList<AudioTrack> removedTracks = new ArrayList<>();
      boolean wasPlaying = this.isPlaying();
      if (this.getPlaylistHandler() != null) {
          this.getPlaylistHandler().pause(true);
      }
      int currentPosition = getCurrentPosition();
      AudioTrack currentItem = getCurrentItem(); // may be null
      boolean removingCurrent = false;

      for (TrackRemovalItem item : items) {
          int resolvedIndex = resolveItemPosition(item.trackIndex, item.trackId);
          if (resolvedIndex >= 0) {
              AudioTrack foundItem = AudioTracks.get(resolvedIndex);
              if (foundItem == currentItem) {
                  removingCurrent = true;
              }
              removedTracks.add(foundItem);
              AudioTracks.remove(resolvedIndex);
          }
      }

      setItems(AudioTracks);
      setCurrentPosition(removingCurrent ? currentPosition : AudioTracks.indexOf(currentItem));
      this.beginPlayback(0, !wasPlaying);

      return removedTracks;
    }

    public void clearItems() {
        if (this.getPlaylistHandler() != null) {
          this.getPlaylistHandler().stop();
        }
        AudioTracks.clear();
        setItems(AudioTracks);
        setCurrentPosition(BasePlaylistManager.INVALID_POSITION);
    }

    private int resolveItemPosition(int trackIndex, String trackId) {
        int resolvedPosition = -1;
        if (trackIndex >= 0 && trackIndex < AudioTracks.size()) {
            resolvedPosition = trackIndex;
        } else if (trackId != null && !"".equals(trackId)) {
            int itemPos = getPositionForItem(trackId.hashCode());
            if (itemPos != BasePlaylistManager.INVALID_POSITION) {
                resolvedPosition = itemPos;
            }
        }
        return resolvedPosition;
    }

    public boolean getLoop() {
      return loop;
    }

    public void setLoop(boolean newLoop) {
      loop = newLoop;
    }

    public float getVolumeLeft() {
        return volumeLeft;
    }

    public float getVolumeRight() {
        return volumeRight;
    }

    public void setVolume(@FloatRange(from = 0.0, to = 1.0) float left, @FloatRange(from = 0.0, to = 1.0) float right) {
        volumeLeft = left;
        volumeRight = right;

        if (currentMediaPlayer != null && currentMediaPlayer.get() != null) {
            Log.i("PlaylistManager", "setVolume completing with volume = " + left);
            currentMediaPlayer.get().setVolume(volumeLeft, volumeRight);
        }
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public void setPlaybackSpeed(@FloatRange(from = 0.0, to = 1.0) float speed) {
        playbackSpeed = speed;
        if (currentMediaPlayer != null && currentMediaPlayer.get() != null && currentMediaPlayer.get() instanceof AudioApi) {
            Log.i("PlaylistManager", "setPlaybackSpeed completing with speed = " + speed);
            ((AudioApi)currentMediaPlayer.get()).setPlaybackSpeed(playbackSpeed);
        }
    }

    public void beginPlayback(@IntRange(from = 0) long seekPosition, boolean startPaused) {
      super.play(seekPosition, startPaused);
      try {
          setVolume(volumeLeft, volumeRight);
          setPlaybackSpeed(playbackSpeed);
      } catch (Exception e) {
          Log.w(TAG, "beginPlayback: Error setting volume or playback speed: " + e.getMessage());
      }
    }

    // If we wanted to implement a *native* player (like cordova-plugin-exoplayer),
    // we could do that here, following the example set by ExoMedia:
    // https://github.com/brianwernick/ExoMedia/blob/master/demo/src/main/java/com/devbrackets/android/exomediademo/manager/PlaylistManager.java
    // For this plugin's purposes (and in ExoMedia's audio demo) there is no need
    // to present audio controls, because that is done via the local notification and lock screen.

}
