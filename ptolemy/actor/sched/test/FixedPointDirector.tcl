# Tests for the FixedPointDirector class
#
# @Author: Christopher Hylands (based on Director.tcl by Edward A. Lee)
#
# @Version: $Id$
#
# @Copyright (c) 1997-2008 The Regents of the University of California.
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

set w [java::new ptolemy.kernel.util.Workspace W]
set manager [java::new ptolemy.actor.Manager $w M]

######################################################################
####
#
test FixedPointDirector-2.1 {Constructor tests} {
    set d1 [java::new ptolemy.actor.sched.FixedPointDirector]
    $d1 setName D1
    set d2 [java::new ptolemy.actor.sched.FixedPointDirector $w]
    $d2 setName D2
    set e0 [java::new ptolemy.actor.CompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.actor.sched.FixedPointDirector $e0 D3]

    # These methods could be abstract, but are not for testing purposes
    # so we call them here
    $d1 fireAtCurrentTime $e0

    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 .D2 .E0.D3}

######################################################################
####
#
test FixedPointDirector-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set d4 [java::cast ptolemy.actor.sched.FixedPointDirector [$d2 clone $w]]
    $d4 setName D4
    list [enumToFullNames [$w directory]]
} {{.M .D2 .E0}}

######################################################################
####
#
test FixedPointDirector-4.1 {Test _makeDirectorOf} {
    # NOTE: Uses the setup above
    $e0 setManager $manager
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {.E0.D3 .D4 {.D2 .E0}}

######################################################################
####
#
test FixedPointDirector-5.1 {Test isFireFunctional} {
    # NOTE: Uses the setup above
    set w5 [java::new ptolemy.kernel.util.Workspace W5]
    set d5 [java::cast ptolemy.actor.sched.FixedPointDirector [$d2 clone $w5]]
    set e5 [java::new ptolemy.actor.CompositeActor $w5]
    $e5 setName E5
    $d5 setContainer $e5
    set e5b [java::new ptolemy.actor.CompositeActor $e5 E5b]
    # d5 is not yet fire functional because it contains only a composite
    set result1	[$d5 isFireFunctional]

    # Add an atomic actor, the top level is now fire functional
    set a5b [java::new ptolemy.actor.test.TestActor $e5b A5b]
    set result2	[$d5 isFireFunctional]

    # Remove an atomic actor, the top level is no longer fire functional
    $a5b setContainer [java::null]
    set result3	[$d5 isFireFunctional]

    set a5 [java::new ptolemy.actor.test.TestActor $e5 A5]
    # d5 is fireFunctional because it contains an AtomicActor  
    list [[$d5 getContainer] getFullName] \
	$result1 $result2 $result3 \
	[listToFullNames [$e5 deepEntityList]] \
	[$d5 isFireFunctional]
} {.E5 0 1 0 .E5.A5 1}

######################################################################
####
#
test FixedPointDirector-5.2 {Test isFireFunctional with no actors} {
    # NOTE: Uses the setup above
    # This returns false because there are no actors
    list [[$d3 getContainer] getFullName] \
	[listToFullNames [$e0 allAtomicEntityList]] \
	[$d3 isFireFunctional]
} {.E0 {} 0}

######################################################################
####
#
test FixedPointDirector-5.3 {Test isFireFunctional with no container} {
    # NOTE: Uses the setup above
    # This returns false because there is no container
    list [java::isnull [$d4 getContainer]] [$d4 isFireFunctional]
} {1 0}
