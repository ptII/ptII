/* Serialize an AttributeChangeToken to and from binary format.

Copyright (c) 2011 The Regents of the University of California.
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
package ptserver.data.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ptolemy.kernel.util.IllegalActionException;
import ptserver.data.AttributeChangeToken;

//////////////////////////////////////////////////////////////////////////
//// AttributeChangeTokenHandler

/** Serialize an AttributeChangeToken to and from binary format.
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class AttributeChangeTokenHandler implements
        TokenHandler<AttributeChangeToken> {

    /** Write the AttributeChangeToken to a byte array.
     *  @param token Token to be converted to bytes.
     *  @param outputStream The stream to write to.
     *  @exception IOException If the stream cannot be written.
     *  @exception IllegalActionException Not thrown in this class.
     *  @see ptserver.data.handler.TokenHandler#convertToBytes(ptolemy.data.Token, java.io.DataOutputStream)
     */
    public void convertToBytes(AttributeChangeToken token,
            DataOutputStream outputStream) throws IOException,
            IllegalActionException {
        outputStream.writeUTF(token.getTargetSettableName());
        outputStream.writeUTF(token.getExpression());
    }

    /** Read an AttributeChangeToken from the input stream.
     *  @param inputStream The stream to read from.
     *  @param tokenType The type of token to be parsed.
     *  @return The populated AttributeChangeToken object.
     *  @exception IOException If the stream cannot be read.
     *  @exception IllegalActionException Not thrown in this class.
     *  @see ptserver.data.handler.TokenHandler#convertToToken(java.io.DataInputStream, Class)
     */
    public AttributeChangeToken convertToToken(DataInputStream inputStream,
            Class<? extends AttributeChangeToken> tokenType)
            throws IOException, IllegalActionException {
        AttributeChangeToken token = new AttributeChangeToken();
        token.setTargetSettableName(inputStream.readUTF());
        token.setExpression(inputStream.readUTF());

        return token;
    }
}
