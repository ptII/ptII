# Tests for the TimeKeeper class
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
set globalIgnoreTime -1
set globalEndTimeReceiver [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue]
#set globalEndTime [java::field $globalEndTimeReceiver INACTIVE]
set globalEndTime -2.0

######################################################################
####
#
test TimeKeeper-2.1 {instantiate objects} {

    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $topLevel "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]

    set time [java::new {ptolemy.actor.util.Time ptolemy.actor.Director} $dir]
    set time1 [$time {add double} 15.0]
    set time2 [$time {add double} 5.0]
    set time3 [$time {add double} 6.0]
    set time4 [$time {add double} 3.0]
    set time5 [$time {add double} $globalIgnoreTime]
    set time6 [$time {add double} $globalEndTime]

    set tok [java::new ptolemy.data.Token]

    list 1
} {1}

######################################################################
####
# Continued from above
test TimeKeeper-3.1 {getNextTime()} {
    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 2]
    $rcvr1 put $tok $time1
    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr2 put $tok $time2
    set rcvr3 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 3]
    $rcvr3 put $tok $time3

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateReceiverList $rcvr1
    $keeper updateReceiverList $rcvr2
    $keeper updateReceiverList $rcvr3

    set newrcvr [$keeper getFirstReceiver]

    list [[$keeper getNextTime] getDoubleValue] [expr {$rcvr2 == $newrcvr} ]

} {5.0 1}

######################################################################
####
# Continued from above
test TimeKeeper-3.2 {getNextTime()} {
    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 20]
    $rcvr1 put $tok $time5
    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr2 put $tok $time2
    set rcvr3 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 3]
    $rcvr3 put $tok $time2

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateReceiverList $rcvr1
    $keeper updateReceiverList $rcvr2
    $keeper updateReceiverList $rcvr3

    set newrcvr [$keeper getFirstReceiver]

    list [[$keeper getNextTime] getDoubleValue] [expr {$rcvr3 == $newrcvr} ]

} {5.0 1}

######################################################################
####
# Continued from above
test TimeKeeper-3.3 {getNextTime()} {
    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 20]
    $rcvr1 put $tok $time5
    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr2 put $tok $time6

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateReceiverList $rcvr1
    $keeper updateReceiverList $rcvr2

    set newrcvr [$keeper getFirstReceiver]

    list [[$keeper getNextTime] getDoubleValue] [expr {$rcvr1 == $newrcvr} ]

} {-1.0 1}

######################################################################
####
# Continued from above
test TimeKeeper-3.4 {getNextTime()} {
    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 2]
    $rcvr1 put $tok $time6
    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 5]
    $rcvr2 put $tok $time6

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateReceiverList $rcvr1
    $keeper updateReceiverList $rcvr2

    set newrcvr [$keeper getFirstReceiver]

    list [[$keeper getNextTime] getDoubleValue] [expr {$rcvr2 == $newrcvr} ]

} {-2.0 1}

######################################################################
####
#
test TimeKeeper-4.1 {Call Methods On Uninitialized TimeKeeper} {

    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $topLevel "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    set val 1
    if { [$keeper getCurrentTime] != 0.0 } {
	set val 0
    }
    if { [[$keeper getNextTime] getDoubleValue] != 0.0 } {
	set val 0
    }
    if { ![java::isnull [$keeper getFirstReceiver]] } {
	set val 0
    }

    list $val;

} {1}

######################################################################
####
#
test TimeKeeper-5.1 {Ignore Tokens} {

    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $topLevel "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]
    set tok [java::new ptolemy.data.Token]

    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 2]
    $rcvr1 put $tok $time

    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 4]
    $rcvr2 put $tok $time5

    set val 0
    if { [$rcvr1 hasToken] == 1 } {
	if { [$rcvr2 hasToken] == 1 } {
	    set val 1
	}
    }

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateReceiverList $rcvr1
    $keeper updateReceiverList $rcvr2

    $keeper removeAllIgnoreTokens

    set newVal 1
    if { [$rcvr2 hasToken] == 0 } {
	set newVal 0
    }

    list $val $newVal

} {1 0}

######################################################################
####
# Continued from above.
test TimeKeeper-5.2 {Ignore Tokens} {

    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 2]
    $rcvr1 put $tok $time

    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 4]
    $rcvr2 put $tok $time5
    $rcvr2 put $tok $time4

    set val 0
    if { [$rcvr1 hasToken] == 1 } {
	if { [$rcvr2 hasToken] == 1 } {
	    set val 1
	}
    }

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateReceiverList $rcvr1
    $keeper updateReceiverList $rcvr2

    $keeper removeAllIgnoreTokens

    set newVal 1
    if { [$rcvr2 hasToken] == 0 } {
	set newVal 0
    }

    set time [[$rcvr2 getReceiverTime] getDoubleValue]

    list $val $time

} {1 3.0}

######################################################################
####
# Continued from above.
test TimeKeeper-5.3 {Ignore Tokens} {

    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 2]
    $rcvr1 put $tok $time5

    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 4]
    $rcvr2 put $tok $time5

    set val 0
    if { [$rcvr1 hasToken] == 1 } {
	if { [$rcvr2 hasToken] == 1 } {
	    set val 1
	}
    }

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateReceiverList $rcvr1
    $keeper updateReceiverList $rcvr2

    $keeper removeAllIgnoreTokens

    set newVal 1
    if { [$rcvr1 hasToken] == 0 } {
	if { [$rcvr1 hasToken] == 0 } {
	    set newVal 0
	}
    }

    set time [[$rcvr2 getReceiverTime] getDoubleValue]

    list $val $newVal

} {1 0}
