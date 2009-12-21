package com.dotspots.mozilla;

import javax.swing.event.EventListenerList;

import org.mozilla.xpconnect.gecko.nsIChannel;
import org.mozilla.xpconnect.gecko.nsIDOMDocument;
import org.mozilla.xpconnect.gecko.nsIDOMWindowInternal;
import org.mozilla.xpconnect.gecko.nsIDocShell;
import org.mozilla.xpconnect.gecko.nsIRequest;
import org.mozilla.xpconnect.gecko.nsIURI;
import org.mozilla.xpconnect.gecko.nsIWebProgress;
import org.mozilla.xpconnect.gecko.nsIWebProgressListener;

import com.dotspots.mozilla.dom.xul.Browser;
import com.dotspots.mozilla.xpconnect.XPCNativeWrapper;
import com.sun.jndi.toolkit.url.Uri;

public class ChromeWindowManager {
	public static final String BROWSER_WINDOW = "navigator:browser";

	private static final boolean DETAILED_LOGGING = false;

	private final EventListenerList eventListeners = new EventListenerList();
	private final EventListenerList documentEventListeners = new EventListenerList();

	protected ChromeWindowManager() {
	}

	public void setWindow(final ChromeWindow chromeWindow) {
		this.chromeWindow = chromeWindow;
		eventListeners.addEventListener((Element) chromeWindow.cast(), "load", new EventListener() {
			public void onBrowserEvent(Event event) {
				chromeLoaded();
			}
		});
		eventListeners.addEventListener((Element) chromeWindow.cast(), "unload", new EventListener() {
			public void onBrowserEvent(Event event) {
				chromeUnloaded();
			}
		});
		eventListeners.addEventListener((Element) chromeWindow.cast(), "close", new EventListener() {
			public void onBrowserEvent(Event event) {
				eventListeners.clear();
				chromeClosed();
			}
		});
	}

