package com.dotspots.mozilla.dom.xul;

import org.mozilla.xpconnect.gecko.nsIWebNavigation;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.NodeList;

/**
 * XUL <tabbrowser> element.
 * 
 * https://developer.mozilla.org/en/XUL/tabbrowser
 */
public final class TabbedBrowser extends JavaScriptObject {
	protected TabbedBrowser() {
	}

	public native void addTab(String url) /*-{
		this.loadOneTab(url, null, null, null, false);
	}-*/;

	public native void loadURI(String url, String referrer, String postData, boolean allowThirdPartyFixup) /*-{
		this.loadURI(url, referrer, postData, allowThirdPartyFixup);
	}-*/;

	public native nsIWebNavigation getWebNavigation() /*-{
		return this.webNavigation;
	}-*/;

	public native NodeList<Browser> getBrowsers() /*-{
		return this.browsers;
	}-*/;

	public native Browser getBrowserForTab(Tab target) /*-{
		return this.getBrowserForTab(target);
	}-*/;

	public native TabContainer getTabContainer() /*-{
		return this.tabContainer;
	}-*/;

	public native Tab getSelectedTab() /*-{
		return this.selectedTab;
	}-*/;

	public native void setSelectedTab(Tab tab) /*-{
		this.selectedTab = tab;
	}-*/;

	public native Browser getSelectedBrowser() /*-{
		return this.selectedBrowser;
	}-*/;

}
