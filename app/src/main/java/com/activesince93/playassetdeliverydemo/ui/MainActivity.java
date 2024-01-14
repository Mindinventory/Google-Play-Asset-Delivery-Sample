package com.activesince93.playassetdeliverydemo.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.activesince93.playassetdeliverydemo.R;
import com.activesince93.playassetdeliverydemo.customviews.CustomProgressDialog;
import com.activesince93.playassetdeliverydemo.helper.Utils;
import com.google.android.play.core.assetpacks.AssetPackLocation;
import com.google.android.play.core.assetpacks.AssetPackManager;
import com.google.android.play.core.assetpacks.AssetPackManagerFactory;
import com.google.android.play.core.assetpacks.AssetPackState;
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener;
import com.google.android.play.core.assetpacks.AssetPackStates;
import com.google.android.play.core.assetpacks.model.AssetPackStatus;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AssetManager assetManager;
    private InputStream inputStream = null;
    private final String videoFileName = "sample_video.m4v";
    private final String audioFileName = "audio.mp3";
    private final int REQUEST_WRITE_PERMISSION = 111;
    private MediaPlayer mPlayer;
    private boolean isPaused = false;
    private int pausedLength = 0;
    private long totalSizeToDownloadInBytes = 0;
    private CustomProgressDialog dialog = null;
    private AssetPackManager assetPackManager;
