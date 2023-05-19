package org.hibernate.doclet.asciidoc;

import static org.asciidoctor.extension.InlineMacroProcessor.REGEXP;
import static org.asciidoctor.jruby.AsciidoctorJRuby.Factory.create;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

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
	}

	@Override
	public String toString(List<? extends DocTree> tags, Element element) {
		String text = getText( tags.get( 0 ), element );
		return asciidoctor.convert( text, Options.builder().build() );
	}

	private String getText(DocTree doc, Element element) {
		String elementBase;
		if ( !( ElementKind.CLASS.equals( element.getKind() )
				|| ElementKind.INTERFACE.equals( element.getKind() )
				|| ElementKind.ENUM.equals( element.getKind() ) ) ) {
			elementBase = element.getEnclosingElement().toString();
		}
		else {
			elementBase = element.toString();
		}

		return new AsciidocTagDocTreeVisitor( element, elementBase ).visit( doc, null );
	}

	private class AsciidocTagDocTreeVisitor extends SimpleDocTreeVisitor<String, Void> {
		private final Element element;
		private final String elementBase;

		public AsciidocTagDocTreeVisitor(Element element, String elementBase) {
			this.element = element;
			this.elementBase = elementBase;
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


			String href;
			if ( !( ElementKind.CLASS.equals( referenceElement.getKind() )
					|| ElementKind.INTERFACE.equals( referenceElement.getKind() )
					|| ElementKind.ENUM.equals( referenceElement.getKind() ) ) ) {
				href = subLink(
						elementBase,
						referenceElement.getEnclosingElement().toString()
				) + ".html#" + referenceElement;
			}
			else {
				href = subLink( elementBase, referenceElement.toString() );
			}

			String label = getLinkLabel( node.getLabel(), referenceElement );

			return "javadoc:" + "stub" + "[ href=" + href + ", label=" + label + "]";
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
			labels.forEach( l -> {
				l.accept( new SimpleDocTreeVisitor<String, Void>() {
					@Override
					public String visitText(TextTree node, Void unused) {
						sb.append( node.getBody() );
						return super.visitText( node, unused );
					}
				}, null );
			} );

			String label = sb.toString();
			if ( label.isBlank() ) {
				label = referenceElement.getSimpleName().toString();
			}
			return label;
		}

		/* when we have a link reference in javadoc we need to build a relative link from a place where we are (where the reference lives in the doc)
		 * to where we are pointing. So we want to remove the leading matches to end up with less ../.. in the href.
		 * */
		private String subLink(String a, String b) {
			StringBuilder result = new StringBuilder();
			String[] current = a.split( "\\." );
			String[] ref = b.split( "\\." );

			int index;
			for ( index = 0; index < current.length && index < ref.length; index++ ) {
				if ( !current[index].equals( ref[index] ) ) {
					break;
				}
			}
			if ( index == 0 ) {
				// means no match at the start, most likely we are dealing with a reference to some external lib or to JDK class...
				// TODO: figure out what can be done here ...
				// result.append( "some-external-javadoc-path-to-who-knows-where" );
				result.append( '/' );
			}
			else {
				for ( int i = index - 1; i < current.length; i++ ) {
					result.append( ".." );
					if ( result.length() > 0 ) {
						result.append( '/' );
					}
				}
			}
			for ( int i = Math.max( index - 1, 0 ); i < ref.length; i++ ) {
				result.append( ref[i] );
				if ( i != ref.length - 1 ) {
					result.append( '/' );
				}
			}

			return result.toString();
		}
	}
}
