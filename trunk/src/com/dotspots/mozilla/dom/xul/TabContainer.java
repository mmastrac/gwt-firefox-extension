package com.dotspots.mozilla.dom.xul;

/**
 * XUL <tabbrowser> element's tabContainer.
 * 
 * https://developer.mozilla.org/en/XUL%3atabs
 */
public final class TabContainer extends XulElement {
	protected TabContainer() {
	}

	public native int getItemCount() /*-{
		return this.itemCount;
	}-*/;

	public native Tab getItemAtIndex(int index) /*-{
		return this.getItemAtIndex(index);
	}-*/;
}
