/**
 * Bootstrap singleton.
 */

const nsISupports = Components.interfaces.nsISupports;
const nsIAlertsService = Components.interfaces.nsIAlertsService;
const nsIFactory = Components.interfaces.nsIFactory;
const nsIComponentRegistrar = Components.interfaces.nsIComponentRegistrar;
const mozIJSSubScriptLoader = Components.interfaces.mozIJSSubScriptLoader;

const CID_ALERTS_SERVICE = "@mozilla.org/alerts-service;1";
const CID_JS_SUBSCRIPT_LOADER = "@mozilla.org/moz/jssubscript-loader;1";

const CLASS_ID = Components.ID("@guid");
const CLASS_NAME = "@moduleName";
const CONTRACT_ID = "@@moduleName/singleton;1";

function reportError(message, e) {
    var errorMessage = e 
					    	? e + ((e.stack) 
					    			? "\n" + e.stack 
					    			: "") 
					    	: "";

    Components.utils.reportError(message + "\n" + errorMessage);
    
    try {
        var alertsService = Components.classes[CID_ALERTS_SERVICE].getService(nsIAlertsService);
        alertsService.showAlertNotification("", message, errorMessage, false, "", null);
	} 
	catch (e) {}
}

function Singleton() { 
    try {
	    var globalObject = {
		    // For GWT DOM
		    __gwt_initHandlers: function() {},
		    
		    // For GWT DOM
		    document: {},
		    
		    // Helper function for alerting errors	    
		    alert: reportError,
		    
		    __gwt_module: "@moduleName",
		    __gwt_base: "chrome://@guid/content/"
		};
		
		globalObject.window = globalObject;

        var loader = Components.classes[CID_JS_SUBSCRIPT_LOADER].getService(mozIJSSubScriptLoader);
        loader.loadSubScript("chrome://@guid/content/script.js", globalObject);
    } catch (e) {
        reportError("Error while initializing", e);
    }
};

Singleton.prototype = {
	QueryInterface: function(iid) {
		if (!iid.equals(nsISupports))
			throw Components.results.NS_ERROR_NO_INTERFACE;

		return this;
	}
};

var SingletonFactory = {
	createInstance: function (outer, iid) {
		if (outer != null)
			throw Components.results.NS_ERROR_NO_AGGREGATION;

		return (new Singleton()).QueryInterface(iid);
	}
};

var SingletonModule = {
	registerSelf: function(compMgr, fileSpec, location, type) {
		compMgr = compMgr.QueryInterface(nsIComponentRegistrar);
		compMgr.registerFactoryLocation(CLASS_ID, CLASS_NAME, CONTRACT_ID, fileSpec, location, type);
	},
	
	unregisterSelf: function(compMgr, location, type) {
		compMgr = compMgr.QueryInterface(nsIComponentRegistrar);
		compMgr.unregisterFactoryLocation(CLASS_ID, location);        
	},
	  
	getClassObject: function(compMgr, cid, iid) {
		if (!iid.equals(nsIFactory))
			throw Components.results.NS_ERROR_NOT_IMPLEMENTED;
		
		if (cid.equals(CLASS_ID))
			return SingletonFactory;
		
		throw Components.results.NS_ERROR_NO_INTERFACE;
	},
	
	canUnload: function(compMgr) { return true; }
};

function NSGetModule(compMgr, fileSpec) { return SingletonModule; }
