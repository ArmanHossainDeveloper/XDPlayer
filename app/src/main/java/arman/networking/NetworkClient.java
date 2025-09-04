package arman.networking;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

public class NetworkClient {
    Request request;
    DownloadListener callback;
    Thread thread;

    boolean unknownLength = false;
    boolean hasListener = true;
    HttpURLConnection connection = null;
    BufferedInputStream bis = null;


    byte execute(Request request) {
        if (isRunning()) { return -1; }
        this.request = request;
        callback = request.callback;
        if (callback == null) {
            hasListener = false;
            //callback = getDummyListener(); // keep the download going
            //pause();
            //log("Provide a DownloadListener");
            //return false; // force them to provide a Listener
        }
        thread = new Thread(this::download);
        thread.start();
        return request.state = 2;
    }

    DownloadListener getDummyListener(){
        return new DownloadListener() {
            public void onPause() {}
            public void onResume() {}
            public void onQueued() {}
            public void onRestart() {}
            public void onReceivedResponse(String response) {}
            public void onReceivedLength(String text) {}
            public void onProgress(String text, int progress) {}
            public void onMessage(String message) {}
            public void onFinished(String result) {}
            public void onError(String error) {}
        };
    }

    boolean isRunning(){
        return request != null && request.state == 2;
    }
    /*boolean interruptThread(){
        thread.interrupt();
        return thread.isInterrupted();
    }*/

    private void onPause() {
        if (!hasListener || callback == null) return;
        callback.onPause();
    }
    private void onResume() {
        request.logs += "Preparing...\n";
        if (!hasListener || callback == null) return;
        callback.onResume();

    }
    private void onQueued() {
        request.logs += "Queued\n";
        if (!hasListener || callback == null) return;
        callback.onQueued();

    }
    private void onRestart() {
        request.logs += "Restarted\n";
        if (!hasListener || callback == null) return;
        callback.onRestart();

    }
    private void onProgress(String text, long progress){
        request.downloadedSize = text;
        int pg = unknownLength ? 0 : (int) (progress * 100 / request.length);
        request.progress = pg;
        if (!hasListener || callback == null) return;
        callback.onProgress(text, pg);

    }
    private void onReceivedResponse(String response){
        request.logs += response + "\n";
        if (!hasListener || callback == null) return;
        callback.onReceivedResponse(response);

    }
    private void onReceivedLength(String length){
        request.totalSize = length;
        if (!hasListener || callback == null) return;
        callback.onReceivedLength(length);

    }
    private void onMessage(String message){
        request.logs += message + "\n";
        //if (hasNoListener) return;
        //callback.onMessage(message));

    }
    private void onFinished(String result){
        request.logs += "Download Finished\n";
        if (!hasListener || callback == null) return;
        callback.onFinished(result);

    }
    private void onError(String error){
        request.logs += error + "\n";
        if (!hasListener || callback == null) return;
        callback.onError(error);

    }

