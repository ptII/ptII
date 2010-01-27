/* Code generator for the Procedural languages.

Copyright (c) 2009 The Regents of the University of California.
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

package ptolemy.cg.kernel.generic.program.procedural;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
////ProceduralCodeGenerator

/** Base class for Procedural code generator.
*
*  @author Bert Rodiers
*  @version $Id$
*  @since Ptolemy II 7.1
*  @Pt.ProposedRating red (rodiers)
*  @Pt.AcceptedRating red (rodiers)
*/
public class ProceduralCodeGenerator extends ProgramCodeGenerator {

    /** Create a new instance of the ProceduralCodeGenerator.
     *  @param container The container.
     *  @param name The name of the ProceduralCodeGenerator.
     *  @param outputFileExtension The extension of the output file.
     *   (for example c in case of C and java in case of Java)
     *  @param templateExtension The extension of the template files.
     *   (for example c in case of C and j in case of Java).  
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public ProceduralCodeGenerator(NamedObj container, String name,
            String outputFileExtension, String templateExtension)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, outputFileExtension, templateExtension);

        compile = new Parameter(this, "compile");
        compile.setTypeEquals(BaseType.BOOLEAN);
        compile.setExpression("true");

        generateEmbeddedCode = new Parameter(this, "generateEmbeddedCode");
        generateEmbeddedCode.setTypeEquals(BaseType.BOOLEAN);
        generateEmbeddedCode.setExpression("true");
        // Hide the embeddedCode parameter from the user.
        generateEmbeddedCode.setVisibility(Settable.EXPERT);

        generatorPackageList.setExpression("generic.program.procedural");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, then compile the generated code. The default
     *  value is a parameter with the value true.
     */
    public Parameter compile;

    /** If true, then generate code for that uses the reflection for Java
     *  and JNI for C and is embedded within the model 
     *  The default value is false and this parameter is not usually
     *  editable by the user.  This parameter is set to true when
     *  CompiledCompositeActor is run in an interpreted Ptolemy model.
     *  This parameter
     *  is set to false when a model contains one or more
     *  CompiledCompositeActors and C or Java code is being generated for the
     *  model.
     */
    public Parameter generateEmbeddedCode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an include command line argument the compile command.
     *  @param includeCommand  The include command, for example
     *  "-I/usr/local/include".
     */
    public void addInclude(String includeCommand) {
        _includes.add(includeCommand);
    }

    /** Add a library command line argument the compile command.
     *  @param libraryCommand  The library command, for example
     *  "-L/usr/local/lib".
     *  @see #addLibraryIfNecessary(String)
     */
    public void addLibrary(String libraryCommand) {
        _libraries.add(libraryCommand);
    }

    /** If the compile command does not yet containe a library,
     *         add a library command line argument the compile command.
     *
     *  @param libraryCommand  The library command, for example
     *  "-L/usr/local/lib".
     *  @see #addLibrary(String)
     */
    public void addLibraryIfNecessary(String libraryCommand) {
        if (!_libraries.contains(libraryCommand)) {
            _libraries.add(libraryCommand);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Reset the code generator.
     */
    protected void _reset() {
        super._reset();

        _includes.clear();
        _libraries.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Set of include command line arguments where each element is
     *  a string, for example "-I/usr/local/include".
     */
    protected Set<String> _includes = new HashSet<String>();

    /** List of library command line arguments where each element is
     *  a string, for example "-L/usr/local/lib".
     *  This variable is a list so as to preserve the order that the
     *  library commands were added to the list of libraries matters,
     *  see the manual page for the -L option of the ld command.
     */
    protected List<String> _libraries = new LinkedList<String>();

}
