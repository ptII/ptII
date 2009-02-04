# Tests for the SDFScheduler class
#
# @Author: Steve Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1999-2007 The Regents of the University of California.
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
proc _initialize {toplevel} {
    [$toplevel getManager] initialize
    [$toplevel getManager] wrapup
}

proc _getSchedule {scheduler} {
    list [objectsToNames [iterToObjects [[$scheduler getSchedule] actorIterator]]]
}

proc setTokenConsumptionRate {port rate} {
    set attribute [$port getAttribute tokenConsumptionRate]
    set parameter [java::cast ptolemy.data.expr.Parameter $attribute]
    $parameter setExpression $rate
    $parameter getToken
}

proc setTokenProductionRate {port rate} {
    set attribute [$port getAttribute tokenProductionRate]
    set parameter [java::cast ptolemy.data.expr.Parameter $attribute]
    $parameter setExpression $rate
    $parameter getToken
}

######################################################################
####
#
test SDFScheduler-2.1 {Constructor tests} {
    set s1 [java::new ptolemy.domains.sdf.kernel.SDFScheduler]
    set w [java::new ptolemy.kernel.util.Workspace W]
    set s2 [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    set s3 [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $s3 setName S3
    list [$s1 getFullName] [$s2 getFullName] [$s3 getFullName]
} {.Scheduler .Scheduler .S3}

######################################################################
####
#
test SDFScheduler-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set s4 [java::cast ptolemy.domains.sdf.kernel.SDFScheduler \
            [$s2 clone $w]]
    $s4 setName S4
    enumToFullNames [$w directory]
} {.Scheduler .S3}

######################################################################
####
#
test SDFScheduler-4.1 {Test setScheduler and getScheduler} {
    # NOTE: Uses the setup above
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d0 [java::new ptolemy.domains.sdf.kernel.SDFDirector $e0 D1]
    set s4 [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $s4 setName "TestScheduler"
    $d0 setScheduler $s4
    set d1 [$s4 getContainer]
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]

    list [$d0 getFullName] [$d1 getFullName] [$s4 getFullName]
} {.E0.D1 .E0.D1 .E0.D1.TestScheduler}

test SDFScheduler-4.2 {Test setValid and isValid} {
    # NOTE: Uses the setup above
    $s1 setValid true
    set result0 [$s1 isValid]
    $s1 setValid false
    set result1 [$s1 isValid]
    list $result0 $result1
} {1 0}


######################################################################
####
#
# Tests 5.* test some simple scheduling tasks without hierarchy
test SDFScheduler-5.1 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] [java::field $a2 input] R1]] setWidth 1
    $scheduler setValid false

#set debugger [java::new ptolemy.kernel.util.StreamListener]
#$director addDebugListener $debugger
#$scheduler addDebugListener $debugger

    _initialize $toplevel
    _getSchedule $scheduler
} {{Ramp Consumer}}

######################################################################
####
#
test SDFScheduler-5.2 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] [java::field $a2 input] R1]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a2 output] [java::field $a3 input] R2]] setWidth 1
    $scheduler setValid false

    _initialize $toplevel
    _getSchedule $scheduler

} {{Ramp Delay Consumer}}

######################################################################
####
#
test SDFScheduler-5.3 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestSplit $toplevel Dist]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    set a4 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer2]
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] [java::field $a2 input] R1]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a2 output1] [java::field $a3 input] R2]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a2 output2] [java::field $a4 input] R3]] setWidth 1
    $scheduler setValid false

    _initialize $toplevel
    _getSchedule $scheduler

} {{Ramp Ramp Dist Consumer2 Consumer1}}

######################################################################
####
#
test SDFScheduler-5.4 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestSplit $toplevel Dist]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestJoin $toplevel Comm]
    set a4 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] [java::field $a2 input] R1]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a2 output1] [java::field $a3 input1] R2a]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a2 output2] [java::field $a3 input2] R2d]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a3 output] [java::field $a4 input] R3]] setWidth 1
    $scheduler setValid false

    _initialize $toplevel
    _getSchedule $scheduler

} {{Ramp Ramp Dist Comm Consumer1 Consumer1}}

######################################################################
####
#
# Tests 6.* test multirate scheduling without hierarchy.
test SDFScheduler-6.1 {Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] [java::field $a2 input] R1]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a2 output] [java::field $a3 input] R4]] setWidth 1

    $scheduler setValid false

    _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    list $sched1
} {{{Ramp Delay Consumer}}}

test SDFScheduler-6.2 {Multirate Scheduling tests} {

    setTokenProductionRate [java::field $a1 output] 2
    $scheduler setValid false

    _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenProductionRate [java::field $a1 output] 1
    list $sched1
} {{{Ramp Delay Delay Consumer Consumer}}}

