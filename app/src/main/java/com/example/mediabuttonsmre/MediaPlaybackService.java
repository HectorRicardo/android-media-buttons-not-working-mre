package com.example.mediabuttonsmre;

import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST;
import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION;
import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_STOPPED;
import static com.example.mediabuttonsmre.NotificationsHandler.NOTIFICATION_ID;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
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

  private final MediaSessionCompat.Callback mediaSessionCallbacks =
      new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
          Log.i("MREAPP", "onPlay " + MediaPlaybackService.this.hashCode());
          super.onPlay();

          boolean previouslyForeground =
              mediaSession.getController().getPlaybackState().getState() == STATE_PLAYING;

          mediaSession.setActive(true);
          mediaSession.setPlaybackState(
              playbackStateBuilder
                  .setState(STATE_PLAYING, 0, 1)
                  .setActions(ACTION_PAUSE | ACTION_PLAY_PAUSE)
                  .build());

          if (previouslyForeground) {
            notificationsHandler.postOrUpdateNotification();
          } else {
            if (!started) {
              ContextCompat.startForegroundService(
                  MediaPlaybackService.this,
                  new Intent(MediaPlaybackService.this, MediaPlaybackService.class));
            }
            startForeground(NOTIFICATION_ID, notificationsHandler.createNotification());
          }
        }

        @Override
        public void onPause() {
          Log.i("MREAPP", "onPause");
          super.onPause();
          mediaSession.setPlaybackState(
              playbackStateBuilder
                  .setState(STATE_PAUSED, 0, 1)
                  .setActions(ACTION_PLAY | ACTION_PLAY_PAUSE)
                  .build());
          notificationsHandler.postOrUpdateNotification();
          stopForeground(false);
        }

        @Override
        public void onStop() {
          Log.i("MREAPP", "onStop");
          super.onStop();
          mediaSession.setActive(false);
          mediaSession.setPlaybackState(
              playbackStateBuilder
                  .setState(STATE_STOPPED, 0, 1)
                  .setActions(ACTION_PLAY | ACTION_PLAY_PAUSE)
                  .build());
          stopSelf();
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
          KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
          Log.i("MREAPP", "OnMediaButtonEvent " + toConcatString(keyEvent));
          return super.onMediaButtonEvent(mediaButtonEvent);
        }
      };

  @Override
  public void onCreate() {
    Log.i("MREAPP", "Creating service " + hashCode());
    super.onCreate();

    // Create a media session
    mediaSession = new MediaSessionCompat(this, "MediaPlaybackService");

    // Set available actions to listen for: PLAY and PLAY_PAUSE
    mediaSession.setPlaybackState(
        playbackStateBuilder.setActions(ACTION_PLAY | ACTION_PLAY_PAUSE).build());

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
    notificationsHandler = new NotificationsHandler(getBaseContext(), mediaSession);

    // Play dummy audio so media button events are received. Super weird android bug.
    hack();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
    Log.i(
        "MREAPP", "Receiving start service command " + hashCode() + " " + toConcatString(keyEvent));

    started = true;
    MediaButtonReceiver.handleIntent(mediaSession, intent);
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public IBinder onBind(Intent intent) {
    Log.i("MREAPP", "Binding to service " + hashCode());
    return super.onBind(intent);
  }

  @Override
  public boolean onUnbind(Intent intent) {
    Log.i("MREAPP", "Unbinding from service " + hashCode());
    return super.onUnbind(intent);
  }

  @Override
  public void onDestroy() {
    Log.i("MREAPP", "Destroying  " + hashCode());

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

  /** Play dummy blank audio so media button events are listened for. Super weird bug. */
  private static void hack() {
    AudioTrack at =
        new AudioTrack(
            AudioManager.STREAM_MUSIC,
            48000,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            AudioTrack.getMinBufferSize(
                48000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT),
            AudioTrack.MODE_STREAM);
    at.play();
    at.stop();
    at.release();
  }

  private static String toConcatString(Object object) {
    return object == null ? null : object.toString();
  }
}
