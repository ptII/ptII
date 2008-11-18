# Tests for the DEEvent class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2008 The Regents of the University of California.
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

set comparator [java::new ptolemy.actor.util.test.DoubleCQComparator]

######################################################################
#### Test the constructor
#
test DEEvent-1.1 {Test the Actor constructor with nulls} {
    set null [java::null]
    set event1 [java::new {ptolemy.domains.de.kernel.DEEvent \
			       ptolemy.actor.Actor ptolemy.actor.util.Time \
			       int int} \
		   $null $null 1 2]
    list [$event1 toString]
} {{DEEvent(time = null, microstep = 1, depth = 2, dest = null) -- A PURE EVENT.}}

test DEEvent-1.2 {Test the Actor constructor with an actor} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    set actor1 [java::new ptolemy.actor.AtomicActor $e0 actor1]
    set director [java::new ptolemy.actor.Director]
    $e0 setDirector $director
    set time1 [java::new \
		   {ptolemy.actor.util.Time ptolemy.actor.Director double} \
		   $director 5.0]
    set event2 [java::new {ptolemy.domains.de.kernel.DEEvent \
			       ptolemy.actor.Actor ptolemy.actor.util.Time \
			       int int} \
			       $actor1 $time1 3 4]
    list [$event2 toString]    
} {{DEEvent(time = 5.0, microstep = 3, depth = 4, dest = ..actor1) -- A PURE EVENT.}}

test DEEvent-1.3 {Test the accessors} {
    # Use 1.2 above
    list \
	[[$event2 actor] getFullName] \
	[$event2 depth] \
	[java::isnull [$event2 ioPort]] \
	[$event2 microstep] \
	[[$event2 timeStamp] toString]
} {..actor1 4 1 3 5.0}

test DEEvent-2.1 {Test the IOPort constructor} {
    set null [java::null]
    set event2_1 [java::new {ptolemy.domains.de.kernel.DEEvent \
			       ptolemy.actor.IOPort ptolemy.actor.util.Time \
			       int int} \
		   $null $null 1 2]
    list [$event2_1 toString]
} {{DEEvent(time = null, microstep = 1, depth = 2, dest = null) -- A PURE EVENT.}}

test DEEvent-2.2 {Test the IOPort constructor with an ioport} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    set actor1 [java::new ptolemy.actor.AtomicActor $e0 actor1]
    set port1 [java::new ptolemy.actor.IOPort $actor1 port1]
    set director [java::new ptolemy.actor.Director]
    $e0 setDirector $director
    set time2 [java::new \
		   {ptolemy.actor.util.Time ptolemy.actor.Director double} \
		   $director 7.0]
    set event2_2 [java::new \
		      {ptolemy.domains.de.kernel.DEEvent \
			   ptolemy.actor.IOPort ptolemy.actor.util.Time \
			   int int} \
		      $port1 $time2 8 9]
    list [$event2_2 toString]    
} {{DEEvent(time = 7.0, microstep = 8, depth = 9, dest = ..actor1.port1).}}

test DEEvent-2.3 {Test the accessors} {
    # Use 2.2 above
    list \
	[[$event2_2 actor] getFullName] \
	[$event2_2 depth] \
	[[$event2_2 ioPort] toString] \
	[$event2_2 microstep] \
	[[$event2_2 timeStamp] toString]
} {..actor1 9 {ptolemy.actor.IOPort {..actor1.port1}} 8 7.0}

test DEEvent-3.1 {compareTo} {
    set e3_1 [java::new ptolemy.actor.CompositeActor]
    set actor3_1 [java::new ptolemy.actor.AtomicActor $e3_1 actor3_1]
    set port3_1 [java::new ptolemy.actor.IOPort $actor3_1 port3_1]
    set director3_1 [java::new ptolemy.actor.Director]
    $e3_1 setDirector $director3_1
    set time3_1 [java::new \
		   {ptolemy.actor.util.Time ptolemy.actor.Director double} \
		   $director3_1 11.0]
    set event3_1 [java::new \
		      {ptolemy.domains.de.kernel.DEEvent \
			   ptolemy.actor.IOPort ptolemy.actor.util.Time \
			   int int} \
		      $port3_1 $time3_1 12 13]

    set time3_1b [java::new \
		   {ptolemy.actor.util.Time ptolemy.actor.Director double} \
		   $director3_1 12.0]
    set event3_1b [java::new \
		      {ptolemy.domains.de.kernel.DEEvent \
			   ptolemy.actor.IOPort ptolemy.actor.util.Time \
			   int int} \
		      $port3_1 $time3_1b 14 15]

    list \
	[$event3_1 compareTo $event3_1] \
	[$event3_1b compareTo $event3_1] \
	[$event3_1 compareTo $event3_1b] \
	[$event3_1b compareTo $event3_1b]
} {0 1 -1 0} 

