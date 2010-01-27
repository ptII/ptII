/* A mutation request specified in MoML.

 Copyright (c) 2000-2010 The Regents of the University of California.
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

import java.net.URL;
import java.util.List;

import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// MoMLChangeRequest

/**
 A mutation request specified in MoML.  This class provides the preferred
 mechanism for implementing mutations on a model while it is executing.
 To use it, create an instance of this class, specifying MoML code as
 an argument to the constructor.  Then queue the instance of this class
 with a composite entity by calling its requestChange() method.
 <p>
 If a context is given to the constructor, then the MoML will
 be executed in that context.  If that context has other objects
 that defer their MoML definitions to it (i.e., it is a class
 definition and there are instances of the class), then the
 MoML will also be executed in the context of those objects
 that defer to it.  Thus, the change to a class will propagate
 to instances.  If the context is (deeply) contained by another
 object that has objects that defer their MoML definitions to
 it, then the changes are also propagated to those objects.
 Thus, even when class definitions are nested within class
 definitions, a change within a class definition will
 propagate to all instances of the class(es).
 <p>
 The parser used to implement the change will be the parser contained
 by a ParserAttribute of the top-level element of the context.  If no
 context is given, or there is no ParserAttribute in its top level,
 then a new parser is created, and a new ParserAttribute is placed
 in the top level.
 <p>
 Note that if a context is specified that is above a class
 definition, and a change within the class definition is made
 by referencing the contents of the class definition using dotted
 names, then the change will not propagate. Thus, changes should be
 made in the most specific context (lowest level in the hierarchy)
 possible.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (neuendor)
 */
public class MoMLChangeRequest extends ChangeRequest {
    /** Construct a mutation request.
     *  The originator is the source of the change request.
     *  Since no context is given, a new parser will be used, and it
     *  will create a new top level.
     *  A listener to changes will probably want to check the originator
     *  so that when it is notified of errors or successful completion
     *  of changes, it can tell whether the change is one it requested.
     *  Alternatively, it can call waitForCompletion().
     *  All external references are assumed to be absolute URLs.  Whenever
     *  possible, use a different constructor that specifies the base.
     *  @param originator The originator of the change request.
     *  @param request The mutation request in MoML.
     */
    public MoMLChangeRequest(Object originator, String request) {
        this(originator, null, request, null);
    }

    /** Construct a mutation request to be executed in the specified context.
     *  The context is typically a Ptolemy II container, such as an entity,
     *  within which the objects specified by the MoML code will be placed.
     *  This method resets and uses a parser that is a static member
     *  of this class.
     *  A listener to changes will probably want to check the originator
     *  so that when it is notified of errors or successful completion
     *  of changes, it can tell whether the change is one it requested.
     *  Alternatively, it can call waitForCompletion().
     *  All external references are assumed to be absolute URLs.  Whenever
     *  possible, use a different constructor that specifies the base.
     *  @param originator The originator of the change request.
     *  @param context The context in which to execute the MoML.
     *  @param request The mutation request in MoML.
     */
    public MoMLChangeRequest(Object originator, NamedObj context, String request) {
        this(originator, context, request, null);
    }