test SDFScheduler-6.3 {Multirate Scheduling tests} {

    setTokenProductionRate [java::field $a2 output] 2
    $scheduler setValid false

    _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenProductionRate [java::field $a2 output] 1
    list $sched1
} {{{Ramp Delay Consumer Consumer}}}

test SDFScheduler-6.4 {Multirate Scheduling tests} {

    setTokenConsumptionRate [java::field $a2 input] 2
    $scheduler setValid false

    _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1
} {{{Ramp Ramp Delay Consumer}}}

test SDFScheduler-6.5 {Multirate Scheduling tests} {

    setTokenConsumptionRate [java::field $a3 input] 2
    $scheduler setValid false

    _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1
} {{{Ramp Ramp Delay Delay Consumer}}}

######################################################################
####
#
# Tests 7.* test multirate scheduling with hierarchy
test SDFScheduler-7.1 {Multirate and Hierarchy Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.actor.TypedIOPort $c1 p1]
    $p1 setInput 1
    set p2 [java::new ptolemy.actor.TypedIOPort $c1 p2]
    $p2 setOutput 1
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $c1 d5]
    $c1 setDirector $d5
    set s5 [$d5 getScheduler]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $c1 Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] $p1 R1]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$c1 connect $p1 [java::field $a2 input] R2]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$c1 connect [java::field $a2 output] $p2 R3]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect $p2 [java::field $a3 input] R4]] setWidth 1

    $scheduler setValid false
    $s5 setValid false

 #   set debuglistener [java::new ptolemy.kernel.util.StreamListener]
 #   $scheduler addDebugListener $debuglistener
 #   $director addDebugListener $debuglistener
 #   $manager addDebugListener $debuglistener

    _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    list $sched1 $sched2
} {{{Ramp Cont Consumer}} Delay}

######################################################################
####
#
test SDFScheduler-7.2 {Multirate and Hierarchy Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a1 output] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenProductionRate [java::field $a1 output] 1
    list $sched1 $sched2
} {{{Ramp Cont Cont Consumer Consumer}} Delay}

######################################################################
####
#
test SDFScheduler-7.3 {Multirate and Hierarchy Scheduling tests} {
    # uses previous setup.

    setTokenProductionRate [java::field $a2 output] 2

    $scheduler setValid false
    $s5 setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]

    setTokenProductionRate [java::field $a2 output] 1
    list $sched1 $sched2
} {{{Ramp Cont Consumer Consumer}} Delay}

######################################################################
####
#
test SDFScheduler-7.4 {Multirate and Hierarchy Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a2 input] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1 $sched2
} {{{Ramp Ramp Cont Consumer}} Delay}

######################################################################
####
#
test SDFScheduler-7.5 {Multirate and Hierarchy Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a3 input] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1 $sched2
} {{{Ramp Ramp Cont Cont Consumer}} Delay}

######################################################################
####
#
# Tests 8.* test multiport scheduling without hierarchy.
test SDFScheduler-8.1 {input Multiport, Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    set port [java::field $a3 input]
    $port setMultiport true

    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] [java::field $a3 input] R1]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a2 output] [java::field $a3 input] R2]] setWidth 1

    $scheduler setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    list $sched1
} {{{Ramp2 Ramp1 Consumer}}}

test SDFScheduler-8.2 {input Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a3 input] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1
} {{{Ramp2 Ramp2 Ramp1 Ramp1 Consumer}}}

test SDFScheduler-8.3 {input Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a2 output] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenProductionRate [java::field $a2 output] 1
    list $sched1
} {{{Ramp2 Ramp1 Ramp1 Consumer Consumer}}}

test SDFScheduler-8.4 {input Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a3 input] 2
    setTokenProductionRate [java::field $a2 output] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenConsumptionRate [java::field $a3 input] 1
    setTokenProductionRate [java::field $a2 output] 1
    list $sched1
} {{{Ramp2 Ramp1 Ramp1 Consumer}}}

test SDFScheduler-8.5 {input Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a3 input] 2
    setTokenProductionRate [java::field $a2 output] 2
    setTokenProductionRate [java::field $a1 output] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenConsumptionRate [java::field $a3 input] 1
    setTokenProductionRate [java::field $a2 output] 1
    setTokenProductionRate [java::field $a1 output] 1
    list $sched1
} {{{Ramp2 Ramp1 Consumer}}}

test SDFScheduler-8.6 {input Multiport with no connections} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    set port [java::field $a3 input]
    $port setMultiport true

    $scheduler setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    list $sched1
} {Consumer}

