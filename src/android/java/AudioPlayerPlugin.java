package com.rolamix.plugins.audioplayer;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import com.devbrackets.android.playlistcore.data.MediaProgress;
import com.rolamix.plugins.audioplayer.data.AudioTrack;

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
  private OnStatusCallback statusCallback;
  private RmxAudioPlayer audioPlayerImpl;

  private boolean resetStreamOnPause = true;

  @Override
  public void pluginInitialize() {
    audioPlayerImpl = new RmxAudioPlayer(this, cordova);
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    Log.i(TAG, "execute: " + action + ": ===> " + args.toString());

    // capture callback
    if (INITIALIZE.equals(action)) {
      if (statusCallback == null) {
        statusCallback = new OnStatusCallback(callbackContext);
        onStatus(RmxAudioStatusMessage.RMXSTATUS_REGISTER, "INIT", null);
      }
      return true;
    }
    if (SET_OPTIONS.equals(action)) {
      JSONObject options = args.optJSONObject(0);
      if (options == null) {
        options = new JSONObject();
      }
      resetStreamOnPause = options.optBoolean("resetStreamOnPause", this.resetStreamOnPause);
      audioPlayerImpl.setResetStreamOnPause(resetStreamOnPause);
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
      JSONObject optionsArgs = args.optJSONObject(1);
      PlaylistItemOptions options = new PlaylistItemOptions(optionsArgs);

      cordova.getThreadPool().execute(new Runnable() {
          @Override
          public void run() {
              ArrayList<AudioTrack> trackItems = getTrackItems(items);
              audioPlayerImpl.getPlaylistManager().setAllItems(trackItems, options);

              for (AudioTrack playerItem : trackItems) {
                  if (playerItem.getTrackId() != null) {
                      onStatus(RmxAudioStatusMessage.RMXSTATUS_ITEM_ADDED, playerItem.getTrackId(), playerItem.toDict());
                  }
              }

              new PluginCallback(callbackContext).send(PluginResult.Status.OK);
          }
      });
      return true;
    }

    if (ADD_PLAYLIST_ITEM.equals(action)) {
      JSONObject item = args.optJSONObject(0);
      AudioTrack playerItem = getTrackItem(item);
      audioPlayerImpl.getPlaylistManager().addItem(playerItem); // makes its own check for null

      if (playerItem.getTrackId() != null) {
        onStatus(RmxAudioStatusMessage.RMXSTATUS_ITEM_ADDED, playerItem.getTrackId(), playerItem.toDict());
      }
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (ADD_PLAYLIST_ITEMS.equals(action)) {
      JSONArray items = args.optJSONArray(0);
      ArrayList<AudioTrack> trackItems = getTrackItems(items);
      audioPlayerImpl.getPlaylistManager().addAllItems(trackItems);

      for (AudioTrack playerItem : trackItems) {
        if (playerItem.getTrackId() != null) {
          onStatus(RmxAudioStatusMessage.RMXSTATUS_ITEM_ADDED, playerItem.getTrackId(), playerItem.toDict());
        }
      }

      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (REMOVE_PLAYLIST_ITEM.equals(action)) {
      int trackIndex = args.optInt(0, -1);
      String trackId = args.optString(1, "");
      AudioTrack item = audioPlayerImpl.getPlaylistManager().removeItem(trackIndex, trackId);

      if (item != null) {
        onStatus(RmxAudioStatusMessage.RMXSTATUS_ITEM_REMOVED, item.getTrackId(), item.toDict());
      }

      PluginResult.Status status = item != null ? PluginResult.Status.OK : PluginResult.Status.ERROR;
      PluginResult result = new PluginResult(status, item != null);
      new PluginCallback(callbackContext).send(result);
      return true;
    }

    if (REMOVE_PLAYLIST_ITEMS.equals(action)) {
      JSONArray items = args.optJSONArray(0);
      int removed = 0;

      if (items != null) {
        ArrayList<TrackRemovalItem> removals = new ArrayList<>();
        for (int index = 0; index < items.length(); index++) {
          JSONObject entry = items.optJSONObject(index);
          if (entry == null) {
            continue;
          }
          int trackIndex = entry.optInt("trackIndex", -1);
          String trackId = entry.optString("trackId", "");
          removals.add(new TrackRemovalItem(trackIndex, trackId));

          ArrayList<AudioTrack> removedTracks = audioPlayerImpl.getPlaylistManager().removeAllItems(removals);

          if (removedTracks.size() > 0) {
            for (AudioTrack removedItem : removedTracks) {
              onStatus(RmxAudioStatusMessage.RMXSTATUS_ITEM_REMOVED, removedItem.getTrackId(), removedItem.toDict());
            }
            removed = removedTracks.size();
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
      if (audioPlayerImpl.getPlaylistManager().getPlaylistHandler() != null) {
          boolean isPlaying = audioPlayerImpl.getPlaylistManager().getPlaylistHandler().getCurrentMediaPlayer() != null
                  && audioPlayerImpl.getPlaylistManager().getPlaylistHandler().getCurrentMediaPlayer().isPlaying();
          // There's a bug in the threaded repeater that it stacks up the repeat calls instead of ignoring
          // additional ones or starting a new one. E.g. every time this is called, you'd get a new repeat cycle,
          // meaning you get N updates per second. Ew.
          if (!isPlaying) {
            audioPlayerImpl.getPlaylistManager().getPlaylistHandler().play();
            //audioPlayerImpl.getPlaylistManager().getPlaylistHandler().seek(position);
          }
      }
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (PLAY_BY_INDEX.equals(action)) {
      int index = args.optInt(0, audioPlayerImpl.getPlaylistManager().getCurrentPosition());
      long seekPosition = (long)(args.optLong(1, 0) * 1000.0);

      audioPlayerImpl.getPlaylistManager().setCurrentPosition(index);
      audioPlayerImpl.getPlaylistManager().beginPlayback(seekPosition, false);
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (PLAY_BY_ID.equals(action)) {
      String trackId = args.optString(0);
      if (!"".equals((trackId))) {
        // alternatively we could search for the item and set the current index to that item.
        int code = trackId.hashCode();
        long seekPosition = (long)(args.optLong(1, 0) * 1000.0);
        audioPlayerImpl.getPlaylistManager().setCurrentItem(code);
        audioPlayerImpl.getPlaylistManager().beginPlayback(seekPosition, false);
      }
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (PAUSE.equals(action)) {
      // Hmmm.
      // audioPlayerImpl.getPlaylistManager().invokePausePlay();
      if (audioPlayerImpl.getPlaylistManager().getPlaylistHandler() != null) {
          audioPlayerImpl.getPlaylistManager().getPlaylistHandler().pause(true);
      }
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
      long position = 0;
      MediaProgress progress = audioPlayerImpl.getPlaylistManager().getCurrentProgress();
      if (progress != null) {
        position = progress.getPosition();
      }
      long positionVal = (long)(args.optDouble(0, position / 1000.0f) * 1000.0);

      if (audioPlayerImpl.getPlaylistManager().getPlaylistHandler() != null) { // isPlaying &&
          boolean isPlaying = audioPlayerImpl.getPlaylistManager().getPlaylistHandler().getCurrentMediaPlayer().isPlaying();
          audioPlayerImpl.getPlaylistManager().getPlaylistHandler().seek(positionVal);
          if (!isPlaying) {
              audioPlayerImpl.getPlaylistManager().getPlaylistHandler().pause(false);
          }
      }
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (SEEK_TO_QUEUE_POSITION.equals(action)) {
      // Not supported at the moment
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (SET_PLAYBACK_RATE.equals(action)) {
      float speed = (float) args.optDouble(0, audioPlayerImpl.getPlaylistManager().getPlaybackSpeed());
      audioPlayerImpl.getPlaylistManager().setPlaybackSpeed(speed);
      new PluginCallback(callbackContext).send(PluginResult.Status.OK);
      return true;
    }

    if (SET_PLAYBACK_VOLUME.equals(action)) {
      float volume = (float) args.optDouble(0, audioPlayerImpl.getVolume());
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
      long position = 0;
      MediaProgress progress = audioPlayerImpl.getPlaylistManager().getCurrentProgress();
      if (progress != null) {
        position = progress.getPosition();
      }
      PluginResult result = new PluginResult(PluginResult.Status.OK, position / 1000.0f);
      new PluginCallback(callbackContext).send(result);
      return true;
    }

    if (GET_BUFFER_STATUS.equals(action)) {
      PluginResult result = new PluginResult(PluginResult.Status.OK, audioPlayerImpl.getPlayerStatus(null));
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

  private ArrayList<AudioTrack> getTrackItems(JSONArray items) {
    ArrayList<AudioTrack> trackItems = new ArrayList<>();
    if (items != null && items.length() > 0) {
      for (int index = 0; index < items.length(); index++) {
        JSONObject obj = items.optJSONObject(index);
        AudioTrack track = getTrackItem(obj);
        if (track == null) {
          continue;
        }
        trackItems.add(track);
      }
    }
    return trackItems;
  }

  @Override
  public void onPause(boolean multitasking) {
    super.onPause(multitasking);
    Log.d(TAG, "Plugin paused");
    audioPlayerImpl.pause();
  }

  @Override
  public void onResume(boolean multitasking) {
    super.onResume(multitasking);
    Log.d(TAG, "Plugin resumed");
    audioPlayerImpl.resume();
  }

  @Override
  public void onReset() {
    super.onReset();
    Log.d(TAG, "Plugin reset");
    destroyResources();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    destroyResources();
  }

  private void destroyResources() {
    statusCallback = null;
    audioPlayerImpl.getPlaylistManager().clearItems();
  }

  public void onError(RmxAudioErrorType errorCode, String trackId, String message) {
    if (statusCallback == null) {
      return;
    }
    JSONObject errorObj = OnStatusCallback.createErrorWithCode(errorCode, message);
    onStatus(RmxAudioStatusMessage.RMXSTATUS_ERROR, trackId, errorObj);
  }

  public void onStatus(RmxAudioStatusMessage what, String trackId, JSONObject param) {
    if (statusCallback == null) {
      return;
    }
    statusCallback.onStatus(what, trackId, param);
  }
}
