package org.hibernate.doclet.asciidoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public final class LinksHelper {

	private static final String MODULE_PREFIX = "module:";
	private static final String ELEMENT_LIST = "element-list";
	private static final String PACKAGE_LIST = "package-list";

	private final Map<String, String> links = new HashMap<>();
	private final Elements elements;

	public LinksHelper(Elements elements, List<String> links, Map<String, String> offlineLinks) {
		this.elements = elements;
		for ( String link : links ) {
			link( link );
		}
		for ( Map.Entry<String, String> entry : offlineLinks.entrySet() ) {
			offlineLink( entry.getKey(), entry.getValue() );
		}
	}

	public void link(String link) {
		try {
			URL url = new URI( link ).resolve( ELEMENT_LIST ).toURL();
			readElementList( url.openStream(), link );
		}
		catch (URISyntaxException | MalformedURLException e) {
			throw new RuntimeException( "Unable to read the element list from the base link: " + link, e );
		}
		catch (IOException exc) {
			try {
				URL url = new URI( link ).resolve( PACKAGE_LIST ).toURL();
				readElementList( url.openStream(), link );
			}
			catch (URISyntaxException | IOException e) {
				throw new RuntimeException( "Unable to read the package list from the base link: " + link, e );
			}
		}
	}

	public void offlineLink(String link, String path) {
		Path p = Path.of( path );
		if ( !p.toFile().exists() ) {
			throw new RuntimeException( "Offline Javadoc path '" + path + "' does not exists." );
		}

		try {
			readElementList( Files.newInputStream( p.resolve( ELEMENT_LIST ) ), link );
		}
		catch (IOException e) {
			try {
				readElementList( Files.newInputStream( p.resolve( PACKAGE_LIST ) ), link );
			}
			catch (IOException ex) {
				throw new RuntimeException(
						"Unable to read the package list from the path '" + path + "', for the base link: " + link, e );
			}
		}
	}

	private void readElementList(InputStream input, String path) throws IOException {
		try ( BufferedReader in = new BufferedReader( new InputStreamReader( input ) ) ) {
			String element = null;
			while ( ( element = in.readLine() ) != null ) {
				if ( element.length() > 0 && ( !element.startsWith( MODULE_PREFIX ) )) {
						String pkg = element.replace( '.', '/' );
						links.put( element, path + pkg );

				}
			}
		}
	}

	public String javadocLink(Element referenceElement, Element element) {
		return links.getOrDefault(
				elements.getPackageOf( element ).toString(),
				relativeLink( element )
		) + link( referenceElement );
	}

	public String relativeLink(Element element) {
		String[] locationBase = enclosingType( element ).split( "\\." );

		return IntStream.range( 1, locationBase.length ).mapToObj( i -> ".." )
				.collect( Collectors.joining( "/", "", "/" ) );
	}

	private String link(Element referenceElement) {
		String result = "";
		Element element = referenceElement;

		while ( !( element instanceof TypeElement ) ) {
			result = element.toString() + result;
			element = element.getEnclosingElement();
		}
		String fileName = "";
		PackageElement pkg = elements.getPackageOf( referenceElement );
		Element iter = element;
		while ( !pkg.equals( iter ) ) {
			fileName = iter.getSimpleName() + "." + fileName;
			iter = iter.getEnclosingElement();
		}
		String base = pkg.getQualifiedName().toString();
		Name typeQualifiedName = ( (TypeElement) element ).getQualifiedName();

		return base.isEmpty() ? "/" : typeQualifiedName.toString().substring( 0, base.length() + 1 ).replace( '.', '/' )
				+ fileName + "html#"
				// escaping the comma since Asciidoctor will treat it as separator between macro attributes
				+ result.replace( ",", "&comma;" );
	}

	private String enclosingType(Element element) {
		while ( !( element == null || element instanceof TypeElement ) ) {
			element = element.getEnclosingElement();
		}
		return element == null ? "" : element.toString();
	}

}
