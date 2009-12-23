package com.dotspots.example;

import org.mozilla.xpconnect.gecko.nsIDOMWindowInternal;

import com.dotspots.mozilla.api.TabCreatedHandler;
import com.dotspots.mozilla.dom.NativeEvents;
import com.dotspots.mozilla.dom.xul.Tab;
import com.dotspots.mozilla.extension.Extension;
import com.dotspots.mozilla.extension.ExtensionEntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

@Extension(guid = "gwt-firefox-extension-example@code.google.com", name = "Example Extension")
public class ExampleExtension extends ExtensionEntryPoint {
	public ExampleExtension() {
		System.out.println(ImageBundle.INSTANCE.firefoxLogo().getURL());
	}

	@Override
	public void onExtensionStart() {
		getTabs().addCreatedListener(new TabCreatedHandler() {
			@Override
			public void onTabCreated(Tab tab) {
				final nsIDOMWindowInternal contentWindow = tab.getLinkedBrowser().getContentWindow().cast();
				NativeEvents.addEventListener(contentWindow, "DOMContentLoaded", new EventListener() {
					@Override
					public void onBrowserEvent(Event event) {
						final Document document = contentWindow.getDocument().cast();
						final Element div = document.createElement("div").cast();
						div.setInnerText("DOMContentLoaded fired on " + contentWindow.getLocation().toString());

						div.getStyle().setBackgroundColor("#eeeeee");
						div.getStyle().setBorderWidth(1, Unit.PX);
						div.getStyle().setBorderStyle(BorderStyle.SOLID);

						final ImageElement image = document.createImageElement();
						image.setSrc(ImageBundle.INSTANCE.firefoxLogo().getURL());
						div.appendChild(image);
					}
				});
			}
		});
	}
}
