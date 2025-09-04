package app.arman.vidloader;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ClipMegaAnchor {

    byte[] bytes;
    int i = 0;
    ArrayList<Anchor> anchors = new ArrayList<>();
    long len;

    ArrayList<Anchor> parseAnchors(byte[] byteArray) {
        bytes = byteArray;
        len = bytes.length;
        while (i < len) {
            while (i < len && bytes[i] != 60) {
                //we   are NOT inside a tag
                i++;
            }
            // opening bracket started
            i++;
            if (i < len && bytes[i] == 97 && bytes[i + 1] == 32) {
                // Anchor tag Started
                // a in hand
                i += 2;
                addAnchorTag();
            }
        }
        return anchors;
    }

    void addAnchorTag() {
        Anchor anchor = new Anchor();
        while (hasNextProperty()) {
            if (!getNextProperty(anchor)){ return; }
        }
        // ending bracket in hand
        i++;
        getInnerText(anchor);
        anchors.add(anchor);
    }

    boolean hasNextProperty() {
        skipSpace();
        return bytes[i] != 62; // 62 = ending bracket >
    }

    boolean getNextProperty(Anchor anchor) {
        ByteArrayOutputStream propertyKey = new ByteArrayOutputStream();
        while (bytes[i] != 61 && bytes[i] != 32 && bytes[i] != 62) {  // Until Equals or Space starts, we read
            propertyKey.write(bytes[i++]);
        }

        ByteArrayOutputStream propertyValue = null;
        if (bytes[i] == 61) { // if Equal Sign
            propertyValue = new ByteArrayOutputStream();
            int quot = bytes[++i];
            while (bytes[++i] != quot) {  // Until quot, we read
                if (bytes[i] != 32){
                    propertyValue.write(bytes[i]);
                }
                else {
                    propertyValue.write(37);
                    propertyValue.write(50);
                    propertyValue.write(48);
                }
            }
        }
        // quot in hand
        i++;
        String value = propertyValue == null ? "" : propertyValue.toString();
        switch (propertyKey.toString()) {
            case "href":
                if(value.startsWith("mp3?") || value.startsWith("https")){
                    anchor.href = value;
                } else { return false; }
                break;

            case "class":
                if (value.equals("loadrelated")) { return false; }
                anchor.classe = value;
                break;
        }
        return true;
    }

    void getInnerText(Anchor anchor) {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        while (i < len && (bytes[i] != 60 || bytes[i + 1] != 47 || bytes[i + 2] != 97)) { // if content not ending </a
            content.write(bytes[i++]);
            while (i < len && bytes[i] != 60) {
                content.write(bytes[i++]);
            }
        }
        // ending Anchor in hand
        i += 4;
        anchor.content = content.toString();
    }

    void skipSpace() {
        while (bytes[i] == 32) {// Until Space ends, we skip
            i++;
        }
    }
}
