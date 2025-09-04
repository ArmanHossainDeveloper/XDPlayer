package app.arman.vidloader;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class DownloadableVidList {

     void getListFromUrlString(String urlString, final NetInterface parsingFinishedCallback) {
        new NetConnection().getInputStream(urlString, new NetInterface() {
            public void OnDownloadFinished(byte[] byteArray) {
                ArrayList<Anchor> anchors = getListFromByteArray(byteArray);
                if (anchors.size() > 0){
                    parsingFinishedCallback.OnParsingFinished(anchors);
                }
                else parsingFinishedCallback.OnError("Error");
            }
            void OnError(String message) {
                parsingFinishedCallback.OnError(message);
            }
        });
    }

    ArrayList<Anchor> getListFromInputStream(InputStream inputStream) {
        try {
            if (inputStream != null){
            return getListFromByteArray(getByteArrayFromFixed(inputStream));
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    ArrayList<Anchor> getListFromByteArray(byte[] byteArray) {
        ArrayList<Anchor> anchors = new ClipMegaAnchor().parseAnchors(byteArray);
        ArrayList<Anchor> downloadVid = new ArrayList<>();
        for (Anchor anchor: anchors){
            if (anchor.href.startsWith("http")){
                downloadVid.add(anchor);
            }
        }
        return downloadVid;
    }

    byte[] getByteArrayFromNonFixed(InputStream is) throws IOException {
        int nRead;
        byte[] data;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        while ((nRead = is.read(data = new byte[is.available()], 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
            //log(data.length);
        }
        buffer.flush();

        return buffer.toByteArray();
    }

    byte[] getByteArrayFromFixed(InputStream inputStream) throws IOException {
        byte[] targetArray = new byte[inputStream.available()];
        inputStream.read(targetArray);
        return targetArray;
    }
    void log(int i){
        Log.d("InputStream", "Length: "+i);
    }

}
