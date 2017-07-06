package de.peeeq.gwt;

import com.google.gwt.core.client.EntryPoint;

/**
 *
 */
public class WurstEntryPoint implements EntryPoint {


    @Override
    public void onModuleLoad() {
        initialize();
    }

    public native void initialize() /*-{
        console.log("Initializing webworker...");
        var that = this;
        self.addEventListener('message', function (e) {
            console.log("Received message in web worker");
            that.@de.peeeq.gwt.WurstEntryPoint::onMessage(Ljava/lang/String;)(e.data);
        }, false);
        console.log("Initialized webworker...");
    }-*/;

    private native void postMessage(String code, String payload) /*-{
        console.log("postMessage", code, payload);
        self.postMessage({code: code, payload: payload});
    }-*/;


    void onMessage(String message) {
        postMessage("ok", "Hello '" + message + "'!");
    }

}
