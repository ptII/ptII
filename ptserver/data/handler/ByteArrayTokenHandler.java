/*
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
import ptserver.data.ByteArrayToken;

///////////////////////////////////////////////////////////////////
//// ByteArrayTokenHandler

public class ByteArrayTokenHandler implements TokenHandler<ByteArrayToken> {

    public void convertToBytes(ByteArrayToken token,
            DataOutputStream outputStream) throws IOException,
            IllegalActionException {
        outputStream.writeInt(token.getArray().length);
        outputStream.write(token.getArray());
    }

    private long time = 0;

    public ByteArrayToken convertToToken(DataInputStream inputStream,
            Class<? extends ByteArrayToken> tokenType) throws IOException,
            IllegalActionException {
        int length = inputStream.readInt();
        byte[] array = new byte[length];
        inputStream.readFully(array);
        long t = System.currentTimeMillis();
        System.out.println(t - time);
        time = t;

        return new ByteArrayToken(array);
    }

}
