/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.data.properties;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

public class PropertyResolutionException extends IllegalActionException {

    public PropertyResolutionException(PropertySolverBase solver,
                    Nameable nameable, String detail) {
        this(solver, nameable, null, detail);
    }

    public PropertyResolutionException(PropertySolverBase solver,
                    Nameable nameable, Throwable cause) {
        this(solver, nameable, cause, "");
    }

    public PropertyResolutionException(PropertySolverBase solver,
                    Nameable nameable, Throwable cause, String detail) {
        super(solver, nameable, cause, detail);

        assert ( solver != null );

        _solver = solver;
    }

    public PropertyResolutionException(PropertySolverBase solver, String detail) {
        this(solver, null, null, detail);
    }

    public PropertyResolutionException(PropertySolverBase solver, Throwable cause) {
        this(solver, null, cause, "");
    }

    public PropertyResolutionException(PropertySolverBase solver,
                    Throwable cause, String detail) {
        this(solver, null, cause, detail);
    }

    public PropertySolverBase getSolver() {
            return _solver;
    }

    private PropertySolverBase _solver;
}
