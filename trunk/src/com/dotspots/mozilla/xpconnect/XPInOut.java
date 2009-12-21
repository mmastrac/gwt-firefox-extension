package com.dotspots.mozilla.xpconnect;

import com.google.gwt.core.client.JavaScriptObject;

public class XPInOut<T> extends JavaScriptObject {
	protected XPInOut() {
	}

	public final native void set(T t) /*-{
		this.value = t;
	}-*/;

	public final native T get() /*-{
		return this.value;
	}-*/;
}
