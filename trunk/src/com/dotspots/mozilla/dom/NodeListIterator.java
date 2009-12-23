package com.dotspots.mozilla.dom;

import java.util.Iterator;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;

public class NodeListIterator<T extends Node> implements Iterator<T>, Iterable<T> {
	private int index = 0;
	private final NodeList<T> nodeList;

	public NodeListIterator(NodeList<T> nodeList) {
		this.nodeList = nodeList;
	}

	public boolean hasNext() {
		return index < nodeList.getLength();
	}

	public T next() {
		assert hasNext() : "Iterator moved beyond end of nodelist";
		return nodeList.getItem(index++);
	}

	public void remove() {
		T next = nodeList.getItem(index - 1);
		next.getParentNode().removeChild(next);
	}

	public Iterator<T> iterator() {
		return this;
	}

	public static <T extends Node> Iterable<T> iterable(final NodeList<T> nodeList) {
		return new NodeListIterator<T>(nodeList);
	}
}