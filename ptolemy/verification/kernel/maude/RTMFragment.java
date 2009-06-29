/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2009 The Regents of the University of California.
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
*/
package ptolemy.verification.kernel.maude;

//////////////////////////////////////////////////////////////////////////
//// RTMFragment

/**
 * A Real-Time Maude fragment.
 *
 * @author Thomas Huining Feng
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (tfeng)
 * @Pt.AcceptedRating Red (tfeng)
 */
public class RTMFragment extends RTMTerm {

    protected String frag;

    public RTMFragment(String fragment) {
        super();
        this.frag = fragment;
    }

    @Override
    public String print(int indent, boolean newline) {
        if (newline) {
            return front(indent) + this.frag;
        } else {
            return this.frag;
        }
    }
}
