package com.rolamix.plugins.audioplayer.playlist;

import com.rolamix.plugins.audioplayer.data.AudioTrack;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.devbrackets.android.exomedia.AudioPlayer;
import com.devbrackets.android.playlistcore.manager.BasePlaylistManager;
import com.devbrackets.android.exomedia.listener.OnErrorListener;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class AudioApi extends BaseMediaApi {
    @NonNull
    private AudioPlayer audioPlayer;

    private ReentrantLock errorListenersLock = new ReentrantLock(true);
    private ArrayList<WeakReference<OnErrorListener>> errorListeners = new ArrayList<>();

    public AudioApi(@NonNull Context context) {
        this.audioPlayer = new AudioPlayer(context.getApplicationContext());

        audioPlayer.setOnErrorListener(this);
        audioPlayer.setOnPreparedListener(this);
        audioPlayer.setOnCompletionListener(this);
        audioPlayer.setOnSeekCompletionListener(this);
        audioPlayer.setOnBufferUpdateListener(this);

        // If you have not included the WAKE_LOCK permission in your project,
        // the following lines have no effect.
        int wakeMode = PowerManager.PARTIAL_WAKE_LOCK; // | PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK;
        audioPlayer.setWakeMode(context, wakeMode);
        audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void addErrorListener(OnErrorListener listener) {
      errorListenersLock.lock();
      errorListeners.add(new WeakReference<>(listener));
      errorListenersLock.unlock();
    }

    @Override
    public boolean onError(Exception e) {
        super.onError(e);

        errorListenersLock.lock();
        for(WeakReference<OnErrorListener> listener : errorListeners) {
            if (listener.get() != null) {
                listener.get().onError(e);
            }
        }
        errorListenersLock.unlock();
        return true;
    }

    @Override
    public boolean isPlaying() {
        return audioPlayer.isPlaying();
    }

    @Override
    public void play() {
        audioPlayer.start();
    }

    @Override
    public void pause() {
        audioPlayer.pause();
    }

    @Override
    public void stop() {
        audioPlayer.stopPlayback();
    }

    @Override
    public void reset() {
        audioPlayer.reset();
    }

    @Override
    public void release() {
        audioPlayer.release();
    }

    @Override
    public void setVolume(@FloatRange(from = 0.0, to = 1.0) float left, @FloatRange(from = 0.0, to = 1.0) float right) {
        audioPlayer.setVolume(left, right);
    }

    @Override
    public void seekTo(@IntRange(from = 0L) long milliseconds) {
        audioPlayer.seekTo((int)milliseconds);
    }

    public void setPlaybackSpeed(@FloatRange(from = 0.0, to = 1.0) float speed) {
        audioPlayer.setPlaybackSpeed(speed);
    }

    @Override
    public boolean getHandlesOwnAudioFocus() {
        return false;
    }

    @Override
    public boolean handlesItem(@NotNull AudioTrack item) {
        return item.getMediaType() == BasePlaylistManager.AUDIO;
    }

    @Override
    public void playItem(@NotNull AudioTrack item) {
        try {
            prepared = false;
            bufferPercent = 0;
            audioPlayer.setDataSource(Uri.parse(item.getDownloaded() ? item.getDownloadedMediaUri() : item.getMediaUrl()));
            audioPlayer.prepareAsync();
        } catch (Exception e) {
            // Purposefully left blank
        }
    }

    @Override
    public long getCurrentPosition() {
        return prepared ? audioPlayer.getCurrentPosition() : 0;
    }

    @Override
    public long getDuration() {
        return prepared ? audioPlayer.getDuration() : 0;
    }

    @Override
    public int getBufferedPercent() {
        return bufferPercent;
    }
}
