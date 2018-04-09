package com.rolamix.plugins.audioplayer.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.devbrackets.android.playlistcore.components.image.ImageProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Because we are codegen'ing this to depend on the actual cordova app,
// we can use R directly. Otherwise, we'd have to use the cordova activity,
// but that would be a bit odd since this belongs to a service running
// outside that activity. I'm not sure if that would work.
// import __PACKAGE_NAME__.R;
import com.rolamix.plugins.audioplayer.FakeR;
import com.rolamix.plugins.audioplayer.data.AudioTrack;


public class MediaImageProvider implements ImageProvider<AudioTrack> {
    interface OnImageUpdatedListener {
        void onImageUpdated();
    }

    @NotNull
    private FakeR fakeR;

    @NotNull
    private RequestManager glide;
    @NonNull
    private OnImageUpdatedListener listener;

    @NonNull
    private NotificationImageTarget notificationImageTarget = new NotificationImageTarget();
    @NonNull
    private RemoteViewImageTarget remoteViewImageTarget = new RemoteViewImageTarget();

    @NonNull
    private Bitmap defaultNotificationImage;

    @Nullable
    private Bitmap notificationImage;
    @Nullable
    private Bitmap artworkImage;

    public MediaImageProvider(@NonNull Context context, @NonNull OnImageUpdatedListener listener) {
        glide = Glide.with(context.getApplicationContext());
        fakeR = new FakeR(context.getApplicationContext());
        this.listener = listener;

        // R.drawable.img_playlist_notif_default
        // R.drawable.img_playlist_artwork_default
        defaultNotificationImage = BitmapFactory.decodeResource(context.getResources(), fakeR.getId("drawable", "img_playlist_notif_default"));
        defaultArtworkImage = BitmapFactory.decodeResource(context.getResources(), fakeR.getId("drawable", "img_playlist_artwork_default"));
    }

    @Override
    public int getNotificationIconRes() {
        return R.mipmap.icon; // this comes from cordova itself.
    }

    @Override
    public int getRemoteViewIconRes() {
        return R.mipmap.icon;
    }

    @Nullable
    @Override
    public Bitmap getLargeNotificationImage() {
        return notificationImage != null ? notificationImage : defaultNotificationImage;
    }

    @Nullable
    @Override
    public Bitmap getRemoteViewArtwork() {
        return artworkImage != null ? artworkImage : defaultArtworkImage;
    }

    @Override
    public void updateImages(@NotNull AudioTrack playlistItem) {
        glide.asBitmap().load(playlistItem.getThumbnailUrl()).into(notificationImageTarget);
        glide.asBitmap().load(playlistItem.getArtworkUrl()).into(remoteViewImageTarget);
    }

    /**
     * A class used to listen to the loading of the large notification images and perform
     * the correct functionality to update the notification once it is loaded.
     * <p>
     * <b>NOTE:</b> This is a Glide Image loader class
     */
    private class NotificationImageTarget extends SimpleTarget<Bitmap> {
        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            notificationImage = resource;
            listener.onImageUpdated();
        }
    }

    /**
     * A class used to listen to the loading of the large lock screen images and perform
     * the correct functionality to update the artwork once it is loaded.
     * <p>
     * <b>NOTE:</b> This is a Glide Image loader class
     */
    private class RemoteViewImageTarget extends SimpleTarget<Bitmap> {
        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            artworkImage = resource;
            listener.onImageUpdated();
        }
    }
}
