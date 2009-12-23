package com.dotspots.example;

import org.mozilla.xpconnect.gecko.nsIDOMWindowInternal;

import com.dotspots.mozilla.Extension;
import com.dotspots.mozilla.ExtensionEntryPoint;
import com.dotspots.mozilla.api.TabCreatedHandler;
import com.dotspots.mozilla.dom.NativeEvents;
import com.dotspots.mozilla.dom.xul.Tab;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

@Extension(guid = "C052179D-7E8A-4398-9980-CC6A268C643F", name = "Example Extension")
public class ExampleExtension extends ExtensionEntryPoint {
	@Override
	public void onExtensionStart() {
		getTabs().addCreatedListener(new TabCreatedHandler() {
			@Override
			public void onTabCreated(Tab tab) {
				final nsIDOMWindowInternal contentWindow = tab.getLinkedBrowser().getContentWindow().cast();
				NativeEvents.addEventListener(contentWindow, "DOMContentLoaded", new EventListener() {
					@Override
					public void onBrowserEvent(Event event) {
						final Element div = contentWindow.getDocument().createElement("div").cast();
						div.setInnerText("DOMContentLoaded fired on " + contentWindow.getLocation().toString());
					}
				});
			}
		});
	}
}
