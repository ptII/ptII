# Tests for constant variable model analysis
#
# @Author: Steve Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 2000-2007 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkClassPathSeparator] == "" } then { 
    source [file join $PTII util testsuite jdktools.tcl]
}

if {[string compare sdfModel [info procs sdfModel]] != 0} \
        then {
    source [file join $PTII util testsuite models.tcl]
} {}

if {[info procs test_clone] == "" } then { 
    source [file join $PTII util testsuite testParameters.tcl]
}

if {[info procs test_clone] == "" } then { 
    source [file join $PTII util testsuite testParameters.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
#set VERBOSE 1

######################################################################
####
#

test ConstVariableModelAnalysis-2.0 {test fsms.} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set a [java::new ptolemy.data.expr.Parameter $e0 a]
    set cinit [java::new ptolemy.data.expr.Parameter $e0 init]
    set cstep [java::new ptolemy.data.expr.Parameter $e0 step]
    $cinit setExpression 1
    $cstep setExpression a
    set fsm [java::new ptolemy.domains.modal.kernel.FSMActor $e0 fsm]
    set p1 [java::new ptolemy.data.expr.Parameter $fsm p1]
    set p2 [java::new ptolemy.data.expr.Parameter $fsm p2]
    set s1 [java::new ptolemy.domains.modal.kernel.State $fsm s1]
    set s2 [java::new ptolemy.domains.modal.kernel.State $fsm s2]
    set s1_incomingPort [$s1 getPort incomingPort]
    set s2_incomingPort [$s2 getPort incomingPort]
    set s1_outgoingPort [$s1 getPort outgoingPort]
    set s2_outgoingPort [$s2 getPort outgoingPort]
  
    set t1 [java::new ptolemy.domains.modal.kernel.Transition $fsm t1]
    set t2 [java::new ptolemy.domains.modal.kernel.Transition $fsm t2]
    
    $s1_incomingPort link $t1
    $s2_outgoingPort link $t1
    $s2_incomingPort link $t2
    $s1_outgoingPort link $t2

    set t1_action [getSettable $t1 setActions]
    set t2_action [getSettable $t2 setActions]
  
    $p1 setExpression "1"
    $p2 setExpression "2"
    $t1_action setExpression "p1=1"
    $t2_action setExpression "p1=2"

    set varSet [java::new java.util.HashSet]
    $varSet add $a
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0 $varSet]
    list [lsort [listToNames [$analysis getConstVariables $e0]]] \
	[lsort [listToNames [$analysis getNotConstVariables $e0]]] \
	[lsort [listToNames [$analysis getConstVariables $fsm]]] \
	[lsort [listToNames [$analysis getNotConstVariables $fsm]]]
} {init {a step} {p2 stateDependentCausality} p1}

test ConstVariableModelAnalysis-2.1 {test modal model.} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set a [java::new ptolemy.data.expr.Parameter $e0 a]
    set cinit [java::new ptolemy.data.expr.Parameter $e0 init]
    set cstep [java::new ptolemy.data.expr.Parameter $e0 step]
    $cinit setExpression 1
    $cstep setExpression a
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 e1]
    set cinit [java::new ptolemy.data.expr.Parameter $e1 init]
    set cstep [java::new ptolemy.data.expr.Parameter $e1 step]
    $cinit setExpression a
    $cstep setExpression 1

    set ramp [java::new ptolemy.actor.lib.Ramp $e1 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "5"
    $step setExpression "1"

    set fsm [java::new ptolemy.domains.modal.kernel.FSMActor $e1 fsm]
    set p1 [java::new ptolemy.data.expr.Parameter $fsm p1]
    set p2 [java::new ptolemy.data.expr.Parameter $fsm p2]
    set s1 [java::new ptolemy.domains.modal.kernel.State $fsm s1]
    set s2 [java::new ptolemy.domains.modal.kernel.State $fsm s2]
    set s1_incomingPort [$s1 getPort incomingPort]
    set s2_incomingPort [$s2 getPort incomingPort]
    set s1_outgoingPort [$s1 getPort outgoingPort]
    set s2_outgoingPort [$s2 getPort outgoingPort]
  
    set t1 [java::new ptolemy.domains.modal.kernel.Transition $fsm t1]
    set t2 [java::new ptolemy.domains.modal.kernel.Transition $fsm t2]
    
    $s1_incomingPort link $t1
    $s2_outgoingPort link $t1
    $s2_incomingPort link $t2
    $s1_outgoingPort link $t2

    set t1_action [getSettable $t1 setActions]
    set t2_action [getSettable $t2 setActions]
  
    $p1 setExpression "1"
    $p2 setExpression "2"
    $t1_action setExpression "p1=1"
    $t2_action setExpression "p1=2"

    set dynamicVariableSet [java::new java.util.HashSet]
    $dynamicVariableSet add $a

    set varSet [java::new java.util.HashSet]
    $varSet add $a
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0 $varSet]
    list [lsort [listToNames [$analysis getConstVariables $e0]]] \
	[lsort [listToNames [$analysis getConstVariables $e1]]] \
	[lsort [listToNames [$analysis getConstVariables $ramp]]] \
	[lsort [listToNames [$analysis getConstVariables $fsm]]]
} {init step {NONE firingCountLimit init step} {p2 stateDependentCausality}}
