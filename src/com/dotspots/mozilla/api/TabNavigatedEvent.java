/**
 * 
 */
package com.dotspots.mozilla.api;

import com.dotspots.mozilla.dom.xul.Tab;

class TabNavigatedEvent extends TabEvent<TabNavigatedHandler> {
	static final Type<TabNavigatedHandler> TYPE = new Type<TabNavigatedHandler>();

	public TabNavigatedEvent(Tab tab) {
		super(tab);
	}

	@Override
	protected void dispatch(TabNavigatedHandler handler) {
		handler.onTabNavigated(tab);
	}

	@Override
	public Type<TabNavigatedHandler> getAssociatedType() {
		return TYPE;
	}
}