package arman.networking;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class PersistencyManager {

    void storeRequests(Context context){
        String requestsText = getRequestsAsText();
        writeText(context, requestsText);

    }
    ArrayList<Request> rebuildRequests(Context context){
        ArrayList<Request> requests = new ArrayList<>();
        String requestsText = read(context, "downloads.txt");

        if (requestsText.contains("\n")) {
            String[] requestStrings = requestsText.split("\n\n");

            for (String rs : requestStrings) {
                String[] i = rs.split("\n");
                Request r = new Request(i[0], i[1], i[2], false, null);
                r.state = Byte.parseByte(i[3]);
                r.downloadedSize = i[4];
                r.totalSize = i[5];
                r.progress = Integer.parseInt(i[6]);
                r.length = Long.parseLong(i[7]);

                requests.add(r);
            }
        }
        return requests;
    }

    private String getRequestsAsText(){
        ArrayList<Request> requests = Downloader.getInstance().getRequests();
        StringBuilder model = new StringBuilder();
        for (Request r: requests){
            model.append(r.urlAddress);
            model.append("\n").append(r.fileName);
            model.append("\n").append(r.path);
            model.append("\n").append(r.state);
            model.append("\n").append(r.downloadedSize);
            model.append("\n").append(r.totalSize);
            model.append("\n").append(r.progress);
            model.append("\n").append(r.length);
            model.append("\n\n");
        }

        return model.toString().trim();
    }

    private void log(String text){
        Log.e("TestTag", text);

    }

    private String read(Context context, String fileName){
        File file = new File(context.getFilesDir(), fileName);
        StringBuilder text = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = br.readLine()) != null) { text.append(line).append("\n"); }
        }
        catch (IOException ignored) {}
        return text.toString().trim();
    }

    private void writeText(Context context, String content){
        File file = new File(context.getFilesDir(), "downloads.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))){
            bw.write(content);
        }
        catch (Exception ignored) {}
    }

}
