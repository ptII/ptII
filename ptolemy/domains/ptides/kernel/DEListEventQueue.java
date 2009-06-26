/* Event queue that's ordered by a linked list, which provides a total order among all events in this queue.

@Copyright (c) 2008-2009 The Regents of the University of California.
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

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


 */

package ptolemy.domains.ptides.kernel;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.de.kernel.DEEventQueue;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;

/**
 *  Event queue that is a linked list. This provides a totally ordered sorted event
 *  queue. It also allows all events to be accessed in the order they are sorted.
 *
 *  @author Jia Zou
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 *
 */
public class DEListEventQueue implements DEEventQueue {

    /** Construct an empty event queue.
     */
    public DEListEventQueue() {
        // Construct a calendar queue _cQueue with its default parameters:
        // minBinCount is 2, binCountFactor is 2, and isAdaptive is true.
        _listQueue = new LinkedList();
    }

    /** Clear the event queue.
     */
    public void clear() {
        _listQueue.clear();
    }

    /** Get the smallest event from the event queue. */
    public DEEvent get() throws InvalidStateException {
        DEEvent result = (DEEvent) _listQueue.getFirst();
        if (_debugging) {
            _debug("--- getting from queue: " + result);
        }
        return result;
    }

    /** Get the event from the event queue that is pointed by the index.
     *  @param index an int specifying the index.
     *  @return a DEEvent object pointed to by the index.
     *  @exception InvalidStateException if get() method of the queue throws it.
     */
    public DEEvent get(int index) throws InvalidStateException {
        DEEvent result = (DEEvent) _listQueue.get(index);
        if (_debugging) {
            _debug("--- getting " + index + "th element from queue: " + result);
        }
        return result;
    }

    /** Check if the event queue is empty.
     */
    public boolean isEmpty() {
        return _listQueue.isEmpty();
    }

    /** Put the event queue into the event queue, and then sort it by timestamp order. */
    public void put(DEEvent event) throws IllegalActionException {
        if (_debugging) {
            _debug("+++ putting in queue: " + event);
        }
        _listQueue.addFirst(event);
        Collections.sort(_listQueue);
    }

    /** Returns the size of this event queue.
     */
    public int size() {
        return _listQueue.size();
    }

    /** Take this event and remove it from the event queue.
     *  If the event is a DEEvent, then put the token of this event into the
     *  receiver.
     *  <p>
     *  NOTE: this method should only be called once for each event in the event
     *  queue, unless the event is not a DEEvent. Because each time this method
     *  is called, the token associated with this event is transferred to the receiver.
     *  Also, the same event should not be taken out of the event queue and then put
     *  into the event queue multiple times.
     *
     *  @return The event associated with this index in the event queue.
     *  @exception InvalidStateException
     */
    public DEEvent take() throws InvalidStateException {
        DEEvent result = (DEEvent) _listQueue.remove();
        // put the token of this event into the destined receiver.
        if (result instanceof DETokenEvent) {
            ((PtidesBasicReceiver) ((DETokenEvent) result).receiver())
                    .putToReceiver(((DETokenEvent) result).token());
        }
        if (_debugging) {
            _debug("--- taking from queue: " + result);
        }
        return result;
    }

    /** Take this event and remove it from the event queue.
     *  If the event is a DEEvent, then put the token of this event into the
     *  receiver.
     *  <p>
     *  NOTE: this method should only be called once for each event in the event
     *  queue, unless the event is not a DEEvent. Because each time this method
     *  is called, the token associated with this event is transferred to the receiver.
     *  Also, the same event should not be taken out of the event queue and then put
     *  into the event queue multiple times.
     *
     *  @param index The index of this event in the event queue.
     *  @return The event associated with this index in the event queue.
     *  @exception InvalidStateException
     */
    public DEEvent take(int index) throws InvalidStateException {
        DEEvent result = (DEEvent) _listQueue.remove(index);
        // put the token of this event into the destined receiver.
        if (result instanceof DETokenEvent) {
            ((PtidesBasicReceiver) ((DETokenEvent) result).receiver())
                    .putToReceiver(((DETokenEvent) result).token());
        }
        if (_debugging) {
            _debug("--- taking " + index + "th element from queue: " + result);
        }
        return result;
    }

    /** Return an array representation of this event queue.
     *  @return an array of Objects in the list.
     */
    public Object[] toArray() {
        return _listQueue.toArray();
    }

    /** Add a debugger listen for this event queue.
     *  @see #removeDebugListener
     */
    public void addDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            _debugListeners = new LinkedList();
        } else {
            if (_debugListeners.contains(listener)) {
                return;
            }
        }

        _debugListeners.add(listener);
        _debugging = true;
    }

    /** Remove the debugger listen for this event queue.
     *  @see #addDebugListener
     */
    public void removeDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            return;
        }

        _debugListeners.remove(listener);

        if (_debugListeners.size() == 0) {
            _debugListeners = null;
            _debugging = false;
        }

        return;
    }

    /** Send a debug message to all debug listeners that have registered.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  @param message The message.
     */
    private final void _debug(String message) {
        if ((_debugListeners == null) || !_debugging) {
            return;
        } else {
            Iterator listeners = _debugListeners.iterator();

            while (listeners.hasNext()) {
                ((DebugListener) listeners.next()).message(message);
            }
        }
    }

    /** @serial The list of DebugListeners registered with this object. */
    private LinkedList _debugListeners = null;

    /** @serial A flag indicating whether there are debug listeners. */
    private boolean _debugging;

    /** The queue as represented by a linked list. */
    private LinkedList _listQueue;

}
