package com.dotspots.example;

import org.mozilla.xpconnect.Components;
import org.mozilla.xpconnect.gecko.nsIDOMWindowInternal;

import com.dotspots.mozilla.api.TabCreatedHandler;
import com.dotspots.mozilla.api.TabNavigatedHandler;
import com.dotspots.mozilla.dom.NativeEvents;
import com.dotspots.mozilla.dom.xul.Tab;
import com.dotspots.mozilla.extension.Extension;
import com.dotspots.mozilla.extension.ExtensionEntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

@Extension
public class ExampleExtension extends ExtensionEntryPoint {
	public ExampleExtension() {
	}

	public void log(String message) {
		GWT.log(message, null);
		Components.Utils.reportError(message);
	}

	@Override
	public void onExtensionStart() {
		log("Extension is starting up");

		getTabs().addCreatedListener(new TabCreatedHandler() {
			@Override
			public void onTabCreated(Tab tab) {
				log("Received tab created event: " + getBrowserLocation(tab));
			}
		});

		getTabs().addNavigatedListener(new TabNavigatedHandler() {
			@Override
			public void onTabNavigated(final Tab tab) {
				log("Received tab navigated event: " + getBrowserLocation(tab));

				final nsIDOMWindowInternal contentWindow = tab.getLinkedBrowser().getContentWindow().cast();
				NativeEvents.addEventListener(contentWindow, "DOMContentLoaded", new EventListener() {
					@Override
					public void onBrowserEvent(Event event) {
						log("Received DOMContentLoaded event: " + getBrowserLocation(tab));

						final Document document = contentWindow.getDocument().cast();
						final Element div = document.createElement("div").cast();
						div.setInnerText("DOMContentLoaded fired on " + contentWindow.getLocation().toString());

						div.getStyle().setPosition(Position.FIXED);
						div.getStyle().setTop(0, Unit.PX);
						div.getStyle().setLeft(0, Unit.PX);
						div.getStyle().setRight(0, Unit.PX);
						div.getStyle().setBackgroundColor("#eeeeee");
						div.getStyle().setBorderWidth(1, Unit.PX);
						div.getStyle().setBorderStyle(BorderStyle.SOLID);

						final ImageElement image = document.createImageElement();
						image.setSrc(ImageBundle.INSTANCE.firefoxLogo().getURL());
						div.appendChild(image);

						document.getBody().appendChild(div);
					}
				});
			}
		});
	}

	protected String getBrowserLocation(Tab tab) {
		final nsIDOMWindowInternal contentWindow = tab.getLinkedBrowser().getContentWindow().cast();
		return contentWindow.getLocation().toString();
	}
}
