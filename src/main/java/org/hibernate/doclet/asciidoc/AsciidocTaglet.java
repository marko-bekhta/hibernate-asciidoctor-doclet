package org.hibernate.doclet.asciidoc;

import static org.asciidoctor.extension.InlineMacroProcessor.REGEXP;
import static org.asciidoctor.jruby.AsciidoctorJRuby.Factory.create;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.SimpleDocTreeVisitor;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Taglet;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;

/**
 * Taglet that should work with a {@code @asciidoc} block tags.
 * Everything that follows this tag will be converted using Asciidoctor.
 * For including any javadoc links like a standard inline tag {@code {@link something} } can be used.
 */
public class AsciidocTaglet implements Taglet {
	private static final EnumSet<Location> LOCATIONS = EnumSet.allOf( Location.class );

	private final Asciidoctor asciidoctor;
	private DocletEnvironment environment;
	private LinksHelper linksHelper;

	public AsciidocTaglet() {
		asciidoctor = create();
		// Map should be modifiable (seems like asciidoc internals like to change it.
		// Specifying regexp here to find the text-snippets to replace, as things aren't picked up automagically ...
		Map<String, Object> config = new HashMap<>();
		config.put( REGEXP, "javadoc:([A-Za-z0-9#.$]+)\\[(.*?)\\]" );
		asciidoctor.javaExtensionRegistry().inlineMacro( new JavadocLinkMacroProcessor( "javadoc", config ) );
	}

	@Override
	public Set<Location> getAllowedLocations() {
		return LOCATIONS;
	}

	@Override
	public boolean isInlineTag() {
		return false;
	}

	@Override
	public String getName() {
		return "asciidoc";
	}

	@Override
	public void init(DocletEnvironment env, Doclet doclet) {
		// env provides access to various utils
		this.environment = env;
		if ( doclet instanceof Asciidoclet ) {
			linksHelper = ( (Asciidoclet) doclet ).linksHelper();
		}
		else {
			linksHelper = new LinksHelper( env.getElementUtils(), List.of(), Map.of() );
		}
	}

	@Override
	public String toString(List<? extends DocTree> tags, Element element) {
		String text = getText( tags.get( 0 ), element );
		return asciidoctor.convert( text, Options.builder()
				.attributes( Attributes.builder()
						.sourceHighlighter( "coderay" )
						// using inline styles -- so that we don't need to include a css file in the doclet.
						// if we'd want to customize it -- we can switch to `class` and then copy css file to the output dir
						// using a file manager.
						.attribute( "coderay-css", "style" )
						.copyCss( true )
						.build()
				).build() );
	}

	private String getText(DocTree doc, Element element) {
		return new AsciidocTagDocTreeVisitor( element ).visit( doc, null );
	}

	private class AsciidocTagDocTreeVisitor extends SimpleDocTreeVisitor<String, Void> {
		private final Element element;

		public AsciidocTagDocTreeVisitor(Element element) {
			this.element = element;
		}

		@Override
		public String visitUnknownBlockTag(UnknownBlockTagTree node, Void p) {
			StringBuilder sb = new StringBuilder();
			for ( DocTree dt : node.getContent() ) {
				sb.append( dt.accept( this, null ) );
			}
			return sb.toString();
		}

		@Override
		public String visitText(TextTree node, Void p) {
			return node.getBody().replaceAll( "(?m)^[ \t]", "" );
		}

		@Override
		public String visitLink(LinkTree node, Void unused) {
			ReferenceTree reference = node.getReference();

			Element referenceElement = environment.getDocTrees().getElement(
					DocTreePath.getPath(
							environment.getDocTrees().getPath( element ),
							environment.getDocTrees().getDocCommentTree( element ),
							reference
					) );

			String href = linksHelper.javadocLink( referenceElement, element );
			String label = getLinkLabel( node.getLabel(), referenceElement );

			return "javadoc:" + "stub" + "[ href=" + href + ", label=" + label + "] ";
		}

		/*
		 * If we have a link with a label we want to get that label to be placed in between a tag.
		 * Otherwise, we just use whatever the reference element points to as a label.
		 */
		@Override
		protected String defaultAction(DocTree node, Void p) {
			return "";
		}

		private String getLinkLabel(List<? extends DocTree> labels, Element referenceElement) {
			StringBuilder sb = new StringBuilder();
			labels.forEach( l -> l.accept( new SimpleDocTreeVisitor<String, Void>() {
				@Override
				public String visitText(TextTree node, Void unused) {
					sb.append( node.getBody() );
					return super.visitText( node, unused );
				}
			}, null ) );

			String label = sb.toString();
			if ( label.isBlank() ) {
				label = referenceElement.getSimpleName().toString();
			}
			return label;
		}
	}
}
