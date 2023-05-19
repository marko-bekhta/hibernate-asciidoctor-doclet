package org.hibernate.doclet.asciidoc;

import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.StandardDoclet;

// TODO: figure out how to register styles from asciidoc so they are included in the final javadocs
public class Asciidoclet extends StandardDoclet {

	@Override
	public String getName() {
		// For this doclet, the name of the doclet is just the
		// simple name of the class. The name may be used in
		// messages related to this doclet, such as in command-line
		// help when doclet-specific options are provided.
		return getClass().getSimpleName();
	}

	@Override
	public boolean run(DocletEnvironment environment) {
		return super.run( environment );
	}
}
