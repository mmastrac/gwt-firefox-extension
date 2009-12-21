package com.dotspots.mozilla.xpconnect;

import com.google.gwt.core.client.JavaScriptObject;

public class XPConnectObject extends JavaScriptObject {
	protected XPConnectObject() {
	}

	public final native <T extends XPConnectObject> T wrappedJSObject() /*-{
		return this.wrappedJSObject;
	}-*/;

	public interface Callback {
	}
}
