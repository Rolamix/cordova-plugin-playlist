package com.rolamix.plugins.audioplayer.playlist;

import com.devbrackets.android.exomedia.listener.OnBufferUpdateListener;
import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.listener.OnSeekCompletionListener;
import com.devbrackets.android.playlistcore.api.MediaPlayerApi;
import com.devbrackets.android.playlistcore.listener.MediaStatusListener;
import org.jetbrains.annotations.NotNull;

import com.rolamix.plugins.audioplayer.data.AudioTrack;

public abstract class BaseMediaApi implements MediaPlayerApi<AudioTrack>,
        OnPreparedListener,
        OnCompletionListener,
        OnErrorListener,
        OnSeekCompletionListener,
        OnBufferUpdateListener {

    boolean prepared;
    int bufferPercent;

    private MediaStatusListener<AudioTrack> mediaStatusListener;

    @Override
    public void setMediaStatusListener(@NotNull MediaStatusListener<AudioTrack> listener) {
        mediaStatusListener = listener;
    }

    @Override
    public void onCompletion() {
        if (mediaStatusListener != null) {
            mediaStatusListener.onCompletion(this);
        }
    }

    @Override
    public boolean onError(Exception e) {
        if (mediaStatusListener != null) {
          mediaStatusListener.onError(this);
          return true;
        }
        return false;
    }

    @Override
    public void onPrepared() {
        prepared = true;

        if (mediaStatusListener != null) {
            mediaStatusListener.onPrepared(this);
        }
    }

    @Override
    public void onSeekComplete() {
        if (mediaStatusListener != null) {
            mediaStatusListener.onSeekComplete(this);
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        bufferPercent = percent;

        if (mediaStatusListener != null) {
            mediaStatusListener.onBufferingUpdate(this, percent);
        }
    }
}
