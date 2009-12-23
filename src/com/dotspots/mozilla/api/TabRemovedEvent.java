/**
 * 
 */
package com.dotspots.mozilla.api;

import com.dotspots.mozilla.dom.xul.Tab;

class TabRemovedEvent extends TabEvent<TabRemovedHandler> {
	private static final Type<TabRemovedHandler> TYPE = new Type<TabRemovedHandler>();

	public TabRemovedEvent(Tab tab) {
		super(tab);
	}

	@Override
	protected void dispatch(TabRemovedHandler handler) {
		handler.onTabRemoved(tab);
	}

	@Override
	public Type<TabRemovedHandler> getAssociatedType() {
		return TYPE;
	}
}