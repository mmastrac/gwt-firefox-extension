package com.dotspots.mozilla.extension;

import com.dotspots.mozilla.api.Tabs;
import com.google.gwt.core.client.EntryPoint;

/**
 * Base class for extension entry points.
 */
public abstract class ExtensionEntryPoint implements EntryPoint {
	private final Tabs tabs;

	public ExtensionEntryPoint() {
		tabs = new Tabs();
	}

	@Override
	public void onModuleLoad() {
		onExtensionStart();
	}

	/**
	 * Gets the tabs API.
	 */
	public Tabs getTabs() {
		return tabs;
	}

	public abstract void onExtensionStart();
}
