package com.dotspots.mozilla.dom.xul;

import org.mozilla.xpconnect.gecko.nsIDOMDocument;
import org.mozilla.xpconnect.gecko.nsIDOMWindow2;
import org.mozilla.xpconnect.gecko.nsIDocShell;
import org.mozilla.xpconnect.gecko.nsIWebProgressListener;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * XUL <browser> element.
 * 
 * https://developer.mozilla.org/en/XUL:browser
 */
public final class Browser extends Element {
	protected Browser() {
	}

	public native nsIDOMDocument getContentDocument() /*-{
		return this.contentDocument;
	}-*/;

	public native nsIDOMWindow2 getContentWindow() /*-{
		return this.contentWindow;
	}-*/;

	public native nsIDocShell getDocShell() /*-{
		return this.docShell;
	}-*/;

	public native HandlerRegistration addProgressListener(nsIWebProgressListener listener) /*-{
		var browser = this;
		var Ci = Components.interfaces;

		// The browser wants a weak reference.  This QueryInterface ensures that it is available.
		listener.QueryInterface = function(iid) { 
		if (iid.equals(Ci.nsIWebProgressListener) || iid.equals(Ci.nsISupportsWeakReference) || iid.equals(Ci.nsISupports))
		return this;
		throw Components.results.NS_NOINTERFACE; 
		};

		browser.addProgressListener(listener, Components.interfaces.nsIWebProgress.NOTIFY_ALL);

		// TODO: { remove: function() { browser.removeProgressListener(listener); } };
		return null;
	}-*/;

}
