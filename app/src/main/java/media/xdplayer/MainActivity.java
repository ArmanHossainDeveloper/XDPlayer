package media.xdplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import app.arman.vidloader.UrlManager;
import app.arman.vidloader.UrlProgressCallback;
import arman.common.infocodes.InfoCode;
import arman.common.ui.DrawerActivity;

public class MainActivity extends DrawerActivity {
    public static WebView webview;
    //String playerPath = "storage/emulated/0/Browser/Browser/YoutubePlayer.html";
    String playerDir = InfoCode.SD_CARD + "/Browser/Browser/player";
    String audioPlayer = playerDir + "/Audio/AudioPlayer.html";
    String videoPlayer = playerDir + "/Video/VideoPlayer.html";
    String[] vids;
    UrlManager urlManager;
    boolean isLandscape = true;
    boolean isYtShorts = false;
    public static AudioManager audioManager;
    ComponentName cn;


    @Override
    protected int[] getRequiredPermission() {
        return new int[]{InfoCode.FILE_PERMISSION};
    }
    @Override
    protected void onCreate() {
        setContentView(R.layout.activity_main);
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
        webview = findViewById(R.id.the_viewer);
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
        /*IntentFilter i = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        i.setPriority(10000);
        registerReceiver(new MediaButton(), i);*/
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        cn = new ComponentName(this, MediaButton.class);
        if (audioManager != null) audioManager.registerMediaButtonEventReceiver(cn);
    }

    @Override
    protected void onSettingsChange() {
        //enableSwipers(true);
        enableSwipers(preference.getBoolean("enableSwipers", true));
    }

    @Override
    public void onRotate(boolean landscape) {
        //if (landscape) toast("Landscape");
        //else toast("Portrait");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

        Intent open = new Intent(this, XDPlayerActivity.class);
        open.setAction(i.getAction())
                .setData(i.getData())
                .putExtra("isYtShorts", i.getBooleanExtra("isYtShorts", false))
                .putExtra(Intent.EXTRA_TEXT, i.getStringExtra(Intent.EXTRA_TEXT));

        startActivity(open);
        finish();
    }


    public void handleIntent2(){
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
            if (i.getData() == null) return;
            String data = i.getData().toString().replaceAll("#", "%23").replaceAll("'", "%27");

            if(data.endsWith(".html")){
                webview.loadUrl(data);
            }
            else {
                if (data.endsWith(".mp4")) loadVideo(data);
                else {
                    loadMedia(audioPlayer, data);
                    rotateScreen();
                }
            }
        }

    }

    public void loadVideo(String data) {
        loadMedia(videoPlayer, data);

    }

    public void loadMedia(String player, String src) {
        String type = "video";
        if (player.equals(audioPlayer)) type = "audio";
        String data = read(player);
        int s = data.indexOf("<" + type);
        int e = data.indexOf("</" + type);
        String target = data.substring(s, e);

        boolean success = writeText(player, data.replace(target, "<" + type + " src='" + src + "' preload='metadata' poster='loader.gif' autoplay>"));
        if (success){ webview.loadUrl("file://" + player); }
        else { toast("Failed to Play!");}
    }

    String read(String path){
        String logs = "";
        try (BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;
            while ((line = br.readLine()) != null) { logs += line + "\n"; }
        }
        catch (Exception ignored) { }
        return logs;
    }

    boolean writeText(String path, String text){
        try (FileWriter fw = new FileWriter(path)){
            fw.write(text);
            return true;
        }
        catch (Exception ignored) {}
        return false;
    }

    public void loadPlayer() {
        webview.loadUrl("file://" + playerDir + "/Video/LoadingScreen.html");
    }

    public void reload(View v) {
        toast("Reloading");
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webview.reload();
    }

    public void toggleRotation(View v) {
        rotateScreen();
    }

    public void rotateScreen(){
        if (isLandscape) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        isLandscape = !isLandscape;
    }



    public void exit(View v){
        webview.destroy();
        webview = null;
        finishAndRemoveTask();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }





    /*
    public void copyLink(View v) {
        copyToClipboard(getUrl());
        toast("Link Copied");
    }
    public void saveFavouriteVid(View v) {
        shouldClear = !shouldClear;
        if (shouldClear){ webview.loadUrl(cleaner); }
        else { webview.loadUrl(controls); }
    }
    public void openYoutubeGo(View v) {
        openOtherApp(getUrl(), "com.google.android.apps.youtube.mango", "com.google.android.apps.youtube.lite.frontend.activities.main.MainActivity");
    }
    void openOtherApp(String url, String app, String activity){
        openOtherApp(Intent.ACTION_VIEW, url, app, activity);
    }
    void openOtherApp(String action, String url, String app, String activity){
        Intent i = new Intent(action, Uri.parse(url));
        ComponentName cn = new ComponentName(app, activity);
        i.setComponent(cn);
        startActivity(i);
    }
    public void copyToClipboard(String data){
        ClipData clipData = ClipData.newPlainText("lebel", data);
        clipboard.setPrimaryClip(clipData);
    }
    */


    @Override
    protected void onPause() {
        super.onPause();
        //audioManager.unregisterMediaButtonEventReceiver(cn);
    }

    public void onDestroy() {
        audioManager.unregisterMediaButtonEventReceiver(cn);
        audioManager = null;
        super.onDestroy();
    }


    /*@Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            toast("vol-1");
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }*/

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
            log("onReceive");
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
            Log.e("TestTag", ""+text);
        }

        //void toast(String text){
            //Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        //}

    }

}