	public void chromeLoaded() {
		try {
			final String type = chromeWindow.getDocument().getWindowType();
			// Don't process other types at all (logging can cause a recursive
			// loop)
			if (!type.equals(WindowMediator.BROWSER_WINDOW)) {
				return;
			}

			document = chromeWindow.getDocument();
			uiListener = new WindowModelUiListenerImplementation();
			windowGroupManager.setUiListener(uiListener);
			authenticationManager.addAuthListener(uiListener);

			if (!preferences.getBoolean(PREF_AFTER_FIRST_RUN)) {
				preferences.set(PREF_AFTER_FIRST_RUN, true);
				extensionUiMethods.goTutorial(null);
			}

			if (DETAILED_LOGGING) {
				LoggerManager.INSTANCE.logDebug("Chrome loaded: " + type);
			}

			final nsIWebProgressListener browserProgressListener = nsIWebProgressListener.wrap(new nsIWebProgressListener.Callback() {
				public void onLocationChange(nsIWebProgress webProgress, nsIRequest request, nsIURI location) {
					LoggerManager.INSTANCE.logUserAction(Importance.CONTEXT, Tracking.LOCATION_CHANGED);

					nsIDOMWindowInternal windowInternal = webProgress.getDOMWindow().queryInterface(nsIDOMWindowInternal.iid());

					// Fix for problem where this window is unwrapped in
					// hosted mode
					windowInternal = XPCNativeWrapper.createXPCNativeWrapper(windowInternal);

					final Window safeWindow = Window.get(windowInternal);
					final Window unsafeWindow = Window.get(windowInternal.wrappedJSObject());

					if (DETAILED_LOGGING) {
						LoggerManager.INSTANCE.logDebug("Got safe window: " + safeWindow);
						LoggerManager.INSTANCE.logDebug("Got unsafe window: " + unsafeWindow);
					}

					WindowContext context = new ExtensionWindowContext(chromeWindow, safeWindow, unsafeWindow);

					Uri originalUri = nsIChannel.instanceOf(request) ? Channel.queryInterface(request).getOriginalURI() : null;

					if (unsafeWindow == null) {
						LoggerManager.INSTANCE.logError("Unsafe window == null in onLocationChange()");
					} else {
						attachToWindow(windowInternal);
					}

					windowGroupManager.setWindowUnimportant(context, attachPolicy.validateWindow(windowInternal, false));
					windowGroupManager.locationChange(context, originalUri == null ? null : originalUri.getSpec(), location == null ? null
							: location.getSpec());

					if (NodeExt.isSameNode(document.getBrowser().getSelectedBrowser().getContentWindow(), windowInternal)) {
						windowGroupManager.tabActivated(context);
					}
				}

				public void onProgressChange(nsIWebProgress webProgress, nsIRequest request, int curSelfProgress, int maxSelfProgress,
						int curTotalProgress, int maxTotalProgress) {
				}

				public void onSecurityChange(nsIWebProgress webProgress, nsIRequest request, int state) {
				}

				public void onStateChange(nsIWebProgress webProgress, nsIRequest request, int stateFlags, int status) {
					nsIDOMWindowInternal windowInternal = webProgress.getDOMWindow().queryInterface(nsIDOMWindowInternal.iid());

					if (DETAILED_LOGGING) {
						LoggerManager.INSTANCE.logDebug("onStateChange, state = " + Integer.toString(status, 16) + " window = "
								+ windowInternal);
					}

					if ((stateFlags & (nsIWebProgressListener.STATE_IS_WINDOW | nsIWebProgressListener.STATE_STOP)) == (nsIWebProgressListener.STATE_IS_WINDOW | nsIWebProgressListener.STATE_STOP)) {
						// If status < 0 there was an error, but right now we will try to trigger DotSpots anyway
						// nsresult constants are defined here:
						// http://mxr.mozilla.org/mozilla-central/source/xpcom/base/nsError.h
						if (DETAILED_LOGGING) {
							LoggerManager.INSTANCE.logDebug("Window has stopped loading, triggering stop event");
						}
					}
				}

				public void onStatusChange(nsIWebProgress webProgress, nsIRequest request, int status, String message) {
				}
			});

			documentEventListeners.addEventCookie(document.getBrowser().getTabContainer().addEventListener("TabOpen", new EventListener() {
				public void onBrowserEvent(Event event) {
					final Tab tab = (Tab) event.getEventTarget().cast();
					final Browser browser = tab.getLinkedBrowser();

					browser.addProgressListener(browserProgressListener);
				}
			}));

			documentEventListeners.addEventCookie(document.getBrowser().getTabContainer().addEventListener("TabSelect",
					new EventListener() {
						public void onBrowserEvent(Event event) {
							final Tab tab = (Tab) event.getEventTarget().cast();
							nsIDOMWindow2 window = tab.getLinkedBrowser().getContentWindow();
							final Window safeWindow = Window.get(window);
							final Window unsafeWindow = Window.get(window.wrappedJSObject());
							WindowContext context = new ExtensionWindowContext(chromeWindow, safeWindow, unsafeWindow);
							windowGroupManager.tabActivated(context);
						}
					}));

			// Attach a progress listener to the existing browsers
			for (Browser browser : NodeListIterator.iterable(document.getBrowser().getBrowsers())) {
				browser.addProgressListener(browserProgressListener);
			}

			eventListeners.addEventListener(document.getElementById("contentAreaContextMenu"), "popupshowing", new EventListener() {
				public void onBrowserEvent(final Event event) {
					final Element target = event.getEventTarget().cast();
					if (target.getTagName().equals("popup")) {
						buildContextMenu();
					}
				}
			});

			document.addDocumentLoadListeners(documentEventListeners, new DocumentLoadListener() {
				public void documentLoaded(final nsIDOMWindow window) {
					// Need to call locationChange() again here, because
					// onLocationChange() isn't called for newly opened background tabs.
					// It's still unclear whether DOMContentLoaded will be called every time for a top level document.
					nsIDOMDocument loadedDocument = window.getDocument();
					nsIChannel channel = getChannelForDocument(loadedDocument);
					if (channel != null) {
						Window safeWindow = Window.get(window);
						Window unsafeWindow = Window.get(window.wrappedJSObject());
						WindowContext context = new ExtensionWindowContext(chromeWindow, safeWindow, unsafeWindow);
						String originalUrl = channel.getOriginalURI().getSpec();
						String url = channel.getURI().getSpec();
						windowGroupManager.locationChange(context, originalUrl, url);
					}

					if (DETAILED_LOGGING) {
						LoggerManager.INSTANCE.logDebug("documentLoaded, window = " + window);
					}

					nsIDOMHTMLDocument htmlDocument = loadedDocument.queryInterface(nsIDOMHTMLDocument.iid());
					if (htmlDocument != null && htmlDocument.getBody() != null) {
						// We have a <body> so can trigger before full page load
						triggerDotSpots(window);
					}
				}

				public void documentUnloaded(final nsIDOMWindow window) {
				}

				public void documentHide(final nsIDOMWindow window) {
				}

				public void documentShow(final nsIDOMWindow window) {
				}
			});

			chromeWindow.run(extensionUiMethods.getCreateGlobalMethodsRunnable(),
					extensionUiMethods.getUpdateButtonLocationRunnable(false), extensionUiMethods.getUpdateStateRunnable());
		} catch (final Throwable t) {
			LoggerManager.INSTANCE.logError("Chrome loading", t);
		}
	}

	private nsIChannel getChannelForDocument(nsIDOMDocument document) {
		NodeList<Browser> browsers = chromeWindow.getTabbedBrowser().getBrowsers();
		int numBrowsers = browsers.getLength();
		for (int index = 0; index < numBrowsers; index++) {
			Browser browser = browsers.getItem(index);
			nsIDOMDocument contentDocument = browser.getContentDocument();
			if (NodeExt.isSameNode(contentDocument, document)) {
				nsIDocShell docShell = browser.getDocShell();
				return getChannelForDocShell(docShell);
			}
		}
		return null;
	}

	public static native nsIChannel getChannelForDocShell(nsIDocShell docShell) /*-{
		return docShell.currentDocumentChannel;
	}-*/;

	public void chromeUnloaded() {
		documentEventListeners.clear();

		// This happens at the first browser startup
		if (document == null) {
			return;
		}

		LoggerManager.INSTANCE.logUserAction(Importance.CONTEXT, Tracking.CHROME_UNLOADED);

		final String type = document.getWindowType();
		// Don't process other types at all (logging can cause a recursive
		// loop)
		if (!type.equals(WindowMediator.BROWSER_WINDOW)) {
			return;
		}

		if (DETAILED_LOGGING) {
			LoggerManager.INSTANCE.logDebug("Chrome unloaded");
		}
	}

	public void chromeClosed() {
		windowGroupManager.close();
	}
}
