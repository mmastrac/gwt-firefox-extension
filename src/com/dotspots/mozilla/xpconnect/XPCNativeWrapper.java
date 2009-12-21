package com.dotspots.mozilla.xpconnect;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

public class XPCNativeWrapper {

	/**
	 * Creates a native wrapper if we are running in hosted mode to work around
	 * hosted mode bugs.
	 */
	public static <T extends JavaScriptObject> T createXPCNativeWrapper(T o) {
		return (GWT.isScript()) ? o : createXPCNativeWrapper0(o);
	};

	private static native <T extends JavaScriptObject> T createXPCNativeWrapper0(T o) /*-{
		return new XPCNativeWrapper(o);
	}-*/;

	public static <T extends JavaScriptObject> T unwrapXPCNativeWrapper(T o) {
		return (GWT.isScript()) ? o : unwrapXPCNativeWrapper0(o);
	}

	static native <T extends JavaScriptObject> T unwrapXPCNativeWrapper0(T o) /*-{
		return o.wrappedJSObject;
	}-*/;
}
