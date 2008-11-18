# Tests for the FeedBackDelay class
#
# @Author: John S. Davis II
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

#
#

######################################################################
####
# Global Variables 
set globalEndTimeReceiver [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue]
#set globalEndTime [java::field $globalEndTimeReceiver INACTIVE]
set globalEndTime -2.0
set globalIgnoreTimeReceiver [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue]
set globalIgnoreTime -1.0
# set globalIgnoreTime [java::field $globalIgnoreTimeReceiver IGNORE]
set globalNullTok [java::new ptolemy.domains.dde.kernel.NullToken]

######################################################################
####
#
test FeedBackDelay-1.1 {Constructors} {
    set w [java::new ptolemy.kernel.util.Workspace W]

    set f1 [java::new ptolemy.domains.dde.kernel.FeedBackDelay]
    $f1 setName F1
    set f2 [java::new ptolemy.domains.dde.kernel.FeedBackDelay $w]
    $f2 setName F2
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set f3 [java::new ptolemy.domains.dde.kernel.FeedBackDelay $e0 F3]

    # Set setDelay and getDelay
    set r1 [$f3 getDelay]
    #$f3 setDelay 6.0
    #set r2 [$f3 getDelay]

    list [$f1 getFullName] [$f2 getFullName] [$f3 getFullName] $r1
} {.F1 .F2 .E0.F3 1.0}

######################################################################
####
#
test FeedBackDelay-2.1 {Cycle null tokens with actor/lib/clock} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "director"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    $toplevel setDirector $dir
    $toplevel setManager $mgr
    set dirStopTime [java::cast ptolemy.data.expr.Parameter [$dir getAttribute stopTime]]
    $dirStopTime setToken [java::new ptolemy.data.DoubleToken 26.0]

    set clock [java::new ptolemy.actor.lib.Clock $toplevel "clock"]

    set values [java::cast ptolemy.data.expr.Parameter [$clock getAttribute values]]
    $values setExpression {{1, 1}}
    set period [java::cast ptolemy.data.expr.Parameter [$clock getAttribute period]]
    $period setToken [java::new ptolemy.data.DoubleToken 20.0]
    set offsets [java::cast ptolemy.data.expr.Parameter [$clock getAttribute offsets]]
    $offsets setExpression {{5.0, 15.0}}
    set stopTime [java::cast ptolemy.data.expr.Parameter [$clock getAttribute stopTime]]
    $stopTime setToken [java::new ptolemy.data.DoubleToken 27.0]

    set clockOut [java::cast ptolemy.actor.TypedIOPort [$clock getPort "output"]]
    $clockOut setMultiport true

    set actorReceiver [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorReceiver" 3]
    set join [java::new ptolemy.domains.dde.kernel.test.FlowThrough $toplevel "join"]
    set fork [java::new ptolemy.domains.dde.kernel.test.TwoPut $toplevel "fork"]
    set fBack [java::new ptolemy.domains.dde.kernel.FeedBackDelay $toplevel "fBack"]

    set delay [java::cast ptolemy.data.expr.Parameter [$fBack getAttribute "delay"]]
    $delay setExpression 4.0

    set rcvrIn [$actorReceiver getPort "input"]
    set joinIn [$join getPort "input"]
    set joinOut [$join getPort "output"]
    set forkIn [$fork getPort "input"]
    set forkOut1 [$fork getPort "output1"]
    set forkOut2 [$fork getPort "output2"]
    set fBackIn [$fBack getPort "input"]
    set fBackOut [$fBack getPort "output"]

    $toplevel connect $clockOut $joinIn
    $toplevel connect $joinOut $forkIn
    $toplevel connect $forkOut1 $rcvrIn 
    $toplevel connect $fBackOut $joinIn
    $toplevel connect $fBackIn $forkOut2

    $mgr run

    set time0 [$actorReceiver getAfterTime 0]
    set time1 [$actorReceiver getAfterTime 1]
    set time2 [$actorReceiver getAfterTime 2]

    list $time0 $time1 $time2

} {5.0 15.0 25.0} {KNOWN_FAILED}


