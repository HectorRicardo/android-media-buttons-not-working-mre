package com.example.mediabuttonsmre;

import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST;
import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION;
import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE;
import static com.example.mediabuttonsmre.NotificationsHandler.NOTIFICATION_ID;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

  private final PlaybackStateCompat.Builder playbackStateBuilder =
      new PlaybackStateCompat.Builder();
  private MediaSessionCompat mediaSession;
  private NotificationsHandler notificationsHandler;
  private boolean started = false;
  private boolean foreground = false;

  private final MediaSessionCompat.Callback mediaSessionCallbacks =
      new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
          super.onPlay();

          // Set the session active  (and update metadata and state)
          mediaSession.setActive(true);
          mediaSession.setPlaybackState(
              playbackStateBuilder
                  .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1)
                  .setActions(
                      PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_PLAY_PAUSE)
                  .build());

          if (!started) {
            ContextCompat.startForegroundService(
                MediaPlaybackService.this,
                new Intent(MediaPlaybackService.this, MediaPlaybackService.class));
            startForeground(NOTIFICATION_ID, notificationsHandler.createNotification());
          } else if (!foreground) {
            startForeground(NOTIFICATION_ID, notificationsHandler.createNotification());
          } else {
            notificationsHandler.postOrUpdateNotification();
          }
        }

        @Override
        public void onPause() {
          super.onPause();
          mediaSession.setPlaybackState(
              playbackStateBuilder
                  .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1)
                  .setActions(
                      PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE)
                  .build());
          notificationsHandler.postOrUpdateNotification();
          stopForeground(false);
          foreground = false;
        }

        @Override
        public void onStop() {
          super.onStop();
          mediaSession.setActive(false);
          mediaSession.setPlaybackState(
              playbackStateBuilder
                  .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1)
                  .setActions(
                      PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE)
                  .build());
          stopSelf();
          started = false;
        }
      };

  @Override
  public void onCreate() {
    super.onCreate();

    // Create a media session
    mediaSession = new MediaSessionCompat(this, "MediaPlaybackService");

    // Set available actions to listen for: PLAY and PLAY_PAUSE
    mediaSession.setPlaybackState(
        playbackStateBuilder
            .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE)
            .build());

    // Metadata for session - this won't ever change
    mediaSession.setMetadata(
        new MediaMetadataCompat.Builder()
            .putString(METADATA_KEY_TITLE, "Irrelevant")
            .putString(METADATA_KEY_ARTIST, "irrelevant")
            .putLong(METADATA_KEY_DURATION, 30000)
            .build());

    // Set media session callbacks
    mediaSession.setCallback(mediaSessionCallbacks);

    // Set the media session token
    setSessionToken(mediaSession.getSessionToken());

    // Notifications helper
    notificationsHandler = new NotificationsHandler(this, mediaSession);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    started = true;
    MediaButtonReceiver.handleIntent(mediaSession, intent);
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    // Release media session
    mediaSession.release();
  }

  @Nullable
  @Override
  public BrowserRoot onGetRoot(
      @NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
    return new BrowserRoot("empty", null);
  }

  @Override
  public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaItem>> result) {}
}