test DEEvent-3.2 {compareTo} {
    # Uses 3.1 above
    # Same time as event3_1b, different microstep
    set event3_1c [java::new \
		      {ptolemy.domains.de.kernel.DEEvent \
			   ptolemy.actor.IOPort ptolemy.actor.util.Time \
			   int int} \
		      $port3_1 $time3_1b 16 15]
    list \
	[$event3_1c compareTo $event3_1c] \
	[$event3_1 compareTo $event3_1c] \
	[$event3_1c compareTo $event3_1] \
	[$event3_1c compareTo $event3_1b] \
	[$event3_1b compareTo $event3_1c]
} {0 -1 1 1 -1}

test DEEvent-3.3 {compareTo} {
    # Uses 3.1 above
    # Same time as event3_1b, different dept
    set event3_1d [java::new \
		      {ptolemy.domains.de.kernel.DEEvent \
			   ptolemy.actor.IOPort ptolemy.actor.util.Time \
			   int int} \
		      $port3_1 $time3_1b 14 17]
    list \
	[$event3_1d compareTo $event3_1d] \
	[$event3_1 compareTo $event3_1d] \
	[$event3_1d compareTo $event3_1] \
	[$event3_1b compareTo $event3_1d] \
	[$event3_1d compareTo $event3_1b] \
	[$event3_1c compareTo $event3_1d] \
	[$event3_1d compareTo $event3_1c]
} {0 -1 1 -1 1 1 -1}

test DEEvent-4.1 {equals} {
    # Uses 3.1 above
    # This event is the same as event3_1
    set event4_1 [java::new \
		      {ptolemy.domains.de.kernel.DEEvent \
			   ptolemy.actor.IOPort ptolemy.actor.util.Time \
			   int int} \
		      $port3_1 $time3_1 12 13]
    list \
	[$event3_1 equals $event3_1] \
	[$event3_1 equals $event4_1] \
	[$event4_1 equals $event3_1] \
	[$event3_1 compareTo $event4_1] \
	[$event4_1 compareTo $event3_1]
} {1 1 1 0 0}

test DEEvent-4.2 {equals, with a different actor} {
    # Uses 3.1 above
    set actor4_2 [java::new ptolemy.actor.AtomicActor $e3_1 actor4_2]
    set port4_2 [java::new ptolemy.actor.IOPort $actor4_2 port4_2]
    # This event is the same as event3_1, except w/ a different actor
    set event4_2 [java::new \
		      {ptolemy.domains.de.kernel.DEEvent \
			   ptolemy.actor.IOPort ptolemy.actor.util.Time \
			   int int} \
		      $port4_2 $time3_1 12 13]
    list \
	[$event4_2 equals $event3_1] \
	[$event3_1 equals $event4_2] \
	[$event4_2 equals $event4_1] \
	[$event4_1 equals $event4_2] \
	[$event4_2 equals $event4_2] \
	[$event4_2 compareTo $event4_1] \
	[$event4_1 compareTo $event4_2] \
	[$event4_2 compareTo $event3_1] \
	[$event3_1 compareTo $event4_1] \
	[$event4_2 compareTo $event4_2]
} {0 0 0 0 1 0 0 0 0 0}

test DEEvent-4.3 {equals on a null} {
    # Uses 3.1 above
    # The Javadoc for java.lang.Comparable says: "Note that null
    # is not an instance of any class, and e.compareTo(null)
    # should throw a NullPointerException even though
    # e.equals(null) returns false."
    list [$event4_2 equals [java::null]]
} {0}

test DEEvent-4.5 {hashCode} {
    list \
	[$event3_1 equals $event4_1] \
	[expr {[$event3_1 hashCode] == [$event4_1 hashCode]}]
} {1 1}

test DEEvent-5.1 {hasTheSameTagAs} {
    # Uses 4.1 above

    # Similar to event 3_1 and 4_1, but different microstep
    set event5_1a [java::new \
		      {ptolemy.domains.de.kernel.DEEvent \
			   ptolemy.actor.IOPort ptolemy.actor.util.Time \
			   int int} \
		      $port3_1 $time3_1 1000 13]

    set event5_1b [java::new \
		      {ptolemy.domains.de.kernel.DEEvent \
			   ptolemy.actor.IOPort ptolemy.actor.util.Time \
			   int int} \
		      $port3_1 $time3_1 12 2000 ]

    list \
	[$event3_1 hasTheSameTagAs $event4_1] \
	[$event3_1 hasTheSameTagAs $event4_1] \
	[$event3_1 hasTheSameTagAs $event5_1a] \
	[$event3_1 hasTheSameTagAs $event5_1b] \
	[$event5_1a hasTheSameTagAs $event3_1] \
	[$event5_1b hasTheSameTagAs $event3_1]
} {1 1 0 1 0 1}

test DEEvent-6.1 {hasTheSameTagAndDepthAs} {

    list \
	[$event3_1 hasTheSameTagAndDepthAs $event4_1] \
	[$event3_1 hasTheSameTagAndDepthAs $event4_1] \
	[$event3_1 hasTheSameTagAndDepthAs $event5_1a] \
	[$event3_1 hasTheSameTagAndDepthAs $event5_1b] \
	[$event5_1a hasTheSameTagAndDepthAs $event3_1] \
	[$event5_1b hasTheSameTagAndDepthAs $event3_1]
} {1 1 0 0 0 0}
