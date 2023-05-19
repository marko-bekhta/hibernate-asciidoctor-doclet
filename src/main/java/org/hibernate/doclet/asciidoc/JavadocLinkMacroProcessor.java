package org.hibernate.doclet.asciidoc;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;

/**
 * We'll use this processor to replace "placeholders" generated from {@code {@link something} }, or if someone would
 * add them directly in the docs.
 */
@Name("javadoc")
public class JavadocLinkMacroProcessor extends InlineMacroProcessor {

	public JavadocLinkMacroProcessor() {
	}

	public JavadocLinkMacroProcessor(String macroName) {
		super( macroName );
	}

	public JavadocLinkMacroProcessor(String macroName, Map<String, Object> config) {
		super( macroName, config );
	}

	@Override
	public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
		String href = (String) attributes.get( "href" );

		Map<String, Object> options = new HashMap<>();
		options.put( "type", ":link" );
		options.put( "target", href );
		return createPhraseNode( parent, "anchor", (String) attributes.get( "label" ), attributes, options );
	}
}
