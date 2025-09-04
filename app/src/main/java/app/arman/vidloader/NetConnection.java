package app.arman.vidloader;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetConnection {

    //InputStream mInputStream = null;
    NetInterface taskCallBack;
    void getInputStream(String url, NetInterface callback){
        taskCallBack = callback;
        new DownloadTask().execute(url);
    }

    void getFileSize(String[] urlString, NetInterface callback){
        taskCallBack = callback;
        new UrlLength().execute(urlString);
    }

    class UrlLength extends AsyncTask<String, Void, int[]>{
        protected int[] doInBackground(String... strings) {
            int[] sizes = new int[2];
            for (int i = 0; i < 2; i++){
                if (strings[i] != null){
                    try {
                        sizes[i] = new URL(strings[i]).openConnection().getContentLength();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sizes;
        }
        protected void onPostExecute(int[] ints) {
            taskCallBack.OnInfoReceived(ints);
        }
    }

    class DownloadTask extends AsyncTask<String, Void, byte[]>{

        String error = "Unknown Error";
        protected byte[] doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    return getByteArrayFromNonFixed(connection.getInputStream());
                }
                else {
                    error = "Error! Response Code: " + connection.getResponseCode();
                }
            } catch (IOException e) {
                error = e.getLocalizedMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] byteArray) {
            if (byteArray == null){ taskCallBack.OnError(error); }
            else {taskCallBack.OnDownloadFinished(byteArray);}
            log("Done");
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
    }



    void log(String text){
        Log.d("TestTag", ""+text);
    }

}
