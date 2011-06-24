/* Convert a ServerEventToken into bytes and back for communication
   through the MQTT broker.
   
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

import ptserver.data.ServerEventToken;
import ptserver.data.ServerEventToken.EventType;

//////////////////////////////////////////////////////////////////////////
//// ServerEventTokenHandler

/** ServerEventTokenHandler converts ServerEventToken to/from byte form.
 *  @author Justin Killian
 *  @version $Id$ 
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class ServerEventTokenHandler implements TokenHandler<ServerEventToken> {

    ///////////////////////////////////////////////////////////////////
    ////                      public variables                     ////

    /** Write the ServerEventToken to a byte array.
     *  @param token Token to be converted to bytes.
     *  @param outputStream The stream to write to.
     *  @exception IOException If the stream cannot be written.
     *  @see ptserver.data.handler.TokenHandler#convertToBytes(ptolemy.data.Token, java.io.DataOutputStream)
     */
    public void convertToBytes(ServerEventToken token,
            DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(token.getEventType().ordinal());
        outputStream.writeUTF(token.getMessage());
    }

    /** Read an ServerEventToken from the input stream.
     *  @param inputStream The stream to read from.
     *  @param tokenType The type of token to be parsed.
     *  @return The populated ServerEventToken object.
     *  @exception IOException If the stream cannot be read.
     *  @see ptserver.data.handler.TokenHandler#convertToToken(java.io.DataInputStream, Class)
     */
    public ServerEventToken convertToToken(DataInputStream inputStream,
            Class<? extends ServerEventToken> tokenType) throws IOException {

        // Initialize values.
        EventType eventType = EventType.class.getEnumConstants()[inputStream
                .readInt()];
        String message = "";

        // Try to read the message if available.
        try {
            message = inputStream.readUTF();
        } catch (Exception e) {
            message = "";
        }

        // Return the parsed token.
        return new ServerEventToken(eventType, message);
    }
}
