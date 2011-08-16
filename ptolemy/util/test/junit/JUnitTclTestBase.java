/* Run the Tcl tests under JUnit.

 Copyright (c) 2010-2011 The Regents of the University of California.
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

package ptolemy.util.test.junit;

import java.io.File;
import java.io.FilenameFilter;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ptolemy.moml.MoMLSimpleApplication;

import tcl.lang.Interp;

///////////////////////////////////////////////////////////////////
//// JUnitTclTestBase
/**
 * Run the Tcl tests under JUnit.
 * <p>Derived classes should have a method that calls super.run().
 *
 * <p>If the fileName JVM property is set, then the file named by
 * that property is sourced.  Otherwise, the testDefs.tcl file
 * is sourced and the doallTests Tcl proc that is defined
 * in $PTII/util/testsuite/testDefs.tcl is invoked and then
 * any models in the auto/ directory are invoked.</p>
 *
 * <p>For example, ptolemy.kernel.test.junit.JUnitTclTest extends this class.
 * To run one test file (Port.tcl):
 * <pre>
 * cd $PTII
 * java -DfileName=Port.tcl -classpath ${PTII}:${PTII}/bin/ptjacl.jar:${PTII}/lib/junit-4.8.2.jar org.junit.runner.JUnitCore ptolemy.kernel.test.junit.JUnitTclTest
 * </pre></p>
 *
 * <p>To run all the .tcl files in the directory above this directory
 * <pre>
 * cd $PTII
 * java -classpath ${PTII}:${PTII}/bin/ptjacl.jar:${PTII}/lib/junit-4.8.2.jar org.junit.runner.JUnitCore ptolemy.kernel.test.junit.JUnitTclTest
 * </pre></p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ptolemy.util.test.junit.AutoTests.class, ptolemy.util.test.junit.TclTests.class})
public class JUnitTclTestBase {
}