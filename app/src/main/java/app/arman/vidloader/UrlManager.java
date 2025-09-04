package app.arman.vidloader;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class UrlManager {
    final String clipMegaAD = "%20-%20Downloaded%20from%20";
    Context context;
    Button retryBtn;
    ProgressBar progress;
    String hdVid = "";
    String sdVid = "";
    String youtubeLink;
    UrlProgressCallback progressCallback;
    boolean isHd = true;

    public UrlManager(UrlProgressCallback callback) {
        progressCallback = callback;

    }

    public void setIsHd(boolean b){
        isHd = b;
    }


    public void retry(){
        retryBtn.setVisibility(View.GONE);
        prepare(youtubeLink);
    }

    void prepare(String url){
        downloadList(url);
        //progress.setVisibility(View.VISIBLE);

    }

    NetInterface netInterface = new NetInterface() {
        void OnParsingFinished(ArrayList<Anchor> anchors) {
            for (Anchor anchor: anchors){
                if (anchor.content.contains("720")){
                    hdVid = anchor.href;
                    if (isHd) {
                        progressCallback.onVideoPrepared(hdVid);
                        return;
                    }
                }
                else if (anchor.content.contains("360") ){
                    sdVid = anchor.href;
                    if (!isHd) {
                        progressCallback.onVideoPrepared(sdVid);
                        return;
                    }
                }
            }
            String video = hdVid, quality = "SD";
            if (isHd) {
                video = sdVid;
                quality = "HD";
            }
            progressCallback.onVideoPrepared(video);
            toast(quality + " Unavailable!");
        }



        void OnError(String message) {
            //progress.setVisibility(View.GONE);
            //retryBtn.setVisibility(View.VISIBLE);
            toast("Connection Failed. Retry!");
        }

    };

    void downloadList(String url){
        toast("Preparing Video");
        new DownloadableVidList().getListFromUrlString(url, netInterface);
    }

    void downloadVideo(String url, String quality){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getVideoName(url));

        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        dm.enqueue(request);
        toast("Downloading " + quality);
    }

    double roundOff(double a){
        return Math.round(a*100.0) / 100.0;

    }
    String getSize(int size){
        if (size < 1024) return size+" Byte";
        else if (size < 1024*1024) {
            return roundOff(size/1024.0) + " KB";
        }
        else if (size < 1024*1024*1024) {
            return roundOff(size/(1024*1024.0)) + " MB";
        }
        return roundOff(size/(1024*1024*1024.0)) + " GB";
    }
    String getVideoName(String url){
        String name = url.substring(url.indexOf("&title=") + 7, url.indexOf(clipMegaAD));
        if (name.length() > 70){
            name = name.substring(0, 70);
        }
        name = name.replace("'","");
        name = name.replaceAll("\\W", " ").trim();

        return name+".mp4";
    }
    public void formatLink(String link) {
        if (link != null) {
            if (link.contains("youtu.be/")) {
                youtubeLink = link.replace("youtu.be/", "clipmega.com/watch?v=");
            } else if (link.contains("m.youtube.com")) {
                youtubeLink = link.replace("m.youtube.com", "clipmega.com");
            } else if (link.contains("youtube.com")) {
                youtubeLink = link.replace("youtube.com", "clipmega.com");
            }
            if (youtubeLink.contains("clipmega.com")) {
                if (youtubeLink.contains("shorts/")) progressCallback.onShortVideo();
                prepare(youtubeLink.replace("shorts/", "watch?v="));
            }
        }
    }
    void log(String text){
        Log.e("TestTag", ""+text);
    }
    void toast(String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

    }

}