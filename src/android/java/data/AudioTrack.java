package com.rolamix.plugins.audioplayer.data;

import com.rolamix.plugins.audioplayer.manager.PlaylistManager;
import com.devbrackets.android.playlistcore.annotation.SupportedMediaType;
import com.devbrackets.android.playlistcore.api.PlaylistItem;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.*;

public class AudioTrack implements PlaylistItem {

    private final JSONObject config;
    private float bufferPercentFloat = 0f;
    private int bufferPercent = 0;
    private long duration = 0;

    // In the iOS implementation, this returns nil if the data is bad.
    // We don't really have the option in Java; instead we will check the items afterwards
    // and just not add them to the list if they have bad data.
    public AudioTrack(@NonNull JSONObject config) {
        this.config = config;
    }

    public JSONObject toDict() {
      JSONObject info = new JSONObject();
      try {
          info.put("trackId", getTrackId());
          info.put("isStream", getIsStream());
          info.put("assetUrl", getMediaUrl());
          info.put("albumArt", getThumbnailUrl());
          info.put("artist", getArtist());
          info.put("album", getAlbum());
          info.put("title", getTitle());
      } catch (JSONException e) {
          // I can think of no reason this would ever fail
      }
      return info;
    }

    @Override
    public long getId() {
        // This is used by the underlying PlaylistManager to search for items by ID.
        // ListPlaylistManager.getPositionForItem uses item.id when PlaylistManager.setCurrentItem(id)
        // is called, basically finding the index of that ID.
        // Alternatively, simply use PlaylistManager.setCurrentPosition which uses index directly.
        // Probably easier in almost all cases.
        if (getTrackId() == null) { return 0; }
        return getTrackId().hashCode();
    }

    public boolean getIsStream() {
        return this.config.optBoolean("isStream", false);
    }

    @Nullable
    public String getTrackId() {
      String trackId = this.config.optString("trackId");
      if (trackId.equals("")) { return null; }
      return trackId;
    }

    @Override
    public boolean getDownloaded() {
        return false; // Would really like to set this to true once the cache has it...
    }

    @Override
    public String getDownloadedMediaUri() {
        return null; // ... at which point we can return a value here.
    }

    @Override
    @SupportedMediaType
    public int getMediaType() {
        return PlaylistManager.AUDIO;
    }

    @Override
    public String getMediaUrl() {
      return this.config.optString("assetUrl", "");
    }

    @Override
    public String getThumbnailUrl() {
      String albumArt = this.config.optString("albumArt");
      if (albumArt.equals("")) { return null; } // we should have a good default here.
      return albumArt;
    }

    @Override
    public String getArtworkUrl() {
        return getThumbnailUrl();
    }

    @Override
    public String getTitle() {
        return this.config.optString("title");
    }

    @Override
    public String getAlbum() {
        return this.config.optString("album");
    }

    @Override
    public String getArtist() {
        return this.config.optString("artist");
    }

    // Since it seems ExoPlayer resets the buffering value when you seek,
    // we will set a max value we have seen for a particular item and not lower it.
    // We already know that the item is cached anyway, and testing proves it, there is no
    // playback delay the 2nd time around or when seeking back to positions you already played.

    public float getBufferPercentFloat() {
      return bufferPercentFloat;
    }

    public void setBufferPercentFloat(float buff) {
      // There is a bug in MediaProgress where if bufferPercent == 100 it sets bufferPercentFloat
      // to 100 instead of to 1.
      bufferPercentFloat = Math.min(Math.max(bufferPercentFloat, buff), 1f);
    }

    public int getBufferPercent() {
      return bufferPercent;
    }

    public void setBufferPercent(int buff) {
      bufferPercent = Math.max(bufferPercent, buff);
    }

    public long getDuration() {
      return duration;
    }

    public void setDuration(long dur) {
      duration = Math.max(duration, dur);
    }

}
