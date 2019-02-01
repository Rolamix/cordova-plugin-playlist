package com.rolamix.plugins.audioplayer.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.devbrackets.android.playlistcore.components.notification.DefaultPlaylistNotificationProvider;

import org.jetbrains.annotations.Nullable;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class PlaylistNotificationProvider extends DefaultPlaylistNotificationProvider {
    public PlaylistNotificationProvider(Context context) {
        super(context);
    }

    @Nullable
    @Override
    protected PendingIntent getClickPendingIntent() {
        Context context = this.getContext();
        String pkgName  = context.getPackageName();

        Intent intent = context
                .getPackageManager()
                .getLaunchIntentForPackage(pkgName);

        intent.addFlags(
                FLAG_ACTIVITY_REORDER_TO_FRONT | FLAG_ACTIVITY_SINGLE_TOP);

        return PendingIntent.getActivity(this.getContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
