package com.example.mediabuttonsmre;

import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;

import android.content.ComponentName;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaControllerCompat.TransportControls;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  private MediaControllerCompat mediaController;
  private MediaBrowserCompat mediaBrowser;
  private Button playPauseButton;

  private final MediaControllerCompat.Callback mediaControllerCallback =
      new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
          updateButton();
        }
      };

  private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
      new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
          // Create a MediaControllerCompat and save it
          mediaController =
              new MediaControllerCompat(MainActivity.this, mediaBrowser.getSessionToken());
          MediaControllerCompat.setMediaController(MainActivity.this, mediaController);

          // Register a Callback to stay in sync
          mediaController.registerCallback(mediaControllerCallback);

          // Update UI
          updateButton();

        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    playPauseButton = findViewById(R.id.playPauseButton);

    mediaBrowser =
        new MediaBrowserCompat(
            this, new ComponentName(this, MediaPlaybackService.class), connectionCallbacks, null);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mediaBrowser.connect();
  }

  @Override
  protected void onResume() {
    super.onResume();
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mediaController != null) {
      mediaController.unregisterCallback(mediaControllerCallback);
    }
    mediaBrowser.disconnect();
  }

  private void updateButton() {
    TransportControls transportControls = mediaController.getTransportControls();
    if (mediaController.getPlaybackState().getState() == STATE_PLAYING) {
      playPauseButton.setText("Pause");
      playPauseButton.setOnClickListener(v -> transportControls.pause());
    } else {
      playPauseButton.setText("Play");
      playPauseButton.setOnClickListener(v -> transportControls.play());
    }
  }
}
