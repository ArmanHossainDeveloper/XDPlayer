package media.xdplayer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.VideoView;
import android.media.MediaPlayer;

import java.io.File;

import arman.common.infocodes.InfoCode;
import arman.common.ui.GestureManipulator;

public class XDMediaController {
    Activity activity;
    MediaPlaybackUpdate playbackUpdate;
    GestureManipulator gestureManipulator;

    //private int width;
    //private int height;
    int screenWidth;
    int positionOnError = 0;
    int positionOnActivityPause = 0;

    int safeLimitOnSeek = 0;
    int positionOnSeek = 0;
    int positionOnTestSeek = 0;
    String currentFile = "";
    boolean isDoubleClick, isSeekbarVisible, isOnlineMedia, isOnStart, isPlaying, wasPlaying, isError, isErrorSolved = true, isManualSeek, isTestSeek;
    public boolean shouldUpdateSize = false;
    boolean isLandscape = true;

    VideoView videoView;
    MediaPlayer mediaPlayer;
    boolean isActivityOnPause = false;

    long fileSize = 100;
    /*public MediaController(PlayerActivity context, MediaPlaybackUpdate playbackUpdate) {
        activity = context;
        this.playbackUpdate = playbackUpdate;
    }*/
    public XDMediaController(Activity context, VideoView videoView, GestureManipulator gestureManipulator, MediaPlaybackUpdate playbackUpdate, int screenWidth) {
        activity = context;
        this.videoView = videoView;
        this.screenWidth = screenWidth;
        this.gestureManipulator = gestureManipulator;
        this.playbackUpdate = playbackUpdate;
        setMediaListener();
    }

    public void setMediaListener(){
        videoView.setOnPreparedListener(mp -> {
            isOnStart = true;
            mediaPlayer = mp;
            //mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(1.5f)); // 1.5x speed
            if (isActivityOnPause) resumeAfterActivityPause();
            else if (isError) resumeAfterError();
            else start();
        });
        videoView.setOnErrorListener((mp, what, extra) -> {
            if (!isError) handleError();
            return true;
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                setIsPlaying(false);

            }
        });
        //videoView.setOnCompletionListener(mp -> stopProgressUpdate());

    }

    public void play(String videoPath, boolean isOnline){
        isOnlineMedia = isOnline;
        currentFile = videoPath;
        if (!isOnline){
            fileSize = new File(videoPath).length();
        }
        videoView.setVideoPath(videoPath);
        //positionOnError = videoView.getDuration();
    }
    public void play(Uri uri, boolean isOnline){
        isOnlineMedia = isOnline;
        /*currentFile = uri;
        if (!isOnline){
            fileSize = new File(uri).length();
        }*/
        videoView.setVideoURI(uri);
        //positionOnError = videoView.getDuration();
    }