######################################################################
####
#
test FeedBackDelay-3.1 {Cycle real tokens with actor/lib/clock} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "director"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    $toplevel setDirector $dir
    $toplevel setManager $mgr
    set dirStopTime [java::cast ptolemy.data.expr.Parameter [$dir getAttribute stopTime]]
    $dirStopTime setToken [java::new ptolemy.data.DoubleToken 20.0]

    set clock [java::new ptolemy.actor.lib.Clock $toplevel "clock"]

    set values [java::cast ptolemy.data.expr.Parameter [$clock getAttribute values]]
    $values setExpression {{1, 1}}
    set period [java::cast ptolemy.data.expr.Parameter [$clock getAttribute period]]
    $period setToken [java::new ptolemy.data.DoubleToken 20.0]
    set offsets [java::cast ptolemy.data.expr.Parameter [$clock getAttribute offsets]]
    $offsets setExpression {{5.0, 15.0}}
    set stopTime [java::cast ptolemy.data.expr.Parameter [$clock getAttribute stopTime]]
    $stopTime setToken [java::new ptolemy.data.DoubleToken 27.0]

    set clockOut [java::cast ptolemy.actor.TypedIOPort [$clock getPort "output"]]
    $clockOut setMultiport true

    set actorReceiver [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorReceiver" 3]
    set join [java::new ptolemy.domains.dde.kernel.test.FlowThrough $toplevel "join"]
    set fork [java::new ptolemy.domains.dde.kernel.test.TwoPut $toplevel "fork"]
    set fBack [java::new ptolemy.domains.dde.kernel.FeedBackDelay $toplevel "fBack"]
    set sink [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "sink" 1]

    set delay [java::cast ptolemy.data.expr.Parameter [$fBack getAttribute "delay"]]
    $delay setExpression 4.0

    set realDelay [java::cast ptolemy.data.expr.Parameter [$fBack getAttribute realDelay]]
    $realDelay setToken [java::new ptolemy.data.BooleanToken true]

    set rcvrIn [$actorReceiver getPort "input"]
    set clockOut [$clock getPort "output"]
    set joinIn [$join getPort "input"]
    set joinOut [$join getPort "output"]
    set forkIn [$fork getPort "input"]
    set forkOut1 [$fork getPort "output1"]
    set forkOut2 [$fork getPort "output2"]
    set fBackIn [$fBack getPort "input"]
    set fBackOut [$fBack getPort "output"]
    set sinkIn [$sink getPort "input"]

    $toplevel connect $clockOut $joinIn
    $toplevel connect $joinOut $forkIn
    $toplevel connect $forkOut1 $rcvrIn 
    $toplevel connect $forkOut2 $sinkIn 
    $toplevel connect $fBackOut $joinIn
    $toplevel connect $fBackIn $forkOut1

    $mgr run

    set time0 [$actorReceiver getAfterTime 0]
    set time1 [$actorReceiver getAfterTime 1]
    set time2 [$actorReceiver getAfterTime 2]

    list $time0 $time1 $time2

} {5.0 9.0 13.0} {KNOWN_FAILED}

