package com.dotspots.mozilla.dom;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.EventListener;

/**
 * Exposes addEventListener to GWT code. Not recommended for use on other browsers.
 */
public class InternalEvents {
	private static class InternalHandlerRegistration implements HandlerRegistration {
		private final JavaScriptObject hasEventHandlers;
		private final String eventName;
		private final JavaScriptObject function;

		private InternalHandlerRegistration(JavaScriptObject hasEventHandlers, String eventName, JavaScriptObject function) {
			this.hasEventHandlers = hasEventHandlers;
			this.eventName = eventName;
			this.function = function;
		}

		@Override
		public void removeHandler() {
			removeEventListener0(hasEventHandlers, eventName, function, true);
		}
	}

	private static native JavaScriptObject bindEventListenerAsFunction(EventListener eventListener) /*-{
		return function(e) { eventListener.@com.google.gwt.user.client.EventListener::onBrowserEvent(Lcom/google/gwt/user/client/Event;)(e) };
	}-*/;

	public static HandlerRegistration addEventListener(Element element, String eventName, EventListener eventListener) {
		JavaScriptObject boundEventListener = bindEventListenerAsFunction(eventListener);
		addEventListener0(element, eventName, boundEventListener, true);
		return new InternalHandlerRegistration(element, eventName, boundEventListener);
	}

	private static native HandlerRegistration addEventListener0(JavaScriptObject hasEventHandlers, String eventName,
			JavaScriptObject boundEventListener, boolean useCapture) /*-{
		hasEventHandlers.addEventListener(eventName, boundEventListener, useCapture);
	}-*/;

	private static native HandlerRegistration removeEventListener0(JavaScriptObject hasEventHandlers, String eventName,
			JavaScriptObject boundEventListener, boolean useCapture) /*-{
		removeEventListener(eventName, boundEventListener, useCapture);
	}-*/;

}
