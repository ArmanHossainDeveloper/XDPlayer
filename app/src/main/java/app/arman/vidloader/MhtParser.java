package app.arman.vidloader;

import android.app.Activity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

class MhtParser {

    MhtParser(Activity activity, String filePath){
    }

    MhtParser(){
    }

    public ArrayList<Anchor> parse(byte[] bytes) {
        final int length = bytes.length;
        //ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ArrayList<Anchor> anchors = new ArrayList<>();

        boolean isOpeningTagStarted = false;
        boolean isOpeningTagEnded = false;
        boolean isClosingTagStarted = false;
        boolean isClosingTagEnded = true;
        int i = 0;

        while ( i < length) {
            int b = bytes[i];

            if (isOpeningTagStarted) {
                if ((char) b == 'a' && (char) bytes[i+1] == ' '){
                    // Anchor Link Found
                    Anchor anchor = new Anchor();
                    i += 2;
                    while ((char) bytes[i] != '>'){
                        while ((char) bytes[i] == ' '){
                            i++;
                        }

                        ByteArrayOutputStream tagBuffer = new ByteArrayOutputStream();
                        char tagInt;
                        while ((tagInt = (char) bytes[i]) != '='){
                            tagBuffer.write(tagInt);
                            i++;
                        }
                        i += 2;
                        String tagProperty = tagBuffer.toString();

                        ByteArrayOutputStream propertyBuffer = new ByteArrayOutputStream();
                        char propertyInt;
                        while ((propertyInt = (char) bytes[i]) != '"'){
                            // property data
                            propertyBuffer.write(propertyInt);
                            i++;
                        }
                        i++;
                        String propertyData = propertyBuffer.toString();


                        switch (tagProperty){
                            case "href":
                                anchor.href = propertyData;
                                break;
                            case "class":
                                anchor.classe = propertyData;
                                break;
                        }

                        /*
                        if (tagProperty == "href"){

                        }
                        else if (tagProperty == "class"){

                        }
                        */

                    }
                    i++;
                    anchors.add(anchor);
                }

                while ((char) bytes[i] != '>'){
                    i++;
                }
                i++;
                isOpeningTagStarted = false;
                isOpeningTagEnded = true;
            }
            else if (isOpeningTagEnded) {
                while ((char) bytes[i] != '<' && (char) bytes[i + 1] != '/' ){
                    i++;
                }
                i += 2;
                isOpeningTagEnded = false;
                isClosingTagStarted = true;
            }
            else if (isClosingTagStarted) {
                while ((char) bytes[i] != '>'){
                    i++;
                }
                i++;
                isClosingTagStarted = false;
                isClosingTagEnded = true;
            }
            else if (isClosingTagEnded) {
                while ((char) bytes[i] != '<'){
                    i++;
                }
                i++;
                isClosingTagEnded = false;
                isOpeningTagStarted = true;
            }
        }

        return anchors;
    }


}

