package com.dotspots.mozilla.dom;

import java.util.Iterator;

import org.mozilla.xpconnect.gecko.nsISimpleEnumerator;

import com.google.gwt.core.client.JavaScriptObject;

public class InternalSimpleIteratorWrapper<T extends JavaScriptObject> implements Iterable<T>, Iterator<T> {
	protected final nsISimpleEnumerator iterator;

	public InternalSimpleIteratorWrapper(nsISimpleEnumerator iterator) {
		this.iterator = iterator;
	}

	public boolean hasNext() {
		return iterator.hasMoreElements();
	};

	public T next() {
		return iterator.getNext().cast();
	};

	public void remove() {
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}
}
