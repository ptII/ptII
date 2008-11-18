/*

Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.copernicus.jhdl.circuit;

import soot.*;

import soot.jimple.*;

import ptolemy.actor.*;
import ptolemy.copernicus.jhdl.soot.*;
import ptolemy.copernicus.jhdl.util.*;
import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import java.util.*;


//////////////////////////////////////////////////////////////////////////
////

/**
 * Simple implementation of the Signal interface.

 @author Mike Wirthlin
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
*/
public class SimpleSignal implements Signal {
    public SimpleSignal(String name, int width) {
        _name = name;
        _width = width;
    }

    public SimpleSignal(String name) {
        this(name, Signal.UNRESOLVED);
    }

    public int getSignalWidth() {
        return _width;
    }

    public void setSignalWidth(int width) {
        _width = width;
    }

    public boolean isResolved() {
        if (_width != Signal.UNRESOLVED) {
            return true;
        }

        return false;
    }

    public String toString() {
        return "Port:" + _name + "-" + _width;
    }

    public String getName() {
        return _name;
    }

    protected String _name;
    protected int _width;
}
