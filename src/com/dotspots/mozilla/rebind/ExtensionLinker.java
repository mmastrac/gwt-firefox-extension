package com.dotspots.mozilla.rebind;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.SortedSet;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.AbstractLinker;
import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.SyntheticArtifact;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.impl.StandardLinkerContext;
import com.google.gwt.dev.About;
import com.google.gwt.dev.jjs.JJSOptions;
import com.google.gwt.dev.util.DefaultTextOutput;
import com.google.gwt.dev.util.Util;
import com.google.gwt.dev.util.arg.OptionWarDir;

@LinkerOrder(Order.PRIMARY)
public class ExtensionLinker extends AbstractLinker {
	protected String getModulePrefix(TreeLogger logger, LinkerContext context) throws UnableToCompleteException {
		DefaultTextOutput out = new DefaultTextOutput(true);

		// Setup the well-known variables.
		//
		out.print("/* Linked: " + new Date() + " */");
		out.newline();
		out.print("var $strongName = \"__SYMBOL_ID__\";");
		out.newlineOpt();
		out.print("var $gwt_version = \"" + About.getGwtVersion() + "\";");
		out.newlineOpt();
		out.print("var $wnd = window;");
		out.newlineOpt();
		out.print("var $doc = $wnd.document;");
		out.newlineOpt();
		out.print("var $moduleName, $moduleBase, $stats;");
		out.newlineOpt();

		return out.toString();
	}

	protected String getModuleSuffix(TreeLogger logger, LinkerContext context) throws UnableToCompleteException {
		DefaultTextOutput out = new DefaultTextOutput(true);

		out.print("gwtOnLoad(null, __gwt_module, __gwt_base);");

		return out.toString();
	}

	@Override
	public ArtifactSet link(TreeLogger logger, LinkerContext context, ArtifactSet artifacts) throws UnableToCompleteException {
		ArtifactSet newArtifacts = new ArtifactSet(artifacts);

		UUID uuid = UUID.randomUUID();

		// Violate the war dir out of our context
		JJSOptions jjsOptions = violateJJSOptions(logger, context);

		final String xpiDirProperty = System.getProperty("xpi.dir");

		File xpiDir;

		if (xpiDirProperty == null) {
			File warDir = violateWarDir(logger, jjsOptions);
			xpiDir = new File(warDir, "xpi");
			if (!xpiDir.exists()) {
				logger.log(TreeLogger.ERROR, "Unable to locate xpi directory underneath your war directory");
				throw new UnableToCompleteException();
			}
		} else {
			xpiDir = new File(xpiDirProperty).getAbsoluteFile();
		}

		File xpiChromeDir = new File(xpiDir, "chrome");
		if (!xpiChromeDir.exists()) {
			logger.log(TreeLogger.ERROR, "Unable to locate xpi/chrome directory underneath your war directory");
			throw new UnableToCompleteException();
		}

		final SortedSet<CompilationResult> compilationResults = artifacts.find(CompilationResult.class);

		if (compilationResults.size() > 1) {
			logger.log(TreeLogger.ERROR, "More than one compilation result found. Did you fiddle with the user.agent property?");
			throw new UnableToCompleteException();
		}

		boolean isHostedMode = compilationResults.size() == 0;

		ByteArrayOutputStream contentZipBytes = getContentJarBytes(logger, context, uuid, xpiChromeDir, artifacts);

		ByteArrayOutputStream extensionZipBytes = new ByteArrayOutputStream();
		ZipOutputStream extensionZipWriter = new ZipOutputStream(extensionZipBytes);

		try {
			// Write the singleton
			extensionZipWriter.putNextEntry(new ZipEntry("components/singleton.js"));
			String script = readTemplatedClassResource(context, "singleton.js", uuid);
			extensionZipWriter.write(script.getBytes("UTF-8"));
			extensionZipWriter.closeEntry();

			// Write the content jar
			extensionZipWriter.putNextEntry(new ZipEntry("content.jar"));
			extensionZipWriter.write(contentZipBytes.toByteArray());
			extensionZipWriter.closeEntry();

			for (File file : xpiDir.listFiles()) {
				if (file.isDirectory()) {
					if (file.getName().equals("chrome")) {
						continue;
					}

					recursiveAddXpiContentDir(extensionZipWriter, "", file);
				} else if (file.isFile()) {
					addFileToZip(extensionZipWriter, "", file);
				}
			}

			extensionZipWriter.close();
		} catch (IOException e) {
			logger.log(TreeLogger.ERROR, "Unexpected IO error", e);
			throw new UnableToCompleteException();
		}

		if (isHostedMode) {
			// In hosted mode, artifacts are served by the hosted mode server and not zipped
			newArtifacts.addAll(artifacts);
		} else {
			// In production mode, we don't write them to the output directory
			for (Artifact<?> artifact : artifacts) {
				if (!(artifact instanceof EmittedArtifact)) {
					newArtifacts.add(artifact);
				}
			}
		}

		newArtifacts.add(new SyntheticArtifact(ExtensionLinker.class, "extension.xpi", extensionZipBytes.toByteArray()));

		EmittedArtifact indexHtml = new URLArtifact(logger, ExtensionLinker.class, "index.html");
		newArtifacts.add(indexHtml);

		return newArtifacts;
	}

