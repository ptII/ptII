/* An abstract implementation of the Receiver interface

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.actor;

import java.util.List;

import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AbstractReceiver

/**
 An abstract implementation of the Receiver interface.
 The container methods and some of the more esoteric
 methods are implemented, while the most
 domain-specific methods are left undefined.
 Note that the NoTokenException and NoRoomException exceptions
 that are thrown by several of the methods are
 runtime exceptions, so they need not be declared explicitly by
 the caller.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (bart)
 @see ptolemy.actor.Receiver
 */
public abstract class AbstractReceiver implements Receiver {
    /** Construct an empty receiver with no container.
     */
    public AbstractReceiver() {
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The container of the receiver.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public AbstractReceiver(IOPort container) throws IllegalActionException {
        setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Throw an exception.  By default, a receiver that extends this
     *  class does not support this method.
     *  @exception IllegalActionException Always thrown.
     */
    public void clear() throws IllegalActionException {
        throw new IllegalActionException(getContainer(), "Receiver class "
                + getClass().getName() + " does not support clear().");
    }
    
    /** Return a list with tokens that are currently in the receiver
     *  available for get() or getArray().
     *  @return A list of instances of Token.
     *  @exception IllegalActionException Always thrown in this base class.
     */
    public List<Token> elementList() throws IllegalActionException {
        throw new IllegalActionException(_container, "Receiver does not implement elementList()");
    }

    /** Get a token from this receiver.
     *  @exception NoTokenException If there is no token.
     */
    public abstract Token get() throws NoTokenException;

    /** Get an array of tokens from this receiver.
     *  The <i>numberOfTokens</i> argument specifies the number
     *  of tokens to get.
     *  The length of the returned array will be equal to
     *  <i>numberOfTokens</i>.
     *  <p>
     *  This implementation works by calling get() repeatedly
     *  to populate an array.  Derived classes may offer more
     *  efficient implementations.  This implementation has two
     *  key limitations:
     *  <ul>
     *  <li> The same array is reused on the next call to
     *       this method.  Thus, the caller needs to ensure that
     *       it has accessed all the tokens it needs before the
     *       next call to this method occurs.
     *  <li> The method is not synchronized.
     *  </ul>
     *  These two limitations mean that this implementation
     *  is not suitable for multithreaded domains
     *  where there might be multiple threads reading from
     *  the same receiver. It <i>is</i> suitable, however,
     *  for multithreaded domains where only one thread
     *  is reading from the receiver.  This is true even if
     *  a separate thread is writing to the receiver, as long
     *  as the put() and get() methods are properly synchronized.
     *
     *  @param numberOfTokens The number of tokens to get.
     *  @return The array of tokens.
     *  @exception NoTokenException If there are not <i>numberOfTokens</i>
     *   tokens available.  Note that if this exception is thrown, then
     *   it is possible that some tokens will have been already extracted
     *   from the receiver by the calls to get().  These tokens will be
     *   lost.  They will not be used on the next call to getArray().
     *   Thus, it is highly advisable to call hasToken(int) before
     *   calling this method.
     */
    public Token[] getArray(int numberOfTokens) throws NoTokenException {
        // Check whether we need to reallocate the cached
        // token array.
        if ((_tokenCache == null) || (numberOfTokens != _tokenCache.length)) {
            // Reallocate the token array.
            _tokenCache = new Token[numberOfTokens];
        }

        for (int i = 0; i < numberOfTokens; i++) {
            _tokenCache[i] = get();
        }

        return _tokenCache;
    }

    /** Return the container of this receiver, or null if there is none.
     *  @return The port containing this receiver.
     *  @see #setContainer(IOPort)
     */
    public IOPort getContainer() {
        return _container;
    }

    /**  Return the current time associated with this receiver. For
     *   non-DT receivers, this method reverts to the director's
     *   getCurrentTime() method.  In DT, there is a local time
     *   associated with every receiver.
     *   @return The current time associated with this receiver.
     *   @deprecated As of Ptolemy II 4.1, replaced by
     *   {@link #getModelTime()}
     */
    public double getCurrentTime() {
        return getModelTime().getDoubleValue();
    }

    /**  Return the current time associated with this receiver. For
     *   non-DT receivers, this method reverts to the director's
     *   getCurrentTime() method.  In DT, there is a local time
     *   associated with every receiver.
     *   @return The current time associated with this receiver.
     */
    public Time getModelTime() {
        IOPort containerPort = getContainer();
        Actor containerActor = (Actor) containerPort.getContainer();
        Director containerDirector = containerActor.getDirector();
        return containerDirector.getModelTime();
    }

    /** Return true if the receiver has room to put a token into it
     *  (via the put() method).
     *  Returning true in this method guarantees that the next call to
     *  put() will not result in an exception.
     *  @return True if the next call to put() will not result in a
     *   NoRoomException.
     */
    public abstract boolean hasRoom();

    /** Return true if the receiver has room to put the specified number of
     *  tokens into it (via the put() method).
     *  Returning true in this method guarantees that the next
     *  <i>numberOfTokens</i> calls to put() or a corresponding call
     *  to putArray() will not result in an exception.
     *  @param numberOfTokens The number of tokens to put into this receiver.
     *  @return True if the next <i>numberOfTokens</i> calls to put()
     *   will not result in a NoRoomException.
     */
    public abstract boolean hasRoom(int numberOfTokens);

    /** Return true if the receiver contains a token that can be obtained
     *  by calling the get() method.  In an implementation,
     *  returning true in this method guarantees that the next
     *  call to get() will not result in an exception.
     *  @return True if the next call to get() will not result in a
     *   NoTokenException.
     */
    public abstract boolean hasToken();

    /** Return true if the receiver contains the specified number of tokens.
     *  In an implementation, returning true in this method guarantees
     *  that the next <i>numberOfTokens</i> calls to get(), or a
     *  corresponding call to getArray(), will not result in an exception.
     *  @param numberOfTokens The number of tokens desired.
     *  @return True if the next <i>numberOfTokens</i> calls to get()
     *   will not result in a NoTokenException.
     */
    public abstract boolean hasToken(int numberOfTokens);

    /** Return <i>true</i>.  Most domains have no notion of the state of
     *  the receiver being unknown.  It is always known whether there is
     *  a token available. Certain domains with fixed point semantics,
     *  however, such as SR, will need to override this method.
     *  @return True.
     */
    public boolean isKnown() {
        return true;
    }

    /** Put the specified token into this receiver.
     *  @param token The token to put into the receiver.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If the put fails
     *   (e.g. because of incompatible types).
     */
    public abstract void put(Token token) throws NoRoomException,
            IllegalActionException;

    /** Put a portion of the specified token array into this receiver.
     *  The first <i>numberOfTokens</i> elements of the token array are put
     *  into this receiver by repeated calling put().
     *  The ability to specify a longer array than
     *  needed allows certain domains to have more efficient implementations.
     *  <p>
     *  This implementation works by calling put() repeatedly.
     *  The caller may feel free to reuse the array after this method returns.
     *  Derived classes may offer more efficient implementations.
     *  This implementation is not synchronized, so it
     *  is not suitable for multithreaded domains
     *  where there might be multiple threads writing to
     *  the same receiver. It <i>is</i> suitable, however,
     *  for multithreaded domains where only one thread
     *  is writing to the receiver.  This is true even if
     *  a separate thread is reading from the receiver, as long
     *  as the put() and get() methods are properly synchronized.
     *
     *  @param tokenArray The array containing tokens to put into this
     *   receiver.
     *  @param numberOfTokens The number of elements of the token
     *   array to put into this receiver.
     *  @exception NoRoomException If the token array cannot be put.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     */
    public void putArray(Token[] tokenArray, int numberOfTokens)
            throws NoRoomException, IllegalActionException {
        IOPort container = getContainer();

        // If there is no container, then perform no conversion.
        if (container == null) {
            for (int i = 0; i < numberOfTokens; i++) {
                put(tokenArray[i]);
            }
        } else {
            for (int i = 0; i < numberOfTokens; i++) {
                put(container.convert(tokenArray[i]));
            }
        }
    }

    /** Put a sequence of tokens to all receivers in the specified array.
     *  Implementers will assume that all such receivers
     *  are of the same class.
     *  @param tokens The sequence of token to put.
     *  @param numberOfTokens The number of tokens to put (the array might
     *   be longer).
     *  @param receivers The receivers.
     *  @exception NoRoomException If there is no room for the token.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type), or if the tokens array
     *   does not have at least the specified number of tokens.
     */
    public void putArrayToAll(Token[] tokens, int numberOfTokens,
            Receiver[] receivers) throws NoRoomException,
            IllegalActionException {
        if (numberOfTokens > tokens.length) {
            IOPort container = getContainer();
            throw new IllegalActionException(container,
                    "Not enough tokens supplied.");
        }

        for (int j = 0; j < receivers.length; j++) {
            receivers[j].putArray(tokens, numberOfTokens);
        }
    }

    /** Put to all receivers in the specified array.
     *  Implementers will assume that all such receivers
     *  are of the same class.
     *  @param token The token to put.
     *  @param receivers The receivers.
     *  @exception NoRoomException If there is no room for the token.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     */
    public void putToAll(Token token, Receiver[] receivers)
            throws NoRoomException, IllegalActionException {
        for (int j = 0; j < receivers.length; j++) {
            IOPort container = receivers[j].getContainer();

            // If there is no container, then perform no conversion.
            if (container == null) {
                receivers[j].put(token);
            } else {
                receivers[j].put(container.convert(token));
            }
        }
    }

    /** Reset this receiver to its initial state, which in this base
     *  class is the same as calling clear().
     *  @exception IllegalActionException If reset() is not supported by
     *   the domain.
     */
    public void reset() throws IllegalActionException {
        clear();
    }

    /** Set the container.
     *  @param port The container.
     *  @exception IllegalActionException If the container is not of
     *   an appropriate subclass of IOPort. Not thrown in this base class,
     *   but may be thrown in derived classes.
     *  @see #getContainer()
     */
    public void setContainer(IOPort port) throws IllegalActionException {
        _container = port;
    }

    /** Return the class name and the full name of the object,
     *  with syntax "className {fullName}".
     *  @return The class name and the full name. */
    public String toString() {
        return getClass().getName() + " {" + getContainer().getFullName()
                + ".receiver }";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The container.
    private IOPort _container;

    // The cache used by the getArray() method to avoid reallocating.
    private Token[] _tokenCache;
}
