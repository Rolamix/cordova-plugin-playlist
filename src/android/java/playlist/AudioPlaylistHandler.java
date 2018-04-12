package com.rolamix.plugins.audioplayer.playlist;

import com.rolamix.plugins.audioplayer.data.AudioTrack;

import android.support.annotation.Nullable;
import android.app.Service;
import android.content.Context;
import android.util.Log;

import com.devbrackets.android.playlistcore.api.PlaylistItem;
import com.devbrackets.android.playlistcore.api.MediaPlayerApi;
import com.devbrackets.android.playlistcore.data.MediaProgress;
import com.devbrackets.android.playlistcore.data.PlaybackState;
import com.devbrackets.android.playlistcore.manager.BasePlaylistManager;
import com.devbrackets.android.playlistcore.components.audiofocus.AudioFocusProvider;
import com.devbrackets.android.playlistcore.components.audiofocus.DefaultAudioFocusProvider;
import com.devbrackets.android.playlistcore.components.image.ImageProvider;
import com.devbrackets.android.playlistcore.components.mediacontrols.DefaultMediaControlsProvider;
import com.devbrackets.android.playlistcore.components.mediacontrols.MediaControlsProvider;
import com.devbrackets.android.playlistcore.components.mediasession.DefaultMediaSessionProvider;
import com.devbrackets.android.playlistcore.components.mediasession.MediaSessionProvider;
import com.devbrackets.android.playlistcore.components.notification.DefaultPlaylistNotificationProvider;
import com.devbrackets.android.playlistcore.components.notification.PlaylistNotificationProvider;
import com.devbrackets.android.playlistcore.components.playlisthandler.DefaultPlaylistHandler;


public class AudioPlaylistHandler<I extends PlaylistItem, M extends BasePlaylistManager<I>>
            extends DefaultPlaylistHandler<I, M> {

    AudioPlaylistHandler(
            Context context,
            Class<? extends Service> serviceClass,
            M playlistManager,
            ImageProvider<I> imageProvider,
            PlaylistNotificationProvider notificationProvider,
            MediaSessionProvider mediaSessionProvider,
            MediaControlsProvider mediaControlsProvider,
            AudioFocusProvider<I> audioFocusProvider,
            @Nullable Listener<I> listener
    ) {
        super(context, serviceClass, playlistManager, imageProvider, notificationProvider,
                mediaSessionProvider, mediaControlsProvider, audioFocusProvider, listener);
        // Lmao this entire class exists for the sake of this one line
        // The default value is 30fps (e.g 33ms), which would overwhelm the Cordova webview with messages
        // Ideally we could make this configurable.
        getMediaProgressPoll().setProgressPollDelay(1000);
    }

    @Override
    public void onPrepared(MediaPlayerApi<I> mediaPlayer) {
        super.onPrepared(mediaPlayer);
    }

    @Override
    public void onBufferingUpdate(MediaPlayerApi<I> mediaPlayer, int percent) {
        // super.onBufferingUpdate(mediaPlayer, percent);
        // this super class appears to have a bug too.
        // Makes sure to update listeners of buffer updates even when playback is paused
        MediaProgress progress = getCurrentMediaProgress();
        if (!mediaPlayer.isPlaying() && progress.getBufferPercent() != percent) {
            progress.update(mediaPlayer.getCurrentPosition(), percent, mediaPlayer.getDuration());
            onProgressUpdated(progress);
        }
    }

    @Override
    public void onCompletion(MediaPlayerApi<I> mediaPlayer) {
        Log.i("AudioPlaylistHandler", "onCompletion");
        // This is called when a single item completes playback.
        // For now, the superclass does the right thing, but we may need to override.
        super.onCompletion(mediaPlayer);
    }

    @Override
    public void togglePlayPause() {
        I track = getCurrentPlaylistItem();
        if (isPlaying()) {
            pause(false);

            // For streams, immediately seek to 0, which for a stream actually means
            // "start at the current location in the stream when you play again"
            // Without this, the stream buffer grows out of control, and worse, playback
            // continues where you paused. Accidentally pause for 12 hours? Yeah, you just
            // blew out the memory on your device (or forced the player into an undefined state)
            if (track instanceof AudioTrack && ((AudioTrack) track).getIsStream()) {
                performSeek(0, false);
            }
        } else {
            play();
        }
    }

    public static class Builder<I extends PlaylistItem, M extends BasePlaylistManager<I>> {

        Context context;
        Class<? extends Service> serviceClass;
        M playlistManager;
        ImageProvider<I> imageProvider;

        PlaylistNotificationProvider notificationProvider = null;
        MediaSessionProvider mediaSessionProvider = null;
        MediaControlsProvider mediaControlsProvider = null;
        AudioFocusProvider<I> audioFocusProvider = null;
        Listener<I> listener;

        public Builder(Context context, Class<? extends Service> serviceClass,
                       M playlistManager, ImageProvider<I> imageProvider, Listener<I> listener) {
            this.context = context;
            this.serviceClass = serviceClass;
            this.playlistManager = playlistManager;
            this.imageProvider = imageProvider;
            this.listener = listener;
        }

        public AudioPlaylistHandler<I, M> build() {
            return new AudioPlaylistHandler<>(context,
                serviceClass,
                playlistManager,
                imageProvider,
                notificationProvider != null ? notificationProvider : new DefaultPlaylistNotificationProvider(context),
                mediaSessionProvider != null ? mediaSessionProvider : new DefaultMediaSessionProvider(context, serviceClass),
                mediaControlsProvider != null ? mediaControlsProvider : new DefaultMediaControlsProvider(context),
                audioFocusProvider != null ? audioFocusProvider : new DefaultAudioFocusProvider<>(context),
                listener);
        }
    }
}
