package org.hibernate.doclet.asciidoc.other;

/**
 * @asciidoc
 *
 * Some plain old {@code code tags} can be used as well if someone feels like it.
 *
 * [source,javascript]
 * ----
 * function OnUpdate(doc, meta) {
 *   var strong = 70;
 *   var stmt =
 *     SELECT *                  // {@literal <1>}
 *     FROM `beer-samples`       // {@literal <2>}
 *     WHERE abv {@literal >} $strong;
 *   for (var beer of stmt) {    // {@literal <3>}
 *     break;                    // {@literal <4>}
 *   }
 * }
 * ----
 * {@literal <1> } N1QL queries are embedded directly.
 * {@literal <2> } Token escaping is standard N1QL style.
 * {@literal <3> } Stream results using 'for' iterator.
 * {@literal <4> } Cancel streaming query by breaking out.
 *
 * Other callouts:
 *
 * [source,ruby]
 * ----
 * require 'sinatra' {@literal <1>}
 *
 * get '/hi' do {@literal <2>} {@literal <3>}
 *   "Hello World!"
 * end
 * ----
 * {@literal <1> } Library import
 * {@literal <2> } URL mapping
 * {@literal <3> } Response block
 */

public class OtherDocClass {

	/**
	 * some regular javadoc
	 */
	public static final String OTHER_CONSTANT = "bbb";
}