test SDFScheduler-8.7 {input Multiport with no connections - disconnected graph} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    set port [java::field $a3 input]
    $port setMultiport true

    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer2]
    set port [java::field $a3 input]
    $port setMultiport true

    $scheduler setValid false

    set sched1 {}
    catch { _initialize $toplevel
    set sched1 [_getSchedule $scheduler]} s1
    list $sched1 $s1
} {{} {ptolemy.actor.sched.NotSchedulableException: SDF scheduler found disconnected actors! Usually, disconnected actors in an SDF model indicates an error.  If this is not an error, try setting the SDFDirector parameter allowDisconnectedGraphs to true.
Unreached Actors:
.Toplevel.Consumer2 
Reached Actors:
.Toplevel.Consumer1 }}

test SDFScheduler-8.7.1 {input Multiport with no connections - disconnected graph permitted} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    set port [java::field $a3 input]
    $port setMultiport true

    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer2]
    set port [java::field $a3 input]
    $port setMultiport true

    $scheduler setValid false


    set allowDisconnectedGraphs \
	[java::cast ptolemy.data.expr.Parameter \
	     [$director getAttribute allowDisconnectedGraphs]]
    $allowDisconnectedGraphs setExpression "true"


    set sched1 {}
    catch { _initialize $toplevel
    set sched1 [_getSchedule $scheduler]} s1
    list $sched1 $s1
} {{{Consumer2 Consumer1}} {{Consumer2 Consumer1}}}

test SDFScheduler-8.7.2 {test with lots of disconected actors} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    for {set i 0} { $i < 120} {incr i} {
	set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer-$i]
    }
    $scheduler setValid false

    set sched1 {}
    catch { _initialize $toplevel
    set sched1 [_getSchedule $scheduler]} s1
    list $sched1 $s1

} {{} {ptolemy.actor.sched.NotSchedulableException: SDF scheduler found disconnected actors! Usually, disconnected actors in an SDF model indicates an error.  If this is not an error, try setting the SDFDirector parameter allowDisconnectedGraphs to true.
Unreached Actors:
.Toplevel.Consumer-1 .Toplevel.Consumer-2 .Toplevel.Consumer-3 .Toplevel.Consumer-4 .Toplevel.Consumer-5 .Toplevel.Consumer-6 .Toplevel.Consumer-7 .Toplevel.Consumer-8 .Toplevel.Consumer-9 .Toplevel.Consumer-10 .Toplevel.Consumer-11 .Toplevel.Consumer-12 .Toplevel.Consumer-13 .Toplevel.Consumer-14 .Toplevel.Consumer-15 .Toplevel.Consumer-16 .Toplevel.Consumer-17 .Toplevel.Consumer-18 .Toplevel.Consumer-19 .Toplevel.Consumer-20 .Toplevel.Consumer-21 .Toplevel.Consumer-22 .Toplevel.Consumer-23 .Toplevel.Consumer-24 .Toplevel.Consumer-25 .Toplevel.Consumer-26 .Toplevel.Consumer-27 .Toplevel.Consumer-28 .Toplevel.Consumer-29 .Toplevel.Consumer-30 .Toplevel.Consumer-31 .Toplevel.Consumer-32 .Toplevel.Consumer-33 .Toplevel.Consumer-34 .Toplevel.Consumer-35 .Toplevel.Consumer-36 .Toplevel.Consumer-37 .Toplevel.Consumer-38 .Toplevel.Consumer-39 .Toplevel.Consumer-40 .Toplevel.Consumer-41 .Toplevel.Consumer-42 .Toplevel.Consumer-43 .Toplevel.Consumer-44 .Toplevel.Consumer-45 .Toplevel.Consumer-46 .Toplevel.Consumer-47 .Toplevel.Consumer-48 .Toplevel.Consumer-49 .Toplevel.Consumer-50 .Toplevel.Consumer-51 .Toplevel.Consumer-52 .Toplevel.Consumer-53 .Toplevel.Consumer-54 .Toplevel.Consumer-55 .Toplevel.Consumer-56 .Toplevel.Consumer-57 .Toplevel.Consumer-58 .Toplevel.Consumer-59 .Toplevel.Consumer-60 .Toplevel.Consumer-61 .Toplevel.Consumer-62 .Toplevel.Consumer-63 .Toplevel.Consumer-64 .Toplevel.Consumer-65 .Toplevel.Consumer-66 .Toplevel.Consumer-67 .Toplevel.Consumer-68 .Toplevel.Consumer-69 .Toplevel.Consumer-70 .Toplevel.Consumer-71 .Toplevel.Consumer-72 .Toplevel.Consumer-73 .Toplevel.Consumer-74 .Toplevel.Consumer-75 .Toplevel.Consumer-76 .Toplevel.Consumer-77 .Toplevel.Consumer-78 .Toplevel.Consumer-79 .Toplevel.Consumer-80 .Toplevel.Consumer-81 .Toplevel.Consumer-82 .Toplevel.Consumer-83 .Toplevel.Consumer-84 .Toplevel.Consumer-85 .Toplevel.Consumer-86 .Toplevel.Consumer-87 .Toplevel.Consumer-88 .Toplevel.Consumer-89 .Toplevel.Consumer-90 .Toplevel.Consumer-91 .Toplevel.Consumer-92 .Toplevel.Consumer-93 .Toplevel.Consumer-94 .Toplevel.Consumer-95 .Toplevel.Consumer-96 .Toplevel.Consumer-97 .Toplevel.Consumer-98 .Toplevel.Consumer-99 .Toplevel.Consumer-100 ...
Reached Actors:
.Toplevel.Consumer-0 }}


