package media.xdplayer;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class GestureDetector implements View.OnTouchListener {

    int progress;
    int volume;
    int middle;
    int brightness;

    private int width;
    private int height;
    boolean isOnClick;
    int actionDownX;
    int actionDownY;
    int previousX;
    int previousY;
    /*int difX;
    int difY;*/

    @Override
    public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        actionDownX = (int) event.getX();
                        actionDownY = (int) event.getY();
                        isOnClick = true;
                        return true;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (isOnClick) onClick();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int eventX = (int) event.getX();
                        int eventY = (int) event.getY();
                        if (isOnClick && (Math.abs(actionDownX - eventX) > 20 || Math.abs(actionDownY - eventY) > 20 )){
                            isOnClick = false;
                            width = v.getWidth();
                            height = v.getHeight();
                            previousX = actionDownX;
                            previousY = actionDownY;
                        }
                        if (!isOnClick){
                            int deltaX = eventX - previousX;
                            int deltaY = eventY - previousY;
                            if (Math.abs(deltaX) > 10){
                                onSwipeHorizontal(deltaX);
                                previousX = eventX;
                            }
                            if (Math.abs(deltaY) > 10 ){
                                onSwipeVertical(deltaY);
                                previousY = eventY;
                            }
                        }

                        return true;
                }
        return false;
    }
    /*
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //onToggleSeekbar();
        //InfoCode.log(event.toString());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        mDownX = event.getX();
                        mDownY = event.getY();
                        isOnClick = true;
                        return true;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (isOnClick) {
                            if (mDownY > progress) onToggleSeekbar();
                            else if (mDownX > volume) onToggleVolume();
                            else if (mDownX > middle) onToggleForward();
                            else if (mDownX > brightness) onToggleBackward();
                            else onToggleBrightness();
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (isOnClick && (Math.abs(mDownX - event.getX()) > 20 || Math.abs(mDownY - event.getY()) > 20 )){
                            isOnClick = false;
                        }
                        if (!isOnClick){
                            int difX = (int) (event.getRawX() - initialTouchX);
                            int difY = (int) (event.getRawY() - initialTouchY);
                            int deltaY = difY * 100 / height;
                            if (mDownY > progress) onSeekDelta(difX * 100 / width);
                            else if (mDownX > volume) onVolumeDelta(deltaY);
                            else if (mDownX > middle) onBoostDelta(deltaY);
                            else if (mDownX > brightness) onSpeedDelta(deltaY);
                            else onBrightnessDelta(deltaY);
                        }
                        return true;
                }
        return false;
    }*/

    public void calibrateLayout(LinearLayout gestureLayout){
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            width = gestureLayout.getWidth();
            height = gestureLayout.getHeight();
            progress = (int) (height * 0.8);
            volume = (int) (width * 0.8);
            middle = (int) (width * 0.5);
            brightness = (int) (width * 0.2);
            //InfoCode.log("x : " + width + " y : " + height);
        }, 2000);
    }
    public void onToggleSeekbar(){}
    public void onToggleVolume(){}
    public void onToggleForward(){}
    public void onToggleBackward(){}
    public void onToggleBrightness(){}
    public void onSeekDelta(int percent){}
    public void onVolumeDelta(int percent){}
    public void onBoostDelta(int percent){}
    public void onSpeedDelta(int percent){}
    public void onBrightnessDelta(int percent){}

    public void onClick(){}
    public void onSwipeVertical(int deltaY){}
    public void onSwipeHorizontal(int deltaX){}
    public void onSwipeUp(){}
    public void onSwipeDown(){}
    public void onSwipeLeft(){}
    public void onSwipeRight(){}
}
