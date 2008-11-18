/* The Forward Euler ODE solver.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.ct.kernel.solver;

import ptolemy.data.DoubleToken;
import ptolemy.domains.ct.kernel.CTBaseIntegrator;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ForwardEulerSolver

/**
 The Forward Euler ODE solver. For ODE
 <pre>
 dx/dt = f(x, u, t), x(0) = x0;
 </pre>
 The Forward Euler method approximates the x(t+h) as:
 <pre>
 x(t+h) =  x(t) + h * f(x(t), u(t), t)
 </pre>
 No error control and step size control is performed. This is the
 simplest algorithm for solving an ODE. It is a first order method,
 and has stability problem for some systems.

 @author  Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public class ForwardEulerSolver extends FixedStepSolver {
    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  The name of the solver is set to "CT_Forward_Euler_Solver".
     */
    public ForwardEulerSolver() {
        this(null);
    }

    /** Construct a solver in the given workspace.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  The name of the solver is set to "CT_Forward_Euler_Solver".
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     */
    public ForwardEulerSolver(Workspace workspace) {
        super(workspace);

        try {
            setName(_DEFAULT_NAME);
        } catch (KernelException ex) {
            throw new InternalErrorException(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the integrator to resolve states.
     *  @param integrator The integrator to be fired.
     *  @exception IllegalActionException If there is no director, or can not
     *  read input, or can not send output.
     */
    public void integratorFire(CTBaseIntegrator integrator)
            throws IllegalActionException {
        if (_getRoundCount() == 0) {
            // During the first round, use the current derivative to predict
            // the states at currentModelTime + currentStepSize. The predicted
            // states are the initial guesses for fixed-point iteration.
            CTDirector director = (CTDirector) getContainer();
            double tentativeState = integrator.getState();
            double f = integrator.getDerivative();
            tentativeState = tentativeState
                    + (f * (director.getCurrentStepSize()));

            // Set converged to false such that the integrator will be refired
            // again to check convergence of resolved states.
            _voteForConverged(false);
            integrator.setTentativeState(tentativeState);
            integrator.output.broadcast(new DoubleToken(tentativeState));
        } else {
            // During the second round, store the derivative for the next
            // integration. Because the default converged is true, we do not set
            // it to true again here.
            double f = ((DoubleToken) integrator.input.get(0)).doubleValue();
            integrator.setTentativeDerivative(f);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // static name.
    private static final String _DEFAULT_NAME = "CT_Forward_Euler_Solver";
}
