const
nsIWindowWatcher = Components.interfaces.nsIWindowWatcher;

const
CID_WINDOW_WATCHER = "@mozilla.org/embedcomp/window-watcher;1";

var base = "http://localhost:8080/@moduleName/";
var codeServer = "localhost:9997"

	
function reportError(message, e) {
	var errorMessage = e ? e + ((e.stack) ? "\n" + e.stack : "") : "";

	Components.utils.reportError(message + "\n" + errorMessage);

	try {
		var alertsService = Components.classes[CID_ALERTS_SERVICE]
				.getService(nsIAlertsService);
		alertsService.showAlertNotification("", message, errorMessage, false,
				"", null);
	} catch (e) {
	}
}

reportError('test3');

try {
	// Open up the extension hosted mode
	var ww = Components.classes[CID_WINDOW_WATCHER]
			.getService(nsIWindowWatcher);
	var win = ww.openWindow(null,
			"chrome://@moduleName/content/extension-hosted-window.xul"
					+ "?gwt.module=@moduleName" + "&gwt.base=" + base
					+ "&gwt.codesvr=" + codeServer, "oophm", "chrome", null);
} catch (e) {
	reportError("Error opening window", e);
}