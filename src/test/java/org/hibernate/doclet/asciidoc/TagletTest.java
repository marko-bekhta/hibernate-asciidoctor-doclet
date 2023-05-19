package org.hibernate.doclet.asciidoc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.tools.DiagnosticCollector;
import javax.tools.DocumentationTool;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;

public class TagletTest {

	@Test
	public void simple() throws Exception {
		File tagletCompiledSources = directoryInTarget( "classes" );

		DocumentationTool tool = ToolProvider.getSystemDocumentationTool();
		try ( StandardJavaFileManager fm = tool.getStandardFileManager( null, null, null ) ) {
			JavaFileObject tesJavadocSourceFile = fm.getJavaFileObjects( getSourceFile( TestJavadoc.class ) )
					.iterator().next();

			File renderedJavadocsLocation = directoryInTarget( "javadocs" );
			fm.setLocation(
					DocumentationTool.Location.DOCUMENTATION_OUTPUT, Arrays.asList( renderedJavadocsLocation ) );
			fm.setLocation( DocumentationTool.Location.TAGLET_PATH, Arrays.asList( tagletCompiledSources ) );
			Iterable<? extends JavaFileObject> files = Arrays.asList( tesJavadocSourceFile );
			Iterable<String> options = Arrays.asList(
					// the taglet itself
					"-taglet", AsciidocTaglet.class.getName(),
					// link to external javadocs, we'll point to AsciidoctorJ and JDK
					// -linkoffline  extdocURL  packagelistLoc
					"-link", "https://docs.oracle.com/en/java/javase/11/docs/api/",
					"-linkoffline", "https://javadoc.io/doc/org.asciidoctor/asciidoctorj/2.5.8/", target() + "/dependencies-javadoc/asciidoctorj"
			);

			StringWriter docsGenerationOutput = new StringWriter();
			DiagnosticCollector<JavaFileObject> diagnosticListener = new DiagnosticCollector<>();
			DocumentationTool.DocumentationTask task = tool.getTask(
					new PrintWriter( docsGenerationOutput ), fm, diagnosticListener, null, options, files );

			assertTrue( task.call(), diagnosticListener.getDiagnostics().toString() );

			System.err.println( ">>>>>>>>>>>>>>>>>>>>>>>>>>" );
			System.err.println( docsGenerationOutput );
			System.err.println( "<<<<<<<<<<<<<<<<<<<<<<<<<<" );

			File f = new File( renderedJavadocsLocation, getJavadocFileName( TestJavadoc.class ) );
			String doc = Files.readAllLines( f.toPath(), Charset.defaultCharset() )
					.stream().collect( Collectors.joining( "\n" ) );

			assertTrue( doc.contains( "<code>asciidoc</code>" ) );
		}
	}

	private File directoryInTarget(String name) throws IOException {
		String targetClassesDir = TagletTest.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		return Files.createDirectories( new File( targetClassesDir ).getParentFile().toPath().resolve( name ) )
				.toFile();
	}

	public File getSourceFile(Class<?> clazz) {
		String sourceFileName = File.separator + clazz.getName().replace( ".", File.separator ) + ".java";
		return new File( sourceDir() + sourceFileName ).getAbsoluteFile();
	}

	public String getJavadocFileName(Class<?> clazz) {
		return File.separator + clazz.getName().replace( ".", File.separator ) + ".html";
	}

	public static File sourceDir() {
		// target/test-classes
		return target().getParent().resolve( "src/test/java" ).toFile();
	}

	public static Path target() {
		String targetClassesDir = TagletTest.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		return new File( targetClassesDir ).getParentFile().getAbsoluteFile().toPath();
	}
}
