package com.rolamix.plugins.audioplayer.service;

import __PACKAGE_NAME__.MainApplication;
import android.support.annotation.NonNull;
import android.util.Log;

import com.devbrackets.android.playlistcore.api.MediaPlayerApi;
import com.devbrackets.android.playlistcore.components.playlisthandler.PlaylistHandler;
import com.devbrackets.android.playlistcore.service.BasePlaylistService;

import com.rolamix.plugins.audioplayer.data.AudioTrack;
import com.rolamix.plugins.audioplayer.manager.PlaylistManager;
import com.rolamix.plugins.audioplayer.playlist.AudioApi;
import com.rolamix.plugins.audioplayer.playlist.AudioPlaylistHandler;

/**
 * A simple service that extends {@link BasePlaylistService} in order to provide
 * the application specific information required.
 */
public class MediaService extends BasePlaylistService<AudioTrack, PlaylistManager> {

    @Override
    public void onCreate() {
        super.onCreate();
        // Adds the audio player implementation, otherwise there's nothing to play media with
        AudioApi newAudio = new AudioApi(getApplicationContext());
        newAudio.addErrorListener(getPlaylistManager());
        getPlaylistManager().getMediaPlayers().add(newAudio);
        getPlaylistManager().onMediaServiceInit(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Releases and clears all the MediaPlayersMediaImageProvider
        for (MediaPlayerApi<AudioTrack> player : getPlaylistManager().getMediaPlayers()) {
            player.release();
        }

        getPlaylistManager().getMediaPlayers().clear();
    }

    @NonNull
    @Override
    protected PlaylistManager getPlaylistManager() {
        return ((MainApplication)getApplicationContext()).getPlaylistManager();
    }

    @NonNull
    @Override
    public PlaylistHandler<AudioTrack> newPlaylistHandler() {
        MediaImageProvider imageProvider = new MediaImageProvider(getApplicationContext(), new MediaImageProvider.OnImageUpdatedListener() {
            @Override
            public void onImageUpdated() {
                getPlaylistHandler().updateMediaControls();
            }
        });

        AudioPlaylistHandler.Listener<AudioTrack> listener = new AudioPlaylistHandler.Listener<AudioTrack>() {
            @Override
            public void onMediaPlayerChanged(MediaPlayerApi<AudioTrack> oldPlayer, MediaPlayerApi<AudioTrack> newPlayer) {
                getPlaylistManager().onMediaPlayerChanged(newPlayer);
            }

            @Override
            public void onItemSkipped(AudioTrack item) {
                // We don't need to do anything with this right now
                // The PluginManager receives notifications of the current item changes.
            }
        };

        return new AudioPlaylistHandler.Builder<>(
                getApplicationContext(),
                getClass(),
                getPlaylistManager(),
                imageProvider,
                listener
        ).build();
    }
}
