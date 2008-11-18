/*
  Copyright (c) 2004-2005 The Regents of the University of California.
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


/** Print the value of the java.home property
    @author Christopher Hylands Brooks
    @version $Id$
    @since Ptolemy II 4.0
    @Pt.ProposedRating Green (cxh)
    @Pt.AcceptedRating Red
*/
public class JavaHome {
    public static void main(String[] args) {
        try {
            String javaHome = System.getProperty("java.home");

            if (javaHome == null) {
                throw new Exception("Could not get java.home property");
            }

            // java policy files want forward slashes.
            System.out.print(javaHome.replace('\\', '/'));
        } catch (Exception exception) {
            System.err.print("JavaHome.main(): " + exception);
        }
    }
}