    private void download(){
        onResume();
        BufferedOutputStream bos = null;
        try {
            URL url = new URL(request.urlAddress);
            connection = (HttpURLConnection) url.openConnection();
            //connection.setReadTimeout(1000 * 10); //10s
            connection.setConnectTimeout(1000 * 10); //10s
            //connection.setInstanceFollowRedirects(true);
            boolean overwrite = request.overwrite;
            File file = request.file;
            long downloadedBytes = 0;
            if (!overwrite && file.exists()) {
                downloadedBytes = file.length();
                final String range = String.format(Locale.ENGLISH, "bytes=%d-", downloadedBytes);
                connection.setRequestProperty("Range", range);
            }
            if (!request.headers.isEmpty()){
                for (String name : request.headers.keySet()){
                    connection.setRequestProperty(name, request.headers.get(name));
                }
            }
            //connection.setRequestProperty(Constants.USER_AGENT, getUserAgent);
            connection.setRequestProperty("Accept-Encoding", "identity");

            if (shouldStop()) return;
            connection.connect();


            log("Getting Header");
            Map<String, List<String>> map = connection.getHeaderFields();
            StringBuilder builder = new StringBuilder();
            for (String key : map.keySet()){
                builder.append(key).append(": ");
                for (String value : map.get(key)){
                    builder.append("\n\t\t").append(value);
                }
                builder.append("\n\n");
            }
            onReceivedResponse(builder.toString());

            if (shouldStop()) return;

            int responseCode = connection.getResponseCode();
            if (responseCode >= 400){
                log("Response Error");
                pause();
                onError(responseCode + " " + connection.getResponseMessage());
                return;
            }
            if (responseCode >= 300 ){
                request.urlAddress = connection.getHeaderField("Location");
                download();
                return;
            }
            /*if (responseCode < 200 || responseCode >= 300){
                onError(responseCode + " " + connection.getResponseMessage());
                return;
            }*/
            boolean isResumeSupported = !overwrite && file.exists() && (map.containsKey("Accept-Ranges") ||
                    map.containsKey("Content-Range") || responseCode == 206);
            if (request.length <= 0){
                request.length = connection.getContentLength();
            }
            unknownLength = request.length <= 0;
            onReceivedLength(unknownLength ? "Unknown" : getSize(request.length) );

            bis = new BufferedInputStream(connection.getInputStream());
            //onMessage(isResumeSupported ? "Resume Supported" : "Damn it! Resume NOT Supported" );
            FileOutputStream fos = new FileOutputStream(file, isResumeSupported);
            bos = new BufferedOutputStream(fos);
            byte[] buffer = new byte[1024*256]; // 256KB buffer
            int len;

            if (shouldStop()) return;

            while ((len = bis.read(buffer)) > 0){ // fixed Buffer
                if (shouldStop()) return;

                bos.write(buffer,0 , len);
                onProgress(getSize(downloadedBytes += len), downloadedBytes);

                if (shouldStop()) return;

            }

            if (isRunning()) {
                finished(getSize(downloadedBytes));
            }
        }
        catch (Exception e) {
            if (request.state == 0){
                log("Caught Exception: Paused by Force");
                onPause();
            }
            else {
                pause();
                log("Unexpected Error");
                onError(e.getMessage());
            }
        }
        finally {
            terminate(connection, bis, bos);
            log("stopped: " + request.fileName);
            Downloader.getInstance().onTaskFinished(request);
        }
    }
    private boolean shouldStop(){
        if (thread.isInterrupted()) pause();
        if (request.state == 0){
            log("Should Stop");
            onPause();
            return true;
        }
        return false;
    }


    private void terminate(HttpURLConnection connection, BufferedInputStream bis, BufferedOutputStream bos){
        if (connection != null) connection.disconnect();
        if (bis != null) {
            try { bis.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
        if (bos != null)
            try {
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        //if (request.state != 3) pause();
        onMessage("Thread Terminated");
    }
    String getSize(long size){
        if (size < 1024) return size + "B";
        else if (size < 1024*1024) {
            //double dSize = roundOff(size/1024.0);
            return roundOff(size/1024.0) + "KB";
        }
        else if (size < 1024*1024*1024) {
            //double dSize = roundOff(size/(1024*1024.0));
            return roundOff(size/(1024*1024.0)) + "MB";
        }
        //double dSize = roundOff(size/(1024*1024*1024.0));
        return roundOff(size/(1024*1024*1024.0)) + "GB";
    }
    double roundOff(double a){
        return Math.round(a*100.0) / 100.0;
    }

    void forceStop(){
        new Thread(()-> {
            pause();
            if (connection != null) connection.disconnect();
        }).start();
    }

    void attachListener(){
        if (request != null) {
            callback = request.callback;
            hasListener = true;
        }
    }

    void disconnectListener(){
        hasListener = false;
        //if (request != null) request.callback = null;
        callback = null;
    }
    void pause(){
        request.state = 0;
    }

    void exit(){
        if (request == null) return;
        forceStop();
    }
    private void finished(String text){
        request.state = 3;
        onFinished(text);
    }
    void log(String text){
        Log.e("TestTag", text);

    }

}
