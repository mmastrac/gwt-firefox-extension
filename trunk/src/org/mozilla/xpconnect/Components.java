package org.mozilla.xpconnect;

public final class Components {
	private Components() {
	}

	public native ComponentsUtils getUtils() /*-{
		return Components.utils;
	}-*/;
}