	/**
	 * Writes the XPI chrome folder to an uncompressed JAR.
	 * 
	 * @param uuid
	 */
	private ByteArrayOutputStream getContentJarBytes(TreeLogger logger, LinkerContext context, UUID uuid, File xpiChromeDir,
			final ArtifactSet artifacts) throws UnableToCompleteException {

		final SortedSet<CompilationResult> compilationResults = artifacts.find(CompilationResult.class);
		boolean isHostedMode = compilationResults.size() == 0;

		ByteArrayOutputStream contentZipBytes = new ByteArrayOutputStream();
		ZipOutputStream zipWriter = new ZipOutputStream(contentZipBytes);
		zipWriter.setLevel(ZipOutputStream.STORED);

		// Write the main script
		try {
			final ZipEntry zipEntry = new ZipEntry("chrome/content/script.js");

			zipWriter.putNextEntry(zipEntry);

			// Write the appropriate content script
			if (isHostedMode) {
				String script = readTemplatedClassResource(context, "script.js", uuid);
				zipWriter.write(script.getBytes("UTF-8"));
			} else {
				CompilationResult result = compilationResults.first();
				StringBuffer b = new StringBuffer();
				b.append(getModulePrefix(logger, context));
				for (String str : result.getJavaScript()) {
					b.append(str);
				}
				b.append(getModuleSuffix(logger, context));

				String output = b.toString().replaceAll("__SYMBOL_ID__", result.getStrongName());

				zipWriter.write(output.getBytes("UTF-8"));
			}

			zipWriter.closeEntry();

			recursiveAddXpiContentDir(zipWriter, "chrome/", xpiChromeDir);

			// Write the remaining resources under content/
			for (EmittedArtifact artifact : artifacts.find(EmittedArtifact.class)) {
				zipWriter.putNextEntry(new ZipEntry("chrome/content/" + artifact.getPartialPath()));

				final InputStream contents = artifact.getContents(logger);
				Util.copyNoClose(contents, zipWriter);
				contents.close();

				zipWriter.closeEntry();
			}

			zipWriter.close();
		} catch (IOException e) {
			logger.log(TreeLogger.ERROR, "Unexpected IO error", e);
			throw new UnableToCompleteException();
		}

		return contentZipBytes;
	}

	private String readTemplatedClassResource(LinkerContext context, String filename, UUID uuid) {
		InputStream resource = ExtensionLinker.class.getResourceAsStream(filename);
		String script = Util.readStreamAsString(resource).replaceAll("@moduleName", context.getModuleName()).replaceAll("@guid",
				uuid.toString());

		return script;
	}

	private void recursiveAddXpiContentDir(ZipOutputStream zipWriter, String prefix, File directory) throws IOException {
		// TODO: Document this?
		if (directory.getName().equals(".svn")) {
			return;
		}

		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				recursiveAddXpiContentDir(zipWriter, prefix + file.getName() + "/", file);
			} else if (file.isFile()) {
				addFileToZip(zipWriter, prefix, file);
			}
		}
	}

	private void addFileToZip(ZipOutputStream zipWriter, String prefix, File file) throws IOException, FileNotFoundException {
		zipWriter.putNextEntry(new ZipEntry(prefix + file.getName()));

		final FileInputStream input = new FileInputStream(file);
		Util.copyNoClose(input, zipWriter);
		input.close();

		zipWriter.closeEntry();
	}

	private File violateWarDir(TreeLogger logger, JJSOptions jjsOptions) throws UnableToCompleteException {
		if (jjsOptions instanceof OptionWarDir) {
			final File warDir = ((OptionWarDir) jjsOptions).getWarDir();
			if (warDir == null) {
				logger.log(TreeLogger.ERROR, "No warDir command-line option was specified");
				throw new UnableToCompleteException();
			}

			logger.log(TreeLogger.WARN, "No xpi.dir system property specified, assuming it lives under war/xpi");

			return warDir;
		}

		logger.log(TreeLogger.ERROR, "JJSOptions didn't implement OptionWarDir (broken hack)");
		throw new UnableToCompleteException();
	}

	/**
	 * Violates JJSOptions out of the linker context.
	 */
	private JJSOptions violateJJSOptions(TreeLogger logger, LinkerContext context) throws UnableToCompleteException {
		if (!(context instanceof StandardLinkerContext)) {
			logger.log(TreeLogger.ERROR, "LinkerContext base class changed on us (broken hack)");
			throw new UnableToCompleteException();
		}

		for (Field field : StandardLinkerContext.class.getDeclaredFields()) {
			if (field.getName().equals("jjsOptions")) {
				field.setAccessible(true);
				try {
					return (JJSOptions) field.get(context);
				} catch (IllegalArgumentException e) {
					logger.log(TreeLogger.ERROR, "Unexpected reflection error", e);
					throw new UnableToCompleteException();
				} catch (IllegalAccessException e) {
					logger.log(TreeLogger.ERROR, "Unexpected reflection error", e);
					throw new UnableToCompleteException();
				}
			}
		}

		logger.log(TreeLogger.ERROR, "StandardLinkerContext fields changed on us (broken hack)");
		throw new UnableToCompleteException();
	}

	@Override
	public String getDescription() {
		return "Extension";
	}
}
