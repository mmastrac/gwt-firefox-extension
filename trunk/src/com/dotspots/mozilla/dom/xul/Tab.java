package com.dotspots.mozilla.dom.xul;

/**
 * XUL <tabbrowser> element's tab elements.
 * 
 * https://developer.mozilla.org/en/XUL%3atab
 */
public final class Tab extends XulElement {
	protected Tab() {
	}

	/**
	 * Gets the link XUL <browser> for this tab.
	 */
	public native Browser getLinkedBrowser() /*-{
		return this.linkedBrowser;
	}-*/;
}