######################################################################
####
#
test FeedBackDelay-4.1 {Dual cycle with 0 delay in lower cycle with actor/lib/clock} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "director"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    $toplevel setDirector $dir
    $toplevel setManager $mgr
    set dirStopTime [java::cast ptolemy.data.expr.Parameter [$dir getAttribute stopTime]]
    $dirStopTime setToken [java::new ptolemy.data.DoubleToken 26.0]

    set clock [java::new ptolemy.actor.lib.Clock $toplevel "clock"]

    set values [java::cast ptolemy.data.expr.Parameter [$clock getAttribute values]]
    $values setExpression {{1, 1}}
    set period [java::cast ptolemy.data.expr.Parameter [$clock getAttribute period]]
    $period setToken [java::new ptolemy.data.DoubleToken 20.0]
    set offsets [java::cast ptolemy.data.expr.Parameter [$clock getAttribute offsets]]
    $offsets setExpression {{5.0, 15.0}}
    set stopTime [java::cast ptolemy.data.expr.Parameter [$clock getAttribute stopTime]]
    $stopTime setToken [java::new ptolemy.data.DoubleToken 27.0]

    set clockOut [java::cast ptolemy.actor.TypedIOPort [$clock getPort "output"]]
    set rcvr1 [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "rcvr1" 3]
    set join1 [java::new ptolemy.domains.dde.kernel.test.FlowThrough $toplevel "join1"]
    set fork1 [java::new ptolemy.domains.dde.kernel.test.TwoPut $toplevel "fork1"]
    set fBack1 [java::new ptolemy.domains.dde.kernel.FeedBackDelay $toplevel "fBack1"]
    set rcvr2 [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "rcvr2" 3]
    set join2 [java::new ptolemy.domains.dde.kernel.test.FlowThrough $toplevel "join2"]
    set fork2 [java::new ptolemy.domains.dde.kernel.test.TwoPut $toplevel "fork2"]
    set fBack2 [java::new ptolemy.domains.dde.kernel.FeedBackDelay $toplevel "fBack2"]

    set delay [java::cast ptolemy.data.expr.Parameter [$fBack1 getAttribute "delay"]]
    $delay setExpression 4.0

    set delay [java::cast ptolemy.data.expr.Parameter [$fBack2 getAttribute "delay"]]
    $delay setExpression 0.0

    set rcvr1In [$rcvr1 getPort "input"]
    set join1In [$join1 getPort "input"]
    set join1Out [$join1 getPort "output"]
    set fork1In [$fork1 getPort "input"]
    set fork1Out1 [$fork1 getPort "output1"]
    set fork1Out2 [$fork1 getPort "output2"]
    set fBack1In [$fBack1 getPort "input"]
    set fBack1Out [$fBack1 getPort "output"]

    set rcvr2In [$rcvr2 getPort "input"]
    set join2In [$join2 getPort "input"]
    set join2Out [$join2 getPort "output"]
    set fork2In [$fork2 getPort "input"]
    set fork2Out1 [$fork2 getPort "output1"]
    set fork2Out2 [$fork2 getPort "output2"]
    set fBack2In [$fBack2 getPort "input"]
    set fBack2Out [$fBack2 getPort "output"]

    set clockRelation [$toplevel connect $clockOut $join1In]
    $join2In link $clockRelation 

    $toplevel connect $join1Out $fork1In
    $toplevel connect $fork1Out1 $rcvr1In 
    $toplevel connect $fBack1Out $join1In
    $toplevel connect $fBack1In $fork1Out2

    $toplevel connect $join2Out $fork2In
    $toplevel connect $fork2Out1 $rcvr2In 
    $toplevel connect $fBack2Out $join2In
    $toplevel connect $fBack2In $fork2Out2

    $mgr run

    set time1_0 [$rcvr1 getAfterTime 0]
    set time1_1 [$rcvr1 getAfterTime 1]
    set time1_2 [$rcvr1 getAfterTime 2]

    set time2_0 [$rcvr2 getAfterTime 0]
    set time2_1 [$rcvr2 getAfterTime 1]
    set time2_2 [$rcvr2 getAfterTime 2]

    list $time1_0 $time1_1 $time1_2 $time2_0 $time2_1 $time2_2

} {5.0 15.0 25.0 5.0 -1.0 -1.0} {KNOWN_FAILED}

