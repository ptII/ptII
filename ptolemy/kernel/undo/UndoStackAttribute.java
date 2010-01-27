/* An attribute that holds the undo/redo information about a model.

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
package ptolemy.kernel.undo;

import java.util.List;
import java.util.Stack;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;

///////////////////////////////////////////////////////////////////
//// UndoStackAttribute

/**
 This attribute holds the undo/redo information for a model.
 This attribute is not persistent, so undo/redo information disappears
 when the model is closed. It is also a singleton, meaning that it will
 replace any previous attribute that has the same name
 and is an instance of the same base class, SingletonAttribute.
 <p>
 Two stacks of information are maintained - one for undo information and
 one for redo information. Normally, a push onto this stack puts the
 undo information in the undo stack. However, if the push occurs during
 the execution of an undo, then the information is put on the redo stack.
 The entries on the stack implement the UndoAction interface.
 <p>
 NOTE: the information in the redo stack is emptied when a new undo action is
 pushed onto the undo stack that was not the result of a redo being
 requested. This situation arises when a user requests a series of undo
 and redo operations, and then performs some normal undoable action. At this
 point the information in the redo stack is not relevant to the state of
 the model and so must be cleared.

 @see UndoAction
 @author Neil Smyth and Edward A. Lee
 @version $Id$
 @since Ptolemy II 3.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class UndoStackAttribute extends SingletonAttribute {
    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null,
     *  or a NullPointerException will be thrown. This attribute will
     *  use the workspace of the container for synchronization and
     *  version counts. If the name argument is null, then the name is
     *  set to the empty string. The object is added to the directory
     *  of the workspace if the container is null. Increment the
     *  version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a
     *   period.
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public UndoStackAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the UndoStackAttribute associated with the given object.
     *  This is done by searching up the containment hierarchy until
     *  such an attribute is found. If no such attribute is found,
     *  then create and attach a new one to the top level.
     *  This method gets read access on the workspace associated
     *  with the specified object.
     *  @param object The model for which an undo stack is required
     *   (must not be null or a NullPointerException will the thrown).
     *  @return The current undo stack attribute if there is one, or a new one.
     */
    public static UndoStackAttribute getUndoInfo(final NamedObj object) {
        // Note, the parameter is final so we do not assign to it,
        // so we are sure that we call getReadAccess on the same object.
        try {
            object.workspace().getReadAccess();

            NamedObj topLevel = object.toplevel();
            NamedObj container = object;

            while (container != null) {
                List attrList = container
                        .attributeList(UndoStackAttribute.class);

                if (attrList.size() > 0) {
                    return (UndoStackAttribute) attrList.get(0);
                }

                container = container.getContainer();
            }

            // If we get here, there is no such attribute.
            // Create and attach a new instance.
            try {
                return new UndoStackAttribute(topLevel, "_undoInfo");
            } catch (KernelException e) {
                throw new InternalErrorException(e);
            }
        } finally {
            object.workspace().doneReading();
        }
    }

    /** Merge the top two undo entries into a single action, unless
     *  we are in either a redo or an undo, in which case the merge
     *  happens automatically and need not be explicitly requested
     *  by the client. If there
     *  are fewer than two entries on the stack, do nothing. Note
     *  that when two entries are merged, the one on the top of
     *  the stack becomes the first one executed and the one
     *  below that on the stack becomes the second one executed.
     *  This method gets write access on the workspace.
     */
    public void mergeTopTwo() {
        try {
            workspace().getWriteAccess();

            if ((_inUndo == 0) && (_inRedo == 0)) {
                if (_undoEntries.size() > 1) {
                    UndoAction lastUndo = (UndoAction) _undoEntries.pop();
                    UndoAction firstUndo = (UndoAction) _undoEntries.pop();
                    UndoAction mergedAction = new MergeUndoActions(lastUndo,
                            firstUndo);
                    _undoEntries.push(mergedAction);

                    if (_debugging) {
                        _debug("=======> Merging top two on undo stack:\n"
                                + mergedAction);
                    }
                }
            }
        } finally {
            workspace().doneWriting();
        }
    }

    /** Push an action to the undo stack, or if we are executing an undo,
     *  onto the redo stack. This method gets write access on the workspace.
     *  @param action The undo action.
     */
    public void push(UndoAction action) {
        try {
            workspace().getWriteAccess();

            if (_inUndo > 1) {
                UndoAction previousRedo = (UndoAction) _redoEntries.pop();
                UndoAction mergedAction = new MergeUndoActions(action,
                        previousRedo);
                _redoEntries.push(mergedAction);

                if (_debugging) {
                    _debug("=======> Merging action onto redo stack to get:\n"
                            + mergedAction);
                }

                _inUndo++;
            } else if (_inUndo == 1) {
                if (_debugging) {
                    _debug("=======> Pushing action onto redo stack:\n"
                            + action);
                }

                _redoEntries.push(action);
                _inUndo++;
            } else if (_inRedo > 1) {
                UndoAction previousUndo = (UndoAction) _undoEntries.pop();
                UndoAction mergedAction = new MergeUndoActions(action,
                        previousUndo);

                if (_debugging) {
                    _debug("=======> Merging redo action onto undo stack to get:\n"
                            + mergedAction);
                }

                _undoEntries.push(mergedAction);
                _inRedo++;
            } else if (_inRedo == 1) {
                if (_debugging) {
                    _debug("=======> Pushing redo action onto undo stack:\n"
                            + action);
                }

                _undoEntries.push(action);
                _inRedo++;
            } else {
                if (_debugging) {
                    _debug("=======> Pushing action onto undo stack:\n"
                            + action);
                }

                _undoEntries.push(action);

                if (_debugging) {
                    _debug("======= Clearing redo stack.\n");
                }

                _redoEntries.clear();
            }
        } finally {
            workspace().doneWriting();
        }
    }

    /** Remove the top redo action and execute it.
     *  If there are no redo entries, do nothing.
     *  This method gets write access on the workspace.
     *  @exception Exception If something goes wrong.
     */
    public void redo() throws Exception {
        if (_redoEntries.size() > 0) {
            try {
                workspace().getWriteAccess();

                UndoAction action = (UndoAction) _redoEntries.pop();

                if (_debugging) {
                    _debug("<====== Executing redo action:\n" + action);
                }

                try {
                    _inRedo = 1;

                    // NOTE: We assume that if in executing this
                    // action any change request is made, that the
                    // change request is honored before execute()
                    // returns. Otherwise, _inRedo will erroneously
                    // be back at 0 when that change is finally
                    // executed.
                    action.execute();
                } finally {
                    _inRedo = 0;
                }
            } finally {
                workspace().doneWriting();
            }
        }
    }

    /** Remove the top undo action and execute it.
     *  If there are no undo entries, do nothing.
     *  This method gets write access on the workspace.
     *  @exception Exception If something goes wrong.
     */
    public void undo() throws Exception {
        try {
            workspace().getWriteAccess();

            if (_undoEntries.size() > 0) {
                UndoAction action = (UndoAction) _undoEntries.pop();

                if (_debugging) {
                    _debug("<====== Executing undo action:\n" + action);
                }

                try {
                    _inUndo = 1;
                    action.execute();
                } finally {
                    _inUndo = 0;
                }
            }
        } finally {
            workspace().doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private named static classes      ////

    /** An undo or redo action on the stack. */
    private static class MergeUndoActions implements UndoAction {

        // FindBugs suggested refactoring an inner class into this
        // named static class, which we did.  This makes the class
        // smaller and avoids leaks.

        /** Create an undo action from two actions. */
        public MergeUndoActions(UndoAction firstAction, UndoAction secondAction) {
            _firstAction = firstAction;
            _secondAction = secondAction;
        }

        /** Execute the action. */
        public void execute() throws Exception {
            _firstAction.execute();
            _secondAction.execute();
        }

        public String toString() {
            return "Merged action.\nFirst part:\n" + _firstAction
                    + "\n\nSecond part:\n" + _secondAction;
        }

        private UndoAction _firstAction;
        private UndoAction _secondAction;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Counter used to count pushes during a redo.
    private int _inRedo = 0;

    // Counter used to count pushes during an undo.
    private int _inUndo = 0;

    // The stack of available redo entries
    private Stack _redoEntries = new Stack();

    // The stack of available undo entries
    private Stack _undoEntries = new Stack();
}
