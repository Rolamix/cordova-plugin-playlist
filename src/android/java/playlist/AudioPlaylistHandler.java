package com.rolamix.plugins.audioplayer.playlist;

import com.rolamix.plugins.audioplayer.data.AudioTrack;
import com.rolamix.plugins.audioplayer.manager.PlaylistManager;
import com.rolamix.plugins.audioplayer.notification.PlaylistNotificationProvider;

import android.support.annotation.Nullable;
import android.app.Service;
import android.content.Context;
import android.util.Log;

import com.devbrackets.android.playlistcore.api.PlaylistItem;
import com.devbrackets.android.playlistcore.api.MediaPlayerApi;
import com.devbrackets.android.playlistcore.data.MediaProgress;
import com.devbrackets.android.playlistcore.manager.BasePlaylistManager;
import com.devbrackets.android.playlistcore.components.audiofocus.AudioFocusProvider;
import com.devbrackets.android.playlistcore.components.audiofocus.DefaultAudioFocusProvider;
import com.devbrackets.android.playlistcore.components.image.ImageProvider;
import com.devbrackets.android.playlistcore.components.mediacontrols.DefaultMediaControlsProvider;
import com.devbrackets.android.playlistcore.components.mediacontrols.MediaControlsProvider;
import com.devbrackets.android.playlistcore.components.mediasession.DefaultMediaSessionProvider;
import com.devbrackets.android.playlistcore.components.mediasession.MediaSessionProvider;
import com.devbrackets.android.playlistcore.components.playlisthandler.DefaultPlaylistHandler;


public class AudioPlaylistHandler<I extends PlaylistItem, M extends BasePlaylistManager<I>>
            extends DefaultPlaylistHandler<I, M> {

    private static final String TAG = "AudioPlaylistHandler";
    private boolean didSeekCatchup = false;

    AudioPlaylistHandler(
            Context context,
            Class<? extends Service> serviceClass,
            M playlistManager,
            ImageProvider<I> imageProvider,
            com.devbrackets.android.playlistcore.components.notification.PlaylistNotificationProvider notificationProvider,
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
    public boolean onError(MediaPlayerApi<I> mediaPlayer) {
        ((PlaylistManager)getPlaylistManager()).setCurrentErrorTrack(getCurrentPlaylistItem());
        int currentIndex = getPlaylistManager().getCurrentPosition();
        int currentErrorCount = getSequentialErrors();

        super.onError(mediaPlayer);
        // Do not set startPaused to false if we are at the first item.
        // For all other items, the user MUST have triggered playback;
        // for item 0, they will never have done so at this point (since the tracks
        // are auto-buffered when a list is loaded).
        // This is a bit of a guess. What happens of tracks 2,3,4 are also broken?
        // User has no network? The only way to be *certain* is to capture all user interaction
        // input points and create a global flag "somewhere" that says the user has tried to play.
        // Maintaining that would be a nightmare.
        if (currentIndex > 0 && currentErrorCount <= 3) {
            Log.e(TAG, "ListHandler error: setting startPaused to false");
            setStartPaused(false);
        }

        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayerApi<I> mediaPlayer) {
        Log.i(TAG, "onSeekComplete! " + mediaPlayer.getCurrentPosition());
        getCurrentMediaProgress().update(mediaPlayer.getCurrentPosition(), mediaPlayer.getBufferedPercent(), mediaPlayer.getDuration());
        super.onSeekComplete(mediaPlayer);
        didSeekCatchup = false;
    }

    @Override
    public void onCompletion(MediaPlayerApi<I> mediaPlayer) {
        Log.i("AudioPlaylistHandler", "onCompletion");
        // This is called when a single item completes playback.
        // For now, the superclass does the right thing, but we may need to override.
        super.onCompletion(mediaPlayer);
    }


    @Override
    public void play() {
        if (!didSeekCatchup) {
            I track = getCurrentPlaylistItem();
            // For streams, immediately seek to 0, which for a stream actually means
            // "start at the current location in the stream when you play again"
            // Without this, the stream buffer grows out of control, and worse, playback
            // continues where you paused. Accidentally pause for 12 hours? Yeah, you just
            // blew out the memory on your device (or forced the player to skip)
            if (((PlaylistManager) getPlaylistManager()).getResetStreamOnPause()) {
                if (track instanceof AudioTrack && ((AudioTrack) track).getIsStream()) {
                    performSeek(0, false);
                }
            }
        }

        didSeekCatchup = true;
        setPlayingBeforeSeek(true);
        super.play();
    }


    @Override
    public void pause(boolean temporary) {
        super.pause(temporary);

        if (!didSeekCatchup) {
            I track = getCurrentPlaylistItem();
            // For streams, immediately seek to 0, which for a stream actually means
            // "start at the current location in the stream when you play again"
            // Without this, the stream buffer grows out of control, and worse, playback
            // continues where you paused. Accidentally pause for 12 hours? Yeah, you just
            // blew out the memory on your device (or forced the player to skip)
            if (((PlaylistManager) getPlaylistManager()).getResetStreamOnPause()) {
                if (track instanceof AudioTrack && ((AudioTrack) track).getIsStream()) {
                    performSeek(0, false);
                }
            }
        }
        didSeekCatchup = true;
    }

    public static class Builder<I extends PlaylistItem, M extends BasePlaylistManager<I>> {

        Context context;
        Class<? extends Service> serviceClass;
        M playlistManager;
        ImageProvider<I> imageProvider;

        com.devbrackets.android.playlistcore.components.notification.PlaylistNotificationProvider notificationProvider = null;
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
                notificationProvider != null ? notificationProvider : new PlaylistNotificationProvider(context),
                mediaSessionProvider != null ? mediaSessionProvider : new DefaultMediaSessionProvider(context, serviceClass),
                mediaControlsProvider != null ? mediaControlsProvider : new DefaultMediaControlsProvider(context),
                audioFocusProvider != null ? audioFocusProvider : new DefaultAudioFocusProvider<>(context),
                listener);
        }
    }
}
