package com.dotspots.mozilla.xpconnect;

import org.mozilla.xpconnect.XPInOut;
import org.mozilla.xpconnect.XPOut;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class XPConnect {
	public static <T> XPInOut<T> create(Class<T> clazz) {
		return JavaScriptObject.createObject().cast();
	}

	public static <T> XPInOut<T> create(T t) {
		XPInOut<T> inOut = JavaScriptObject.createObject().cast();
		inOut.set(t);
		return inOut;
	}

	public static <T> XPOut<T> create() {
		return JavaScriptObject.createObject().cast();
	}

	/**
	 * In hosted mode, converts a java array to a JavaScript array.
	 */
	public static JavaScriptObject javaArrayToJavaScriptArray(Object javaArray) {
		assert !GWT.isScript() : "This shouldn't be called when compiling to script";

		JavaScriptObject[] array = (JavaScriptObject[]) javaArray;

		JsArray<JavaScriptObject> jsArray = JsArray.createArray().cast();
		for (int i = 0; i < array.length; i++) {
			jsArray.set(i, array[i]);
		}

		return jsArray;
	}
}