    /** Construct a mutation request to be executed in the specified context.
     *  The context is typically a Ptolemy II container, such as an entity,
     *  within which the objects specified by the MoML code will be placed.
     *  If the top-level containing the specified context has a
     *  ParserAttribute, then the parser associated with that attribute
     *  is used.  Otherwise, a new parser is created, and set to be the
     *  top-level parser.
     *  A listener to changes will probably want to check the originator
     *  so that when it is notified of errors or successful completion
     *  of changes, it can tell whether the change is one it requested.
     *  Alternatively, it can call waitForCompletion(), although there
     *  is severe risk of deadlock when doing that.
     *  @param originator The originator of the change request.
     *  @param context The context in which to execute the MoML.
     *  @param request The mutation request in MoML.
     *  @param base The URL relative to which external references should
     *   be resolved.
     */
    public MoMLChangeRequest(Object originator, NamedObj context,
            String request, URL base) {
        super(originator, request);
        _context = context;
        _base = base;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the context specified in the constructor, or null if none
     *  was specified.
     *  @return The context.
     */
    public NamedObj getContext() {
        return _context;
    }

    /** Return the first container, moving up the hierarchy, for which there
     *  are other objects that defer their MoML definitions to it.
     *  If there is no such container, then return null. If the specified
     *  object has other objects deferring to it, then return the specified
     *  object.  NOTE: It used to be that the returned value of this method
     *  was the recommended context to specify to a constructor. This is
     *  no longer necessary.  Propagation is automatically taken care of
     *  if the context is contained by a deferred-to parent. Thus, you
     *  should give the most immediate container that makes sense for
     *  the context.  It is harmless, however, to use this method to
     *  get the context, so older code will work fine.
     *  @param object The NamedObj to which other objects defer their MoML
     *  definitions.
     *  @return An object that deeply contains this one, or null.
     *  @deprecated No longer needed; just use the specified object as
     *  a context.

     */
    public static NamedObj getDeferredToParent(NamedObj object) {
        if (object == null) {
            return null;
        } else if (!(object instanceof InstantiableNamedObj)) {
            return getDeferredToParent(object.getContainer());
        } else {
            List deferList = ((InstantiableNamedObj) object).getChildren();

            if ((deferList != null) && (deferList.size() > 0)) {
                return object;
            } else {
                return getDeferredToParent(object.getContainer());
            }
        }
    }

    /** Set whether or not this change is undoable.
     *  @param undoable whether or not this change should be treated
     *   as an incremental change that is undoable
     */
    public void setUndoable(boolean undoable) {
        _undoable = undoable;
    }

    /** Set whether or not the undo from this change should be merged with
     *  the previous undoable change.
     *  @param mergeWithPrevious whether or not this change should be merged
     */
    public void setMergeWithPreviousUndo(boolean mergeWithPrevious) {
        _mergeWithPreviousUndo = mergeWithPrevious;
    }

    /** Specify whether or not to report errors via the handler that
     *  is registered with the parser. The initial default is to not
     *  report errors to the registered handler. If this method is not
     *  called with a true argument, errors will be reported to the
     *  registered handler. Note that in either case, if the handler
     *  returns ErrorHandler.CANCEL, then exceptions will be reported
     *  to any change listeners that are registered with this object
     *  via their changeFailed() method.  If the handler returns
     *  ErrorHandler.CONTINUE, then the exception will not be reported
     *  to any change listeners and the change listener will think
     *  that the change succeeded.
     *
     *  @see ErrorHandler
     *  @param report False to disable error reporting.
     */
    public void setReportErrorsToHandler(boolean report) {
        _reportToHandler = report;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the change by evaluating the request and propagating
     *  the request if appropriate.
     *  @exception Exception If an exception is thrown
     *   while evaluating the request.
     */
    protected void _execute() throws Exception {
        // NOTE: To see what is being parsed, change _DEBUG to true.
        if (_DEBUG) {
            System.out.println("****** Executing MoML change:");
            System.out.println(getDescription());

            if (_context != null) {
                System.out.println("------ in context "
                        + _context.getFullName());
            }
        }

        // Check to see whether there is a parser...
        if (_context != null) {
            _parser = ParserAttribute.getParser(_context);
            _parser.reset();
        }

        if (_parser == null) {
            // There is no previously associated parser (can only
            // happen if _context is null).
            _parser = new MoMLParser();
        }

        if (_context != null) {
            _parser.setContext(_context);
        }

        // Tell the parser whether this change is undoable.
        if (_undoable) {
            _parser.setUndoable(true);
        }

        ErrorHandler handler = MoMLParser.getErrorHandler();

        if (!_reportToHandler) {
            MoMLParser.setErrorHandler(null);
        }

        _preParse(_parser);
        try {
            _parser.parse(_base, getDescription());
        } finally {
            if (!_reportToHandler) {
                MoMLParser.setErrorHandler(handler);
            }
        }

        // Merge the undo entry created if needed
        if (_undoable && _mergeWithPreviousUndo) {
            UndoStackAttribute undoInfo = UndoStackAttribute
                    .getUndoInfo(_context);
            undoInfo.mergeTopTwo();
        }
        _postParse(_parser);
    }

    /** Do nothing. This is a strategy pattern method that is called
     *  by the _execute() method just after doing the parse.
     *  Subclasses may override this.
     *  @param parser The parser
     */
    protected void _postParse(MoMLParser parser) {
    }

    /** Do nothing. This is a strategy pattern method that is called
     *  by the _execute() method just before doing the parse.
     *  Subclasses may override this to do some setup of the parser.
     *  @param parser The parser
     */
    protected void _preParse(MoMLParser parser) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The URL relative to which external references should be resolved.
    private URL _base;

    // The context in which to execute the request.
    private NamedObj _context;

    // Flag to print out information about what's being done.
    private static boolean _DEBUG = false;

    // Indicates that the undo MoML from this change request should be merged
    // in with the undo MoML from the previos undoable change request if they
    // both have the same context.
    private boolean _mergeWithPreviousUndo = false;

    // The parser given in the constructor.
    private MoMLParser _parser;

    // Flag indicating whether to report to the handler registered
    // with the parser.
    private boolean _reportToHandler = false;

    // Flag indicating if this change is undoable or not
    private boolean _undoable = false;
}
