/* Manages undo/redo actions on a MoML model.

 Copyright (c) 2000-2005 The Regents of the University of California.
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
package ptolemy.moml;

import ptolemy.kernel.undo.UndoAction;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// MoMLUndoEntry

/**
 This is an undo action on the undo/redo stack.  The undo/redo stack
 is stored in an instance of UndoInfoAttribute associated with the top-level
 of a model.  If undo/redo is enabled, a MoMLParser will create entries
 automatically and put them on the stack whenever it parses MoML.  So the
 easiest mechanism to perform undoable actions is to specify those actions
 in MoML and issue a MoMLChangeRequest to execute them. An alternative,
 however, is to create an instance of this class with no MoML, using
 the single argument constructor, and to override execute() to directly
 perform the undo.

 @see MoMLParser
 @see ptolemy.kernel.undo.UndoStackAttribute
 @see MoMLChangeRequest
 @author  Neil Smyth and Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class MoMLUndoEntry implements UndoAction, ChangeListener {
    /** Create an undo entry comprised of the specified MoML code.
     *  @param context The context in which to execute the undo.
     *  @param undoMoML The MoML specification of the undo action.
     */
    public MoMLUndoEntry(NamedObj context, String undoMoML) {
        _context = context;
        _undoMoML = undoMoML;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do nothing. This method is called when a change is successfully
     *  executed.
     *  @param change The change that was successfully executed.
     */
    public void changeExecuted(ChangeRequest change) {
        // In case this has been set before...
        _exception = null;
    }

    /** Record the exception so that execute() can throw it.
     *  @param change The change that failed.
     *  @param exception The exception that occurred.
     *  @see #execute()
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        _exception = exception;
    }

    /** Parse the MoML specified in the constructor call in the context
     *  specified in the constructor call using the parser associated
     *  with the context (as determined by ParserAttribute.getParser()).
     *  @see ParserAttribute#getParser(NamedObj)
     */
    public void execute() throws Exception {
        // If the MoML is empty, the parser will throw an exception.
        if (_undoMoML == null || _undoMoML.trim().equals("")) {
            // Nothing to do.
            return;
        }
        // Use a MoMLChangeRequest so that changes get propagated
        // as appropriate to models that defer to this.
        MoMLChangeRequest request = new MoMLChangeRequest(this, _context,
                _undoMoML);

        // An undo entry is always undoable so that redo works.
        request.addChangeListener(this);
        request.setUndoable(true);
        request.execute();

        // The above call will result in a call to changeFailed()
        // if the execution fails.
        if (_exception != null) {
            throw _exception;
        }
    }

    /** Return the MoML of the undo action.
     *  @return MoML for the undo action.
     */
    public String toString() {
        return _undoMoML + "\n...in context: " + _context.getFullName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The context in which to execute the undo.
    private NamedObj _context;

    // Exception that occurs during a change.
    private Exception _exception = null;

    // The MoML specification of the undo.
    private String _undoMoML;
}
