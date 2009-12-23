package com.dotspots.mozilla.extension;

public @interface Extension {
	/**
	 * A globally-unique uuid used for the singleton class ID, as well as for the chrome package registration.
	 */
	public String guid();

	public String name();
}
