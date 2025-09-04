package arman.networking;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class Downloader {

    // SingleTon
    private static Downloader instance;
    private Downloader(){}
    public static Downloader getInstance(){
        if (instance != null) return instance;
        return instance = new Downloader();
    }

    PersistencyManager persistency = new PersistencyManager();
    NetworkClient client = new NetworkClient();
    ArrayList<Request> requests = new ArrayList<>(),
                       queuedRequests = new ArrayList<>();

    //Request newRequest;
    //boolean isNewRequest;
    boolean autoStart = true;

    public void build(String urlAddress, String name, String path, boolean overwrite, HashMap<String, String> requestHeaders){
        Request newRequest = new Request(urlAddress, name, path, overwrite, requestHeaders);
        requests.add(newRequest);
        autoStart(newRequest);
        //isNewRequest = true;
    }

    public void attachListener(){
        client.attachListener();
    }
    public void disconnectListener(){
        client.disconnectListener();
    }

    public void forceStop(){
        if (client != null) client.forceStop();

    }

    void autoStart(Request r){
        if (client == null || !autoStart) return;
        if (client.isRunning()){
            r.state = 1;
            queuedRequests.add(r);
            return;
        }
        resume(r);
    }

    /*void start(){
        if (!isNewRequest || client.isRunning()) return;
        isNewRequest = false;
        resume(newRequest);
    }*/

    public ArrayList<Request> getRequests(){
        return requests;
    }
    /*public ArrayList<Request> getRebuiltRequests(){
        return rebuiltRequests;
    }*/

    public boolean rebuildDownloads(Context context){
        ArrayList<Request> rebuiltRequests = persistency.rebuildRequests(context);
        if (rebuiltRequests.isEmpty()) return false;
        requests.addAll(rebuiltRequests);
        return true;
    }
    public void storeDownloads(Context context){
        client.exit();
        persistency.storeRequests(context);
    }
    public void onTaskFinished(Request r){
        queuedRequests.remove(r);
        if (queuedRequests.isEmpty()) return;
        resume(queuedRequests.get(0));
    }

    public void nullify(){
        client.exit();
        client = null;
        persistency = null;
        requests = null;
        instance = null;
    }
    public boolean remove(Request r){
        if (r.state == 1 || r.state == 2) return false;
        return requests.remove(r);
    }

    public boolean pauseResume(Request r){
        if (client == null) return false;

        switch (r.state){
            case 0: // Paused
                if (client.isRunning()) queue(r);
                else resume(r);
                break;

            case 1: // Queued
                removeQueue(r); // remove Queue
                break;

            case 2: // Running
                 pause(); // pause it
                break;
        }

        return true;
    }

    /*public boolean restart(Request r){
        r.overwrite = true; // Must Allow Overwriting to Restart.
        return r.overwrite = resume(r); // Don't allow Overwriting, if problem occurs.

    }*/

    private void pause(){
        log("Pause()");
        client.forceStop();

    }
    private void removeQueue(Request r){
        queuedRequests.remove(r);
        r.state = 0;
        r.callback.onPause();
    }
    private void queue(Request r){
        r.state = 1;
        queuedRequests.add(r);
        r.callback.onQueued();
    }
    private void resume(Request r){
        client.execute(r);

    }

    void log(String text){
        Log.e("TestTag", text);

    }



}