test SDFScheduler-8.11 {output Multiport, Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer2]
    set port [java::field $a1 output]
    $port setMultiport true

    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] [java::field $a2 input] R1]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] [java::field $a3 input] R2]] setWidth 1

    $scheduler setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    list $sched1
} {{{Ramp Consumer2 Consumer1}}}

test SDFScheduler-8.12 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a1 output] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenProductionRate [java::field $a1 output] 1
    list $sched1
} {{{Ramp Consumer2 Consumer2 Consumer1 Consumer1}}}

test SDFScheduler-8.13 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a2 input] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1
} {{{Ramp Ramp Consumer2 Consumer2 Consumer1}}}

test SDFScheduler-8.14 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a1 output] 2
    setTokenConsumptionRate [java::field $a2 input] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenProductionRate [java::field $a1 output] 1
    setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1
} {{{Ramp Consumer2 Consumer2 Consumer1}}}

test SDFScheduler-8.15 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a1 output] 2
    setTokenConsumptionRate [java::field $a2 input] 2
    setTokenConsumptionRate [java::field $a3 input] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenProductionRate [java::field $a1 output] 1
    setTokenConsumptionRate [java::field $a2 input] 1
    setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1
} {{{Ramp Consumer2 Consumer1}}}

test SDFScheduler-8.16 {output Multiport with no connections} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set port [java::field $a3 output]
    $port setMultiport true

    $scheduler setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    list $sched1
} {Ramp}


######################################################################
####
#
# Tests 9.* test multiport, multirate scheduling with hierarchy
test SDFScheduler-9.1 {Input Multirate and Hierarchy Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.actor.TypedIOPort $c1 p1]
    $p1 setInput 1
    $p1 setMultiport true
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $c1 d5]
    $c1 setDirector $d5
    set s5 [$d5 getScheduler]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $c1 Consumer]
    set port [java::field $a3 input]
    $port setMultiport true

    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] $p1 R1]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a2 output] $p1 R2]] setWidth 1
    $c1 connect $p1 [java::field $a3 input] R3
    set r3 [$c1 getRelation R3]
    [java::cast ptolemy.actor.IORelation $r3] setWidth 2

    $scheduler setValid false
    $s5 setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    list $sched1 $sched2
} {{{Ramp2 Ramp1 Cont}} Consumer}

test SDFScheduler-9.2 {Input Multiport, Multirate, and Hierarchy Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a3 input] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1 $sched2
} {{{Ramp2 Ramp2 Ramp1 Ramp1 Cont}} Consumer}

test SDFScheduler-9.3 {Input Multiport, Multirate, and Hierarchy Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a2 output] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenProductionRate [java::field $a2 output] 1
    list $sched1 $sched2
} {{{Ramp2 Ramp1 Ramp1 Cont Cont}} Consumer}

test SDFScheduler-9.4 {Input Multiport, Multirate, and Hierarchy Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a3 input] 2
    setTokenProductionRate [java::field $a2 output] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenConsumptionRate [java::field $a3 input] 1
    setTokenProductionRate [java::field $a2 output] 1
    list $sched1 $sched2
} {{{Ramp2 Ramp1 Ramp1 Cont}} Consumer}

test SDFScheduler-9.5 {Input Multiport, Multirate, and Hierarchy Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a3 input] 2
    setTokenProductionRate [java::field $a2 output] 2
    setTokenProductionRate [java::field $a1 output] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenConsumptionRate [java::field $a3 input] 1
    setTokenProductionRate [java::field $a2 output] 1
    setTokenProductionRate [java::field $a1 output] 1
    list $sched1 $sched2
} {{{Ramp2 Ramp1 Cont}} Consumer}

