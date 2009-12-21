package com.dotspots.mozilla.rebind;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.SortedMap;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.AbstractLinker;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.SelectionProperty;
import com.google.gwt.core.ext.linker.SyntheticArtifact;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.impl.HostedModeLinker;
import com.google.gwt.dev.About;
import com.google.gwt.dev.util.DefaultTextOutput;

@LinkerOrder(Order.PRIMARY)
public class ExtensionLinker extends AbstractLinker {
	protected String getModulePrefix(TreeLogger logger, LinkerContext context) throws UnableToCompleteException {
		DefaultTextOutput out = new DefaultTextOutput(true);

		// Setup the well-known variables.
		//
		out.print("/* Linked: " + new Date() + " */");
		out.newline();
		out.print("var $symbol_id = \"__SYMBOL_ID__\";");
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

		EmittedArtifact hostedHtml = new URLArtifact(logger, HostedModeLinker.class, "hosted.html");
		newArtifacts.add(hostedHtml);

		EmittedArtifact extensionHostedHtml = new URLArtifact(logger, ExtensionLinker.class, "extension-hosted.html");
		newArtifacts.add(extensionHostedHtml);

		for (CompilationResult compilationResult : artifacts.find(CompilationResult.class)) {
			// Only emit the gecko 1.8 version
			for (SelectionProperty property : context.getProperties()) {
				if (property.getName().equals("user.agent")) {
					String value = property.tryGetValue();
					if (value != null && value.equals("gecko1_8")) {
						newArtifacts.addAll(emitResult(logger, context, compilationResult));
						return newArtifacts;
					}

					for (SortedMap<SelectionProperty, String> props : compilationResult.getPropertyMap()) {
						// Only emit the gecko 1.8 version
						String perResultValue = props.get(property);
						if (perResultValue != null && perResultValue.equals("gecko1_8")) {
							newArtifacts.addAll(emitResult(logger, context, compilationResult));
							return newArtifacts;
						}
					}
				}
			}
		}

		return newArtifacts;
	}

	private Collection<? extends EmittedArtifact> emitResult(TreeLogger logger, LinkerContext context, CompilationResult result)
			throws UnableToCompleteException {
		StringBuffer b = new StringBuffer();
		b.append(getModulePrefix(logger, context));
		for (String str : result.getJavaScript()) {
			b.append(str);
		}
		b.append(getModuleSuffix(logger, context));

		final SyntheticArtifact script = emitString(logger, b.toString().replaceAll("__SYMBOL_ID__", result.getStrongName()), "script.js");

		return Arrays.asList(script);
	}

	@Override
	public String getDescription() {
		return "Extension";
	}
}
