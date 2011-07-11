/* Read in a Key from the input port and write it out to a KeyStore.

 @Copyright (c) 2003-2011 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.security;

import java.io.FileOutputStream;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// KeyWriter

/** Read in a Key from the input port and write it out to a KeyStore.

 <p>Keystores are ways to manage keys and certificates.
 See the {@link KeyStoreActor} documentation for more information about
 keystores.

 <p>The input is of type {@link KeyToken}.
 This actor does not support writing PublicKeys because
 PublicKeys require certificates.  Instead, to write a PublicKey/PrivateKey
 pair to a keystore, use the <code>keytool</code> executable.
 Currently, this actor only support writing SecretKeys and PrivateKeys
 to a keystore.

 @author  Christopher Brooks
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class KeyWriter extends KeyStoreActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public KeyWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(KeyToken.KEY);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port, which contains on KeyToken.
     */
    public TypedIOPort input;

    /** The output port, which contains a True boolean token when
     *  the key has been written.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the key to the keystore.
     *  @exception IllegalActionException If there's no director,
     *  if there are problems setting the key.
     */
    public boolean postfire() throws IllegalActionException {
        // See io.LineWriter for an example of an actor that writes to a file.
        if (input.hasToken(0)) {
            KeyToken keyToken = (KeyToken) input.get(0);
            java.security.Key key = keyToken.getValue();

            if (key instanceof java.security.PrivateKey) {
                throw new IllegalActionException(this,
                        "Key is a PrivateKey, which is not supported because "
                                + "it requires a certificate");
            }

            // Now we add the key to the keystore, protected
            // by the password.
            try {
                _keyStore.setKeyEntry(_alias, key, _keyPassword.toCharArray(),
                        null /* No certificate */);
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to set key '" + key + "' to alias '" + alias
                                + "'");
            }

            try {
                FileOutputStream keyStoreOutputStream = null;

                try {
                    keyStoreOutputStream = new FileOutputStream(
                            fileOrURL.asFile());
                    _keyStore.store(keyStoreOutputStream,
                            _storePassword.toCharArray());
                    keyStoreOutputStream.close();
                } finally {
                    try {
                        if (keyStoreOutputStream != null) {
                            keyStoreOutputStream.close();
                        }
                    } catch (Throwable throwable) {
                        System.out.println("Ignoring failure to close stream "
                                + "on " + fileOrURL.asFile());
                        throwable.printStackTrace();
                    }
                }

                output.broadcast(BooleanToken.TRUE);
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, throwable,
                        "Failed to store " + fileOrURLDescription());
            }
        }

        return super.postfire();
    }
}
