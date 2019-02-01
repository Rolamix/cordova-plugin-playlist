package com.rolamix.plugins.audioplayer;

import android.util.Log;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.content.Context;

import org.apache.cordova.CordovaInterface;
import org.json.JSONException;
import org.json.JSONObject;

import __PACKAGE_NAME__.MainApplication;
import com.rolamix.plugins.audioplayer.data.AudioTrack;
import com.rolamix.plugins.audioplayer.manager.PlaylistManager;
import com.rolamix.plugins.audioplayer.manager.MediaControlsListener;

import com.devbrackets.android.playlistcore.data.MediaProgress;
import com.devbrackets.android.playlistcore.data.PlaybackState;
import com.devbrackets.android.playlistcore.data.PlaylistItemChange;
import com.devbrackets.android.playlistcore.listener.PlaylistListener;
import com.devbrackets.android.playlistcore.listener.ProgressListener;
import com.devbrackets.android.playlistcore.listener.PlaybackStatusListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.google.android.exoplayer2.ExoPlaybackException;

/**
*
* The implementation of this player borrows from ExoMedia's demo example
* and utilizes heavily those classes, basically because that is "the" way
* to actually use ExoMedia.
*/
public class RmxAudioPlayer implements PlaybackStatusListener<AudioTrack>,
          PlaylistListener<AudioTrack>, ProgressListener, OnErrorListener, MediaControlsListener {

  public static String TAG = "RmxAudioPlayer";

  // PlaylistCore requires this but we don't use it
  // It would be used to switch between playlists. I guess we could
  // support that in the future, might be cool.
  private static final int PLAYLIST_ID = 32;
  private CordovaInterface cordova;
  private PlaylistManager playlistManager;
  private OnStatusReportListener statusListener;

  private int lastBufferPercent = 0;
  private boolean trackDuration = false;
  private boolean trackLoaded = false;
  private boolean resetStreamOnPause = true;

  public RmxAudioPlayer(@NonNull OnStatusReportListener statusListener, @NonNull CordovaInterface cordova) {
    // AudioPlayerPlugin and RmxAudioPlayer are separate classes in order to increase
    // the portability of this code.
    // Because AudioPlayerPlugin itself holds a strong reference to this class,
    // we can hold a strong reference to this shared callback. Normally not a good idea
    // but these two objects will always live together (And the plugin couldn't function
    // at all if this one gets garbage collected).
    this.cordova = cordova;
    this.statusListener = statusListener;

    getPlaylistManager();
    playlistManager.setId(PLAYLIST_ID);
    playlistManager.setPlaybackStatusListener(this);
    playlistManager.setOnErrorListener(this);
    playlistManager.setMediaControlsListener(this);
  }

  public PlaylistManager getPlaylistManager() {
    Context app = cordova.getActivity().getApplicationContext();
    playlistManager = ((MainApplication)app).getPlaylistManager();
    return playlistManager;
  }

  public boolean getResetStreamOnPause() {
    return resetStreamOnPause;
  }

  public void setResetStreamOnPause(boolean val) {
    resetStreamOnPause = val;
    getPlaylistManager().setResetStreamOnPause(getResetStreamOnPause());
  }

  public float getVolume() {
    return (getVolumeLeft() + getVolumeRight()) / 2f;
  }

  public float getVolumeLeft() {
    return playlistManager.getVolumeLeft();
  }

  public float getVolumeRight() {
    return playlistManager.getVolumeRight();
  }

  public void setVolume(float both) {
    setVolume(both, both);
  }

  public void setVolume(float left, float right) {
    playlistManager.setVolume(left, right);
  }


  @Override
  public void onPrevious(AudioTrack currentItem, int currentIndex) {
    JSONObject param = new JSONObject();
    String trackId = currentItem == null ? "NONE" : currentItem.getTrackId();

    try {
        param.put("currentIndex", currentIndex);
        param.put("currentItem", currentItem != null ? currentItem.toDict() : null);
    } catch (JSONException e) {
        Log.i(TAG, "Error generating onPrevious status message: " + e.toString());
    }

    onStatus(RmxAudioStatusMessage.RMX_STATUS_SKIP_BACK, trackId, param);
  }

  @Override
  public void onNext(AudioTrack currentItem, int currentIndex) {
    JSONObject param = new JSONObject();
    String trackId = currentItem == null ? "NONE" : currentItem.getTrackId();

    try {
        param.put("currentIndex", currentIndex);
        param.put("currentItem", currentItem != null ? currentItem.toDict() : null);
    } catch (JSONException e) {
        Log.i(TAG, "Error generating onNext status message: " + e.toString());
    }
    onStatus(RmxAudioStatusMessage.RMX_STATUS_SKIP_FORWARD, trackId, param);
  }

  @Override
  public boolean onError(Exception e) {
      String errorMsg = e.toString();
      RmxAudioErrorType errorType = RmxAudioErrorType.RMXERR_NONE_SUPPORTED;

      if (e instanceof  ExoPlaybackException) {
          switch (((ExoPlaybackException) e).type) {
              case ExoPlaybackException.TYPE_SOURCE:
                  errorMsg = "ExoPlaybackException.TYPE_SOURCE: " + ((ExoPlaybackException) e).getSourceException().getMessage();
                  break;
              case ExoPlaybackException.TYPE_RENDERER:
                  errorType = RmxAudioErrorType.RMXERR_DECODE;
                  errorMsg = "ExoPlaybackException.TYPE_RENDERER: " + ((ExoPlaybackException) e).getRendererException().getMessage();
                  break;
              case ExoPlaybackException.TYPE_UNEXPECTED:
                  errorType = RmxAudioErrorType.RMXERR_DECODE;
                  errorMsg = "ExoPlaybackException.TYPE_UNEXPECTED: " + ((ExoPlaybackException) e).getUnexpectedException().getMessage();
                  break;
          }
      }

      AudioTrack errorItem = playlistManager.getCurrentErrorTrack();
      String trackId = errorItem != null ? errorItem.getTrackId() : "INVALID";

      Log.i(TAG, "Error playing audio track: [" + trackId + "]: " + errorMsg);
      onError(errorType, trackId, errorMsg);
      playlistManager.setCurrentErrorTrack(null);
      return true;
  }

  @Override
  public void onMediaPlaybackStarted(AudioTrack item, long currentPosition, long duration) {
      Log.i(TAG, "onMediaPlaybackStarted: ==> " + item.getTitle() + ": " + currentPosition + "," + duration);
      // this is the first place that valid duration is seen. Immediately before, we get the PLAYING status change,
      // and before that, it announces PREPARING twice and all values are 0.
      // Problem is, this method is only called if playback is already in progress when the track changes,
      // which is useless in most cases. So, these values are actually handled in onProgressUpdated.
  }

  @Override
  public void onItemPlaybackEnded(AudioTrack item) {
      AudioTrack nextItem = playlistManager.getCurrentItem();
      // String title = item != null ? item.getTitle() : "(null)";
      // String currTitle = nextItem != null ? nextItem.getTitle() : "(null)";
      // String currTrackId = nextItem != null ? nextItem.getTrackId() : null;
      // Log.i(TAG, "onItemPlaybackEnded: ==> " + title + "," + trackId + " ==> next item: " + currTitle + "," + currTrackId);

      if (item != null) {
          String trackId = item.getTrackId();
          JSONObject trackStatus = getPlayerStatus(item);
          onStatus(RmxAudioStatusMessage.RMXSTATUS_COMPLETED, trackId, trackStatus);
      }

      if (nextItem == null) { // if (!playlistManager.isNextAvailable()) {
        onStatus(RmxAudioStatusMessage.RMXSTATUS_PLAYLIST_COMPLETED, "INVALID", null);
      }
  }

  @Override
  public void onPlaylistEnded() {
      Log.i(TAG, "onPlaylistEnded");
      playlistManager.setShouldStopPlaylist(false);
  }

  @Override
  public boolean onPlaylistItemChanged(@Nullable AudioTrack currentItem, boolean hasNext, boolean hasPrevious) {
      JSONObject info = new JSONObject();
      String trackId = currentItem == null ? "NONE" : currentItem.getTrackId();
      try {
          info.put("currentItem", currentItem != null ? currentItem.toDict() : null);
          info.put("currentIndex", playlistManager.getCurrentPosition());
          info.put("isAtEnd", !hasNext);
          info.put("isAtBeginning", !hasPrevious);
          info.put("hasNext", hasNext);
          info.put("hasPrevious", hasPrevious);
      } catch (JSONException e) {
          Log.e(TAG, "Error creating onPlaylistItemChanged message: " + e.toString());
      }

      lastBufferPercent = 0;
      trackDuration = false;
      trackLoaded = false;

      onStatus(RmxAudioStatusMessage.RMXSTATUS_TRACK_CHANGED, trackId, info);
      return true;
  }

  @Override
  public boolean onPlaybackStateChanged(@NonNull PlaybackState playbackState) {
      // in testing, I saw PREPARING, then PLAYING, and buffering happened
      // during PLAYING. Tapping play/pause toggles PLAYING and PAUSED
      // sending a seek command produces SEEKING here
      // RETRIEVING is never sent.

      AudioTrack currentItem = playlistManager.getCurrentItem();
      JSONObject trackStatus = getPlayerStatus(currentItem);
      Log.i("AudioPlayerActiv/opsc", playbackState.toString() + ", " + trackStatus.toString() + ", " + currentItem);

      switch (playbackState) {
          case STOPPED:
              onStatus(RmxAudioStatusMessage.RMXSTATUS_STOPPED, "INVALID", null);
              break;

          case RETRIEVING: // these are all loading states
          case PREPARING: {
            if (currentItem != null && currentItem.getTrackId() != null) {
                  onStatus(RmxAudioStatusMessage.RMXSTATUS_LOADING, currentItem.getTrackId(), trackStatus);
            }
            break;
          }
          case SEEKING:{
            MediaProgress progress = playlistManager.getCurrentProgress();
            if (currentItem != null && currentItem.getTrackId() != null && progress != null) {
                JSONObject info = new JSONObject();
                try {
                    info.put("position", progress.getPosition() / 1000f);
                    onStatus(RmxAudioStatusMessage.RMXSTATUS_SEEK, currentItem.getTrackId(), info);
                } catch (JSONException e) {
                    Log.e(TAG, "Error generating seeking status message: " + e.toString());
                }
            }
            break;
          }
          case PLAYING:
              if (currentItem != null && currentItem.getTrackId() != null) {
                  // Can also check here that duration == 0, because that is what happens on the first PLAYING invokation.
                  // We'll leave this for now.
                  if (!trackLoaded) {
                    onStatus(RmxAudioStatusMessage.RMXSTATUS_CANPLAY, currentItem.getTrackId(), trackStatus);
                    trackLoaded = true;
                  }
                  onStatus(RmxAudioStatusMessage.RMXSTATUS_PLAYING, currentItem.getTrackId(), trackStatus);
              }
              break;
          case PAUSED:
              if (currentItem != null && currentItem.getTrackId() != null) {
                  onStatus(RmxAudioStatusMessage.RMXSTATUS_PAUSE, currentItem.getTrackId(), trackStatus);
              }
              break;
          // we'll handle error in the listener. ExoMedia only raises this in the case of catastrophic player failure.
          case ERROR:
          default:
              break;
      }

      return true;
  }

  @Override
  public boolean onProgressUpdated(@NonNull MediaProgress progress) {
      // Order matters here. We must update the item's duration and buffer before pulling the track status,
      // because those values are adjusted to account for the buffering-reset in ExoPlayer.
      AudioTrack currentItem = playlistManager.getCurrentItem();
      PlaybackState playbackState = playlistManager.getCurrentPlaybackState();

      if (currentItem != null) { // I mean, this call makes no sense otherwise..
        currentItem.setDuration(progress.getDuration());
        currentItem.setBufferPercent(progress.getBufferPercent());
        currentItem.setBufferPercentFloat(progress.getBufferPercentFloat());

        JSONObject trackStatus = getPlayerStatus(currentItem);

        if (progress.getBufferPercent() != lastBufferPercent) {
            if (progress.getBufferPercent() >= 100f) {
                // Unlike iOS this will get raised continuously.
                // Extracting the source event from playlistcore would be really hard.
                // The gate above should do the trick.
                onStatus(RmxAudioStatusMessage.RMXSTATUS_LOADED, currentItem.getTrackId(), trackStatus);
            }

            if (!trackLoaded) {
              onStatus(RmxAudioStatusMessage.RMXSTATUS_CANPLAY, currentItem.getTrackId(), trackStatus);
              trackLoaded = true;
            }

            if (!trackDuration && progress.getDuration() > 0) {
                onStatus(RmxAudioStatusMessage.RMXSTATUS_DURATION, currentItem.getTrackId(), trackStatus);
                trackDuration = true;
            }

            onStatus(RmxAudioStatusMessage.RMXSTATUS_BUFFERING, currentItem.getTrackId(), trackStatus);
            lastBufferPercent = progress.getBufferPercent();
        }

        // dont send on prepare, if null
        if (playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.SEEKING
          || (playbackState == PlaybackState.PREPARING && progress.getDuration() == 0)) {
            onStatus(RmxAudioStatusMessage.RMXSTATUS_PLAYBACK_POSITION, currentItem.getTrackId(), trackStatus);
        }
      }

      return true;
  }

  public JSONObject getPlayerStatus(@Nullable AudioTrack statusItem) {
    // TODO: Make this its own object.
    AudioTrack currentItem = statusItem != null ? statusItem : playlistManager.getCurrentItem();
    PlaybackState playbackState = playlistManager.getCurrentPlaybackState();
    MediaProgress progress = playlistManager.getCurrentProgress();

    String status = "unknown";
    switch (playbackState) {
      case STOPPED: { status = "stopped"; break; }
      case ERROR: { status = "error"; break; }
      case RETRIEVING:
      case SEEKING: // { status = "seeking"; break; } // seeking === loading
      case PREPARING: { status = "loading"; break; }
      case PLAYING: { status = "playing"; break; }
      case PAUSED: { status = "paused"; break; }
      default:
          break;
    }

    String trackId = "";
    boolean isStream = false;
    float bufferPercentFloat = 0;
    int bufferPercent = 0;
    long duration = 0;
    long position = 0;

    // The media players hold onto their current playback position between songs,
    // despite my efforts to reset it. So we will just filter out this state.
    if (progress != null) { // && !status.equals("loading")) {
      position = progress.getPosition();
    }

    // the position and duration vals are in milliseconds.
    if (currentItem != null) {
        isStream = currentItem.getIsStream();
        trackId = currentItem.getTrackId();
        bufferPercentFloat = currentItem.getBufferPercentFloat(); // progress.
        bufferPercent = currentItem.getBufferPercent(); // progress.
        duration = currentItem.getDuration(); // progress.
    }

    JSONObject trackStatus = new JSONObject();
    try {
        trackStatus.put("trackId", trackId);
        trackStatus.put("isStream", isStream);
        trackStatus.put("currentIndex", playlistManager.getCurrentPosition());
        trackStatus.put("status", status);
        trackStatus.put("currentPosition", position / 1000.0);
        trackStatus.put("duration", duration / 1000.0);
        trackStatus.put("playbackPercent", duration > 0 ? (((double)position / duration) * 100.0) : 0);
        trackStatus.put("bufferPercent", bufferPercent);
        trackStatus.put("bufferStart", 0.0);
        trackStatus.put("bufferEnd", (bufferPercentFloat * duration) / 1000.0);
    } catch (JSONException e) {
        Log.e(TAG, "Error generating player status: " + e.toString());
    }

    return trackStatus;
  }

  public void pause() {
    Log.i(TAG, "Pausing, removing event listeners");
    removePlaylistListeners();
  }

  public void resume() {
    Log.i(TAG, "Resumed, wiring up event listeners");
    getPlaylistManager();
    registerPlaylistListeners();
    //Makes sure to retrieve the current playback information
    updateCurrentPlaybackInformation();
  }

  private void updateCurrentPlaybackInformation() {
    PlaylistItemChange<AudioTrack> itemChange = playlistManager.getCurrentItemChange();
    if (itemChange != null) {
        onPlaylistItemChanged(itemChange.getCurrentItem(), itemChange.getHasNext(), itemChange.getHasPrevious());
    }

    PlaybackState currentPlaybackState = playlistManager.getCurrentPlaybackState();
    if (currentPlaybackState != PlaybackState.STOPPED) {
        onPlaybackStateChanged(currentPlaybackState);
    }

    MediaProgress mediaProgress = playlistManager.getCurrentProgress();
    if (mediaProgress != null) {
        onProgressUpdated(mediaProgress);
    }
  }

  private void registerPlaylistListeners() {
      playlistManager.registerPlaylistListener(this);
      playlistManager.registerProgressListener(this);
  }

  private void removePlaylistListeners() {
      playlistManager.unRegisterPlaylistListener(this);
      playlistManager.unRegisterProgressListener(this);
  }

  private void onError(RmxAudioErrorType errorCode, String trackId, String message) {
    statusListener.onError(errorCode, trackId, message);
  }

  private void onStatus(RmxAudioStatusMessage what, String trackId, JSONObject param) {
    statusListener.onStatus(what, trackId, param);
  }

}
