package org.mozilla.xpconnect;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;

public final class Components {
	protected Components() {
	}

	public static final class Utils extends JavaScriptObject {
		protected Utils() {
		}

		public static native void reportError(String error) /*-{
			Components.utils.reportError(error);
		}-*/;

		public static native void reportError(JavaScriptException e) /*-{
			Components.utils.reportError(e.@com.google.gwt.core.client.JavaScriptException::getException()());
		}-*/;
	}
}
