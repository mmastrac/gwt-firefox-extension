package com.dotspots.mozilla.dom;

import com.dotspots.mozilla.dom.xul.TabbedBrowser;
import com.google.gwt.core.client.JavaScriptObject;

public final class ChromeWindow extends JavaScriptObject {
	protected ChromeWindow() {
	}

	public native void alert(Object message) /*-{
		this.alert(message);
	}-*/;

	public native void close() /*-{
		this.close();
	}-*/;

	public native ChromeWindow open(String url) /*-{
		return this.open(url);
	}-*/;

	public native ChromeDocument getDocument() /*-{
		return this.document;
	}-*/;

	public native TabbedBrowser getTabbedBrowser() /*-{
		return this.getBrowser();
	}-*/;

	public native void openUILink(final String url, final JavaScriptObject event, final boolean ignoreButton, final boolean ignoreAlt,
			final boolean allowKeywordFixup, final String postData) /*-{
		this.openUILink(url, event, ignoreButton, ignoreAlt, allowKeywordFixup, postData);
	}-*/;
}
