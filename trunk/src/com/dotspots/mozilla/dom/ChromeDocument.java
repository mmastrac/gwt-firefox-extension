package com.dotspots.mozilla.dom;

import com.dotspots.mozilla.dom.xul.TabbedBrowser;
import com.dotspots.mozilla.dom.xul.XulElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;

public final class ChromeDocument extends Document {
	protected ChromeDocument() {
	}

	public XulElement createXulElement(String tagName) {
		return createElementNS(XulElement.XUL_NAMESPACE, tagName).cast();
	};

	private native Element createElementNS(String ns, String tagName) /*-{
		return this.createElementNS(ns, tagName);
	}-*/;

	public native String getWindowType() /*-{
		return this.documentElement.getAttribute('windowtype');
	}-*/;

	public native TabbedBrowser getBrowser() /*-{
		return this.defaultView.gBrowser;
	}-*/;

	public native Element getPopupRangeParent() /*-{
		// Prefer popupRangeParent over popupNode, because it can supply the exact text node
		return this.popupRangeParent;
	}-*/;

	public native Node getPopupNode() /*-{
		return this.popupNode;
	}-*/;
}
