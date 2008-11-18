# Tests for the functionality of functions in UtilityFunctions class.
#
# @author: Bart Kienhuis
#
# @Version $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

#
#


######################################################################

test FixFunctions-1.0 {Check Fix method for a single FixPoint} {
    set parser [java::new ptolemy.data.expr.PtParser]

    set tree [$parser generateParseTree "fix(5.34, 10, 4)"]
    set res [$tree evaluateParseTree]
    set value [$res toString]

    list $value
} {fix(5.34375,10,4)}

test FixFunctions-1.1 {Check Fix method for a matrix of FixPoints} {
    set parser [java::new ptolemy.data.expr.PtParser]

    set tree [$parser generateParseTree "fix(\[ -.040609, -.001628,\
	.17853, .37665, .37665, .17853, -.001628, -.040609 \], 10,  2)"]
    set res [$tree evaluateParseTree]

    set value [$res toString]
    list $value

} {{[fix(-0.0390625,10,2), fix(0.0,10,2), fix(0.1796875,10,2),\
fix(0.375,10,2), fix(0.375,10,2), fix(0.1796875,10,2),\
fix(0.0,10,2), fix(-0.0390625,10,2)]}}

test FixFunctions-1.2 {Check Fix method for an array of FixPoints} {
    set parser [java::new ptolemy.data.expr.PtParser]

    set tree [$parser generateParseTree "fix(\{ -.040609, -.001628,\
	.17853, .37665, .37665, .17853, -.001628, -.040609 \}, 10,  2)"]
    set res [$tree evaluateParseTree]

    set value [$res toString]
    list $value

} {{{fix(-0.0390625,10,2), fix(0.0,10,2), fix(0.1796875,10,2),\
fix(0.375,10,2), fix(0.375,10,2), fix(0.1796875,10,2),\
fix(0.0,10,2), fix(-0.0390625,10,2)}}}

test FixFunctions-2.0 {Check quantize method, returning a FixPoint} {
    set parser [java::new ptolemy.data.expr.PtParser]

    set tree [$parser generateParseTree "quantize(5.34, 10, 4)" ]

    #$tree displayParseTree " "
    set res [$tree evaluateParseTree]

    set value [$res toString]
    list $value

} {5.34375}

test FixFunctions-2.1 {Check quantize method, returning a matrix of FixPoints} {
    set parser [java::new ptolemy.data.expr.PtParser]

    set tree [$parser generateParseTree "quantize(\[ -.040609, -.001628,\
.17853, .37665, .37665, .17853, -.001628, -.040609 \], 10,  2)" ]

    #$tree displayParseTree " "
    set res [$tree evaluateParseTree]

    set value [$res toString]
    list $value

} {{[-0.0390625, 0.0, 0.1796875, 0.375, 0.375, 0.1796875, 0.0, -0.0390625]}}

test FixFunctions-2.2 {Check quantize method, returning an array of FixPoints} {
    set parser [java::new ptolemy.data.expr.PtParser]

    set tree [$parser generateParseTree "quantize(\{ -.040609, -.001628,\
.17853, .37665, .37665, .17853, -.001628, -.040609 \}, 10,  2)" ]

    #$tree displayParseTree " "
    set res [$tree evaluateParseTree]

    set value [$res toString]
    list $value

} {{{-0.0390625, 0.0, 0.1796875, 0.375, 0.375, 0.1796875, 0.0, -0.0390625}}}
