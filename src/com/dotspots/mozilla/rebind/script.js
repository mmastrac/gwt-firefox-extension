const
nsIWindowWatcher = Components.interfaces.nsIWindowWatcher;

const
CID_WINDOW_WATCHER = "@mozilla.org/embedcomp/window-watcher;1";

var base = "http://localhost:8080/@moduleName/";
var codeServer = "localhost:9997"

// Open up the extension hosted mode
var ww = Components.classes[CID_WINDOW_WATCHER].getService(nsIWindowWatcher);
var win = ww.openWindow(null,
		"chrome://@moduleName/content/extension-hosted.html"
				+ "?gwt.module=@moduleName" + "&gwt.base=" + base
				+ "&gwt.codesvr=" + codeServer, "oophm", "chrome", null);
