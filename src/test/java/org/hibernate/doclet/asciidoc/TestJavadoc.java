package org.hibernate.doclet.asciidoc;

import org.hibernate.doclet.asciidoc.other.AnotherDocClass;
import org.hibernate.doclet.asciidoc.other.OtherDocClass;

/**
 * @asciidoc some text in `asciidoc`
 *
 * {@link java.util.Objects#checkFromIndexSize(int, int, int)}
 *
 * [source,java]
 * ----
 * var a = new FileOutputStream("file.name");
 * ----
 *
 * Link to some docs javadoc:InnerClass#CONSTANT[package=pkg, class=InnerClass, variable=CONSTANT]
 * {@link AnotherDocClass.Inner#method(String)}
 *
 * Another section with
 * Links {@link InnerClass#CONSTANT} - link to a constant
 * {@link InnerClass#getInstance() some text} - link to a static method with a text
 * {@link InnerClass#methodWithParameters(String, Integer)} - method with parameters.
 * {@link OtherDocClass#OTHER_CONSTANT} and {@link AnotherDocClass#ANOTHER_CONSTANT}
 *
 * and then some text after links
 *
 * @see String#replace(char, char)
 */
public class TestJavadoc {

	/**
	 * {@link StringBuilder for reference} generated by regular javadoc.
	 *
	 * @asciidoc more asciidoc javadoc with a link to a jdk class {@link StringBuilder}
	 */
	public void methodName() {
	}


	/**
	 * @asciidoc
	 *
	 * Marker interface for Fetches that are actually references to
	 * another fetch based on "normalized navigable path"
	 *
	 * The following query is used throughout the javadocs for these impls
	 * to help describe what it going on and why certain methods do certain things.
	 *
	 * [source,java,subs=attributes+]
	 * ----
	 * {@literal @}Entity
	 * class Person {
	 *     ...
	 *     {@literal @}ManyToOne (mappedBy="owner")
	 *     Address getAddress() {...}
	 * }
	 *
	 * {@literal @}Entity
	 * class Address {
	 *     ...
	 *     {@literal @}ManyToOne
	 *     Person getOwner() {...}
	 * }
	 *
	 * from Person p
	 * 		join fetch p.address a
	 * 		join fetch a.owner o
	 * 		join fetch o.address oa
	 * ----
	 *
	 *
	 * Here we have one root result and 3 fetches.  2 of the fetches are bi-directional:
	 *
	 * 		`o`:: The paths `p` and `p.address.owner` (aliased as `o`) are the same table reference in SQL terms
	 * 		`oa`:: The paths `p.address` and `p.address.owner.address` (aliased as `oa`) are again the same table reference
	 *
	 * @author Steve Ebersole
	 */
	public static class InnerClass {
		public static final String CONSTANT = "some value";

		/**
		 * Constructor
		 */
		InnerClass() {

		}

		/**
		 * method no params
		 */
		public boolean isSomething() {
			return true;
		}

		/**
		 * method with params
		 */
		public void methodWithParameters(String string, Integer integer) {

		}

		/**
		 * static method
		 *
		 * @return an instance
		 */
		public static InnerClass getInstance() {
			return new InnerClass();
		}
	}
}
