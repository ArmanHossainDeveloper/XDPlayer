package arman.networking;

public interface DownloadListener {
    void onPause();
    void onResume();
    void onQueued();
    void onRestart();
    void onReceivedResponse(String response);
    void onReceivedLength(String length);
    void onProgress(String downloaded, int progressPercentage);
    void onMessage(String message);
    void onFinished(String result);
    void onError(String error);
}