test SDFScheduler-9.11 {Output Multirate and Hierarchy Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set Consumer1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    set Consumer2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer2]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.actor.TypedIOPort $c1 p1]
    $p1 setOutput 1
    $p1 setMultiport true
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $c1 d5]
    $c1 setDirector $d5
    set s5 [$d5 getScheduler]
    set Ramp [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $c1 Ramp]
    set port [java::field $Ramp output]
    $port setMultiport true

    $c1 connect [java::field $Ramp output] $p1 R1
    set r1 [$c1 getRelation R1]
    [java::cast ptolemy.actor.IORelation $r1] setWidth 2
    [java::cast ptolemy.actor.IORelation [$toplevel connect $p1 [java::field $Consumer1 input] R2]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect $p1 [java::field $Consumer2 input] R3]] setWidth 1

    $scheduler setValid false
    $s5 setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    list $sched1 $sched2
} {{{Cont Consumer2 Consumer1}} Ramp}

test SDFScheduler-9.12 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $Ramp output] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenProductionRate [java::field $Ramp output] 1
    list $sched1 $sched2
} {{{Cont Consumer2 Consumer2 Consumer1 Consumer1}} Ramp}

test SDFScheduler-9.13 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $Consumer1 input] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenConsumptionRate [java::field $Consumer1 input] 1
    list $sched1 $sched2
} {{{Cont Cont Consumer2 Consumer2 Consumer1}} Ramp}

test SDFScheduler-9.14 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $Ramp output] 2
    setTokenConsumptionRate [java::field $Consumer1 input] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenProductionRate [java::field $Ramp output] 1
    setTokenConsumptionRate [java::field $Consumer1 input] 1
    list $sched1 $sched2
} {{{Cont Consumer2 Consumer2 Consumer1}} Ramp}

test SDFScheduler-9.15 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $Ramp output] 2
    setTokenConsumptionRate [java::field $Consumer1 input] 2
    setTokenConsumptionRate [java::field $Consumer2 input] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenProductionRate [java::field $Ramp output] 1
    setTokenConsumptionRate [java::field $Consumer1 input] 1
    setTokenConsumptionRate [java::field $Consumer2 input] 1
    list $sched1 $sched2
} {{{Cont Consumer2 Consumer1}} Ramp}

######################################################################
####
#
# Tests 10.* test multiport scheduling without hierarchy.
test SDFScheduler-10.1 {input Broadcast Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]

    set r1 [$toplevel connect [java::field $a1 output] [java::field $a3 input] R1]
    [java::cast ptolemy.actor.IORelation $r1] setWidth 1
    [java::field $a2 output] link $r1

    $scheduler setValid false
    catch {[$scheduler getSchedule]} e1
    list $e1
} {{ptolemy.actor.sched.NotSchedulableException: Output ports drive the same relation. This is not legal in SDF.
  in .Toplevel.Ramp1.output and .Toplevel.Ramp2.output}}

test SDFScheduler-10.11 {output Broadcast Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer2]

    set r1 [$toplevel connect [java::field $a1 output] [java::field $a2 input] R1]
    [java::cast ptolemy.actor.IORelation $r1] setWidth 1
    [java::field $a3 input] link $r1

    $scheduler setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    list $sched1
} {{{Ramp Consumer2 Consumer1}}}

test SDFScheduler-10.12 {output Broadcast Multirate Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a1 output] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenProductionRate [java::field $a1 output] 1
    list $sched1
} {{{Ramp Consumer2 Consumer2 Consumer1 Consumer1}}}

test SDFScheduler-10.13 {output Broadcast Multirate Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a2 input] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1
} {{{Ramp Ramp Consumer2 Consumer2 Consumer1}}}

test SDFScheduler-10.14 {output Broadcast Multirate Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a1 output] 2
    setTokenConsumptionRate [java::field $a2 input] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenProductionRate [java::field $a1 output] 1
    setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1
} {{{Ramp Consumer2 Consumer2 Consumer1}}}

test SDFScheduler-10.15 {output Broadcast Multirate Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a1 output] 2
    setTokenConsumptionRate [java::field $a2 input] 2
    setTokenConsumptionRate [java::field $a3 input] 2

    $scheduler setValid false
    $s5 setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    setTokenProductionRate [java::field $a1 output] 1
    setTokenConsumptionRate [java::field $a2 input] 1
    setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1
} {{{Ramp Consumer2 Consumer1}}}


