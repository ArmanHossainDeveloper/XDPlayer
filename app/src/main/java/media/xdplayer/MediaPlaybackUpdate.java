package media.xdplayer;

import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;

public class MediaPlaybackUpdate {
    SeekBar seekBar;
    TextView currentPositionTV;
    TextView durationTV;

    Handler handler;
    boolean isPaused;
    boolean isUpdating;
    MediaInfoCallback infoCallback;
    boolean shouldCalibrateProgress;
    MediaPlaybackUpdate(MediaInfoCallback infoCallback, SeekBar seekBar, TextView currentPosition, TextView duration){
        this.infoCallback = infoCallback;
        this.seekBar = seekBar;
        currentPositionTV = currentPosition;
        durationTV = duration;
        handler = new Handler();

    }


    void setDuration() {
        int duration = getDuration();
        durationTV.setText(getTime(duration));
        seekBar.setMax(duration);
    }

    int getSeekbarProgress() {

        return seekBar.getProgress();
    }

    public void calibrateCurrentProgress(){
        shouldCalibrateProgress = true;
    }
    void updateProgress(){
        handler.postDelayed(new Runnable(){
            @Override
            public void run() {
                if (infoCallback.isPlaying()) {
                    int current = getCurrentPosition();
                    currentPositionTV.setText(getTime(current));
                    seekBar.setProgress(current);
                }
                if (isPaused) return;
                handler.postDelayed(this, 1000);
            }
        }, 0);
    }


    /*void updateProgressEfficiently(){
        if (isUpdating) return;
        isUpdating = true;
        handler.postDelayed(new Runnable(){

            @Override
            public void run() {
                if (videoView.isPlaying()) {
                    *//*if (shouldCalibrateProgress) {
                        current = getCurrentPosition();
                        shouldCalibrateProgress = false;
                    }*//*
                    int current = getCurrentPosition();
                    if (isPaused) return;
                    currentPositionTV.setText(getTime(current));
                    seekBar.setProgress(current);
                }
                if (!isPaused) handler.postDelayed(this, 1000);
            }
        }, 0);
    }
*/
    String getTime(int sec){
        int h = sec / 60;
        String time = format2digit(h / 60) + ":"
                    + format2digit(h % 60) + ":"
                    + format2digit(sec % 60);
        return time;
    }
    String format2digit(int integer){
        return String.format("%02d", integer);
    }
    int getDuration(){
        return infoCallback.getDuration() / 1000;
    }
    int getCurrentPosition(){
        return infoCallback.getCurrentPosition() / 1000;
    }
    void setIsPaused(boolean isPaused){
        this.isPaused = isPaused;

    }

    void toggleProgressTV(boolean isPlaying){
        if (isPlaying) currentPositionTV.setBackgroundColor(0xff000000);
        else currentPositionTV.setBackgroundColor(0xffff0000);

    }

    void calculateProgress(){
            int current = getCurrentPosition();
            currentPositionTV.setText(getTime(current));
            seekBar.setProgress(current);
    }
    void setProgress(int current){
            current = current / 1000;
            currentPositionTV.setText(getTime(current));
            //seekBar.setProgress(current);
    }
    void hideProgress(){

    }


}
