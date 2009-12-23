package com.dotspots.mozilla.api;

import com.dotspots.mozilla.dom.xul.Tab;
import com.google.gwt.event.shared.EventHandler;

public interface TabCreatedHandler extends EventHandler {
	public void onTabCreated(Tab tab);
}
