/**
 * 
 */
package com.dotspots.mozilla.api;

import com.dotspots.mozilla.dom.xul.Tab;

class TabCreatedEvent extends TabEvent<TabCreatedHandler> {
	static final Type<TabCreatedHandler> TYPE = new Type<TabCreatedHandler>();

	public TabCreatedEvent(Tab tab) {
		super(tab);
	}

	@Override
	protected void dispatch(TabCreatedHandler handler) {
		handler.onTabCreated(tab);
	}

	@Override
	public Type<TabCreatedHandler> getAssociatedType() {
		return TYPE;
	}
}