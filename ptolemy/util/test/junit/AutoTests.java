/* Run the Ptolemy model tests in the auto/ directory using JUnit.

 Copyright (c) 2011 The Regents of the University of California.
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
import java.io.IOException;
import java.lang.reflect.Constructor;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

///////////////////////////////////////////////////////////////////
//// AutoTests
/**
 * Run the Ptolemy model tests in the auto/ directory using JUnit.
 * 
 * <p>
 * This test must be run from the directory that contains the auto/ directory,
 * for example:
 * </p>
 * 
 * <pre>
 * (cd ~/ptII/ptolemy/actor/lib/io/test; java -classpath ${PTII}:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar org.junit.runner.JUnitCore ptolemy.util.test.junit.AutoTests)
 * </pre>
 * 
 * <p>
 * This test uses JUnitParams from <a
 * href="http://code.google.com/p/junitparams/#in_browser"
 * >http://code.google.com/p/junitparams/</a>, which is released under <a
 * href="http://www.apache.org/licenses/LICENSE-2.0#in_browser">Apache License
 * 2.0</a>.
 * </p>
 * 
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@RunWith(JUnitParamsRunner.class)
public class AutoTests {

	/**
	 * Return a two dimensional array of arrays of strings that name the model
	 * to be executed. If auto/ does not exist, or does not contain files that
	 * end with .xml or .moml, return a list with one element that is empty.
	 * 
	 * @return The List of model names in auto/
         * @exception IOException If there is a problem accessing the auto/ directory.
	 */
	public Object[] parametersForRunModel() throws IOException {
		File auto = new File("auto/");
		if (auto.isDirectory()) {
			String[] modelFiles = auto.list(new FilenameFilter() {
				/**
				 * Return true if the file name ends with .xml or .moml
				 * 
				 * @param directory
				 *            Ignored
				 * @param name
				 *            The name of the file.
				 * @return true if the file name ends with .xml or .moml
				 */
				public boolean accept(File directory, String name) {
					String fileName = name.toLowerCase();
					return fileName.endsWith(".xml")
							|| fileName.endsWith(".moml");
				}
			});
			int i = 0;
			Object[][] data = new Object[modelFiles.length][1];
                        if (modelFiles.length > 0) {
                            for (String modelFile : modelFiles) {
				data[i++][0] = new File("auto/" + modelFile).getCanonicalPath();
                            }
                            return data;
                        } else {
                            return new Object[][] { { THERE_ARE_NO_AUTO_TESTS } };
                        }
		}
		return new Object[][] { { THERE_ARE_NO_AUTO_TESTS } };
	}

	/**
	 * Find the ptolemy.moml.MoMLSimpleApplication class and its constructor
	 * that takes a String.
	 * 
	 * @exception Throwable
	 *                If the class or constructor cannot be found.
	 */
	@Before
	public void setUp() throws Throwable {
		_applicationClass = Class.forName("ptolemy.moml.MoMLSimpleApplication");
		_applicationConstructor = _applicationClass
				.getConstructor(String.class);
	}

	/**
	 * Execute a model.
	 * 
	 * @param fullPath
	 *            The full path to the model file to be executed. If the
	 *            fullPath ends with the value of the
	 *            {@link #THERE_ARE_NO_AUTO_TESTS}, then the method returns
	 *            immediately.
	 * @exception Throwable
	 *                If thrown while executing the model.
	 */
	@Test
	@Parameters
	public void RunModel(String fullPath) throws Throwable {
		if (fullPath.endsWith(THERE_ARE_NO_AUTO_TESTS)) {
			System.out.println("No auto/*.xml tests in "
					+ System.getProperty("user.dir"));
			return;
		}
		System.out.println("----------------- testing " + fullPath);
		System.out.flush();
		_applicationConstructor.newInstance(fullPath);
	}

	// /////////////////////////////////////////////////////////////////
	// // protected variables ////

	/**
	 * The MoMLSimpleApplication class. We use reflection her to avoid false
	 * dependencies if auto/ does not exist.
	 */
	protected static Class _applicationClass;

	/** The MoMLSimpleApplication(String) constructor. */
	protected static Constructor _applicationConstructor;

	/** The path to the .xml or .moml file that contains the model. */
	protected String _modelFile;

	/**
	 * A special string that is passed when there are no known failed tests.
	 * This is necessary to avoid an exception in the JUnitParameters.
	 */
	protected final static String THERE_ARE_NO_AUTO_TESTS = "ThereAreNoAutoTests";
}
