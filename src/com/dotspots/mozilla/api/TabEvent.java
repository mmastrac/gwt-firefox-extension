/**
 * 
 */
package com.dotspots.mozilla.api;

import com.dotspots.mozilla.dom.xul.Tab;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

abstract class TabEvent<H extends EventHandler> extends GwtEvent<H> {
	protected final Tab tab;

	public TabEvent(Tab tab) {
		this.tab = tab;
	}
}