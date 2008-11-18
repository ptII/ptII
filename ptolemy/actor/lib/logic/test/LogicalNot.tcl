# Test LogicalNot.
#
# @Author: John Li
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
test LogicalNot-1.1 {test constructor and clone} {
    set e0 [sdfModel 1]
    set logic [java::new ptolemy.actor.lib.logic.LogicalNot $e0 logic]
    set newObject [java::cast ptolemy.actor.lib.logic.LogicalNot \
		       [$logic clone [$e0 workspace]]]
    # Success here is just not throwing an exception.
    list {}
} {{}}


######################################################################
#### Test Equals in an SDF model
#
test LogicalNot-2.1 {Basic value: True} {
    set in1 [java::new ptolemy.actor.lib.Const $e0 in1]
    set in1value [getParameter $in1 value]
    $in1value setToken [java::new ptolemy.data.BooleanToken true]

    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set input [java::field [java::cast ptolemy.actor.lib.Transformer \
            $logic] input]
    set r1 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $in1] output] \
       $input]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Transformer \
            $logic] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicalNot-2.2 {Basic value: False} {
    $in1value setToken [java::new ptolemy.data.BooleanToken false]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}
