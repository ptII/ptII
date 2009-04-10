# Tests for the State class
#
# @Author: Xiaojun Liu
#
# @Version: $Id$
#
# @Copyright (c) 2000-2009 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test State-1.1 {test creating a state} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName e0
    set fsm [java::new ptolemy.domains.modal.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.modal.kernel.State $fsm s0]
    set p0 [$s0 getPort incomingPort]
    set p1 [$s0 getPort outgoingPort]
    list [$s0 getName] [$s0 getFullName] [$p0 getFullName] [$p1 getFullName]
} {s0 .e0.fsm.s0 .e0.fsm.s0.incomingPort .e0.fsm.s0.outgoingPort}

test State-1.2 {container of a state need not be an FSMActor} {
    set e0 [java::new ptolemy.kernel.CompositeEntity]
    $e0 setName e0
    set fsm [java::new ptolemy.domains.modal.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.modal.kernel.State $fsm s0]
    $s0 setContainer [java::null]
    set msg0 [$s0 getFullName]
    $s0 setContainer $e0
    list {}
} {{}}

######################################################################
####
#
test State-2.1 {test listing preemptive and non-preemptive transitions} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set fsm [java::new ptolemy.domains.modal.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.modal.kernel.State $fsm s0]
    set s1 [java::new ptolemy.domains.modal.kernel.State $fsm s1]
    set t0 [java::new ptolemy.domains.modal.kernel.Transition $fsm t0]
    [java::field $s0 outgoingPort] link $t0
    [java::field $s1 incomingPort] link $t0
    set t1 [java::new ptolemy.domains.modal.kernel.Transition $fsm t1]
    [java::field $s0 outgoingPort] link $t1
    [java::field $s1 incomingPort] link $t1
    set tok0 [java::field ptolemy.data.BooleanToken FALSE]
    set tok1 [java::field ptolemy.data.BooleanToken TRUE]
    [java::field $t1 preemptive] setToken $tok1
    set ls1 [listToNames [$s0 preemptiveTransitionList]]
    set ls2 [listToNames [$s0 nonpreemptiveTransitionList]]
    [java::field $t1 preemptive] setToken $tok0
    set ls3 [listToNames [$s0 preemptiveTransitionList]]
    set ls4 [listToNames [$s0 nonpreemptiveTransitionList]]
    [java::field $t0 preemptive] setToken $tok1
    [java::field $t1 preemptive] setToken $tok1
    set ls5 [listToNames [$s0 preemptiveTransitionList]]
    set ls6 [listToNames [$s0 nonpreemptiveTransitionList]]
    list $ls1 $ls2 $ls3 $ls4 $ls5 $ls6
} {t1 t0 {} {t0 t1} {t0 t1} {}}

######################################################################
####
#
test State-3.1 {test setting refinement} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName e0
    set fsm [java::new ptolemy.domains.modal.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.modal.kernel.State $fsm s0]
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 e1]
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e0 e2]
    set re0 [java::isnull [$s0 getRefinement]]
    [java::field $s0 refinementName] setExpression e1
    set ar0 [$s0 getRefinement]
    set ref0 [java::cast ptolemy.kernel.Entity [$ar0 get 0]]
    set re1 [$ref0 getFullName]
    [java::field $s0 refinementName] setExpression e2
    set ar0 [$s0 getRefinement]
    set ref1 [java::cast ptolemy.kernel.Entity [$ar0 get 0]]
    set re2 [$ref1 getFullName]
    [java::field $s0 refinementName] setExpression e3
    catch {$s0 getRefinement} msg
    list $re0 $re1 $re2 $msg
} {1 .e0.e1 .e0.e2 {ptolemy.kernel.util.IllegalActionException: Cannot find refinement with name "e3" in .e0
  in .e0.fsm.s0}}

