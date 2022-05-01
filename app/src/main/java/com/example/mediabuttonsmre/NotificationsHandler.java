package com.example.mediabuttonsmre;

import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;
import static androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC;
import static androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW;
import static androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Action;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

public class NotificationsHandler {

  public static final int NOTIFICATION_ID = 1; // has to be positive

  private static final String CHANNEL_ID = "channelId";
  private static final String CHANNEL_NAME = "channelName";

  private final Context context;
  private final MediaSessionCompat mediaSession;
  private final NotificationCompat.Builder notificationBuilder;

  private final Action pauseAction;
  private final Action playAction;

  public NotificationsHandler(Context context, MediaSessionCompat mediaSession) {
    this.context = context;
    this.mediaSession = mediaSession;
    playAction =
        new Action(
            R.drawable.exo_icon_play, "play", buildMediaButtonPendingIntent(context, ACTION_PLAY));
    pauseAction =
        new Action(
            R.drawable.exo_icon_pause,
            "pause",
            buildMediaButtonPendingIntent(context, ACTION_PAUSE));

    createNotificationChannel(context);
    MediaDescriptionCompat mediaDescription =
        mediaSession.getController().getMetadata().getDescription();

    notificationBuilder =
        new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(mediaDescription.getIconBitmap())
            .setContentTitle(mediaDescription.getTitle())
            .setContentText(mediaDescription.getSubtitle())
            .setSubText(mediaDescription.getDescription())
            .setDeleteIntent(buildMediaButtonPendingIntent(context, ACTION_STOP))
            .setVisibility(VISIBILITY_PUBLIC)
            .setColor(Color.BLACK) // you need this because of bug
            .setStyle(
                new MediaStyle()
                    .setMediaSession(mediaSession.getSessionToken())
                    .setShowActionsInCompactView(0));
  }

  private static void createNotificationChannel(Context context) {
    NotificationManagerCompat.from(context)
        .createNotificationChannel(
            new NotificationChannelCompat.Builder(CHANNEL_ID, IMPORTANCE_LOW)
                .setName(CHANNEL_NAME)
                .build());
  }

  public Notification createNotification() {
    return notificationBuilder
        .clearActions()
        .addAction(
            mediaSession.getController().getPlaybackState().getState() == STATE_PLAYING
                ? pauseAction
                : playAction)
        .build();
  }

  public void postOrUpdateNotification() {
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, createNotification());
  }
}
