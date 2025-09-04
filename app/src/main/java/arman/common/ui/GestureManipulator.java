package arman.common.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.Toast;

import arman.common.infocodes.InfoCode;

public abstract class GestureManipulator {

    private View controller;
    private View target;
    private float scaleFactor = 1.0f;
    //private float dx = 0, dy = 0; // Pan distances
    //private float minX, maxX, minY, maxY;
    private Matrix matrix = new Matrix();
    int targetWidth, targetHeight, parentWidth, parentHeight;
    boolean isZoomPanFrozen = false, isFill = false;
    float zeroX = 0;
    float zeroY = 0;

    Scroller scroller;


    public GestureManipulator(View v){
        controller = target = v;
        init(v.getContext());
    }
    public GestureManipulator(View controller, View target){
        this.controller = controller;
        this.target = target;
        init(controller.getContext());
    }
    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context){
        scroller = new Scroller(context);
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        GestureDetector gestureDetector = new GestureDetector(context, new GestureListener());

        controller.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        public boolean onScale(ScaleGestureDetector detector) {
            return onZoom(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());

        }
    }
    private class GestureListener extends android.view.GestureDetector.SimpleOnGestureListener {
        public boolean onSingleTapUp(MotionEvent e) {
            onClick((int) e.getX(), (int) e.getY());
            return true;
        }
        public void onLongPress(MotionEvent e) {
            onLongClick((int) e.getX(), (int) e.getY());
        }
        public boolean onDoubleTap(MotionEvent e) {
            onDoubleClick((int) e.getX(), (int) e.getY());
            return true;
        }
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {

            onPan(-dx, -dy);

            /*float x = target.getX();
            float y = target.getY();
            target.scrollBy((int) dx, (int) dy);*/

            return true;
        }
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            GestureManipulator.this.onFling(velocityX, velocityY);
            return true;
        }
    }


    public void onClick(int x, int y){

    }
    public void onLongClick(int x, int y){

    }
    public void onDoubleClick(int x, int y){

    }

    public boolean onZoom(float factor, float focusX, float focusY){
        if (isZoomPanFrozen) return true;

        float minFactor = 1.0f / scaleFactor;
        float maxFactor = 50.0f / scaleFactor;
        factor = Math.max(minFactor, Math.min(factor, maxFactor));

        float newScale = factor * scaleFactor;
        if (scaleFactor != newScale) {
            scaleFactor = newScale;
            matrix.postScale(factor, factor, focusX, focusY);
            if (InfoCode.sdk > 29) target.setAnimationMatrix(matrix);
            return true;
        }
        return false;
    }

    public void onPan(float dx, float dy){
        if (isZoomPanFrozen) return;

        float width = targetWidth * scaleFactor;
        float height = targetHeight * scaleFactor;

        RectF rect = new RectF();
        matrix.mapRect(rect);
        float matrixX = rect.left;
        float matrixY = rect.top;

        float sizeDiffX = width - parentWidth;
        float sizeDiffY = height - parentHeight;
        float halfBlankX = sizeDiffX / 2;
        float halfBlankY = sizeDiffY / 2;

        float left = zeroX - matrixX;
        float right = sizeDiffX - left;
        float top = zeroY - matrixY;
        float bottom = sizeDiffY - top;

        if (width <= parentWidth) dx = zeroX - halfBlankX - matrixX;
        else if (dx > left) dx = left;
        else if (dx < -right) dx = -right;

        if (height <= parentHeight) dy = zeroY - halfBlankY - matrixY;
        else if (dy > top) dy = top;
        else if (dy < -bottom) dy = -bottom;

        /*log("parentWidth: " + parentWidth + ", parentHeight: " + parentHeight);
        log("width: " + width + ", height: " + height);
        log("dx: " + dx + ", dy: " + dy);*/

        matrix.postTranslate(dx, dy);
        if (InfoCode.sdk > 29) target.setAnimationMatrix(matrix);
    }

    public void onFling(float vx, float vy){
        /*scroller.fling(target.getScrollX(), target.getScrollY(), (int) vx, (int) vy, 0, 1000, 0, 1000);
        target.scrollTo(scroller.getCurrX(), scroller.getCurrY());
        target.postInvalidate();*/


    }

    public void setSize(int tw, int th) {
        parentWidth = tw;
        parentHeight = th;
        targetWidth = tw;
        targetHeight = th;
        zeroX = 0;
        zeroY = 0;


    }
    public void setSize(int pw, int ph, int tw, int th) {
        parentWidth = pw;
        parentHeight = ph;
        targetWidth = tw;
        targetHeight = th;
        zeroX = -(parentWidth - targetWidth) / 2.0f;
        zeroY = -(parentHeight - targetHeight) / 2.0f;


    }

    public void toggleFreezeZoomPan(){
        isZoomPanFrozen = !isZoomPanFrozen;
    }
    public void toggleFillCrop(){
        isFill = !isFill;
        boolean isFrozen =  isZoomPanFrozen;
        if (isFrozen) isZoomPanFrozen = false;

        float fx = targetWidth / 2.0f;
        float fy = targetHeight / 2.0f;

        float width = targetWidth * scaleFactor;
        float height = targetHeight * scaleFactor;
        float widthFactor = (float) parentWidth / targetWidth;
        float heightFactor = (float) parentHeight / targetHeight;

        float factor = 1/*Math.min(widthFactor, heightFactor)*/ / scaleFactor;
        if (isFill) factor = Math.max(widthFactor, heightFactor) / scaleFactor;

        onZoom(factor, 0, 0);
        onPan(0, 0);
        isZoomPanFrozen = isFrozen;
    }

    /*public void onRotate(){
        int i = parentWidth;
        parentWidth = parentHeight;
        parentHeight = i;
        i = targetWidth;
        targetWidth = targetHeight;
        targetHeight = i;
    }*/

    void log(String text){
        InfoCode.log(text);
    }
    void toast(String text){
        Toast.makeText(target.getContext(), text, Toast.LENGTH_SHORT).show();
    }






}





















