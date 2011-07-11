/* Sign the input data using a private key.

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
package ptolemy.actor.lib.security;

import java.security.PrivateKey;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// SignatureSigner

/** Sign the input data using a private key.

 <p>In cryptography, digital signatures can be used to verify that the
 data was not modified in transit.  However, the data itself is passed
 in clear text.

 <p>The <i>provider</i> and <i>signatureAlgorithm</i> parameters should
 be set to the values used to generate the privateKey.  See {@link
 PrivateKeyReader} and {@link SignatureActor} for possible values.

 <p>The <i>provider</i> and <i>signatureAlgorithm</i> parameters should
 be set to the same value as the corresponding parameters in the
 SignatureVerifier actor.

 <p>Each time fire() is called, the <i>privateKey</i> is used to create a
 signature for each block of unsigned byte array data read from the
 <i>input</i> port.  The signed data is passed to a SignatureVerifier
 actor on the <i>signature</i> port as an unsigned byte array.

 <p>The <i>input</i> data itself is passed to in <b>clear text</b>
 on the <i>output</i> port.

 <p>This actor relies on the Java Cryptography Architecture (JCA) and Java
 Cryptography Extension (JCE).
 See the {@link ptolemy.actor.lib.security.CryptographyActor} documentation for
 resources about JCA and JCE.

 @see PrivateKeyReader
 @author Christopher Hylands Brooks, Contributor: Rakesh Reddy
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class SignatureSigner extends SignatureActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SignatureSigner(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        privateKey = new TypedIOPort(this, "privateKey", true, false);
        privateKey.setTypeEquals(KeyToken.KEY);

        signature = new TypedIOPort(this, "signature", false, true);
        signature.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The private key to be used by the SignatureVerifier actor
     *  to verify the data on the <i>output</i> port.
     *  The type of this input port is an ObjectToken containing
     *  a java.security.PrivateKey.
     */
    public TypedIOPort privateKey;

    /** The signature of the data.  The type of this output port
     *  is unsigned byte array.  The data is read in on the <i>input</i>
     *  port and the signature is generated on this port.  The
     *  <i>output</i> port contains the data in clear text.
     */
    public TypedIOPort signature;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a signature for the input data and send the signature
     *  to the signature port.  The <i>output</i> port contains the data
     *  in clear text.
     *
     *  @exception IllegalActionException If calling send(), super.fire()
     *  throws it, or if there is a problem cryptographic configuration.
     */
    public void fire() throws IllegalActionException {
        // super.fire() should be called before accessing _signature
        // so that we handle any updates of _signature made necessary
        // by attribute changes.
        super.fire();

        if (privateKey.hasToken(0)) {
            KeyToken keyToken = (KeyToken) privateKey.get(0);
            _privateKey = (PrivateKey) keyToken.getValue();
        }

        if (input.hasToken(0)) {
            try {
                // Process the input data to generate a signature.
                byte[] dataBytes = ArrayToken
                        .arrayTokenToUnsignedByteArray((ArrayToken) input
                                .get(0));

                _signature.initSign(_privateKey);
                _signature.update(dataBytes);

                output.send(0,
                        ArrayToken.unsignedByteArrayToArrayToken(dataBytes));
                signature.send(0, ArrayToken
                        .unsignedByteArrayToArrayToken(_signature.sign()));
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, throwable,
                        "Problem sending data");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /* The private key to be used for the signature.
     */
    private PrivateKey _privateKey = null;
}