######################################################################
####
#
# Tests 11.* test multirate scheduling with transparent hierarchy
test SDFScheduler-11.1 {Multirate and transparent hierarchy Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.actor.TypedIOPort $c1 p1]
    $p1 setInput 1
    set p2 [java::new ptolemy.actor.TypedIOPort $c1 p2]
    $p2 setOutput 1
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $c1 Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] $p1 R1]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$c1 connect $p1 [java::field $a2 input] R2]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$c1 connect [java::field $a2 output] $p2 R3]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect $p2 [java::field $a3 input] R4]] setWidth 1

    $scheduler setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    list $sched1
} {{{Ramp Delay Consumer}}}

######################################################################
####
#
test SDFScheduler-11.2 {Multirate and transparent hierarchy Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a1 output] 2

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenProductionRate [java::field $a1 output] 1
    list $sched1
} {{{Ramp Delay Delay Consumer Consumer}}}

######################################################################
####
#
test SDFScheduler-11.3 {Multirate and transparent hierarchy Scheduling tests} {
    # uses previous setup.

    setTokenProductionRate [java::field $a2 output] 2

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenProductionRate [java::field $a2 output] 1
    list $sched1
} {{{Ramp Delay Consumer Consumer}}}

######################################################################
####
#
test SDFScheduler-11.4 {Multirate and transparent hierarchy Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a2 input] 2

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1
} {{{Ramp Ramp Delay Consumer}}}

######################################################################
####
#
test SDFScheduler-11.5 {Multirate and transparent hierarchy Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a3 input] 2

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1
} {{{Ramp Ramp Delay Delay Consumer}}}

######################################################################
####
#
# Tests 12.* test multiport, multirate scheduling with transparent hierarchy
test SDFScheduler-12.1 {Input Multirate and transparent hierarchy Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.actor.TypedIOPort $c1 p1]
    $p1 setInput 1
    $p1 setMultiport true
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $c1 Consumer]
    set port [java::field $a3 input]
    $port setMultiport true

    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] $p1 R1]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a2 output] $p1 R2]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$c1 connect $p1 [java::field $a3 input] R3]] setWidth 1

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    list $sched1
} {{{Ramp2 Ramp1 Consumer}}}

test SDFScheduler-12.2 {Input Multiport, Multirate, and transparent hierarchy Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a3 input] 2

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1
} {{{Ramp2 Ramp2 Ramp1 Ramp1 Consumer}}}

test SDFScheduler-12.3 {Input Multiport, Multirate, and transparent hierarchy Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a2 output] 2

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenProductionRate [java::field $a2 output] 1
    list $sched1
} {{{Ramp2 Ramp1 Ramp1 Consumer Consumer}}}

test SDFScheduler-12.4 {Input Multiport, Multirate, and transparent hierarchy Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a3 input] 2
    setTokenProductionRate [java::field $a2 output] 2

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenConsumptionRate [java::field $a3 input] 1
    setTokenProductionRate [java::field $a2 output] 1
    list $sched1
} {{{Ramp2 Ramp1 Ramp1 Consumer}}}

test SDFScheduler-12.5 {Input Multiport, Multirate, and transparent hierarch Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a3 input] 2
    setTokenProductionRate [java::field $a2 output] 2
    setTokenProductionRate [java::field $a1 output] 2

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenConsumptionRate [java::field $a3 input] 1
    setTokenProductionRate [java::field $a2 output] 1
    setTokenProductionRate [java::field $a1 output] 1
    list $sched1
} {{{Ramp2 Ramp1 Consumer}}}

test SDFScheduler-12.11 {Output Multirate and hierarch Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer2]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.actor.TypedIOPort $c1 p1]
    $p1 setOutput 1
    $p1 setMultiport true
    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $c1 Ramp]
    set port [java::field $a1 output]
    $port setMultiport true

    $c1 connect [java::field $a1 output] $p1 R1
    set r1 [$c1 getRelation R1]
    [java::cast ptolemy.actor.IORelation $r1] setWidth 2
    [java::cast ptolemy.actor.IORelation [$toplevel connect $p1 [java::field $a2 input] R2]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect $p1 [java::field $a3 input] R3]] setWidth 1

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    list $sched1
} {{{Ramp Consumer2 Consumer1}}}

test SDFScheduler-12.12 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a1 output] 2

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenProductionRate [java::field $a1 output] 1
    list $sched1
} {{{Ramp Consumer2 Consumer2 Consumer1 Consumer1}}}

test SDFScheduler-12.13 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenConsumptionRate [java::field $a2 input] 2

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1
} {{{Ramp Ramp Consumer2 Consumer2 Consumer1}}}

test SDFScheduler-12.14 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a1 output] 2
    setTokenConsumptionRate [java::field $a2 input] 2

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenProductionRate [java::field $a1 output] 1
    setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1
} {{{Ramp Consumer2 Consumer2 Consumer1}}}

