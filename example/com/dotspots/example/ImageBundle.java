package com.dotspots.example;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface ImageBundle extends ClientBundle {
	public static final ImageBundle INSTANCE = GWT.create(ImageBundle.class);

	@Source("firefoxLogo.png")
	public ImageResource firefoxLogo();
}
