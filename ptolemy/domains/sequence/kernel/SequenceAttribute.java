/* SequenceAttribute is a subclass of Parameter with support for integerToken.

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
package ptolemy.domains.sequence.kernel;

import java.util.Collection;
import java.util.regex.Pattern;

import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////////
//// SequenceAttribute

/**
 * The sequence number for actor in the sequence domain.
 * This parameter stores an integer value that is required to be unique within
 * the portion of the model under the control of a single SequenceDirector.
 * Duplicate sequence numbers will trigger an exception.
 * FIXME: Shouldn't these numbers be forced to be unique, like by their
 * position in the XML file?
 * @author Elizabeth Latronico (Bosch)
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (beth)
 * @Pt.AcceptedRating Red (beth)
 */
public class SequenceAttribute extends Parameter implements Comparable {
    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public SequenceAttribute() {
        super();

    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public SequenceAttribute(Workspace workspace) {
        super(workspace);

    }

    /** Construct an attribute with the given name contained by the specified
     *  container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     *   type is set to Integer for sequence number
     */
    public SequenceAttribute(NamedObj container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setTypeEquals(BaseType.INT);

        _attachText("_iconDescription", "<svg>\n" + "<line x=\"-30\" y=\"-2\" "
                + "width=\"60\" height=\"4\" " + "style=\"fill:red\"/>\n"
                + "<rect x=\"15\" y=\"-10\" " + "width=\"4\" height=\"20\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
        
     }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Implement compareTo method to compare sequence numbers 
     *  Only the sequence numbers are compared (independent of any process name).
     * 
     *  Object may be a SequenceAttribute or a ProcessAttribute
     *   @param obj The SequenceAttribute object.
     *  @return 0 if the sequence numbers are the same.
     */
    public int compareTo(Object obj) {
            
            int seq1 = this.getSequenceNumber();
            int seq2 = 0;
             
        // Check for either a SequenceAttribute or ProcessAttribute (which is a SequenceAttribute)
        if (obj instanceof SequenceAttribute) {
            // If the second object is a ProcessAttribute, use the correct getSequenceNumber()
                // FIXME:  Is this needed, or is it OK just to use (SequenceAttribute) x.getSequenceNumber()?
                // FIXME:  This is bad coding style, because SequenceAtribute should not know about 
                // its subclass ProcessAttribute - refactor?
            if (obj instanceof ProcessAttribute) {
                    seq2 = ((ProcessAttribute) obj).getSequenceNumber();
            } else {
                    seq2 = ((SequenceAttribute) obj).getSequenceNumber();
            }
            
            if (seq1 < seq2) {
                return -1;
            } else if (seq1 > seq2) {
                return 1;
            } else {
                return 0;
            }
        }
            throw new IllegalArgumentException(
                    "SequenceAttribute can only be compared to other" +
                    " instances of SequenceAttribute.");
    }
    
    /** Returns the sequence number as an int, or 0 if there is none.
     * 
     * @return int sequence number
     */    
        
    // FIXME:  0 is actually a valid sequence number - want different default return?
    
    public int getSequenceNumber()
    {
        int seqNumber = 0;
        
        // Return the expression as an integer
        seqNumber = Integer.parseInt(this.getExpression());
        
        return seqNumber;
   
    }
    
    /** Implement validate method to validate the SequenceAttribute and ProcessAttributes .
     *  @return The current list of value listeners, which are evaluated
     *   as a consequence of this call to validate(). 
     *  @exception IllegalActionException If thrown by the parent class.
     */
    public Collection validate() throws IllegalActionException {

        Collection result = null;
        result = super.validate();
        NamedObj container = getContainer();
        String token = "" ;
        StringBuffer sbf = new StringBuffer();
        if (container != null) {
        
                // Beth 10/14/08 - added check for no director
                // If there is no director, don't need any of these warnings
                
                if (((CompositeActor)container.getContainer()).getDirector() != null)
                {
        
                        if ( ( this.getClass() == SequenceAttribute.class ) && ((CompositeActor)container.getContainer()).getDirector().getClass() == ProcessDirector.class ) {
                    
                    sbf.append("Warning: " + container.getName() +"'s Sequence Attribute will be ignored");
                    System.out.println (sbf);
                }
                if ( ( this.getClass() == ProcessAttribute.class ) && ((CompositeActor)container.getContainer()).getDirector().getClass() == SequenceDirector.class ) {
                    sbf.append("Warning: " + container.getName() +"'s Process Attribute will be ignored");
                    System.out.println (sbf);
                }
         
                String changedToken = this.getToken().toString();

                if (changedToken != null && !changedToken.equals("") && changedToken.contains("{")) {
                        changedToken = changedToken.replace("\'", "").replace("\"", "");
                        String tokens[] = changedToken.split(",");
                
                        for (int i=0; i < tokens.length; i++) {
                                token = tokens[i].replace("{", "").replace("}", "").trim();
                                
                                // Beth changed 01/19/09
                                // Want to allow any non-whitespace character as a process name
                                if ( i == 0 && !Pattern.matches("[^\\s]+",token)) {
                                        System.out.println ("Warning for actor " + container.getName() + ": A process name must have at least one character; please change atrribute: " + this.getToken().toString());
                                }

                                // Beth changed 01/19/09
                                // The sequence number must be a non-blank number 
                                // (we want [\\d]+, not [\\d]*, because [\\d]* would match a zero-length
                                // expression, i.e. an empty string)
                                if ( i == 1 && !Pattern.matches("[\\d]+",token)) {  
                                        System.out.println ("Warning for actor " + container.getName() + ": A sequence number must be at least one digit; please change atrribute: " + this.getToken().toString());
                                }

                        }
                }
                }
        }
        return result;  
    }
}