test SDFScheduler-12.15 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    setTokenProductionRate [java::field $a1 output] 2
    setTokenConsumptionRate [java::field $a2 input] 2
    setTokenConsumptionRate [java::field $a3 input] 2

    $scheduler setValid false
     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    setTokenProductionRate [java::field $a1 output] 1
    setTokenConsumptionRate [java::field $a2 input] 1
    setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1
} {{{Ramp Consumer2 Consumer1}}}

######################################################################
####
#
# Tests 13.* test error cases.
test SDFScheduler-13.1 {connected graph, disconnected relation} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    set port [java::field $a3 input]
    $port setMultiport true

    set r1 [java::new ptolemy.actor.TypedIORelation $toplevel R1]
    $r1 setWidth 1
    [java::field $a3 input] link $r1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a2 output] [java::field $a3 input] R2]] setWidth 1

    $scheduler setValid false

    #set debuglistener [java::new ptolemy.kernel.util.StreamListener]
    #$scheduler addDebugListener $debuglistener

    set err1 ""
    set sched1 ""
    catch { _initialize $toplevel
    set sched1 [_getSchedule $scheduler]} err1
    list $sched1 $err1
} {{} {ptolemy.actor.sched.NotSchedulableException: Actors remain that cannot be scheduled!

Note that there are many reasons why a graph cannot be scheduled:
* SDF Graphs with feedback loops should have an actor with a delay in the loop, such as VariableDelay.* The SDF director has an "allowDisconnectedGraphs"parameter, which, when true, permits disconnected SDF graphs.
* The token consumption rate and production rates might be mismatched.  Usually, actors produce one token or consume one token on a port.  To produce or consume multiple tokens per firing, add a "tokenConsumptionRate" or "tokenConsumptionRate" parameter to the appropriate port.
For details, see the SDF chapter in Volume Three of the Ptolemy II design doc at http://ptolemy.eecs.berkeley.edu/ptolemyII/designdoc.htm
Unscheduled actors:
.Toplevel.Consumer 
Scheduled actors:
.Toplevel.Ramp2 }}

test SDFScheduler-13.2 {Output External port connected } {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.actor.TypedIOPort $c1 p1]
    $p1 setInput 1
    $p1 setMultiport true
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $c1 d5]
    $c1 setDirector $d5
    set s5 [$d5 getScheduler]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $c1 Consumer]
    set port [java::field $a3 input]
    $port setMultiport true

    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] $p1 R1]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a2 output] $p1 R2]] setWidth 1
    $c1 connect $p1 [java::field $a3 input] R3
    set r3 [$c1 getRelation R3]
    [java::cast ptolemy.actor.IORelation $r3] setWidth 2

    $scheduler setValid false
    $s5 setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    list $sched1 $sched2
} {{{Ramp2 Ramp1 Cont}} Consumer}

test SDFScheduler-13.3 {_debugging code coverage} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.actor.TypedIOPort $c1 p1]
    $p1 setInput 1
    set p2 [java::new ptolemy.actor.TypedIOPort $c1 p2]
    $p2 setOutput 1
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $c1 d5]
    $c1 setDirector $d5
    set s5 [$d5 getScheduler]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $c1 Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] $p1 R1]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$c1 connect $p1 [java::field $a2 input] R2]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$c1 connect [java::field $a2 output] $p2 R3]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect $p2 [java::field $a3 input] R4]] setWidth 1

    $scheduler setValid false
    $s5 setValid false

#    set debuglistener [java::new ptolemy.kernel.util.StreamListener]
#    $scheduler addDebugListener $debuglistener
#    $director addDebugListener $debuglistener

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    set sched2 [_getSchedule $s5]
    list $sched1 $sched2
} {{{Ramp Cont Consumer}} Delay}

