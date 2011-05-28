/*
 Convert a token to a byte stream and back.
 
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

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// TokenHandler
/**
 * Convert a token of a particular type to a byte stream and back.
 * 
 * @param <T> Type of Token that the handler handles
 * @author ahuseyno
 * @version $Id$ 
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public interface TokenHandler<T extends Token> {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Convert the token to a byte stream according to the handler's parsing algorithm
     * and output it to the outputStream.
     * @param token the token to be converted
     * @param outputStream the outputStream holding stream of bytes
     * @exception IOException if there is a problem with the outputStream 
     * @exception IllegalActionException if there is the state becomes inconsistent
     */
    public void convertToBytes(T token, DataOutputStream outputStream)
            throws IOException, IllegalActionException;

    /**
     * Return a token of the specified type by reading from the inputStream
     * and converting to the token according to the parsing algorithm 
     * defined in the handler.
     * @param inputStream The inputStream that contains serialized version
     * of a token
     * @return Token parsed from inputStream
     * @exception IOException if there is a problem with the outputStream 
     * @exception IllegalActionException if there is the state becomes inconsistent
     */
    public T convertToToken(DataInputStream inputStream) throws IOException,
            IllegalActionException;

    /**
     * Return the position of the handler in the handler list of the TokenHandler.
     * Position field is used as the token type identifier in the byte stream
     * @return position The position in the handler list of the TokenHandler
     * @see TokenHandler 
     * @see #setPosition(short)
     */
    public short getPosition();

    /**
     * Set the position of the handler in the token handler list.
     * Position field is used as the token type identifier in the byte stream
     * @param position
     * @see TokenHandler 
     * @see #getPosition()
     */
    public void setPosition(short position);
}
