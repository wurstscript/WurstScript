package de.peeeq.gwt;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

/**
 *
 */
public class WurstEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        System.out.println("HELLO GRADLE GWT!!!");
        Label label = new Label("Hello from GWT");
        RootPanel.get("content").add(label);


    }
}