test SDFScheduler-13.4 {Error message for transparent hierarchy multiport disconnected} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp1]
 
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.actor.TypedIOPort $c1 p1]
    $p1 setInput 1
    $p1 setMultiport true
 
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $c1 Consumer]
    set a3input [java::field $a3 input]
    set a4 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $c1 Consumer2]
    set a4input [java::field $a4 input]

    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] $p1 R1]] setWidth 1

    [java::cast ptolemy.actor.IORelation [$c1 connect $p1 $a3input R3]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$c1 connect $p1 $a4input R4]] setWidth 1

    $scheduler setValid false
  
    catch {
       _initialize $toplevel
	set sched1 [_getSchedule $scheduler]
    } err

    list $sched1 $err
} {{{Ramp Cont Consumer}} {ptolemy.actor.sched.NotSchedulableException: Actors remain that cannot be scheduled!

Note that there are many reasons why a graph cannot be scheduled:
* SDF Graphs with feedback loops should have an actor with a delay in the loop, such as VariableDelay.* The SDF director has an "allowDisconnectedGraphs"parameter, which, when true, permits disconnected SDF graphs.
* The token consumption rate and production rates might be mismatched.  Usually, actors produce one token or consume one token on a port.  To produce or consume multiple tokens per firing, add a "tokenConsumptionRate" or "tokenConsumptionRate" parameter to the appropriate port.
For details, see the SDF chapter in Volume Three of the Ptolemy II design doc at http://ptolemy.eecs.berkeley.edu/ptolemyII/designdoc.htm
Unscheduled actors:
.Toplevel.Cont.Consumer2 
Scheduled actors:
.Toplevel.Ramp1 .Toplevel.Cont.Consumer }}

test SDFScheduler-13.5 {Error message for opaque hierarchy multiport disconnected} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp1]
 
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set director2 [java::new ptolemy.domains.sdf.kernel.SDFDirector $c1 Director2]
    set scheduler2 [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director2 getScheduler]]
    set p1 [java::new ptolemy.actor.TypedIOPort $c1 p1]
    $p1 setInput 1
    $p1 setMultiport true
 
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $c1 Consumer]
    set a3input [java::field $a3 input]
    set a4 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $c1 Consumer2]
    set a4input [java::field $a4 input]

    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] $p1 R1]] setWidth 1

    [java::cast ptolemy.actor.IORelation [$c1 connect $p1 $a3input R3]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$c1 connect $p1 $a4input R4]] setWidth 1

    $scheduler setValid false
  
    set sched1 ""
    set err ""
 #   catch {
       _initialize $toplevel
	set sched1 [_getSchedule $scheduler]
	set sched2 [_getSchedule $scheduler2]
  #  } err
   
    list $sched1 $sched2 $err
} {{{Ramp1 Cont}} {{Consumer2 Consumer}} {}}

######################################################################
####
#
# Tests 14.* test 0-rate ports
# Test the case where a zero-rate input port is connected to
# a chain of three actors connected in sequence. None of
# these three actors should fire.
test SDFScheduler-14.1 {Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay1]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay2]
    set a4 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay3]
    set a5 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay4]
    set a6 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay5]
    set a7 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a1 output] [java::field $a2 input] R1]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a2 output] [java::field $a3 input] R2]] setWidth 1
    set r3 [$toplevel connect [java::field $a3 output] [java::field $a4 input] R3]
    [java::cast ptolemy.actor.IORelation $r3] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a4 output] [java::field $a5 input] R4]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a5 output] [java::field $a6 input] R5]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $a6 output] [java::field $a7 input] R6]] setWidth 1
    
    setTokenProductionRate [java::field $a4 output] 0


    $scheduler setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    list $sched1
} {{{Ramp Delay1 Delay2 Delay3}}}

# Test zero-rate ports.
# Test the case where a zero-rate output port is connected to
# a chain of three sequentially connected actors. None of
# these three actors should fire.
test SDFScheduler-14.2 {Multirate Scheduling tests} {

    setTokenConsumptionRate [java::field $a4 input] 0
    setTokenProductionRate [java::field $a4 output] 1

    $scheduler setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    list $sched1
} {{{Delay3 Delay4 Delay5 Consumer}}}

# Test zero-rate ports.
# Test the case where a zero-rate input port is connected to
# a chain of three actors connected in sequence
# and where a zero-rate output port is connected to
# a chain of three sequentially connected actors.
# Only the actor with the zero-rate ports should fire.
test SDFScheduler-14.3 {Multirate Scheduling tests} {

    setTokenConsumptionRate [java::field $a4 input] 0
    setTokenProductionRate [java::field $a4 output] 0

    $scheduler setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    list $sched1
} {Delay3}

# Test zero-rate ports.
# Test the case where an zero-rate input port is connected to more
# than one actor. None of the connected actors should fire.
test SDFScheduler-14.4 {Multirate Scheduling tests} {

    setTokenConsumptionRate [java::field $a4 input] 0
    setTokenProductionRate [java::field $a4 output] 1
    [java::field $a4 input] setMultiport true
    set b1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    [java::cast ptolemy.actor.IORelation [$toplevel connect [java::field $b1 output] [java::field $a4 input] R7]] setWidth 1

    $scheduler setValid false

     _initialize $toplevel
    set sched1 [_getSchedule $scheduler]
    [java::field $a4 input] setMultiport false
    list $sched1
} {{{Delay3 Delay4 Delay5 Consumer}}}

