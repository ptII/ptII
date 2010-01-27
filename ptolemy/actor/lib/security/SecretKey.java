/* Create a secret key and send it on the output.

 Copyright (c) 2003-2005 The Regents of the University of California.
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

import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;
import java.util.Set;

import javax.crypto.KeyGenerator;

import ptolemy.actor.lib.Source;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//import javax.crypto.SecretKey;
///////////////////////////////////////////////////////////////////
//// SecretKey

/**
 Create a secret key and send it on the <i>output</i>.

 <p>The secret key is a symmetric key can be used by the {@link
 SymmetricEncryption} and {@link SymmetricDecryption} actors.
 See {@link ptolemy.actor.lib.security.SymmetricEncryption} for a
 description of symmetric vs. asymmetric algorithms.

 <p>This key should be not be visible to users as the security of
 the encrypted message relies on the secrecy of this key.

 <p>This actor relies on the Java Cryptography Architecture (JCA) and Java
 Cryptography Extension (JCE).  See the
 {@link ptolemy.actor.lib.security.CryptographyActor} documentation for
 resources about the JCE.

 @see KeyWriter
 @see SecretKeyReader
 @author Christopher Hylands Brooks, Contributor: Rakesh Reddy
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class SecretKey extends Source {
    /** Construct an actor with the given container and name.
     *  The Java virtual machine is queried for algorithm and provider
     *  choices and these choices are added to the appropriate parameters.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SecretKey(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        algorithm = new StringParameter(this, "algorithm");

        Set algorithms = Security.getAlgorithms("KeyGenerator");
        Iterator algorithmsIterator = algorithms.iterator();

        for (int i = 0; algorithmsIterator.hasNext(); i++) {
            String algorithmName = (String) algorithmsIterator.next();

            algorithm.addChoice(algorithmName);
        }

        algorithm.setExpression("DES");

        output.setTypeEquals(KeyToken.KEY);

        provider = new StringParameter(this, "provider");
        provider.setExpression("SystemDefault");
        provider.addChoice("SystemDefault");

        Provider[] providers = Security.getProviders();

        for (int i = 0; i < providers.length; i++) {
            provider.addChoice(providers[i].getName());
        }

        keySize = new Parameter(this, "keySize", new IntToken(56));
        keySize.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Specify the algorithm to be used to process data.  The algorithm is
     *  specified as a string. The algorithms are limited to those
     *  implemented by providers using the Java JCE which are found on the
     *  system.
     *  The initial default is "DES".
     */
    public StringParameter algorithm;

    /** Specify a provider for the given algorithm.  Takes the algorithm name
     *  as a string. The default value is "SystemDefault" which allows the
     *  system chooses the provider based on the JCE architecture.
     */
    public StringParameter provider;

    /** Specify the size of the key to be created.
     *  The key size is an integer value representing the number of
     *  bits in the key.  The initial default depends on the algorithm
     *  that is selected, not all algorithms use <i>keySize</i>.
     *
     *  <p>DSA is the most common algorithm that uses <i>keySize</i>,
     *  the Sun documentation says:
     *  "The length, in bits, of the modulus p. This must range from
     *  512 to 1024, and must be a multiple of 64. The default keysize
     *  is 1024."
     *  Refer to
     *  <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppB"><code>http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppB</code></a>
     *  for a list of possible key sizes for certain algorithms.
     *  The initial default is 1024.
     */
    public Parameter keySize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to reinitialize the state if
     *  the <i>algorithm</i>, <i>provider</i>, or <i>keysize</i>
     *  parameter is changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == algorithm) {
            _updateSecretKeyNeeded = true;
            _algorithm = ((StringToken) algorithm.getToken()).stringValue();
        } else if (attribute == provider) {
            _updateSecretKeyNeeded = true;
            _provider = ((StringToken) provider.getToken()).stringValue();
        } else if (attribute == keySize) {
            _updateSecretKeyNeeded = true;
            _keySize = ((IntToken) keySize.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Send the key that was generated in initialize() on the output port.
     *
     *  @exception IllegalActionException If thrown by base class.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        _updateSecretKey();
        output.send(0, _secretKeyToken);
    }

    /** Initialize the key by using the cached values of the parameters.
     *
     *  @exception IllegalActionException If thrown by base class or
     *  if the algorithm is not found, or if the padding scheme is illegal,
     *  or if the specified provider does not exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _updateSecretKey();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The name of the algorithm to be used. */
    protected String _algorithm;

    /** The key size to be used when processing information. */
    protected int _keySize;

    /** The provider to be used for a provider specific implementation. */
    protected String _provider;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Update the secret key by calling KeyGenerator.getInstance()
     *  with an argument that is created from the values of _algorithm,
     *  _provider and _keySize.
     */
    private void _updateSecretKey() throws IllegalActionException {
        if (_updateSecretKeyNeeded) {
            try {
                KeyGenerator keyGen;

                if (_provider.equalsIgnoreCase("SystemDefault")) {
                    keyGen = KeyGenerator.getInstance(_algorithm);
                } else {
                    keyGen = KeyGenerator.getInstance(_algorithm, _provider);
                }

                keyGen.init(_keySize, new SecureRandom());
                _secretKey = keyGen.generateKey();
                _secretKeyToken = new KeyToken(_secretKey);
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, throwable,
                        "Failed to initialize Key.");
            }

            _updateSecretKeyNeeded = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /* The public key to be used for asymmetric encryption. */
    private javax.crypto.SecretKey _secretKey = null;

    private KeyToken _secretKeyToken;

    // Set to true of we need to call _updateSecretKey.
    private boolean _updateSecretKeyNeeded = true;
}
