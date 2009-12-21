package com.dotspots.mozilla.dom.xul;

import com.dotspots.mozilla.dom.InternalEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.EventListener;

public class XulElement extends Element {
	public final static String XUL_NAMESPACE = "http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul";

	public HandlerRegistration addEventListener(String eventName, EventListener eventListener) {
		return InternalEvents.addEventListener(this, eventName, eventListener);
	}
}
