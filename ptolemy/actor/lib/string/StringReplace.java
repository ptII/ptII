/* Replace an instance of a string with another input string according
 to a regular expression.

 Copyright (c) 2003-2010 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.actor.lib.string;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// StringReplace

/**
 On each firing, look for instances of the pattern specified by <i>pattern</i>
 in <i>stringToEdit</i> and replace them with the string given by
 <i>replacement</i>.  If <i>replaceAll</i> is true, then replace
 all instances that match <i>pattern</i>.  Otherwise, replace only
 the first instance that matches.  If there is no match, then the
 output is the string provided by <i>stringToEdit</i>, unchanged.
 The <i>pattern</i> is given by a regular expression.
 For a reference on regular expression syntax see:
 <a href="http://download.oracle.com/javase/tutorial/essential/regex/#in_browser">
 http://download.oracle.com/javase/tutorial/essential/regex/</a>.

 <p>
 The <i>replacement</i> string, as usual with string-valued parameters
 in Ptolemy II, can include references to parameter values in scope.
 E.g., if the enclosing composite actor has a parameter named "x"
 with value 1, say, then the replacement string a${x}b will become
 "a1b".
 <p>
 In addition, the <i>replacement</i> string can reference the pattern
 that is matched using the syntax "$$0".  For example, the regular
 expression "t[a-z]+" in <i>pattern</i> will match the character t followed by a
 sequence of one or more lower-case letters.
 If <i>replacement</i> is "p$$0" then "this is a test" becomes
 "pthis is a ptest".
 <p>
 If the <i>pattern</i> containes parenthesized subpatterns, such
 as "(t[a-z]+)|(T([a-z]+))", then the value of <i>replacement</i>
 can reference the match of each parenthesized subpattern with
 the syntax "$$n", where "n" is an integer between 1 and 9.
 For example, if <i>pattern</i>="(t[a-z]+)|(T([a-z]+))"
 and <i>replacement</i>="p$$1$$3", then "this is a Test" becomes
 "pthis is a pest". The index "n" corresponds to the order
 of opening parentheses in the pattern.
 <p>
 To get a "$" into the replacement string, use
 "\$$".  To get a "\" into the replacement string, use "\\".

 @author Antonio Yordan-Nones, Neil E. Turner, Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (djstone)
 @Pt.AcceptedRating Green (net)
 */
public class StringReplace extends StringSimpleReplace {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StringReplace(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        replaceAll = new Parameter(this, "replaceAll");
        replaceAll.setExpression("true");
        replaceAll.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** When the boolean value is true, replace all instances that match the
     *  pattern, and when false, replace the first instance.
     */
    public Parameter replaceAll;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to compile a regular expression when
     *  it is changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>pattern</i> and the regular expression fails to
     *   compile.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == pattern) {
            try {
                String patternValue = ((StringToken) pattern.getToken())
                        .stringValue();
                _pattern = Pattern.compile(patternValue);
            } catch (PatternSyntaxException ex) {
                String patternValue = ((StringToken) pattern.getToken())
                        .stringValue();
                throw new IllegalActionException(this, ex,
                        "Failed to compile regular expression \""
                                + patternValue + "\"");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Perform pattern matching and substring replacement, and output
     *  the modified string. If no match is found, output the
     *  unmodified stringToEdit string.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        // We don't call super.fire because we use a regex here.
        //super.fire();
        if (_debugging) {
            _debug("Called fire()");
        }
        replacement.update();
        stringToEdit.update();
        pattern.update();

        String replacementValue = ((StringToken) replacement.getToken())
                .stringValue();
        String stringToEditValue = ((StringToken) stringToEdit.getToken())
                .stringValue();
        boolean replaceAllTokens = ((BooleanToken) replaceAll.getToken())
                .booleanValue();

        Matcher match = _pattern.matcher(stringToEditValue);
        String outputString;

        if (replaceAllTokens) {
            outputString = match.replaceAll(replacementValue);
        } else {
            outputString = match.replaceFirst(replacementValue);
        }

        output.send(0, new StringToken(outputString));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The compiled regular expression.
    private Pattern _pattern;
}
