# Test Comparator.
#
# @Author: Edward A. Lee
#
# @Version: $Id$
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

######################################################################
####
#
test Comparator-1.1 {test constructor and clone} {
    set e0 [sdfModel 1]
    set compare [java::new ptolemy.actor.lib.logic.Comparator $e0 compare]
    set newObject [java::cast ptolemy.actor.lib.logic.Comparator [$compare clone [$e0 workspace]]]
    set comparison [java::cast ptolemy.kernel.util.StringAttribute \
            [$compare getAttribute comparison]]
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Major numbers refer to the truth table
#### Minor numbers refer to the logic function (or 0 for setup)
#
test Compare-2.0 {Truth Table: True, True} {
    set in1 [java::new ptolemy.actor.lib.Const $e0 in1]
    set in1value [getParameter $in1 value]
    $in1value setExpression {1.0}
    set in2 [java::new ptolemy.actor.lib.Const $e0 in2]
    set in2value [getParameter $in2 value]
    $in2value setExpression {2.0}

    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set left [java::field $compare left]
    set right [java::field $compare right]
    set r1 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $in1] output] \
       $left]
    set r2 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $in2] output] \
       $right]
    $e0 connect \
       [java::field $compare output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    # Success here is just not throwing an exception.
    list {}
} {{}}

test Compare-2.1 {>} {
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test Compare-2.2 {<} {
    $comparison setExpression {<}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test Compare-2.3 {<=} {
    $comparison setExpression {<=}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test Compare-2.3 {>=} {
    $comparison setExpression {>=}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

