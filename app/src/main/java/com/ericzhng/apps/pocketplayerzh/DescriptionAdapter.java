package com.ericzhng.apps.pocketplayerzh;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;


public
class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

    private Context mContext;

    public
    DescriptionAdapter(Context mContext) {
        this.mContext = mContext;
    }


    @Override
    public
    String getCurrentContentTitle(Player player) {
        int window = player.getCurrentWindowIndex();
        return getTitle(window);
    }

    private
    String getTitle(int window) {
        return mContext.getString(R.string.app_name);
    }

    @Nullable
    @Override
    public
    PendingIntent createCurrentContentIntent(Player player) {
        int window = player.getCurrentWindowIndex();
        return createPendingIntent(window);
    }

    private
    PendingIntent createPendingIntent(int window) {

        PendingIntent contentPendingIntent = PendingIntent.getActivity(mContext,0 ,
                new Intent(mContext, PlayerActivity.class), 0);

        return contentPendingIntent;
    }

    @Nullable
    @Override
    public
    String getCurrentContentText(Player player) {
        int window = player.getCurrentWindowIndex();
        return getDescription(window);
    }

    private
    String getDescription(int window) {
        return mContext.getString(R.string.notification_text);
    }

    @Nullable
    @Override
    public
    Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {

        int window = player.getCurrentWindowIndex();

        return getLargeIcon();
    }

    private Bitmap getLargeIcon() {
        Resources res = mContext.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_cancel_black_24px);
        return largeIcon;
    }
}
