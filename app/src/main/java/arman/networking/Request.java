package arman.networking;

import java.io.File;
import java.util.HashMap;

public class Request {
    public String fileName;
    public String path;
    public String downloadedSize = "0B";
    public String totalSize = "Unknown";
    public int progress;
    public long length;
    public String logs = "";

    public String urlAddress;
    File file;
    HashMap<String, String> headers = new HashMap<>();
    DownloadListener callback;

    boolean overwrite;
    public byte state = 0;

    Request(String urlAddress, String name, String path, boolean overwrite, HashMap<String, String> headers){
        this.urlAddress = urlAddress;
        fileName = name;
        this.path = path;

        file = new File(path+name);
        File dir = file.getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
        this.overwrite = overwrite;
        //this.autoStart = autoStart;
        if (headers != null) this.headers = headers;
    }

    public void setDownloadListener(DownloadListener callback) {
        this.callback = callback;
        //if (autoStart) Downloader.getInstance().start();
    }

}
