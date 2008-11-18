/* An actor that converts an array of unsigned bytes into a string.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.actor.lib.conversions;

import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////
/// UnsignedByteArrayToString

/**
 Convert an array of bytes into a string.  The conversion is performed
 using the default character set, returned by the system property
 "file.encoding".  The output is a string assembled from these bytes.

 @author Winthrop Williams, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (winthrop)
 @Pt.AcceptedRating Red (winthrop)
 */
public class UnsignedByteArrayToString extends Converter {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public UnsignedByteArrayToString(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume one array token of integer tokens on the input port
     *  and output a new string token on the output port.  The least
     *  significant byte of the first integer generates the first
     *  character in the string, etc.  NOTE: Java has many options
     *  regarding its character set.  This actor relys on the default
     *  setting on the platform on which it is run.  However, it
     *  assumes that this character set is an 8-bit character set.
     *
     *  @exception IllegalActionException If there is no director.
     *  FIXME: Either verify that it does check for the director,
     *  or remove this statement.  This statement occurs in other
     *  conversion actor(s) as well.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        ArrayToken dataArrayToken = (ArrayToken) input.get(0);
        byte[] dataBytes = new byte[dataArrayToken.length()];

        for (int j = 0; j < dataArrayToken.length(); j++) {
            UnsignedByteToken dataToken = (UnsignedByteToken) dataArrayToken
                    .getElement(j);
            dataBytes[j] = dataToken.byteValue();
        }

        // Convert using the default character encoding.
        String outputString = new String(dataBytes);
        output.send(0, new StringToken(outputString));
    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }

        return super.prefire();
    }
}
