package media.xdplayer;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import java.io.File;

import app.arman.vidloader.UrlManager;
import app.arman.vidloader.UrlProgressCallback;
import arman.common.infocodes.InfoCode;
import arman.common.ui.DrawerActivity;

public class TempActivity extends DrawerActivity {
    public static WebView webview;
    //String playerPath = "storage/emulated/0/Browser/Browser/YoutubePlayer.html";
    String playerDir = InfoCode.SD_CARD + "/Browser/Browser/player";
    String audioPlayer = playerDir + "/Audio/AudioPlayer.html";
    String videoPlayer = playerDir + "/Video/VideoPlayer.html";
    String customizedPlayer = playerDir + "/Temp/TempPlayer.html";
    String[] vids;
    UrlManager urlManager;
    LinearLayout popupMenu;
    boolean isMenuOpen;
    boolean isLandscape = true;
    boolean isYtShorts = false;

    boolean shouldExit = false;

    public static AudioManager audioManager;
    ComponentName cn;
    File file;

    DownloadManager dm;
    long currentDownloadID;

    Handler handler;
    Runnable runnable;

    @Override
    protected int[] getRequiredPermission() {
        return new int[]{InfoCode.FILE_PERMISSION};
    }
    @Override
    protected void onCreate() {
        setContentView(R.layout.activity_temp);
        initialize();
        onSettingsChange();
        handleIntent();
    } // onCreate



