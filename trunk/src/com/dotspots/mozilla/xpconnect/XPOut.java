package com.dotspots.mozilla.xpconnect;

import com.google.gwt.core.client.JavaScriptObject;

public class XPOut<T> extends JavaScriptObject {
	protected XPOut() {
	}

	public final native T get() /*-{
		return this.value;
	}-*/;
}
