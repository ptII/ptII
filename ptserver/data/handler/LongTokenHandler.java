/*
 Convert a LongToken to/from byte stream format for communication over MQTT protocol with a remote model.
 
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

import ptolemy.data.LongToken;

//////////////////////////////////////////////////////////////////////////
//// LongTokenHandler
/**
 * Convert a LongToken to/from byte stream format for communication over MQTT protocol with a remote model.
 * 
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class LongTokenHandler implements TokenHandler<LongToken> {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Convert a LongToken to a byte stream using an algorithm defined into the DataOutputStream.
     * @see ptserver.data.handler.TokenHandler#convertToBytes(ptolemy.data.Token, java.io.DataOutputStream)
     * @exception IOException if there is a problem writing to the stream
     */
    public void convertToBytes(LongToken token, DataOutputStream outputStream)
            throws IOException {
        outputStream.writeLong(token.longValue());
    }

    /** 
     * Reads a long from the inputStream and converts it to the LongToken.
     * @see ptserver.data.handler.TokenHandler#convertToToken(java.io.DataInputStream, Class)
     * @exception IOException if there is a problem reading from the stream
     */
    public LongToken convertToToken(DataInputStream inputStream,
            Class<LongToken> tokenType)
            throws IOException {
        return new LongToken(inputStream.readLong());
    }
}