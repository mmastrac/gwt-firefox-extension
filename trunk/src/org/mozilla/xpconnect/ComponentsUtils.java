package org.mozilla.xpconnect;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;

public class ComponentsUtils extends JavaScriptObject {
	private ComponentsUtils() {
	}

	public native void reportError(String error) /*-{
		this.reportError(error);
	}-*/;

	public native void reportError(JavaScriptException e) /*-{
		this.reportError(e.@com.google.gwt.core.client.JavaScriptException::getException()());
	}-*/;
}
