package com.dotspots.mozilla.rebind;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import com.google.gwt.core.ext.Linker;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.EmittedArtifact;

public class URLArtifact extends EmittedArtifact {
	private static final long serialVersionUID = 1L;

	private final TreeLogger logger;

	private final String resourceName;

	public URLArtifact(TreeLogger logger, Class<? extends Linker> linker, String resourceName, String outputName) {
		super(linker, outputName);
		this.logger = logger;
		this.resourceName = resourceName;
	}

	public URLArtifact(TreeLogger logger, Class<? extends Linker> linker, String resourceName) {
		this(logger, linker, resourceName, resourceName);
	}

	@Override
	public InputStream getContents(TreeLogger logger) throws UnableToCompleteException {
		return getLinker().getResourceAsStream(resourceName);
	}

	@Override
	public long getLastModified() {
		final URLConnection connection;

		try {
			connection = getLinker().getResource(resourceName).openConnection();
			return connection.getLastModified();
		} catch (IOException e1) {
			logger.log(TreeLogger.ERROR, "Failed to get last modified date", e1);
		}

		return -1;
	}
}
