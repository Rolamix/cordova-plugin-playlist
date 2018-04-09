package com.rolamix.plugins.audioplayer.manager;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ArrayList;
import android.app.Application;
import android.util.Log;
import android.support.annotation.NonNull;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;

import com.devbrackets.android.playlistcore.manager.ListPlaylistManager;
import com.devbrackets.android.playlistcore.manager.BasePlaylistManager;
//import com.devbrackets.android.playlistcore.components.playlisthandler.PlaylistHandler;
import com.devbrackets.android.playlistcore.api.MediaPlayerApi;
import com.devbrackets.android.playlistcore.listener.PlaybackStatusListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;

import com.rolamix.plugins.audioplayer.data.AudioTrack;
import com.rolamix.plugins.audioplayer.playlist.AudioApi;
import com.rolamix.plugins.audioplayer.service.MediaService;

/**
 * A PlaylistManager that extends the {@link ListPlaylistManager} for use with the
 * {@link MediaService} which extends {@link com.devbrackets.android.playlistcore.service.BasePlaylistService}.
 */
public class PlaylistManager extends ListPlaylistManager<AudioTrack> implements OnErrorListener {

    private static final String TAG = "PlaylistManager";
    private List<AudioTrack> mediaItems = new ArrayList<>();

    private boolean mediaServiceStarted = false;
    private float volumeLeft = 1.0f;
    private float volumeRight = 1.0f;
    private float playbackSpeed = 1.0f;
    private boolean loop = false;
    private boolean shouldStopPlaylist = false;

    private WeakReference<MediaControlsListener> mediaControlsListener = new WeakReference<>(null);
    private WeakReference<OnErrorListener> errorListener = new WeakReference<>(null);
    private WeakReference<MediaPlayerApi<AudioTrack>> currentMediaPlayer = new WeakReference<>(null);

    public PlaylistManager(Application application) {
        super(application, MediaService.class);
        this.setParameters(mediaItems, -1);
    }

    public void onMediaServiceInit(boolean hasInit) {
        mediaServiceStarted = hasInit; // this now implies that this.getPlaylistHandler() is not null.
    }

    public void onMediaPlayerChanged(MediaPlayerApi<AudioTrack> currentMediaPlayer) {
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

    @Override
    public boolean onError(Exception e) {
        Log.i(TAG, "onError: " + e.toString());
        if (errorListener.get() != null) {
          errorListener.get().onError(e);
        }
        return true;
    }

    public boolean isShouldStopPlaylist() {
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
    public MediaItem getCurrentItem() {
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
    public MediaItem previous() {
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
    public MediaItem next() {
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
              mediaControlsListener.get().onNext(prevItem, getCurrentPosition());
            }
        }
        nextInvoked = false;
    }


    /*
     * List management
     */

    public void setAllItems(List<AudioTrack> items) {
      clearItems();
      addAllItems(items);
      setCurrentPosition(0);
      // We assume that if the playlist is fully loaded in one go,
      // that the next thing to happen will be to play. So let's start
      // paused, which will allow the player to pre-buffer until the
      // user says Go.
      beginPlayback(0, true);
    }

    public void addItem(AudioTrack item) {
        if (item == null) { return; }
        mediaItems.add(item);
        setItems(mediaItems);
    }

    public void addAllItems(List<AudioTrack> items) {
        AudioTrack currentItem = getCurrentItem(); // may be null
        mediaItems.addAll(items);
        setItems(mediaItems); // not *strictly* needed since they share the reference, but for good measure..
        setCurrentPosition(mediaItems.indexOf(currentItem));
    }

    public boolean removeItem(int index, @Nullable String itemId) {
        AudioTrack currentItem = getCurrentItem(); // may be null
        boolean removed = false;

        if (index >= 0 && index < mediaItems.size()) {
            mediaItems.remove(index);
            removed = true;
        } else if (itemId != null && !"".equals(itemId)) {
            int itemPos = getPositionForItem(itemId.hashCode());
            if (itemPos != BasePlaylistManager.INVALID_POSITION) {
                mediaItems.remove(itemPos);
                removed = true;
            }
        }

        setItems(mediaItems);
        setCurrentPosition(mediaItems.indexOf(currentItem));
        return removed;
    }

    public void clearItems() {
        mediaItems.clear();
        setItems(mediaItems);
        setCurrentPosition(BasePlaylistManager.INVALID_POSITION);
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

        if (currentMediaPlayer.get() != null) {
            Log.i("PlaylistManager", "setVolume completing with volume = " + left);
            currentMediaPlayer.get().setVolume(volumeLeft, volumeRight);
        }
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public void setPlaybackSpeed(@FloatRange(from = 0.0, to = 1.0) float speed) {
        playbackSpeed = speed;
        if (currentMediaPlayer.get() != null && currentMediaPlayer.get() instanceof AudioApi) {
            Log.i("PlaylistManager", "setPlaybackSpeed completing with speed = " + speed);
            ((AudioApi)currentMediaPlayer.get()).setPlaybackSpeed(playbackSpeed);
        }
    }

    public void beginPlayback(@IntRange(from = 0) long seekPosition, boolean startPaused) {
      super.play(seekPosition, startPaused);
      setVolume(volumeLeft, volumeRight);
      setPlaybackSpeed(playbackSpeed);
    }

    // If we wanted to implement a *native* player (like cordova-plugin-exoplayer),
    // we could do that here, following the example set by ExoMedia:
    // https://github.com/brianwernick/ExoMedia/blob/master/demo/src/main/java/com/devbrackets/android/exomediademo/manager/PlaylistManager.java
    // For this plugin's purposes (and in ExoMedia's audio demo) there is no need
    // to present audio controls, because that is done via the local notification and lock screen.

}
