/* Base class for code generator adapter.

 Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic;

import ptolemy.kernel.DecoratedAttributesImplementation;
import ptolemy.kernel.util.DecoratedAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////////
//// CodeGeneratorAdapter

/**
* Base class for code generator adapter.
*
* <p>Subclasses should override generateFireCode(),
* generateInitializeCode() generatePostfireCode(),
* generatePreinitializeCode(), and generateWrapupCode() methods by
* appending a corresponding code block.
*
* <p>Subclasses should be sure to properly indent the code by
* either using the code block functionality in methods like
* _generateBlockCode(String) or by calling
* {@link ptolemy.codegen.kernel.CodeStream#indent(String)},
* for example:
* <pre>
*     StringBuffer code = new StringBuffer();
*     code.append(super.generateWrapupCode());
*     code.append("// Local wrapup code");
*     return processCode(CodeStream.indent(code.toString()));
* </pre>
*
* @author Bert Rodiers
* @version $Id$
* @since Ptolemy II 7.1
* @Pt.ProposedRating Red (rodiers)
* @Pt.AcceptedRating Red (rodiers)
*/
//FIXME: Why extend NamedObj? Extend Attribute and store in the actor being adapted?
abstract public class CodeGeneratorAdapter extends NamedObj {

    //FIXME: Overhaul comments.
    
    /** Create and return the decorated attributes for the corresponding Ptolemy Component.
     *  @param target The NamedObj that will be decorated.
     *  @param genericCodeGenerator The code generator that is the decorator for the
     *  corresponding Ptolemy Component.
     *  @return The decorated attributes.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public DecoratedAttributes createDecoratedAttributes(NamedObj target, GenericCodeGenerator genericCodeGenerator) throws IllegalActionException, NameDuplicationException {
            return new DecoratedAttributesImplementation(target, genericCodeGenerator);
    }

    /**
     * Generate sanitized name for the given named object. Remove all
     * underscores to avoid conflicts with systems functions.
     * @param namedObj The named object for which the name is generated.
     * @return The sanitized name.
     */
    public static String generateName(NamedObj namedObj) {
        String name = StringUtilities.sanitizeName(namedObj.getFullName());

        // FIXME: Assume that all objects share the same top level. In this case,
        // having the top level in the generated name does not help to
        // expand the name space but merely lengthen the name string.
        //        NamedObj parent = namedObj.toplevel();
        //        if (namedObj.toplevel() == namedObj) {
        //            return "_toplevel_";
        //        }
        //        String name = StringUtilities.sanitizeName(namedObj.getName(parent));
        if (name.startsWith("_")) {
            name = name.substring(1, name.length());
        }
        return name.replaceAll("\\$", "Dollar");
    }

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(GenericCodeGenerator)
     */
    abstract public GenericCodeGenerator getCodeGenerator();
    
    /** Set the code generator associated with this adapter class.
     *  @param codeGenerator The code generator associated with this
     *   adapter class.
     *  @see #getCodeGenerator()
     */
    abstract public void setCodeGenerator(GenericCodeGenerator codeGenerator);
    
    /** Set the current type of the decorated attributes.
     *  The type information of the parameters are not saved in the
     *  model hand hence this has to be reset when reading the model
     *  again.
     *  @param decoratedAttributes The decorated attributes
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     */
    public void setTypesOfDecoratedVariables(
            DecoratedAttributes decoratedAttributes) throws IllegalActionException {        
    }

}
