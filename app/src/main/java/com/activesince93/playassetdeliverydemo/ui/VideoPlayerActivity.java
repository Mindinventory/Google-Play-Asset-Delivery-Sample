package com.activesince93.playassetdeliverydemo.ui;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.activesince93.playassetdeliverydemo.R;
import androidx.media3.common.MediaItem;
import androidx.media3.ui.PlayerView;
import androidx.media3.exoplayer.ExoPlayer;

public class VideoPlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        initViews();
    }

    /**
     * This method is used to initialize views
     */
    private void initViews() {
        playerView = findViewById(R.id.playerView);
        uri = getIntent().getData();
    }

    /**
     * lifecycle method to initialize ExoPlayer
     */
    @Override
    public void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    /**
     * lifecycle method to initialize ExoPlayer
     */
    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Build.VERSION.SDK_INT < 24 || player == null)) {
            initializePlayer();
        }
    }

    /**
     * lifecycle method to release ExoPlayer
     */
    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < 24) {
            releasePlayer();
        }
    }

    /**
     * lifecycle method to release ExoPlayer
     */
    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    /**
     * Method to initialize ExoPlayer
     */
    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);
            MediaItem mediaItem = MediaItem.fromUri(uri);
            player.setMediaItem(mediaItem);
        }
    }

    /**
     * Method to release ExoPlayer
     */
    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentMediaItemIndex();
            player.release();
            player = null;
        }
    }

    /**
     * Method to hide System UI & show ExoPlayer in FullScreen
     */
    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
