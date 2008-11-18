/* A simple class for testing standard output.

Copyright (c) 2003-2005 The University of Maryland
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY

*/

//////////////////////////////////////////////////////////////////////////
//// Turtle

/**

A simple class for testing standard output.

@author Ankush Varma
@version $Id$
@since Ptolemy II 4.0
@Pt.ProposedRating Red (ssb)
@Pt.AcceptedRating Red (ssb)

*/
public class PrintStreamTest {
    public static void main(String[] args) {
        System.out.println(1);
        System.out.println(2.012);

        String string = new String("Fear the Turtle!!!");
        System.out.println(string);

        Object object = string;
        System.out.println(object);

        System.out.println(true);
        System.out.println(false);
    }
}
