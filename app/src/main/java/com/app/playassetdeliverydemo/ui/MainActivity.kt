package com.app.playassetdeliverydemo.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.app.playassetdeliverydemo.R
import com.app.playassetdeliverydemo.customviews.CustomProgressDialog
import com.app.playassetdeliverydemo.databinding.ActivityMainBinding
import com.app.playassetdeliverydemo.databinding.LayoutAudioPlayerBinding
import com.app.playassetdeliverydemo.helper.Utils.isInternetConnected
import com.app.playassetdeliverydemo.helper.Utils.showToast
import com.google.android.play.core.assetpacks.AssetPackManager
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.AssetPackState
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener
import com.google.android.play.core.assetpacks.AssetPackStates
import com.google.android.play.core.assetpacks.model.AssetPackStatus
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private var assetManager: AssetManager? = null
    private var inputStream: InputStream? = null
    private val videoFileName = "big_buck_bunny.mp4"
    private val audioFileName = "dummy_aud.mp3"
    private val requestWritePermission = 111
    private var mPlayer: MediaPlayer? = null
    private var isPaused = false
    private var pausedLength = 0
    private var totalSizeToDownloadInBytes: Long = 0
    private var dialog: CustomProgressDialog? = null
    private var assetPackManager: AssetPackManager? = null
    private val fastFollowAssetPack = "fast_follow_asset_pack"
    private val onDemandAssetPack = "on_demand_asset_pack"
    private var waitForWifiConfirmationShown = false
    private val logTAG = "MainActivity"
    private var assetPackState: AssetPackState? = null
    private var isFastFollow = false
    private var isOnDemand = false
    private var context: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initInstallTime()
    }

    /**
     * This method is used to initialize views
     */
    private fun initViews() = with(binding) {
        context = this@MainActivity
        btnFF.setOnClickListener(this@MainActivity)
        btnOD.setOnClickListener(this@MainActivity)
        btnWatchVideo.setOnClickListener(this@MainActivity)
        btnPlayAudio.setOnClickListener(this@MainActivity)
        if (dialog == null) {
            dialog = CustomProgressDialog(this@MainActivity)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnFF -> {
                isFastFollow = true
                isOnDemand = false
                initAssetPackManager()
            }

            R.id.btnOD -> {
                isFastFollow = false
                isOnDemand = true
                initAssetPackManager()
            }

            R.id.btnWatchVideo -> {
                showAlertDialog()
            }

            R.id.btnPlayAudio -> {
                dialog?.showProgressDialog()
                if (isPermissionGranted) {
                    getInputStreamFromAssetManager()
                }
            }
        }
    }

    /**
     * start install-time delivery mode
     */
    private fun initInstallTime() {
        try {
            val context = createPackageContext("com.app.playassetdeliverydemo", 0)
            assetManager = context.assets
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private val isPermissionGranted: Boolean
        /**
         * Checks for the Permission is granted or not
         */
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ), requestWritePermission
                    )
                    false
                } else {
                    true
                }
            } else {
                true
            }
        }

    /**
     * This method is used to get Input Stream from the provided filepath
     */
    private fun getInputStreamFromAssetManager() {
        // var list: Array<String?>? = null
        try {
            inputStream = assetManager?.open(audioFileName)
            // list = assetManager!!.list("") // returns entire list of assets in directory
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (inputStream != null) {
            writeDataToFile(inputStream)
        } else {
            showToast(context, "InputStream Empty")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == requestWritePermission && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getInputStreamFromAssetManager()
        } else if (requestCode == requestWritePermission && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            showToast(context, "Please provide the Permission for app to work")
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * This method will create a new hidden temporary file & write datafrom inputstrem to temporary
     * file, so as to play video from file.
     * Note : If you do not want hidden file then just remove ".(dot)" from prefix of fileName
     */
    private fun writeDataToFile(inputStream: InputStream?) {
        val tempFileName = ".tempFile"
        val filePath = getExternalFilesDir("").toString() + File.separator + tempFileName
        val file = File(filePath)
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            val outputStream = FileOutputStream(file, false)
            var read: Int
            val bytes = ByteArray(8192)
            if (inputStream != null) {
                while (inputStream.read(bytes).also { read = it } != -1) {
                    outputStream.write(bytes, 0, read)
                }
            }
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        playVideoInExoplayer(file)
    }

    /**
     * This method will play video from requested file from assets
     *
     * @param file: File
     */
    private fun playVideoInExoplayer(file: File) {
        dialog?.hideProgressDialog()
        val mediaUri = Uri.parse(file.absolutePath)
        val intent = Intent()
        intent.setData(mediaUri)
        intent.putExtra(Intent.EXTRA_TITLE, mediaUri.lastPathSegment)
        intent.setClass(this, VideoPlayerActivity::class.java)
        startActivity(intent)
    }

    /**
     * This method will show Alert Dialog to play audio file
     */
    private fun showAlertDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogBinding = LayoutAudioPlayerBinding.inflate(layoutInflater)
        dialogBuilder.setView(dialogBinding.root)
        dialogBinding.imgPlay.setOnClickListener {
            isPaused = false
            playAudioFromFile()
        }
        dialogBinding.imgPause.setOnClickListener {
            if (mPlayer != null && mPlayer!!.isPlaying) {
                isPaused = true
                mPlayer?.pause()
                pausedLength = mPlayer?.currentPosition ?: 0
            }
        }
        dialogBinding.imgStop.setOnClickListener {
            if (mPlayer != null && mPlayer!!.isPlaying) {
                isPaused = false
                mPlayer?.stop()
                mPlayer?.release()
                mPlayer = null
            }
        }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    /**
     * This method will play audio from requested file from assets
     *
     * @param //audioFileName: path of file in assets, only filename if placed into assets directory
     */
    private fun playAudioFromFile() {
        try {
            val fd = assetManager?.openFd(videoFileName)
            if (mPlayer == null && !isPaused) {
                mPlayer = MediaPlayer()
                val attributes =
                    AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                mPlayer?.setAudioAttributes(attributes)
                mPlayer?.setDataSource(fd?.fileDescriptor, fd?.startOffset ?: 0L, fd?.length ?: 0L)
            }
            if (mPlayer != null && isPaused) {
                isPaused = false
                mPlayer?.seekTo(pausedLength)
            }
            if (mPlayer != null) {
                mPlayer?.prepare()
                mPlayer?.start()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * This method will get Instance of AssetPackManager For fast-follow & on-demand deliver mode
     */
    private fun initAssetPackManager() {
        dialog?.showProgressDialog()
        if (isInternetConnected(applicationContext)) {
            if (assetPackManager == null) {
                assetPackManager = AssetPackManagerFactory.getInstance(applicationContext)
            }
            registerListener()
        } else {
            showToast(context, "Please Connect to Internet")
        }
    }
    //    private void checkDownloadSize() {
    //        getPackStates(fastFollowAssetPack);
    //    }
    /**
     * This method will be triggered when the pack download size is more than 150MB
     * & user is not available on wifi or switched from wifi to Mobile Data.
     * A confirmation Dialog will be shown to user, asking for consent to download
     * asset pack on Mobile data
     */
    private fun showWifiConfirmationDialog() {
        dialog?.hideProgressDialog()
        if (!waitForWifiConfirmationShown) {/*assetPackManager!!.showCellularDataConfirmation(this@MainActivity)
                .addOnSuccessListener { resultCode ->
                    if (resultCode == RESULT_OK) {
                        Log.d(logTAG, "Confirmation dialog has been accepted.")
                        registerListener()
                    } else if (resultCode == RESULT_CANCELED) {
                        Log.d(logTAG, "Confirmation dialog has been denied by the user.")
                        showToast(context, "Please Connect to Wifi to begin app files to download")
                    }
                }*/

            assetPackManager?.showConfirmationDialog(this@MainActivity)
                ?.addOnSuccessListener { resultCode ->
                    if (resultCode == RESULT_OK) {
                        Log.d(logTAG, "Confirmation dialog has been accepted.")
                        registerListener()
                    } else if (resultCode == RESULT_CANCELED) {
                        Log.d(logTAG, "Confirmation dialog has been denied by the user.")
                        showToast(context, "Please Connect to Wifi to begin app files to download")
                    }
                }
            waitForWifiConfirmationShown = true
        }
    }

    /**
     * This method will check Asset Pack is available on device or not,
     * if not available, it will register listener for it & start downloading by calling
     * fetch method.
     */
    private fun registerListener() {
        if (dialog != null && dialog!!.isShowing) dialog?.showProgressDialog()
        val onDemandAssetPackPath = getAbsoluteAssetPath(onDemandAssetPack)
        val fastFollowAssetPackPath = getAbsoluteAssetPath(fastFollowAssetPack)
        if (onDemandAssetPackPath == null || fastFollowAssetPackPath == null) {
            assetPackManager?.registerListener(assetPackStateUpdateListener)
            val assetPackList: MutableList<String> = ArrayList()
            assetPackList.add(fastFollowAssetPack)
            assetPackList.add(onDemandAssetPack)
            assetPackManager?.fetch(assetPackList)
        } else {
            initClickedAssetPack()
        }
    }

    /**
     * start fast-follow delivery mode
     */
    private fun initFastFollow() {
        val assetsPath = getAbsoluteAssetPath(fastFollowAssetPack)
        if (assetsPath == null) {
            getPackStates(fastFollowAssetPack)
        }
        if (assetsPath != null) {
            val file = File(assetsPath + File.separator + videoFileName)
            if (file.exists()) {
                playVideoInExoplayer(file)
            }
        }
    }

    /**
     * start on-demand delivery mode
     */
    private fun initOnDemand() {
        val assetsPath = getAbsoluteAssetPath(onDemandAssetPack)
        if (assetsPath == null) {
            getPackStates(onDemandAssetPack)
        }
        if (assetsPath != null) {
            val file = File(assetsPath + File.separator + audioFileName)
            if (file.exists()) {
                playVideoInExoplayer(file)
            }
        }
    }

    /**
     * This method will check which button was clicked & call respective method to get file & play
     */
    private fun initClickedAssetPack() {
        if (isFastFollow) {
            isFastFollow = false
            initFastFollow()
        } else if (isOnDemand) {
            isOnDemand = false
            initOnDemand()
        }
    }

    /**
     * AssetPackStateUpdateListener that listens to multiple events while downlading
     */
    private var assetPackStateUpdateListener = AssetPackStateUpdateListener { state ->
        when (state.status()) {
            AssetPackStatus.PENDING -> Log.i(logTAG, "Pending")
            AssetPackStatus.DOWNLOADING -> {
                val downloaded = state.bytesDownloaded()
                val totalSize = state.totalBytesToDownload()
                val percent = 100.0 * downloaded / totalSize
                Log.i(logTAG, "PercentDone=" + String.format("%.2f", percent))
            }

            AssetPackStatus.TRANSFERRING -> {}
            AssetPackStatus.COMPLETED ->                     // Asset pack is ready to use. Start the Game/App.
                initClickedAssetPack()

            AssetPackStatus.FAILED ->                     // Request failed. Notify user.
                Log.e(logTAG, state.errorCode().toString())

            AssetPackStatus.CANCELED -> {}
            AssetPackStatus.WAITING_FOR_WIFI -> showWifiConfirmationDialog()
            AssetPackStatus.NOT_INSTALLED -> {}
            AssetPackStatus.UNKNOWN -> {}
        }
    }

    /**
     * This method is used to Get download information about asset packs
     */
    private fun getPackStates(assetPackName: String) {
        assetPackManager?.getPackStates(listOf(assetPackName))?.addOnCompleteListener { task ->
            val assetPackStates: AssetPackStates
            try {
                assetPackStates = task.result
                assetPackState = assetPackStates.packStates()[assetPackName]
                if (assetPackState != null) {
                    if (assetPackState?.status() == AssetPackStatus.WAITING_FOR_WIFI) {
                        totalSizeToDownloadInBytes = assetPackState?.totalBytesToDownload() ?: 0L
                        if (totalSizeToDownloadInBytes > 0) {
                            val sizeInMb = totalSizeToDownloadInBytes / (1024 * 1024)
                            if (sizeInMb >= 150) {
                                showWifiConfirmationDialog()
                            } else {
                                registerListener()
                            }
                        }
                    }
                    Log.d(
                        logTAG,
                        "status: " + assetPackState?.status() + ", name: " + assetPackState?.name() + ", errorCode: " + assetPackState?.errorCode() + ", bytesDownloaded: " + assetPackState?.bytesDownloaded() + ", totalBytesToDownload: " + assetPackState?.totalBytesToDownload() + ", transferProgressPercentage: " + assetPackState?.transferProgressPercentage()
                    )
                }
            } catch (e: Exception) {
                dialog?.hideProgressDialog()
                Log.d("MainActivity", e.message ?: "")
            }
        }
    }

    /**
     * @param assetPack         : Name of assetPack i.e : fast-follow asser pack or on-demand asset pack
     * @param relativeAssetPath : if Assets are placed in assets directory then just pass name of asset
     * otherwise if placed in subdirectory "subdirectory/asset-name"
     * @return absolute asset path as String
     */
    private fun getAbsoluteAssetPath(assetPack: String, relativeAssetPath: String = ""): String? {
        val assetPackPath =
            assetPackManager?.getPackLocation(assetPack) ?: // asset pack is not ready
            return null
        val assetsFolderPath = assetPackPath.assetsPath()
        // equivalent to: FilenameUtils.concat(assetPackPath.path(), "assets");
        return FilenameUtils.concat(assetsFolderPath, relativeAssetPath) //return assetPath
    }

    /**
     * lifecycle method to unregister the listener
     */
    override fun onDestroy() {
        super.onDestroy()
        if (assetPackManager != null) assetPackManager?.unregisterListener(
            assetPackStateUpdateListener
        )
    }
}