######################################################################
####
#
test FeedBackDelay-4.2 {Dual cycle with very small delay in lower cycle with actor/lib/clock} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "director"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    $toplevel setDirector $dir
    $toplevel setManager $mgr
    set dirStopTime [java::cast ptolemy.data.expr.Parameter [$dir getAttribute stopTime]]
    $dirStopTime setToken [java::new ptolemy.data.DoubleToken 27.0]

    set clock [java::new ptolemy.actor.lib.Clock $toplevel "clock"]

    set values [java::cast ptolemy.data.expr.Parameter [$clock getAttribute values]]
    $values setExpression {{1, 1}}
    set period [java::cast ptolemy.data.expr.Parameter [$clock getAttribute period]]
    $period setToken [java::new ptolemy.data.DoubleToken 20.0]
    set offsets [java::cast ptolemy.data.expr.Parameter [$clock getAttribute offsets]]
    $offsets setExpression {{5.0, 15.0}}
    set stopTime [java::cast ptolemy.data.expr.Parameter [$clock getAttribute stopTime]]
    $stopTime setToken [java::new ptolemy.data.DoubleToken 27.0]

    set rcvr1 [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "rcvr1" 3]
    set join1 [java::new ptolemy.domains.dde.kernel.test.FlowThrough $toplevel "join1"]
    set fork1 [java::new ptolemy.domains.dde.kernel.test.TwoPut $toplevel "fork1"]
    set fBack1 [java::new ptolemy.domains.dde.kernel.FeedBackDelay $toplevel "fBack1"]
    set rcvr2 [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "rcvr2" 3]
    set join2 [java::new ptolemy.domains.dde.kernel.test.FlowThrough $toplevel "join2"]
    set fork2 [java::new ptolemy.domains.dde.kernel.test.TwoPut $toplevel "fork2"]
    set fBack2 [java::new ptolemy.domains.dde.kernel.FeedBackDelay $toplevel "fBack2"]

    set delay [java::cast ptolemy.data.expr.Parameter [$fBack1 getAttribute "delay"]]
    $delay setExpression 4.0
    set delay [java::cast ptolemy.data.expr.Parameter [$fBack2 getAttribute "delay"]]
    $delay setExpression 4.0

    set clockOut [java::cast ptolemy.actor.TypedIOPort [$clock getPort "output"]]
    set rcvr1In [$rcvr1 getPort "input"]
    set join1In [$join1 getPort "input"]
    set join1Out [$join1 getPort "output"]
    set fork1In [$fork1 getPort "input"]
    set fork1Out1 [$fork1 getPort "output1"]
    set fork1Out2 [$fork1 getPort "output2"]
    set fBack1In [$fBack1 getPort "input"]
    set fBack1Out [$fBack1 getPort "output"]

    set rcvr2In [$rcvr2 getPort "input"]
    set join2In [$join2 getPort "input"]
    set join2Out [$join2 getPort "output"]
    set fork2In [$fork2 getPort "input"]
    set fork2Out1 [$fork2 getPort "output1"]
    set fork2Out2 [$fork2 getPort "output2"]
    set fBack2In [$fBack2 getPort "input"]
    set fBack2Out [$fBack2 getPort "output"]

    set clockRelation [$toplevel connect $clockOut $join1In]
    $join2In link $clockRelation

    $toplevel connect $join1Out $fork1In
    $toplevel connect $fork1Out1 $rcvr1In 

    $toplevel connect $fBack1Out $join1In
    $toplevel connect $fBack1In $fork1Out2

    $toplevel connect $join2Out $fork2In
    $toplevel connect $fork2Out1 $rcvr2In 
    $toplevel connect $fBack2Out $join2In
    $toplevel connect $fBack2In $fork2Out2

    $mgr run

    set time1_0 [$rcvr1 getAfterTime 0]
    set time1_1 [$rcvr1 getAfterTime 1]
    set time1_2 [$rcvr1 getAfterTime 2]

    set time2_0 [$rcvr2 getAfterTime 0]
    set time2_1 [$rcvr2 getAfterTime 1]
    set time2_2 [$rcvr2 getAfterTime 2]

    list $time1_0 $time1_1 $time1_2 $time2_0 $time2_1 $time2_2 

} {5.0 15.0 25.0 5.0 15.0 25.0} {KNOWN_FAILED}
