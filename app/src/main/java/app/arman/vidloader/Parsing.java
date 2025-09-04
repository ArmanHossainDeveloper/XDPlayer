package app.arman.vidloader;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

public class Parsing {

    /*
    Parent document = new Parent();
    document = addChild();

    recursive function
    addChild:
        Arraylist<Tag> tags;
        stack.push(Tag.name)
        startTag = stack.get();
        if closingTag
            if startTag = currentTag
            return

        while(!='<'):
            Tag.content;
            isTagClosing

        if(+1 !='/'):
            startTag.addChild()


        while(!='>'):
            stack.pop

        return tags;









    */
    byte[] bytes;
    int i = 0;
    LinkedList<Tag> list = new LinkedList<>();

    void parse(){
        Tag document = new Tag();
        keepDoingThis(document);
        // now We have A document of Parsed Html
    }
    void keepDoingThis(Tag document){
        list.add(document);
        while (bytes[i] != -1){
            Tag tag = list.getLast();
            skipSpace();
            if(bytes[i] == 60){
                //we are inside a tag
                i++;
                skipSpace();

                if(bytes[i] == 33){

                    //we hit comment
                    i += 3;
                    while (bytes[i] != 45 || bytes[i+1] != 45 || bytes[i+2] != 62){
                        i++;
                        while (bytes[i] != 45){
                            i++;
                        }
                    }
                    i += 3;
                }
                if(bytes[i] == 47){
                    //Last tag ending
                    while (bytes[i] != 62){
                        i++;
                    }
                    // ending brac in hand
                    i++;
                    list.removeLast();
                    /*if (tag != stack.firstElement()){
                    }*/
                }
                else{
                    //get childtag
                    Tag childTag = new Tag();
                    getTagName(childTag);
                    String[] strings = {"br", "hr", "img"};
                    boolean isSelfClosing = false;
                    for (String s :strings) {
                        if (s.equals(childTag.name)) {
                            isSelfClosing = true;
                            break;
                        }
                    }
                    if (!isSelfClosing){
                        list.add(childTag);
                    }
                    while(hasNextProperty()){
                        getNextProperty(childTag);
                    }
                    // ending brac in hand
                    i++;
                    tag.child.add(childTag);
                    list.push(childTag);
                    //skipSpace();

                }
            }
            else{
                // get text
                Tag textNode = new Tag();
                getTextContent(textNode);
                if (tag == null){
                    document.child.add(textNode);
                }
                else {
                    tag.child.add(textNode);
                }
            }
        }
    }


    void getTagName(Tag tag){
        ByteArrayOutputStream name = new ByteArrayOutputStream();
        while (bytes[i] != 32){  // Until Space starts, we read
            name.write(bytes[i++]);
        }
        tag.name = name.toString();
    }

    boolean hasNextProperty(){
        skipSpace();
        if (bytes[i] != 62) { // if tag not ending >
            return true;
        }
        return false;
    }

    void getNextProperty(Tag tag){
        ByteArrayOutputStream propertyKey = new ByteArrayOutputStream();
        while (bytes[i] != 61 && bytes[i] != 32 && bytes[i] != 62) {  // Until Equals or Space starts, we read
            propertyKey.write(bytes[i++]);
        }

        ByteArrayOutputStream propertyValue = null;
        if (bytes[i] == 61){ // if Equal Sign
            propertyValue = new ByteArrayOutputStream();
            int quot = bytes[++i];
            while (bytes[++i] != quot ) {  // Until quot, we read
                propertyValue.write(bytes[i]);
            }
        }
        tag.property.put(propertyKey.toString(), propertyValue != null? propertyValue.toString() : "");

    }



    void getChild(Tag tag){
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        while (bytes[++i] != 60) { // if content not ending <
            content.write(bytes[i]);
        }
        tag.content = content.toString();
    }

    void getTextContent(Tag tag){
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        while (bytes[i] != 60) { // if content not ending <
            content.write(bytes[i++]);
        }
        tag.content = content.toString();
    }

    void skipSpace(){
        while (bytes[i] == 32) {// Until Space ends, we skip
            i++;
        }
    }



}