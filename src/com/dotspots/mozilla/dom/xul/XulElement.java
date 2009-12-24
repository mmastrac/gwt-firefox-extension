package com.dotspots.mozilla.dom.xul;

import com.dotspots.mozilla.dom.NativeEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.EventListener;

/**
 * Base class for all XUL elements.
 */
public abstract class XulElement extends Element {
	public final static String XUL_NAMESPACE = "http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul";

	protected XulElement() {
	}

	public final HandlerRegistration addEventListener(String eventName, EventListener eventListener) {
		return NativeEvents.addEventListener(this, eventName, eventListener);
	}
}
