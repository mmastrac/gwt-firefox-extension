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

	/**
	 * Adds a weakly-referenced progress listener to the browser. You'll want to stash a references to the
	 * HandlerRegistration somewhere so your event handler doesn't get GC'd.
	 */
	public HandlerRegistration addProgressListener(final nsIWebProgressListener listener) {
		addProgressListener0(listener);

		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				removeProgressListener0(listener);
			}
		};
	}

	private native void addProgressListener0(nsIWebProgressListener listener) /*-{
		var browser = this;
		var Ci = Components.interfaces;

		// The browser wants a weak reference.  This QueryInterface ensures that it is available.
		listener.QueryInterface = function(iid) { 
		if (iid.equals(Ci.nsIWebProgressListener) || iid.equals(Ci.nsISupportsWeakReference) || iid.equals(Ci.nsISupports))
		return this;
		throw Components.results.NS_NOINTERFACE; 
		};

		browser.addProgressListener(listener, Components.interfaces.nsIWebProgress.NOTIFY_ALL);
	}-*/;

	private native void removeProgressListener0(nsIWebProgressListener listener) /*-{
		browser.removeProgressListener(listener);
	}-*/;

}
