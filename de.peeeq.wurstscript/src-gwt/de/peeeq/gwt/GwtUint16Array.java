package de.peeeq.gwt;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType( isNative = true, namespace = JsPackage.GLOBAL, name = "Uint16Array")
public class GwtUint16Array
{
    @JsProperty( name = "length" )
    public native int getCount();
}
