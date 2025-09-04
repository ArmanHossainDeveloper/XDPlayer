/*
 * Copyright 2014 Mario Guggenberger <mg@protyposis.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package media.xdplayer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import net.protyposis.android.mediaplayer.UriSource;
import net.protyposis.android.mediaplayer.VideoView;

import java.io.File;

import arman.common.infocodes.InfoCode;
import arman.common.ui.GestureManipulator;


public class MediaPlayerExtendedViewActivity extends Activity {

    private static final String TAG = MediaPlayerExtendedViewActivity.class.getSimpleName();

    private VideoView videoView;

    private Uri mVideoUri;
    private int mVideoPosition;
    private float mVideoPlaybackSpeed;
    private boolean mVideoPlaying;



    TextView statusUpdateTV, currentProgressTV, durationTV;
    LinearLayout gestureLayout, progressLayout, volumeControllerLayout;
    SeekBar progressSeekbar, volumeSeekbar;
    private int width;

    int gestureWidth = 0;

    View freezeView, ratioView;
    VolumeController volumeController;
    MediaController2 mediaController;

    String videoPath = InfoCode.SD_CARD + "/Download/Downloader/common cold.mp4";
    boolean isSeekbarVisible;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_webmplayer);
        super.onCreate(savedInstanceState);

        videoView = findViewById(R.id.video_view);

        // Init video playback state (will eventually be overwritten by saved instance state)
        mVideoUri = getUri();

        mVideoPosition = 0;
        mVideoPlaybackSpeed = 1;
        initialize();

    }

    private void initialize() {
        statusUpdateTV = find(R.id.status_update_tv);
        currentProgressTV = find(R.id.current_progress_tv);
        durationTV = find(R.id.duration_tv);
        gestureLayout = find(R.id.gesture_layout);
        progressSeekbar = find(R.id.progress_seekbar);
        progressLayout = find(R.id.progress_layout);
        volumeControllerLayout = find(R.id.volume_controller_layout);
        volumeSeekbar = find(R.id.volume_seekbar);

        progressSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) mediaController.onSeek(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        freezeView = find(R.id.freeze_layout_view);
        ratioView = find(R.id.raito_view);




        //find(R.id.back_button).setOnClickListener(v -> finishAndRemoveTask());


        volumeController = new VolumeController(this, volumeSeekbar, volumeControllerLayout);

        MediaInfoCallback infoCallback = new MediaInfoCallback() {
            @Override
            public boolean isPlaying() {
                return videoView.isPlaying();
            }

            @Override
            public int getDuration() {
                return videoView.getDuration();
            }

            @Override
            public int getCurrentPosition() {
                return videoView.getCurrentPosition();
            }
        };

        MediaPlaybackUpdate playbackUpdate = new MediaPlaybackUpdate(infoCallback, progressSeekbar, currentProgressTV, durationTV);
        mediaController = new MediaController2(this, videoView, playbackUpdate, getScreenWidth());


        new GestureManipulator(gestureLayout, videoView){
            @Override
            public void onClick(int x, int y) {
                if (gestureWidth < 100) gestureWidth = gestureLayout.getWidth();
                int i = gestureWidth / 5;
                if (x < i){
                    toggleSeekbarView();
                }
                /*else if (x > i && x < i * 2){
                    mediaController.onToggleBackward();
                }*/
                else if (x > i * 2 && x < i * 3){
                    mediaController.togglePausePlay();
                }
                /*else if (x > i * 3 && x < i * 4){
                    mediaController.onToggleForward();
                }*/
                else if (x > i * 4){
                    onToggleVolume();
                }

            }

            @Override
            public void onDoubleClick(int x, int y) {
                if (gestureWidth < 100) gestureWidth = gestureLayout.getWidth();
                int i = gestureWidth / 5;
                if (x < i){
                    toggleSeekbarView();
                }
                else if (x > i && x < i * 2){
                    mediaController.onToggleBackward();
                }
                else if (x > i * 2 && x < i * 3){
                    mediaController.togglePausePlay();
                }
                else if (x > i * 3 && x < i * 4){
                    mediaController.onToggleForward();
                }
                else if (x > i * 4){
                    onToggleVolume();
                }
            }
        };
    }


    Uri getUri(){
        Intent i = getIntent();
        String action = i.getAction();
        Uri uri = i.getData();
        if (action.contains("MAIN")){
            String filePath = "/storage/emulated/0/Download/Downloader/new/videoplayback.webm";
            File file = new File(filePath);
            uri = Uri.fromFile(file);

        }
        return uri;
    }

    /*@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVideoUri = savedInstanceState.getParcelable("uri");
        mVideoPosition = savedInstanceState.getInt("position");
        mVideoPlaybackSpeed = savedInstanceState.getFloat("playbackSpeed");
        mVideoPlaying = savedInstanceState.getBoolean("playing");
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        initPlayer();
        volumeController.onResume();
        //logRamState();
    }

    private void initPlayer() {
        videoView.setVideoSource(new UriSource(this, mVideoUri));
        videoView.setPlaybackSpeed(mVideoPlaybackSpeed);
        videoView.seekTo(mVideoPosition > 0 ? mVideoPosition : 0);
        if (mVideoPlaying) {
            videoView.start();
        }
    }

    @Override
    protected void onPause() {
        // Get current player state before pausing because the MPXVideoView cannot keep its state through a pause
        mVideoPosition = videoView.getCurrentPosition();
        mVideoPlaybackSpeed = videoView.getPlaybackSpeed();
        mVideoPlaying = videoView.isPlaying();
        videoView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        volumeController.onDestroy();
        super.onDestroy();
    }

    public void onToggleVolume() {
        volumeController.onToggleVolume();
    }
    void toggleSeekbarView() {
        isSeekbarVisible = !isSeekbarVisible;
        if (isSeekbarVisible) {
            //mediaController.pause();
            mediaController.onSeekbarVisible();
            //mediaController.showProgress();
            //mediaController.updateProgress();
            show(progressLayout);
        }
        else {
            //mediaController.resume();
            hide(progressLayout);
            mediaController.onSeekbarHidden();
        }

    }



    int getScreenWidth(){
        SharedPreferences preference = getSharedPreferences("settings", 0);
        int width = preference.getInt("ScreenWidth", 0);
        if (width > 100) return width;
        if (InfoCode.sdk > 29) {
            WindowMetrics wm = getWindowManager().getCurrentWindowMetrics();
            Rect bonds = wm.getBounds();
            width = Math.min(bonds.width(), bonds.height());

            SharedPreferences.Editor editor = preference.edit();
            editor.putInt("ScreenWidth", width);
            editor.apply();

            //activity.toast("Height " + height);
        }
        //return width;
        gestureWidth = width;
        return width;
    }


    public <T extends View> T find(int id){return findViewById(id);}

    void show(View v){
        v.setVisibility(View.VISIBLE);
    }
    void hide(View v){
        v.setVisibility(View.GONE);
    }
    void logRamState() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        InfoCode.log("Memory (available/total/low): " + (memoryInfo.availMem / 1024 / 1024) + "/"  + (memoryInfo.totalMem / 1024 / 1024) + "/" + memoryInfo.lowMemory);

    }

    /*

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // the uri is stored in the base activity
        outState.putInt("position", mVideoPosition);
        outState.putFloat("playbackSpeed", mVideoView.getPlaybackSpeed());
        outState.putBoolean("playing", mVideoPlaying);
    }


    */



}

