/*    public void jumpSeek(int progress){
        //boolean isPlaying = videoView.isPlaying();
        //if (isPlaying) {
        //    playbackUpdate.setIsPaused(true);
        //}
        videoView.seekTo(progress * 1000);
        playbackUpdate.calibrateCurrentProgress();
        calculateProgress();
    } */
    public void resumeAfterError(){
        isError = false;
        int msec = positionOnError;// - 5000;
        if (isManualSeek){
            msec = positionOnSeek;
            isManualSeek = false;
        }
        else if (isTestSeek){
            msec = positionOnTestSeek;
            isTestSeek = false;
        }

        if (InfoCode.sdk < 26) mediaPlayer.seekTo(msec);
        else mediaPlayer.seekTo(msec, MediaPlayer.SEEK_CLOSEST);

        long newSize = new File(currentFile).length();
        int delta = (int) (newSize - fileSize);
        if (delta > 1024*1024){// buffer > 1 MB
            if (wasPlaying) {
                resume();
            }
            fileSize = newSize;
        }
        //resume();
        //playbackUpdate.setDuration();
        playbackUpdate.calculateProgress();
    }
    public void handleError(){
        isError = true;
        mediaPlayer.stop();
        wasPlaying = isPlaying;
        setIsPlaying(false);
        isErrorSolved = false;
        positionOnError = mediaPlayer.getCurrentPosition();
        videoView.setVideoPath(currentFile);
    }


    public void start(){
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        int viewWidth = 0;
        int viewHeight = 0;
        if (videoHeight < videoWidth){
            //Landscape Video
            viewHeight = screenWidth;
            viewWidth = screenWidth * videoWidth / videoHeight;
        }
        else {
            //Portrait Short Video
            isLandscape = false;
            viewWidth = screenWidth;
            viewHeight = screenWidth * videoHeight / videoWidth;
        }
        rotateScreen(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(viewWidth, viewHeight);
        videoView.setLayoutParams(params);
        resume();
        playbackUpdate.setDuration();
        shouldUpdateSize = true;
        updateViewSize();
    }

    public void updateViewSize(){
        if (!shouldUpdateSize) return;
        videoView.postDelayed(()->{
            View parent = (View) videoView.getParent();
            gestureManipulator.setSize(parent.getWidth(), parent.getHeight(), videoView.getWidth(), videoView.getHeight());

        }, 2000);
        shouldUpdateSize = false;
    }

    public void stop(){
        videoView.stopPlayback();
        playbackUpdate.setIsPaused(true);
        //playbackUpdate.pauseUpdate();
    }
    public void previous(){

    }
    public void next(){

    }
    public void rotateScreen(boolean shouldReact){
        shouldUpdateSize = shouldReact;
        if (isLandscape) activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    public void toggleRotate(){
        isLandscape = !isLandscape;
        rotateScreen(true);
    }


    public void pause(){
        //stopProgressUpdate();
        if (!isPlaying) return;
        mediaPlayer.pause();
        setIsPlaying(false);

    }
    public void resume(){
        if (isPlaying) return;
        //if (isError && !isErrorSolved) isErrorSolved = true;
        if (isOnStart){
            isOnStart = false;
            isErrorSolved = true;
        }
        mediaPlayer.start();
        setIsPlaying(true);
    }

    void calculateProgress(){
        if (isSeekbarVisible) playbackUpdate.calculateProgress();

    }
    void startProgressUpdate(){
        //playbackUpdate.updateProgressEfficiently();
        playbackUpdate.setIsPaused(false);
        playbackUpdate.updateProgress();

    }
    void stopProgressUpdate(){
        playbackUpdate.setIsPaused(true);

    }


    private void jumpSeek(int msec) {
        //if (!isErrorSolved) return;
        //isManualSeek = true;
        //positionOnSeek = getCurrentPosition();
        isTestSeek = true;
        isManualSeek = false;
        positionOnTestSeek = getCurrentPosition();
        msec += positionOnTestSeek;
        if (InfoCode.sdk < 26) mediaPlayer.seekTo(msec);
        else mediaPlayer.seekTo(msec, MediaPlayer.SEEK_CLOSEST);
        //playbackUpdate.calibrateCurrentProgress();
        calculateProgress();
        /*if (wasPlaying) resume();
        else pause();*/
    }

    private void forwardRewind(int msec){
        if (isDoubleClick) {
            isDoubleClick = false;
            jumpSeek(msec);
        } else {
            isDoubleClick = true;
            new Handler().postDelayed(this::revert, 1000);

            /*wasPlaying = isPlaying;
            if (isPlaying) pause();
            else resume();*/

        }
    }

    public void togglePausePlay(){
        if (isPlaying) pause();
        else resume();
    }

    public void onToggleForward() {
        //positionOnSeek = playbackUpdate.getSeekbarProgress();
        //forwardRewind(5000);
        jumpSeek(5000);
    }

    public void onToggleBackward() {
        //forwardRewind(-5000);
        jumpSeek(-5000);
    }
    private void revert(){
        isDoubleClick = false;

    }
    int getCurrentPosition(){
        //if (positionOnError > 0) return positionOnError;
        //int currentPosition = mediaPlayer.getCurrentPosition();
        /*if (positionOnError > 0) {
            positionOnError = currentPosition = 0;
        }*/
        return mediaPlayer.getCurrentPosition();//currentPosition;// / 1000;
    }

    public void onSeek(int progress){
        progress = progress * 1000;
        int currentPosition = mediaPlayer.getCurrentPosition();
        if (isPlaying || currentPosition < safeLimitOnSeek || (isOnStart && isErrorSolved)){
            isManualSeek = true;
            isTestSeek = false;
            positionOnSeek = currentPosition;
            if (currentPosition > safeLimitOnSeek) safeLimitOnSeek = currentPosition;
            //positionOnSeek = getCurrentPosition();//playbackUpdate.getSeekbarProgress();
        }
        else if (isOnStart){
            if (currentPosition < positionOnError){
                videoView.seekTo(progress);
                playbackUpdate.setProgress(progress);
                resume();

            }
            else return;
        }
        else{
            isTestSeek = true;
            isManualSeek = false;
            positionOnTestSeek = currentPosition;
        }

        /*else {
            isManualSeek = true;
            positionOnSeek = getCurrentPosition();//playbackUpdate.getSeekbarProgress();
        }*/
        videoView.seekTo(progress);
        //playbackUpdate.calibrateCurrentProgress();
        playbackUpdate.setProgress(progress);
        //if (!isErrorSolved) isErrorSolved = true;
    }

    public void onSeekbarVisible() {
        isSeekbarVisible = true;
        calculateProgress();
        startProgressUpdate();
    }
    public void onSeekbarHidden() {
        stopProgressUpdate();
        isSeekbarVisible = false;
    }
    public void onActivityPause() {
        if (isActivityOnPause) return;
        isActivityOnPause = true;
        wasPlaying = isPlaying;
        //pause();
        mediaPlayer.stop();
        setIsPlaying(false);
        positionOnActivityPause = mediaPlayer.getCurrentPosition();
        positionOnError = 0;
    }

    public void onActivityResume() {
        if (!isActivityOnPause) return;
        videoView.setVideoPath(currentFile);
        //resume();

    }
    public void resumeAfterActivityPause() {
        isActivityOnPause = false;
        int msec = positionOnActivityPause; //currentErrorPosition - 5000;
        if (InfoCode.sdk < 26) mediaPlayer.seekTo(msec);
        else mediaPlayer.seekTo(msec, MediaPlayer.SEEK_CLOSEST);
        //handleResume();
        if (wasPlaying) resume();


        //playbackUpdate.setDuration();
        playbackUpdate.calculateProgress();
    }
    void setIsPlaying(boolean isPlaying){
        this.isPlaying = isPlaying;
        playbackUpdate.toggleProgressTV(isPlaying);
    }
}
