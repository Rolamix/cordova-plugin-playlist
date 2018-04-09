package com.rolamix.plugins.audioplayer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import __PACKAGE_NAME__.MainApplication;
import com.rolamix.plugins.audioplayer.data.AudioTrack;
import com.rolamix.plugins.audioplayer.manager.PlaylistManager;
import com.rolamix.plugins.audioplayer.manager.OnStatusCallback;

import com.devbrackets.android.playlistcore.data.MediaProgress;
import com.devbrackets.android.playlistcore.data.PlaybackState;
import com.devbrackets.android.playlistcore.data.PlaylistItemChange;
import com.devbrackets.android.playlistcore.listener.PlaylistListener;
import com.devbrackets.android.playlistcore.listener.ProgressListener;
import com.devbrackets.android.playlistcore.listener.PlaybackStatusListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;

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
  private OnStatusCallback statusCallback;
  private PlaylistManager playlistManager;
  private OnStatusReportListener statusListener;

  public RmxAudioPlayer(@NonNull OnStatusReportListener statusListener) {
    // AudioPlayerPlugin and RmxAudioPlayer are separate classes in order to increase
    // the portability of this code.
    // Because AudioPlayerPlugin itself holds a strong reference to this class,
    // we can hold a strong reference to this shared callback. Normally not a good idea
    // but these two objects will always live together (And the plugin couldn't function
    // at all if this one gets garbage collected).
    this.statusListener = statusListener;
    getPlaylistManager();
    playlistManager.setId(PLAYLIST_ID);
    playlistManager.setPlaybackStatusListener(this);
    playlistManager.setMediaControlsListener(this);
  }

  public PlaylistManager getPlaylistManager() {
    Context app = cordova.getActivity().getApplicationContext();
    playlistManager = ((MainApplication)app).getPlaylistManager();
    return playlistManager;
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
    param.set("currentIndex", currentIndex);
    param.set("currentItem", currentItem);
    onStatus(RmxAudioStatusMessage.RMX_STATUS_SKIP_BACK, currentItem.getTrackId(), param);
  }

  @Override
  public void onNext(AudioTrack currentItem, int currentIndex) {
    JSONObject param = new JSONObject();
    param.set("currentIndex", currentIndex);
    param.set("currentItem", currentItem);
    onStatus(RmxAudioStatusMessage.RMX_STATUS_SKIP_FORWARD, currentItem.getTrackId(), param);
  }

  @Override
  public boolean onError(Exception e) {
      Log.i(TAG, "Error playing audio track: " + e.toString());
      String errorMsg = e.toString();
      AudioTrack currentItem = playlistManager.getCurrentItem();
      String trackId = currentItem != null ? currentItem.getTrackId() : "INVALID";
      onError(RmxAudioErrorType.RMXERR_DECODE, trackId, errorMsg);
      return true;
  }

  @Override
  public void onMediaPlaybackStarted(AudioTrack item, long currentPosition, long duration) {
      Log.i(TAG, "onMediaPlaybackStarted: ==> " + item.getTitle() + ": " + currentPosition + "," + duration);
      // this is the first place that valid duration is seen. Immediately before, we get the PLAYING status change,
      // and before that, it announces PREPARING twice and all values are 0.

      JSONObject trackStatus = getPlayerStatus();
      onStatus(RmxAudioStatusMessage.RMXSTATUS_CANPLAY, item.getTrackId(), trackStatus);
      onStatus(RmxAudioStatusMessage.RMXSTATUS_DURATION, item.getTrackId(), trackStatus);
  }

  @Override
  public void onItemPlaybackEnded(MediaItem item) {
      String title = item != null ? item.getTitle() : "(null)";
      String trackId = item != null ? item.getTrackId() : null;
      Log.i(TAG, "onItemPlaybackEnded: ==> " + title);

      JSONObject trackStatus = getPlayerStatus();
      onStatus(RmxAudioStatusMessage.RMXSTATUS_COMPLETED, trackId, trackStatus);

      if (!playlistManager.isNextAvailable()) {
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
      info.put("currentItem", currentItem);
      info.put("currentIndex", playlistManager.getCurrentPosition());
      info.put("isAtEnd", !hasNext);
      info.put("isAtBeginning", !hasPrevious);
      info.put("hasNext", hasNext);
      info.put("hasPrevious", hasPrevious);

      onStatus(RmxAudioStatusMessage.RMXSTATUS_TRACK_CHANGED, currentItem.getTrackId(), info);
      return true;
  }

  @Override
  public boolean onPlaybackStateChanged(@NonNull PlaybackState playbackState) {
      Log.i("AudioPlayerActiv/opsc", playbackState.toString());
      // in testing, I saw PREPARING, then PLAYING, and buffering happened
      // during PLAYING. Tapping play/pause toggles PLAYING and PAUSED
      // sending a seek command produces SEEKING here
      // RETRIEVING is never sent.

      JSONObject trackStatus = getPlayerStatus();
      AudioTrack currentItem = playlistManager.getCurrentItem();

      switch (playbackState) {
          case STOPPED:
              onStatus(RmxAudioStatusMessage.RMXSTATUS_STOPPED, "INVALID", null);
              break;

          case RETRIEVING: // these are all loading states
          case PREPARING: {
            onStatus(RmxAudioStatusMessage.RMXSTATUS_LOADING, currentItem.getTrackId(), trackStatus);
            break;
          }
          case SEEKING:{
            MediaProgress progress = playlistManager.getCurrentProgress();
            JSONObject info = new JSONObject();
            info.put("position", progress.getPosition() / 1000f);
            onStatus(RmxAudioStatusMessage.RMXSTATUS_SEEK, currentItem.getTrackId(), info);
            break;
          }
          case PLAYING:
              onStatus(RmxAudioStatusMessage.RMXSTATUS_PLAYING, currentItem.getTrackId(), trackStatus);
              break;
          case PAUSED:
              onStatus(RmxAudioStatusMessage.RMXSTATUS_PAUSE, currentItem.getTrackId(), trackStatus);
              break;
          case ERROR: // we'll handle error in the listener.
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
      if (currentItem != null) { // I mean, this call makes no sense otherwise..
        currentItem.setDuration(progress.getDuration());
        currentItem.setBufferPercent(progress.getBufferPercent());
        currentItem.setBufferPercentFloat(progress.getBufferPercentFloat());

        JSONObject trackStatus = getPlayerStatus();

        if (progress.getBufferPercent() >= 100f) {
          // Unlike iOS this will get raised continuously.
          // Extracting the source event from playlistcore would be really hard.
          onStatus(RmxAudioStatusMessage.RMXSTATUS_LOADED, item.getTrackId(), trackStatus);
        } else {
          onStatus(RmxAudioStatusMessage.RMXSTATUS_BUFFERING, item.getTrackId(), trackStatus);
        }

        onStatus(RmxAudioStatusMessage.RMXSTATUS_PLAYBACK_POSITION, currentItem.getTrackId(), trackStatus);
      }

      return true;
  }

  public JSONObject getPlayerStatus() {
    // Todo: Make this its own object.
    AudioTrack currentItem = playlistManager.getCurrentItem();
    PlaybackState playbackState = playlistManager.getCurrentPlaybackState();
    MediaProgress progress = playlistManager.getCurrentProgress();

    String status = "unknown";
    switch (playbackState) {
      case STOPPED: { status = "stopped"; break; }
      case ERROR: { status = "error"; break; }
      case RETRIEVING:
      case PREPARING: { status = "loading"; break; }
      case SEEKING: { status = "seeking"; break; }
      case PLAYING: { status = "playing"; break; }
      case PAUSED: { status = "paused"; break; }
      default:
          break;
    }

    // the position and duration vals are in milliseconds.
    // buffer percent is a whole number (11 = 11%) and percent float is the fraction (0.11)
    // String info = progress.getPosition() + "," + progress.getDuration() + "," + progress.getBufferPercent() + "," + progress.getBufferPercentFloat();
    // Log.i("AudioPlayerActiv/opu", info);
    float bufferPercentFloat = currentItem.getBufferPercentFloat(); // progress.
    int bufferPercent = currentItem.getBufferPercent(); // progress.
    long duration = currentItem.getDuration(); // progress.

    JSONObject trackStatus = new JSONObject();
    trackStatus.put("trackId", currentItem.getTrackId());
    trackStatus.put("currentIndex", playlistManager.getCurrentPosition());
    trackStatus.put("status", status);
    trackStatus.put("currentPosition", progress.getPosition() / 1000f);
    trackStatus.put("duration", duration / 1000f);
    trackStatus.put("playbackPercent", (progress.getPosition() / duration) * 100.0f);
    trackStatus.put("bufferPercent", bufferPercent);
    trackStatus.put("bufferStart", 0f);
    trackStatus.put("bufferEnd", (bufferPercentFloat * duration) / 1000.0f);

    return trackStatus;
  }

  public void pause() {
    removePlaylistListeners();
  }

  public void resume() {
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
