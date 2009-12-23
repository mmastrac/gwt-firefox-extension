package com.dotspots.mozilla.api;

import com.dotspots.mozilla.dom.xul.Tab;
import com.google.gwt.event.shared.EventHandler;

public interface TabRemovedHandler extends EventHandler {
	public void onTabRemoved(Tab tab);
}