//    private final String installTimeAssetPack = "install_time_asset_pack";
//    private final String fastFollowAssetPack = "fast_follow_asset_pack";
    private final String onDemandAssetPack = "on_demand_asset_pack";
    private boolean waitForWifiConfirmationShown = false;
    private final String TAG = "MainActivity";
    private AssetPackState assetPackState;
    private boolean isFastFollow = false;
    private boolean isOnDemand = false;
    private Button btn_it;
    private Button btn_ff;
    private Button btn_od;
    private Button btn_watch_video;
    private Button btnPlayAudio;
    private TextView txt_download_status;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initInstallTime();
    }

    /**
     * This method is used to initialize views
     */
    private void initViews() {
        context = MainActivity.this;
        btn_it = findViewById(R.id.btn_it);
        btn_ff = findViewById(R.id.btn_ff);
        btn_od = findViewById(R.id.btn_od);
        txt_download_status = findViewById(R.id.txt_download_status);
        btn_watch_video = findViewById(R.id.btn_watch_video);
        btnPlayAudio = findViewById(R.id.btn_play_audio);

        btn_it.setOnClickListener(this);
        btn_ff.setOnClickListener(this);
        btn_od.setOnClickListener(this);
        btn_watch_video.setOnClickListener(this);
        btnPlayAudio.setOnClickListener(this);

        if (dialog == null) {
            dialog = new CustomProgressDialog(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_it: {
                playInstallTimeVideo();
                break;
            }

            case R.id.btn_ff: {
                dialog.showProgresDialog();

                isFastFollow = true;
                isOnDemand = false;
                initAssetPackManager();
                break;
            }

            case R.id.btn_od: {
                dialog.showProgresDialog();

                isFastFollow = false;
                isOnDemand = true;
                initAssetPackManager();
                break;
            }

            case R.id.btn_play_audio: {
                showAlertDialog();
                break;
            }

            case R.id.btn_watch_video: {
                dialog.showProgresDialog();
                if (isPermissionGranted()) {
                    getInputStreamFromAssetManager(videoFileName);
                }
                break;
            }

        }
    }

    /**
     * start install-time delivery mode
     */
    private void initInstallTime() {
        try {
            Context context = createPackageContext("com.activesince93.playassetdeliverydemo", 0);
            assetManager = context.getAssets();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void playInstallTimeVideo() {
        String[] list = null;
        try {
            inputStream = assetManager.open(videoFileName);
            Log.i(TAG, "activesince931 assets length: " + inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks for the Permission is granted or not
     */
    private Boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            } else {
                return true;
            }
        } else {
            return true;
        }
        return true;
    }

    /**
     * This method is used to get Input Stream from the provided filepath
     */
    private void getInputStreamFromAssetManager(String fileName) {
        String[] list = null;
        try {
            inputStream = assetManager.open(fileName);
            list = assetManager.list(""); // returns entire list of assets in directory
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (inputStream != null) {
            writeDataToFile(inputStream);
        } else {
            Utils.showToast(context, "InputStream Empty");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getInputStreamFromAssetManager(videoFileName);
        } else if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Utils.showToast(context, "Please provide the Permission for app to work");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * This method will create a new hidden temporary file & write datafrom inputstrem to temporary
     * file, so as to play video from file.
     * Note : If you do not want hidden file then just remove ".(dot)" from prefix of fileName
     */
    private void writeDataToFile(InputStream inputStream) {
        String tempFileName = ".tempFile";
        String filePath = getExternalFilesDir("") + File.separator + tempFileName;
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(file, false);
            int read;
            byte[] bytes = new byte[8192];
            if (inputStream != null) {
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        playVideoInExoplayer(file);
    }

    /**
     * This method will play video from requested file from assets
     *
     * @param file: File
     */
    void playVideoInExoplayer(File file) {
        dialog.hideProgresDialog();
        Uri mediaUri = Uri.parse(file.getAbsolutePath());
        Intent intent = new Intent();
        intent.setData(mediaUri);
        intent.putExtra(Intent.EXTRA_TITLE, mediaUri.getLastPathSegment());
        intent.setClass(this, VideoPlayerActivity.class);
        startActivity(intent);
    }

    /**
     * This method will show Alert Dialog to play audio file
     */
    private void showAlertDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_audio_player, null);
        dialogBuilder.setView(dialogView);

        ImageView img_play, img_pause, img_stop;
        img_play = dialogView.findViewById(R.id.img_play);
        img_pause = dialogView.findViewById(R.id.img_pause);
        img_stop = dialogView.findViewById(R.id.img_stop);

        img_play.setOnClickListener(v -> {
            isPaused = false;
            playAudioFromFile(audioFileName);
        });

        img_pause.setOnClickListener(v -> {
            if (mPlayer.isPlaying()) {
                isPaused = true;
                mPlayer.pause();
                pausedLength = mPlayer.getCurrentPosition();
            }
        });

        img_stop.setOnClickListener(v -> {
            if (mPlayer.isPlaying()) {
                isPaused = false;
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    /**
     * This method will play audio from requested file from assets
     *
     * @param audioFileName: path of file in assets, only filename if placed into assets directory
     */
    private void playAudioFromFile(String audioFileName) {
        try {
            AssetFileDescriptor fd = assetManager.openFd(audioFileName);
            if (mPlayer == null && !isPaused) {
                mPlayer = new MediaPlayer();
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            }
            if (mPlayer != null && isPaused) {
                isPaused = false;
                mPlayer.seekTo(pausedLength);
            }
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will get Instance of AssetPackManager For fast-follow & on-demand deliver mode
     */
    private void initAssetPackManager() {
        if (Utils.isInternetConnected(getApplicationContext())) {
            if (assetPackManager == null) {
                assetPackManager = AssetPackManagerFactory.getInstance(getApplicationContext());
            }
            registerListener();
        } else {
            Utils.showToast(context, "Please Connect to Internet");
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
    private void showWifiConfirmationDialog() {
        dialog.hideProgresDialog();
        if (!waitForWifiConfirmationShown) {
            assetPackManager.showCellularDataConfirmation(MainActivity.this)
                    .addOnSuccessListener(new OnSuccessListener<Integer>() {
                        @Override
                        public void onSuccess(Integer resultCode) {
                            if (resultCode == RESULT_OK) {
                                Log.d(TAG, "Confirmation dialog has been accepted.");
                                registerListener();
                            } else if (resultCode == RESULT_CANCELED) {
                                Log.d(TAG, "Confirmation dialog has been denied by the user.");
                                Utils.showToast(context, "Please Connect to Wifi to begin app files to download");
                            }
                        }
                    });
            waitForWifiConfirmationShown = true;
        }
    }

    /**
     * This method will check Asset Pack is available on device or not,
     * if not available, it will register listener for it & start downloading by calling
     * fetch method.
     */
    private void registerListener() {
        dialog.showProgresDialog();
        String onDemandAssetPackPath = getAbsoluteAssetPath(onDemandAssetPack, "");
//        String fastFollowAssetPackPath = getAbsoluteAssetPath(fastFollowAssetPack, "");

//        if (onDemandAssetPackPath == null || fastFollowAssetPackPath == null) {
        if (onDemandAssetPackPath == null) {
            assetPackManager.registerListener(assetPackStateUpdateListener);
            List<String> assetPackList = new ArrayList<>();
//            assetPackList.add(fastFollowAssetPack);
            assetPackList.add(onDemandAssetPack);
            assetPackManager.fetch(assetPackList);
        } else {
            initClickedAssetPack();
        }
    }

    /**
     * start fast-follow delivery mode
     */
    private void initFastFollow() {
//        String assetsPath = getAbsoluteAssetPath(fastFollowAssetPack, "");
//        if (assetsPath == null) {
//            getPackStates(fastFollowAssetPack);
//        }
//        Log.i(TAG, "activesince931 Asset Path: " + assetsPath);
//        if (assetsPath != null) {
//            File file = new File(assetsPath + File.separator + videoFileName);
//            Log.i(TAG, "activesince931 File: " + file.getPath());
//            Log.i(TAG, "activesince931 File exists: " + file.exists());
//            if (file.exists()) {
//                playVideoInExoplayer(file);
//            }
//        }
    }

    /**
     * start on-demand delivery mode
     */
    private void initOnDemand() {
        String assetsPath = getAbsoluteAssetPath(onDemandAssetPack, "");
        if (assetsPath == null) {
            getPackStates(onDemandAssetPack);
        }
        Log.i(TAG, "activesince931 Asset Path: " + assetsPath);
        if (assetsPath != null) {
            File file = new File(assetsPath + File.separator + videoFileName);
            Log.i(TAG, "activesince931 File: " + file.getPath());
            Log.i(TAG, "activesince931 File exists: " + file.exists());
            if (file.exists()) {
                playVideoInExoplayer(file);
            }
        }
    }

    /**
     * This method will check which button was clicked & call respective method to get file & play
     */
    private void initClickedAssetPack() {
        if (isFastFollow) {
            isFastFollow = false;
            initFastFollow();
        } else if (isOnDemand) {
            isOnDemand = false;
            initOnDemand();
        }
    }

    /**
     * AssetPackStateUpdateListener that listens to multiple events while downlading
     */
    AssetPackStateUpdateListener assetPackStateUpdateListener = new AssetPackStateUpdateListener() {
        @Override
        public void onStateUpdate(AssetPackState state) {
            switch (state.status()) {
                case AssetPackStatus.PENDING:
                    Log.i(TAG, "Pending");
                    txt_download_status.setText("PENDING");
                    break;

                case AssetPackStatus.DOWNLOADING:
                    long downloaded = state.bytesDownloaded();
                    long totalSize = state.totalBytesToDownload();
                    double percent = 100.0 * downloaded / totalSize;
                    txt_download_status.setText("DOWNLOADING: " + percent);
                    Log.i(TAG, "PercentDone=" + String.format("%.2f", percent));
                    break;

                case AssetPackStatus.TRANSFERRING:
                    // 100% downloaded and assets are being transferred.
                    // Notify user to wait until transfer is complete.
                    break;

                case AssetPackStatus.COMPLETED:
                    // Asset pack is ready to use. Start the Game/App.
                    txt_download_status.setText("COMPLETED");
                    initClickedAssetPack();
                    break;

                case AssetPackStatus.FAILED:
                    // Request failed. Notify user.
                    Log.e(TAG, String.valueOf(state.errorCode()));
                    break;

                case AssetPackStatus.CANCELED:
                    // Request canceled. Notify user.
                    break;

                case AssetPackStatus.WAITING_FOR_WIFI:
                    showWifiConfirmationDialog();
                    break;

                case AssetPackStatus.NOT_INSTALLED:
                    // Asset pack is not downloaded yet.
                    break;
                case AssetPackStatus.UNKNOWN:
                    // The Asset pack state is unknown
                    break;
            }

        }
    };

    /**
     * This method is used to Get download information about asset packs
     */
    private void getPackStates(String assetPackName) {
        assetPackManager.getPackStates(Collections.singletonList(assetPackName))
                .addOnCompleteListener(new OnCompleteListener<AssetPackStates>() {
                    @Override
                    public void onComplete(Task<AssetPackStates> task) {
                        AssetPackStates assetPackStates;
                        try {
                            assetPackStates = task.getResult();
                            assetPackState =
                                    assetPackStates.packStates().get(assetPackName);

                            if (assetPackState != null) {
                                if (assetPackState.status() == AssetPackStatus.WAITING_FOR_WIFI) {
                                    totalSizeToDownloadInBytes = assetPackState.totalBytesToDownload();
                                    if (totalSizeToDownloadInBytes > 0) {
                                        long sizeInMb = totalSizeToDownloadInBytes / (1024 * 1024);
                                        if (sizeInMb >= 150) {
                                            showWifiConfirmationDialog();
                                        } else {
                                            registerListener();
                                        }
                                    }
                                }

                                Log.d(TAG, "status: " + assetPackState.status() +
                                        ", name: " + assetPackState.name() +
                                        ", errorCode: " + assetPackState.errorCode() +
                                        ", bytesDownloaded: " + assetPackState.bytesDownloaded() +
                                        ", totalBytesToDownload: " + assetPackState.totalBytesToDownload() +
                                        ", transferProgressPercentage: " + assetPackState.transferProgressPercentage());
                            }
                        } catch (Exception e) {
                            dialog.hideProgresDialog();
                            Log.d("MainActivity", e.getMessage());
                        }
                    }
                });
    }

    /**
     * @param assetPack         : Name of assetPack i.e : fast-follow asser pack or on-demand asset pack
     * @param relativeAssetPath : if Assets are placed in assets directory then just pass name of asset
     *                          otherwise if placed in subdirectory "subdirectory/asset-name"
     * @return absolute asset path as String
     */
    private String getAbsoluteAssetPath(String assetPack, String relativeAssetPath) {
        AssetPackLocation assetPackPath = assetPackManager.getPackLocation(assetPack);

        if (assetPackPath == null) {
            // asset pack is not ready
            return null;
        }

        String assetsFolderPath = assetPackPath.assetsPath();
        // equivalent to: FilenameUtils.concat(assetPackPath.path(), "assets");
        String assetPath = FilenameUtils.concat(assetsFolderPath, relativeAssetPath);
        return assetPath;
    }


    /**
     * lifecycle method to unregister the listener
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        assetPackManager.unregisterListener(assetPackStateUpdateListener);
    }
}