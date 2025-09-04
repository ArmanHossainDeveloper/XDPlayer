package app.arman.vidloader;
import java.io.ByteArrayOutputStream;

public class QuotedPrintable {

    public static byte[] decode(byte[] bytes) {
        final int length = bytes.length;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < length; i++) {
            int b = bytes[i];
            if (b == '=' && i+2 < length) {
                //Escape Situation
                int u = Character.digit((char) bytes[++i], 16);
                int l = Character.digit((char) bytes[++i], 16);
                if (u != -1 & l != -1) { buffer.write( (char) ((u << 4) + l) );}
            }
            else { buffer.write(b); }
        }
        return buffer.toByteArray();
    }

}