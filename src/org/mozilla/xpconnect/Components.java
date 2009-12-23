package org.mozilla.xpconnect;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;

public final class Components {
	protected Components() {
	}

	public static final class Utils extends JavaScriptObject {
		protected Utils() {
		}

		public native void reportError(String error) /*-{
			this.reportError(error);
		}-*/;

		public native void reportError(JavaScriptException e) /*-{
			this.reportError(e.@com.google.gwt.core.client.JavaScriptException::getException()());
		}-*/;
	}
}
