package app.arman.vidloader;

import java.io.InputStream;
import java.util.ArrayList;

public abstract class NetInterface {
    void OnDownloadFinished(byte[] byteArray){}
    void OnParsingFinished(ArrayList<Anchor> anchors){}
    void OnInfoReceived(int[] sizes){}
    void OnError(String message){}
}
