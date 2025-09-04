package media.xdplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class VolumeController {


    Context activity;
    boolean isVisible;
    SeekBar seekBar;
    LinearLayout controllerLayout;
    public static AudioManager audioManager;
    ComponentName cn;




    VolumeController(Context context, SeekBar vSeekbar, LinearLayout vcLayout){
        activity = context;
        seekBar = vSeekbar;
        controllerLayout = vcLayout;
        initialize();
    }
    void initialize(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int volume, boolean fromUser) {
                if (fromUser) seek(volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        setupAudioManager();
    }
    public void setupAudioManager() {
        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        cn = new ComponentName(activity, MediaButton.class);
        if (audioManager != null) audioManager.registerMediaButtonEventReceiver(cn);

    }

    public void onResume() {
        if (audioManager != null) audioManager.registerMediaButtonEventReceiver(cn);
    }
    public void onDestroy() {
        audioManager.unregisterMediaButtonEventReceiver(cn);
        audioManager = null;
    }






    private void seek(int volume) {
        setVolume(volume);
    }

    public void onVolumeChange() {

    }
    public void onToggleVolume() {
        //activity.sendBroadcast(new Intent().setAction("ToggleVolumeController"));
        isVisible = !isVisible;
        if (isVisible){
            seekBar.setProgress(getSystemVolume());
            controllerLayout.setVisibility(View.VISIBLE);
        }
        else controllerLayout.setVisibility(View.GONE);
    }



    public static void AdjustVolumeStep(int direction) {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0);
    }

    int getSystemVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
    public void setVolume(int level) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0);
    }

    public static class MediaButton extends BroadcastReceiver {
        //Context context;
        public MediaButton(){ super();}

        @Override
        public void onReceive(Context context, Intent i) {

            //this.context = context;

            KeyEvent event = (KeyEvent) i.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) return;
            if (event.getAction() == KeyEvent.ACTION_DOWN){
                switch (event.getKeyCode()){
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        togglePausePlay();
                        break;

                    case KeyEvent.KEYCODE_MEDIA_NEXT: next(); break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS: previous(); break;
                }
            }

            //toast(event.toString());
            //abortBroadcast();
        }

        private void togglePausePlay() {

        }

        private void next() { //Vol ++
            AdjustVolumeStep(AudioManager.ADJUST_RAISE);
        }

        private void previous() { //Vol --
            AdjustVolumeStep(AudioManager.ADJUST_LOWER);
        }


    }




}
