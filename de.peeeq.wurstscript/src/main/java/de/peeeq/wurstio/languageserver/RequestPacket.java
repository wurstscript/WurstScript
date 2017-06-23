package de.peeeq.wurstio.languageserver;

import com.google.gson.JsonElement;

/**
 * @author peter
 */
public class RequestPacket {
    private int sequenceNr;
    private String path;
    private JsonElement data;


    public int getSequenceNr() {
        return sequenceNr;
    }

    public void setSequenceNr(int sequenceNr) {
        this.sequenceNr = sequenceNr;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RequestPacket [sequenceNr=" + sequenceNr + ", path=" + path + ", data=" + data + "]";
    }


}
