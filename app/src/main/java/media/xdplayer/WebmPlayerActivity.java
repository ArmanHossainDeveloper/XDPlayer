package media.xdplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import net.protyposis.android.mediaplayer.VideoView;

import arman.common.infocodes.InfoCode;
import arman.common.ui.DrawerActivity;
import arman.common.ui.GestureManipulator;

public class WebmPlayerActivity extends DrawerActivity {

    VideoView videoView;
    TextView statusUpdateTV, currentProgressTV, durationTV;
    LinearLayout gestureLayout, progressLayout, volumeControllerLayout;
    SeekBar progressSeekbar, volumeSeekbar;
    private int width;
    private int height;

    int gestureWidth = 0;

    View brightnessView, backwardView, forwardView, volumeView, freezeView, seekbarToggleView, ratioView;
    //GestureDetector gestureDetector;
    WebmController mediaController;
    VolumeController volumeController;
    GestureManipulator gestureManipulator;

    String videoPath = InfoCode.SD_CARD + "/Download/Downloader/common cold.mp4";
    boolean isSeekbarVisible;

    @Override
    protected int[] getRequiredPermission() {
        return new int[]{InfoCode.FILE_PERMISSION/*, InfoCode.WAKELOCK_PERMISSION*/};
    }

    @Override
    protected void onCreate() {
        setContentView(R.layout.activity_webmplayer);
        setFullScreen(true);
        initialize();
        onSettingsChange();
        handleIntent();
    }
    private void initialize() {
        videoView = find(R.id.video_view);
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

        find(R.id.back_button).setOnClickListener(v -> exit());
        find(R.id.rotate_button).setOnClickListener(v -> mediaController.toggleRotate());
        find(R.id.freeze_layout_view).setOnClickListener(v -> gestureManipulator.toggleFreezeZoomPan());
        find(R.id.raito_view).setOnClickListener(v -> gestureManipulator.toggleFillCrop());

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
        gestureManipulator = new GestureManipulator(gestureLayout, videoView){
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
        mediaController = new WebmController(this, videoView, gestureManipulator, playbackUpdate, getScreenWidth());

    }

    @Override
    protected void onSettingsChange() {
    }

    @Override
    protected void onResume() {
        mediaController.onActivityResume();
        super.onResume();
        volumeController.onResume();
    }

    @Override
    protected void onPause() {
        mediaController.onActivityPause();
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        mediaController.onActivityPause();
        super.onDestroy();
        volumeController.onDestroy();
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

    boolean isYtShorts = false;
    public void handleIntent(){
        Intent i = getIntent();
        String action = i.getAction();
        if (action == null) return;

        if (action.contains("action.SEND")) {
            //isYtShorts = i.getBooleanExtra("isYtShorts", false);
            //if (isYtShorts) rotateScreen();
            loadOnlineMedia(i.getStringExtra(Intent.EXTRA_TEXT));
            //urlManager.setIsHd(i.getBooleanExtra("isHd", true));
            //urlManager.formatLink(i.getStringExtra(Intent.EXTRA_TEXT));
        }

        else if (action.contains("action.VIEW")) {
            if (i.getData() == null) return;
            String path = i.getData().toString();//.replaceAll("#", "%23").replaceAll("'", "%27");
            loadLocalMedia(path);

            //loadLocalMedia(i.getData());

        }
        else if (action.contains("action.MAIN")) {
            loadLocalMedia(videoPath);
        }

    }
    public void loadLocalMedia(String path){
        mediaController.play(path, false);

    }
    public void loadLocalMedia(Uri uri){
        mediaController.play(uri, false);

    }
    public void loadOnlineMedia(String url){
        mediaController.play(url, true);

    }




/*
    @Override
    protected void onResume() {
        super.onResume();
        //volumeController.onResume();
    }


    public void onDestroy() {
        //volumeController.onDestroy();
        super.onDestroy();
    }*/
    @Override
    public void onRotate(boolean landscape) {
        mediaController.updateViewSize();
    }


    int getScreenWidth(){
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


}
