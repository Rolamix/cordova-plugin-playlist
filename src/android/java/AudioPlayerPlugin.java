package com.rolamix.plugins.audioplayer;

import com.rolamix.plugins.audioplayer.manager.PlaylistManager;
import com.rolamix.plugins.audioplayer.manager.OnStatusCallback;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.Manifest;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
// import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.LOG;

import com.devbrackets.android.playlistcore.listener.PlaylistListener;
import com.devbrackets.android.playlistcore.listener.ProgressListener;

/**
 *
 * The core Cordova interface for the audio player
 * TODO: Move the proxied calls audioPlayerImpl.getPlaylistManager()
 * into the audio player class itself so the plugin doesn't know about
 * the playlist manager.
 *
 */
public class AudioPlayerPlugin extends CordovaPlugin implements RmxConstants, OnStatusReportListener {

  public static String TAG = "RmxAudioPlayer";

  // PlaylistCore requires this but we don't use it
  // It would be used to switch between playlists. I guess we could
  // support that in the future, might be cool.
  private static final int PLAYLIST_ID = 32;
  private OnStatusCallback statusCallback;
  private RmxAudioPlayer audioPlayerImpl;

  @Override
  public void pluginInitialize() {
    audioPlayerImpl = new RmxAudioPlayer(this);
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    // capture callback
    if (STORE_CHANNEL.equals(action)) {
      statusCallback = new OnStatusCallback(callbackContext);
      onStatus(RmxAudioStatusMessage.RMXSTATUS_REGISTER, "INIT", null);
      return true;
    }
    if (INITIALIZE.equals(action)) {
      JSONObject options = args.optJSONObject(0);
      // We don't do anything with these yet.
      new PluginCallback(callbackContext).send(PluginResult.Status.OK, options);
      return true;
    }
    if (RELEASE.equals(action)) {
      destroyResources();
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    // Playlist management
    if (SET_PLAYLIST_ITEMS.equals(action)) {
      JSONArray items = args.optJSONArray(0);
      ArrayList<AudioTrack> trackItems = getTrackItems(items);
      audioPlayerImpl.getPlaylistManager().setAllItems(trackItems);

      for (AudioTrack playerItem : trackItems) {
        if (track.getTrackId() != null) {
          onStatus(RmxAudioStatusMessage.RMXSTATUS_ITEM_ADDED, playerItem.getTrackId(), playerItem.toDict());
        }
      }

      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (ADD_PLAYLIST_ITEM.equals(action)) {
      JSONObject item = args.optJSONObject(0);
      AudioTrack track = getTrackItem(item);
      audioPlayerImpl.getPlaylistManager().addItem(track);

      if (track.getTrackId() != null) {
        onStatus(RmxAudioStatusMessage.RMXSTATUS_ITEM_ADDED, track.getTrackId(), playerItem.toDict());
      }
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (ADD_PLAYLIST_ITEMS.equals(action)) {
      JSONArray items = args.optJSONArray(0);
      ArrayList<AudioTrack> trackItems = getTrackItems(items);
      audioPlayerImpl.getPlaylistManager().addAllItems(trackItems);

      for (AudioTrack playerItem : trackItems) {
        if (track.getTrackId() != null) {
          onStatus(RmxAudioStatusMessage.RMXSTATUS_ITEM_ADDED, playerItem.getTrackId(), playerItem.toDict());
        }
      }

      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (REMOVE_PLAYLIST_ITEM.equals(action)) {
      int trackIndex = args.optInt(0, -1);
      String trackId = args.optString(1, "");
      boolean success = audioPlayerImpl.getPlaylistManager().removeItem(trackIndex, trackId);

      if (track.getTrackId() != null && success) {
        onStatus(RmxAudioStatusMessage.RMXSTATUS_ITEM_REMOVED, track.getTrackId(), playerItem.toDict());
      }

      PluginResult.Status status = success ? PluginResult.Status.OK : PluginResult.Status.ERROR;
      PluginResult result = new PluginResult(status, success);
      new PluginCallback(callbackContext).send(result);
      return true;
    }

    if (REMOVE_PLAYLIST_ITEMS.equals(action)) {
      JSONArray items = args.optJSONArray(0);
      int removed = 0;

      if (items != null) {
        for (int index = 0; index < items.length(); index++) {
          JSONObject entry = items.optJSONObject(index);
          if (entry == null) { continue; }
          int trackIndex = entry.optInt("trackIndex", -1);
          String trackId = entry.optString("trackId", "");
          if (audioPlayerImpl.getPlaylistManager().removeItem(trackIndex, trackId)) {
            if (track.getTrackId() != null) {
              onStatus(RmxAudioStatusMessage.RMXSTATUS_ITEM_REMOVED, track.getTrackId(), playerItem.toDict());
            }
            removed++;
          }
        }
      }

      PluginResult result = new PluginResult(PluginResult.Status.OK, removed);
      new PluginCallback(callbackContext).send(result);
      return true;
    }

    if (CLEAR_PLAYLIST_ITEMS.equals(action)) {
      audioPlayerImpl.getPlaylistManager().clearItems();

      onStatus(RmxAudioStatusMessage.RMXSTATUS_PLAYLIST_CLEARED, "INVALID", null);
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }


    // Playback
    if (PLAY.equals(action)) {
      // Hmmm.
      // audioPlayerImpl.getPlaylistManager().getPlaylistHandler().play();
      // audioPlayerImpl.getPlaylistManager().invokePausePlay();
      MediaProgress progress =  audioPlayerImpl.getPlaylistManager().getCurrentProgress();
      // This may not do the right thing, we may need to use 0, not getPosition
      audioPlayerImpl.getPlaylistManager().beginPlayback(progress.getPosition(), false);
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (PLAY_BY_INDEX.equals(action)) {
      int index = args.optInt(0, audioPlayerImpl.getPlaylistManager().getCurrentPosition());
      audioPlayerImpl.getPlaylistManager().setCurrentPosition(index);
      audioPlayerImpl.getPlaylistManager().beginPlayback(0, false);
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (PLAY_BY_ID.equals(action)) {
      String trackId = args.optString(0);
      if (trackId != "") {
        // alternatively we could search for the item and set the current index to that item.
        int code = trackId.hashCode();
        audioPlayerImpl.getPlaylistManager().setCurrentItem(code);
        audioPlayerImpl.getPlaylistManager().beginPlayback(0, false);
      }
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (PAUSE.equals(action)) {
      // Hmmm.
      // audioPlayerImpl.getPlaylistManager().getPlaylistHandler().pause();
      audioPlayerImpl.getPlaylistManager().invokePausePlay();
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (SKIP_FORWARD.equals(action)) {
      audioPlayerImpl.getPlaylistManager().invokeNext();
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (SKIP_BACK.equals(action)) {
      audioPlayerImpl.getPlaylistManager().invokePrevious();
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    // On Android, the duration, playback position, etc are in milliseconds as whole numbers.
    // On iOS, it uses seconds as floats, e.g. 63.3 seconds. So we need to convert here.

    if (SEEK.equals(action)) {
      MediaProgress progress =  audioPlayerImpl.getPlaylistManager().getCurrentProgress();
      float currPos = progress.getPosition() / 1000f;
      double positionD = args.optDouble(0, currPos) * 1000.0;
      int position = positionD.intValue();

      audioPlayerImpl.getPlaylistManager().invokeSeekStarted();
      audioPlayerImpl.getPlaylistManager().invokeSeekEnded(position);
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (SEEK_TO_QUEUE_POSITION.equals(action)) {
      // Not supported at the moment
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (SET_PLAYBACK_RATE.equals(action)) {
      float speed = args.optDouble(0, audioPlayerImpl.getPlaylistManager().getPlaybackSpeed());
      audioPlayerImpl.getPlaylistManager().setPlaybackSpeed(speed);
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (SET_PLAYBACK_VOLUME.equals(action)) {
      float volume = args.optDouble(0, audioPlayerImpl.getVolume());
      audioPlayerImpl.setVolume(volume);
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (SET_LOOP_ALL.equals(action)) {
      boolean loop = args.optBoolean(0, audioPlayerImpl.getPlaylistManager().getLoop());
      audioPlayerImpl.getPlaylistManager().setLoop(loop);
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }



    // Getters
    if (GET_PLAYBACK_RATE.equals(action)) {
      float speed = audioPlayerImpl.getPlaylistManager().getPlaybackSpeed();
      PluginResult result = new PluginResult(PluginResult.Status.OK, speed);
      new PluginCallback(callbackContext).send(result);
      return true;
    }

    if (GET_PLAYBACK_VOLUME.equals(action)) {
      PluginResult result = new PluginResult(PluginResult.Status.OK, audioPlayerImpl.getVolume());
      new PluginCallback(callbackContext).send(result);
      return true;
    }

    if (GET_PLAYBACK_POSITION.equals(action)) {
      MediaProgress progress =  audioPlayerImpl.getPlaylistManager().getCurrentProgress();
      PluginResult result = new PluginResult(PluginResult.Status.OK, progress.getPosition() / 1000.0f);
      new PluginCallback(callbackContext).send(result);
      return true;
    }

    if (GET_BUFFER_STATUS.equals(action)) {
      PluginResult result = new PluginResult(PluginResult.Status.OK, audioPlayerImpl.getPlayerStatus());
      new PluginCallback(callbackContext).send(result);
      return true;
    }

    if (GET_TOTAL_DURATION.equals(action)) {
      // Not yet implemented on android. I'm not sure how to, since the tracks haven't loaded yet.
      // On iOS, the AVQueuePlayer gets the metadata for all tracks immediately, that's why that works there.
      float totalDuration = 0f;
      PluginResult result = new PluginResult(PluginResult.Status.OK, totalDuration);
      new PluginCallback(callbackContext).send(result);
      return true;
    }

    if (GET_QUEUE_POSITION.equals(action)) {
      // Not yet implemented on android. I'm not sure how to, since the tracks haven't loaded yet.
      // On iOS, the AVQueuePlayer gets the metadata for all tracks immediately, that's why that works there.
      float queuePosition = 0f;
      PluginResult result = new PluginResult(PluginResult.Status.OK, queuePosition);
      new PluginCallback(callbackContext).send(result);
      return true;
    }

    return false;
  }

  private AudioTrack getTrackItem(JSONObject item) {
    if (item != null) {
      AudioTrack track = new AudioTrack(item);
      if (track.getTrackId() != null) {
        return track;
      }
      return null;
    }
    return null;
  }

  private List<AudioTrack> getTrackItems(JSONArray items) {
    ArrayList<AudioTrack> trackItems = new ArrayList();
    if (items != null && items.length() > 0) {
      for(int index = 0; index < items.length(); index++) {
        JSONObject obj = items.optJSONObject(index);
        AudioTrack track = getTrackItem(obj);
        if (track == null) { continue; }
        trackItems.add(track);
      }
    }
    return trackItems;
  }

  @Override
  public void onPause(boolean multitasking) {
      super.onPause(multitasking);
      audioPlayerImpl.pause();
  }

  @Override
  public void onResume(boolean multitasking) {
      super.onResume(multitasking);
      audioPlayerImpl.resume();
  }

  @Override
  public void onReset() {
    super.onReset();
    destroyResources();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    destroyResources();
  }

  private void destroyResources() {
    audioPlayerImpl.pause();
    playlistManager.clearItems();
  }

  public void onError(RmxAudioErrorType errorCode, String trackId, String message) {
    if (!statusCallback) { return; }
    JSONObject errorObj = OnStatusCallback.createErrorWithCode(errorCode, message);
    onStatus(RmxAudioStatusMessage.RMXSTATUS_ERROR, trackId, errorObj);
  }

  public void onStatus(RmxAudioStatusMessage what, String trackId, JSONObject param) {
    if (!statusCallback) { return; }
    statusCallback.onStatus(what, trackId, param);
  }
}
