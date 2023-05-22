package org.hibernate.doclet.asciidoc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.StandardDoclet;

public class Asciidoclet extends StandardDoclet {

	private final List<String> links = new ArrayList<>();
	private final Map<String, String> offlineLinks = new LinkedHashMap<>();
	private LinksHelper linksHelper;
	@Override
	public String getName() {
		// For this doclet, the name of the doclet is just the
		// simple name of the class. The name may be used in
		// messages related to this doclet, such as in command-line
		// help when doclet-specific options are provided.
		return getClass().getSimpleName();
	}

	@Override
	public Set<Option> getSupportedOptions() {
		Set<Option> supportedOptions = new HashSet<>();

		for ( Option option : super.getSupportedOptions() ) {
			if ( option.getNames().contains( "-link" ) ) {
				supportedOptions.add( new DelegatingOption( option ) {
					@Override
					protected void doProcess(String opt, List<String> arguments) {
						links.add( arguments.get( 0 ) );
					}
				} );
			}
			else if ( option.getNames().contains( "-linkoffline" ) ) {
				supportedOptions.add( new DelegatingOption( option ) {
					@Override
					protected void doProcess(String opt, List<String> arguments) {
						offlineLinks.put( arguments.get( 0 ), arguments.get( 1 ) );
					}
				} );
			}
			else {
				supportedOptions.add( option );
			}
		}

		return supportedOptions;
	}

	@Override
	public boolean run(DocletEnvironment environment) {
		this.linksHelper = new LinksHelper( environment.getElementUtils(), links, offlineLinks );
		return super.run( environment );
	}

	public LinksHelper linksHelper() {
		return linksHelper;
	}

	private abstract static class DelegatingOption implements Option {

		private final Option option;

		private DelegatingOption(Option option) {
			this.option = option;
		}

		@Override
		public int getArgumentCount() {
			return option.getArgumentCount();
		}

		@Override
		public String getDescription() {
			return option.getDescription();
		}

		@Override
		public Kind getKind() {
			return option.getKind();
		}

		@Override
		public List<String> getNames() {
			return option.getNames();
		}

		@Override
		public String getParameters() {
			return option.getParameters();
		}

		@Override
		public boolean process(String opt, List<String> arguments) {
			doProcess( opt, arguments );
			return option.process( opt, arguments );
		}

		protected abstract void doProcess(String opt, List<String> arguments);
	}

}
