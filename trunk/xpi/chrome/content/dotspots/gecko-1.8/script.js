const nsIWindowWatcher = Components.interfaces.nsIWindowWatcher;

const CID_WINDOW_WATCHER = "@mozilla.org/embedcomp/window-watcher;1";

var base = "http://localhost:8080/com.dotspots.ExtensionModule/";

var ww = Components.classes[CID_WINDOW_WATCHER].getService(nsIWindowWatcher);
var win = ww.openWindow(null, "chrome://dotspots/content/extension-hosted.html?gwt.module=" + __gwt_module + "&gwt.base=" + base + "&gwt.codesvr=localhost:9997&gwt.hosted=localhost:9997", "dotSpotsOophm", "chrome", null);
