package com.app.playassetdeliverydemo.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.app.playassetdeliverydemo.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null
    private var uri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    /**
     * This method is used to initialize views
     */
    private fun initViews() {
        uri = intent.data
    }

    /**
     * lifecycle method to initialize ExoPlayer
     */
    public override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    /**
     * lifecycle method to initialize ExoPlayer
     */
    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Build.VERSION.SDK_INT < 24 || player == null) {
            initializePlayer()
        }
    }

    /**
     * lifecycle method to release ExoPlayer
     */
    public override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < 24) {
            releasePlayer()
        }
    }

    /**
     * lifecycle method to release ExoPlayer
     */
    public override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    /**
     * Method to initialize ExoPlayer
     */
    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(this@VideoPlayerActivity).build()
            binding.playerView.player = player
            val mediaItem = MediaItem.fromUri(
                uri ?: Uri.parse("")
            )
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.playWhenReady = true
        }
    }

    /**
     * Method to release ExoPlayer
     */
    private fun releasePlayer() {
        if (player != null) {
            player?.release()
            player = null
        }
    }

    /**
     * Method to hide System UI & show ExoPlayer in FullScreen
     */
    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.playerView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