    private void initialize() {
        urlManager = new UrlManager(new UrlProgressCallback() {
            @Override
            public void onShortVideo() {
                rotateScreen();
            }

            @Override
            public void onVideoPrepared(String videoUrl) {
                loadVideo(videoUrl);
            }
        });

        popupMenu = findViewById(R.id.popup_menu);
        webview = findViewById(R.id.web_view);
        webview.setHorizontalScrollBarEnabled(false);
        webview.setVerticalScrollBarEnabled(false);

        webview.setWebChromeClient(new WebChromeClient(){
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                toast(consoleMessage.message() + "at line:" + consoleMessage.lineNumber());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        webview.setWebViewClient(new WebViewClient(){});


        class WebAppInterface {
            @JavascriptInterface
            public void toggleRotation() {
                rotateScreen();
            }

            @JavascriptInterface
            public void setSystemVolumeLevel(int level) {
                //toast("" + level);
                setVolume(level);
            }

            @JavascriptInterface
            public int getSystemVolume() {
                return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            }
        }
        webview.addJavascriptInterface(new WebAppInterface(), "Android");

        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setDisplayZoomControls(false);
        settings.setBuiltInZoomControls(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setMediaPlaybackRequiresUserGesture(false);

        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        //currentDownloadID = preference.getLong("currentDownloadID", 0);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        cn = new ComponentName(this, MediaButton.class);
        handler = new Handler();
        runnable = this::load;
    }


    @Override
    protected void onSettingsChange() {
        currentDownloadID = preference.getLong("currentDownloadID", 0);
        enableSwipers(preference.getBoolean("enableSwipers", true));
    }

    @Override
    public void onRotate(boolean landscape) {
        //if (landscape) toast("Landscape");
        //else toast("Portrait");
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (audioManager != null) audioManager.registerMediaButtonEventReceiver(cn);
    }


    public void handleIntent(){
        Intent i = getIntent();
        String action = i.getAction();
        if (action == null) return;

        if (action.contains("action.SEND")) {
            isYtShorts = i.getBooleanExtra("isYtShorts", false);
            if (isYtShorts) rotateScreen();
            loadPlayer();
            loadVideo(i.getStringExtra(Intent.EXTRA_TEXT));
            //urlManager.setIsHd(i.getBooleanExtra("isHd", true));
            //urlManager.formatLink(i.getStringExtra(Intent.EXTRA_TEXT));

        }
        else if (action.contains("action.VIEW")) {
            webview.loadUrl("file://" + customizedPlayer);
        }

    }


    public void backPressed(View v){
        onBackPressed();
    }


    public void hideMenu(View v){
        hideMenu();
    }

    public void loadVideo(String url) {
        downloadVideo(url);
        load();
        //toast("" + file.exists());
        //new Handler().postDelayed(this::load, 6000);
    }

    private void load(){
        //refreshFile();
        //toast("" + file.exists());
        if (file.exists() && webview != null) {
            handler.postDelayed(()->{
                webview.loadUrl("file://" + customizedPlayer);
            }, 2000);

        }
        else handler.postDelayed(runnable, 2000);
        //toast("Waiting!");
    }


    void downloadVideo(String url){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        refreshFile();
        if (file.exists()) {
            if (currentDownloadID != 0 && dm != null) {
                if (dm.remove(currentDownloadID) > 0) {
                    currentDownloadID = 0;
                }
            }
            file.delete();
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "temp.mp4");
        currentDownloadID = dm.enqueue(request);
        saveCurrentDownloadID(currentDownloadID);
    }

    public void saveCurrentDownloadID(long ID) {
        SharedPreferences.Editor editor = preference.edit();
        editor.putLong("currentDownloadID", ID);
        editor.apply();
    }

    private void refreshFile() {
        file = new File(Environment.getExternalStorageDirectory(), "/Download/temp.mp4");
    }

    public void loadPlayer() {
        webview.loadUrl("file://" + playerDir + "/Video/LoadingScreen.html");
    }

    public void retry(View v) {
        hideMenu();
        //urlManager.retry();
    }

    public void reload(View v) {
        hideMenu();
        openMainActivity();
        //toast("Reloading");
        //webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        //webview.clearCache(true);
        //webview.reload();
        //refreshFile();
        //loadPlayer();
        //load();
    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class).setAction("action.VIEW").setData(Uri.parse("file://" + customizedPlayer));
        startActivity(intent);
    }

    public void toggleRotation(View v) {
        hideMenu();
        rotateScreen();
    }

    public void rotateScreen(){
        if (isLandscape) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        isLandscape = !isLandscape;
    }




    @Override
    public void onBackPressed() {
        if (shouldExit){
            hideMenu();
            exit(null);
        }
        else if (isMenuOpen){
            hideMenu();
        }
        else {
            shouldExit = true;
            openMenu();
            new Handler().postDelayed(this::revert, 500);
        }
    }



    public void cancelDownloads(View v){
        if (currentDownloadID != 0 && dm != null){
            if (dm.remove(currentDownloadID) > 0){
                currentDownloadID = 0;
                toast("Download Canceled");
            }
        }
        hideMenu();
    }

    public void exit(View v){
        handler.removeCallbacks(runnable);
        webview.destroy();
        webview = null;
        finishAndRemoveTask();
    }

    public void stopAndExit(View v){
        cancelDownloads(null);
        exit(null);
    }

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }*/

    void revert(){ shouldExit = false; }



    void hideMenu(){ setMenuVisibility(false); }
    void openMenu(){
        setMenuVisibility(true);
    }
    void setMenuVisibility(boolean visible){
        if (visible) popupMenu.setVisibility(View.VISIBLE);
        else popupMenu.setVisibility(View.GONE);
        isMenuOpen = visible;
    }

    public void onDestroy() {
        audioManager.unregisterMediaButtonEventReceiver(cn);
        audioManager = null;
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            webview.reload();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public static void volume(int direction) {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0);
        webview.loadUrl("javascript: setVolume(" + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + ");");
        //audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, direction));
    }

    public void setVolume(int level) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0);
        //audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, direction));
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
                        togglePlay();
                        break;

                    case KeyEvent.KEYCODE_MEDIA_NEXT: next(); break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS: previous(); break;
                }
            }
            InfoCode.log("onReceive");
            //toast(event.toString());
            //abortBroadcast();
        }

        private void togglePlay() {
            webview.loadUrl("javascript: pausePlay();");
        }

        private void next() { //Vol ++
            volume(AudioManager.ADJUST_RAISE);
        }

        private void previous() { //Vol --
            volume(AudioManager.ADJUST_LOWER);
        }



        void log(String text){
            InfoCode.log(text);
        }

    }


}
