# Tests for the ComponentPort class
#
# @Author: Edward A. Lee, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2010 The Regents of the University of California.
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
#
test ComponentPort-2.1 {Construct Ports} {
    set e1 [java::new ptolemy.kernel.ComponentEntity]
    set p1 [java::new ptolemy.kernel.ComponentPort]
    set p2 [java::new ptolemy.kernel.ComponentPort $e1 P2]

    set w [java::new ptolemy.kernel.util.Workspace]
    set p3 [java::new ptolemy.kernel.ComponentPort $w]
    set w2 [java::new ptolemy.kernel.util.Workspace "workspace2"]
    set p4 [java::new ptolemy.kernel.ComponentPort $w2]
    set p5 [java::new ptolemy.kernel.ComponentPort [java::null]]

    list [$p1 getFullName] [$p2 getFullName] \
	    [$p3 getFullName] [$p4 getFullName] [$p5 getFullName]
} {. ..P2 . . .}

######################################################################
####
#
test ComponentPort-2.2 {Construct Ports} {
    set e1 [java::new ptolemy.kernel.ComponentEntity]
    $e1 setName E1
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {.E1.P1 .E1.P2}

######################################################################
####
#
test ComponentPort-2.3 {Check getInsidePorts on opaque ports} {
    set e1 [java::new ptolemy.kernel.ComponentEntity]
    $e1 setName E1
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 P1]
    enumToFullNames [$p1 insidePorts]
} {}

######################################################################
####
#
test ComponentPort-2.3.1 {Check deepInsidePorts on opaque ports} {
    set e1 [java::new ptolemy.kernel.ComponentEntity]
    $e1 setName E1
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 P1]
    enumToFullNames [$p1 deepInsidePorts]
} {}

######################################################################
####
#
test ComponentPort-3.1 {Make transparent port} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 E2]
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set a1 [java::new ptolemy.kernel.ComponentRelation $e1 A1]
    $p2 link $a1
    $p1 link $a1
    enumToFullNames [$a1 linkedPorts]
} {.E1.E2.P2 .E1.P1}

######################################################################
####
#
test ComponentPort-3.2 {Make multiple aliases and test deepInsidePorts} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 E2]
    set p1 [java::new ptolemy.kernel.ComponentPort $e2 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set p3 [java::new ptolemy.kernel.ComponentPort $e1 P3]
    set a1 [java::new ptolemy.kernel.ComponentRelation $e1 A1]
    $p1 link $a1
    $p2 link $a1
    $p3 link $a1
    list [enumToFullNames [$p3 deepInsidePorts]] \
            [enumToFullNames [$p3 insidePorts]] \
	    [$p2 isDeeplyConnected $p1] \
	    [$p1 isDeeplyConnected $p2] \
	    [$p1 isDeeplyConnected $p3] \
	    [$p3 isDeeplyConnected $p2] 
} {{.E1.E2.P1 .E1.E2.P2} {.E1.E2.P1 .E1.E2.P2} 1 1 0 0} 

######################################################################
####
#
test ComponentPort-3.3 {test connectedPorts} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 E2]
    set p1 [java::new ptolemy.kernel.ComponentPort $e2 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set p3 [java::new ptolemy.kernel.ComponentPort $e1 P3]
    set a1 [java::new ptolemy.kernel.ComponentRelation $e1 A1]
    $p1 link $a1
    $p2 link $a1
    $p3 link $a1
    list [enumToNames [$p3 connectedPorts]] \
            [enumToNames [$p2 connectedPorts]] \
            [enumToNames [$p1 connectedPorts]]
} {{} {P1 P3} {P2 P3}}

######################################################################
####
#
test ComponentPort-3.3b {test insidePorts} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 E2]
    set p1 [java::new ptolemy.kernel.ComponentPort $e2 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set p3 [java::new ptolemy.kernel.ComponentPort $e1 P3]
    set a1 [java::new ptolemy.kernel.ComponentRelation $e1 A1]
    $p1 link $a1
    $p2 link $a1
    $p3 link $a1
    list [enumToNames [$p3 insidePorts]] \
            [enumToNames [$p2 insidePorts]] \
            [enumToNames [$p1 insidePorts]]
} {{P1 P2} {} {}}

######################################################################
####
#
test ComponentPort-3.3c {test connectedPorts with null links} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 E2]
    set p1 [java::new ptolemy.kernel.ComponentPort $e2 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set p3 [java::new ptolemy.kernel.ComponentPort $e1 P3]
    set a1 [java::new ptolemy.kernel.ComponentRelation $e1 A1]
    $p1 insertLink 3 $a1
    $p2 insertLink 3 $a1
    $p3 insertLink 3 $a1
    list [enumToNames [$p3 connectedPorts]] \
            [enumToNames [$p2 connectedPorts]] \
            [enumToNames [$p1 connectedPorts]]
} {{} {P1 P3} {P2 P3}}

######################################################################
####
#
test ComponentPort-3.3d {test insidePorts with null links} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 E2]
    set p1 [java::new ptolemy.kernel.ComponentPort $e2 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set p3 [java::new ptolemy.kernel.ComponentPort $e1 P3]
    set a1 [java::new ptolemy.kernel.ComponentRelation $e1 A1]
    $p1 insertLink 3 $a1
    $p2 insertLink 3 $a1
    $p3 insertLink 3 $a1
    list [enumToNames [$p3 insidePorts]] \
            [enumToNames [$p2 insidePorts]] \
            [enumToNames [$p1 insidePorts]]
} {{P1 P2} {} {}}

######################################################################
####
#
test ComponentPort-3.3e {test insidePorts with null links} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 E2]
    set p1 [java::new ptolemy.kernel.ComponentPort $e2 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set p3 [java::new ptolemy.kernel.ComponentPort $e1 P3]
    set a1 [java::new ptolemy.kernel.ComponentRelation $e1 A1]
    $p1 insertLink 3 $a1
    $p1 insertLink 3 [java::null]
    $e1 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="E1" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.0-devel">
    </property>
    <port name="P3" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="E2" class="ptolemy.kernel.ComponentEntity">
        <port name="P1" class="ptolemy.kernel.ComponentPort">
        </port>
        <port name="P2" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="A1" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="E2.P1" insertAt="4" relation="A1"/>
</entity>
}

######################################################################
####
#
test ComponentPort-3.3f {test insidePorts with null inside links} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 E2]
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 P1]
    set a1 [java::new ptolemy.kernel.ComponentRelation $e1 A1]
    $p1 insertLink 3 $a1
    $p1 insertInsideLink 3 [java::null]
    $e1 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="E1" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.0-devel">
    </property>
    <port name="P1" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="E2" class="ptolemy.kernel.ComponentEntity">
    </entity>
    <relation name="A1" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="P1" insertAt="4" relation="A1"/>
</entity>
}

######################################################################
####
#
# NOTE: Level-crossing links are now allowed.
test ComponentPort-3.4 {Level-crossing link error} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1 E2]
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set a1 [java::new ptolemy.kernel.ComponentRelation $e2 A1]
    catch {$p1 link $a1} msg
    list $msg
} {{}}

######################################################################
####
#
test ComponentPort-3.5 {Level-crossing link} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1 E2]
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set a1 [java::new ptolemy.kernel.ComponentRelation $e2 A1]
    $p1 liberalLink $a1
    enumToNames [$a1 linkedPorts]
} {P1}

######################################################################
####
#
test ComponentPort-3.6 {Construct transparent ports, then modify them} {
    set e0 [java::new ptolemy.kernel.CompositeEntity]
    $e0 setName E0
    set e2 [java::new ptolemy.kernel.ComponentEntity $e0 E2]
    set e4 [java::new ptolemy.kernel.ComponentEntity $e0 E4]

    set p1 [java::new ptolemy.kernel.ComponentPort $e0 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set p3 [java::new ptolemy.kernel.ComponentPort $e0 P3]
    set p4 [java::new ptolemy.kernel.ComponentPort $e4 P4]

    set a1 [java::new ptolemy.kernel.ComponentRelation $e0 A1]
    set a2 [java::new ptolemy.kernel.ComponentRelation $e0 A2]

    $p2 link $a1
    $p4 link $a2
    $p1 link $a1
    $p3 link $a2

    set result {}
    foreach obj [list $p1 $p2 $p3 $p4] {
        lappend result [enumToNames [$obj insidePorts]]
    }
    foreach obj [list $a1 $a2] {
        lappend result [enumToNames [$obj linkedPorts]]
    }

    # Now the modification
    $p4 unlink $a2
    $p2 link $a2
    $p3 link $a1

    foreach obj [list $p1 $p2 $p3 $p4] {
        lappend result [enumToNames [$obj insidePorts]]
    }
    foreach obj [list $a1 $a2] {
        lappend result [enumToNames [$obj linkedPorts]]
    }
    list $result
} {{P2 {} P4 {} {P2 P1} {P4 P3} {P2 P3} {} {P2 P2 P1} {} {P2 P1 P3} {P3 P2}}}

######################################################################
####
#
test ComponentPort-4.1 {Cross Level Link} {
    # Create objects
    set e0 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setName E0
    set e1 [java::new ptolemy.kernel.CompositeEntity $e0 "E1"]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "E2"]
    set e3 [java::new ptolemy.kernel.ComponentEntity $e0 "E3"]
    set p1 [java::new ptolemy.kernel.ComponentPort $e2 "P1"]
    set p2 [java::new ptolemy.kernel.ComponentPort $e3 "P2"]
    set r1 [java::new ptolemy.kernel.ComponentRelation $e0 "R1"]

    # Connect
    $p1 liberalLink $r1
    $p2 link $r1

    list [enumToNames [$p1 linkedRelations]] \
            [enumToNames [$p2 linkedRelations]]
} {R1 R1}

######################################################################
####
# Example from figure of design document.
test ComponentPort-5.1 {Transparent entity} {
    # Create objects
    set e0 [java::new ptolemy.kernel.CompositeEntity]
    $e0 setName E0
    set e1 [java::new ptolemy.kernel.ComponentEntity $e0 "E1"]
    set e2 [java::new ptolemy.kernel.CompositeEntity $e0 "E2"]
    set e3 [java::new ptolemy.kernel.ComponentEntity $e2 "E3"]
    set e4 [java::new ptolemy.kernel.ComponentEntity $e0 "E4"]
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 "P1"]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 "P2"]
    set p3 [java::new ptolemy.kernel.ComponentPort $e3 "P3"]
    set p4 [java::new ptolemy.kernel.ComponentPort $e2 "P4"]
    set p5 [java::new ptolemy.kernel.ComponentPort $e4 "P5"]
    set p6 [java::new ptolemy.kernel.ComponentPort $e0 "P6"]
    set r1 [java::new ptolemy.kernel.ComponentRelation $e0 "R1"]
    set r2 [java::new ptolemy.kernel.ComponentRelation $e2 "R2"]
    set r3 [java::new ptolemy.kernel.ComponentRelation $e0 "R3"]
    set r4 [java::new ptolemy.kernel.ComponentRelation $e0 "R4"]

    # Connect
    $p1 link $r1
    $p2 link $r1
    $p2 link $r2
    $p3 link $r2
    $p4 link $r2
    $p4 link $r3
    $p5 link $r3
    $p6 link $r4

    list [enumToNames [$p1 deepConnectedPorts]] \
            [enumToNames [$p2 deepConnectedPorts]] \
            [enumToNames [$p3 deepConnectedPorts]] \
            [enumToNames [$p4 deepConnectedPorts]] \
            [enumToNames [$p5 deepConnectedPorts]]
} {{P3 P5} P1 {P1 P5} P5 {P1 P3}}

# NOTE: Uses topology built in 5.1
test ComponentPort-5.2 {numInsideLinks} {
    list [$p1 numInsideLinks] \
            [$p2 numInsideLinks] \
            [$p3 numInsideLinks] \
            [$p4 numInsideLinks] \
            [$p5 numInsideLinks]
} {0 1 0 1 0}

# NOTE: Uses topology built in 5.1
test ComponentPort-5.3 {test description} {
    $p2 description 31
} {ptolemy.kernel.ComponentPort {.E0.E2.P2} links {
    {ptolemy.kernel.ComponentRelation {.E0.R1}}
} insidelinks {
    {ptolemy.kernel.ComponentRelation {.E0.E2.R2}}
}}

# NOTE: Uses topology built in 5.1
test ComponentPort-5.4 {test description} {
    $p2 description 3
} {ptolemy.kernel.ComponentPort {.E0.E2.P2}}

# NOTE: Uses topology built in 5.1
test ComponentPort-5.5 {test clone} {
    set pn [java::cast ptolemy.kernel.ComponentPort [$p2 clone]]
    $pn description 31
} {ptolemy.kernel.ComponentPort {.P2} links {
} insidelinks {
}}

# NOTE: Uses topology built in 5.1
test ComponentPort-6.0 {unlinkAll} {
    $p2 unlinkAll
    list [enumToNames [$p1 deepConnectedPorts]] \
            [enumToNames [$p2 deepConnectedPorts]] \
            [enumToNames [$p3 deepConnectedPorts]] \
            [enumToNames [$p4 deepConnectedPorts]] \
            [enumToNames [$p5 deepConnectedPorts]]
} {{} {} P5 P5 P3}

# NOTE: Uses topology built in 6.1
test ComponentPort-6.0 {unlinkAll} {
    $p6 unlinkAllInside
    enumToNames [$p6 deepConnectedPorts]
} {}

test ComponentPort-7.0 {transparent ports in a loop} {
    set w [java::new ptolemy.kernel.CompositeEntity]
    set a [java::new ptolemy.kernel.CompositeEntity $w A]
    set p1 [java::cast ptolemy.kernel.ComponentPort [$a newPort P1]]
    set p2 [java::cast ptolemy.kernel.ComponentPort [$a newPort P2]]
    set rin [java::new ptolemy.kernel.ComponentRelation $a Rinside]
    set rout [java::new ptolemy.kernel.ComponentRelation $w Routside]
    $p1 link $rin
    $p1 link $rout
    $p2 link $rin
    $p2 link $rout
    catch {$p1 deepConnectedPorts} msg
    list $msg
} {{ptolemy.kernel.util.InvalidStateException: Loop in topology!
  in .<Unnamed Object>.A.P2, .<Unnamed Object>.A.P1}}

test ComponentPort-7.1 { deepInsidePorts in a loop} {
    # Use configuration in 7.0
    catch {$p1 deepInsidePorts} msg
    list $msg
} {{ptolemy.kernel.util.InvalidStateException: Loop in topology!
  in .<Unnamed Object>.A.P2, .<Unnamed Object>.A.P1}}

test ComponentPort-7.2 {transaprent ports in another loop} {
    set w [java::new ptolemy.kernel.CompositeEntity]
    set a [java::new ptolemy.kernel.CompositeEntity $w A]
    set b [java::new ptolemy.kernel.ComponentEntity $w B]
    set p1 [java::cast ptolemy.kernel.ComponentPort [$a newPort P1]]
    set p2 [java::cast ptolemy.kernel.ComponentPort [$a newPort P2]]
    set p3 [java::cast ptolemy.kernel.ComponentPort [$b newPort P3]]
    set rin [java::new ptolemy.kernel.ComponentRelation $a Rinside]
    set rout [java::new ptolemy.kernel.ComponentRelation $w Routside]
    set rb_a [java::new ptolemy.kernel.ComponentRelation $w B_to_A]
    $p1 link $rin
    $p1 link $rout
    $p2 link $rin
    $p2 link $rout
    $p1 link $rb_a
    $p3 link $rb_a
    catch {$p3 deepConnectedPorts} msg
    list $msg
} {{ptolemy.kernel.util.InvalidStateException: Loop in topology!
  in .<Unnamed Object>.A.P2, .<Unnamed Object>.A.P1, .<Unnamed Object>.B.P3}}

test ComponentPort-7.3 { deepInsidePorts in another loop} {
    # Use configuration in 7.2
    list [enumToNames [$p3 deepInsidePorts]]
} {{}}


######################################################################
####
# Example from figure of design document.
test ComponentPort-8.1 {test _deepConnectedPorts - similar to 5.1 above} {
    # _deepConnectedPorts is a deprecated protected method in ComponentPort
    # that needs testing anyway
    # Create objects
    set e0 [java::new ptolemy.kernel.CompositeEntity]
    $e0 setName E0
    set e1 [java::new ptolemy.kernel.ComponentEntity $e0 "E1"]
    set e2 [java::new ptolemy.kernel.CompositeEntity $e0 "E2"]
    set e3 [java::new ptolemy.kernel.ComponentEntity $e2 "E3"]
    set e4 [java::new ptolemy.kernel.ComponentEntity $e0 "E4"]
    set p1 [java::new ptolemy.kernel.test.TestComponentPort $e1 "P1"]
    set p2 [java::new ptolemy.kernel.test.TestComponentPort $e2 "P2"]
    set p3 [java::new ptolemy.kernel.test.TestComponentPort $e3 "P3"]
    set p4 [java::new ptolemy.kernel.test.TestComponentPort $e2 "P4"]
    set p5 [java::new ptolemy.kernel.test.TestComponentPort $e4 "P5"]
    set r1 [java::new ptolemy.kernel.ComponentRelation $e0 "R1"]
    set r2 [java::new ptolemy.kernel.ComponentRelation $e2 "R2"]
    set r3 [java::new ptolemy.kernel.ComponentRelation $e0 "R3"]

    # Connect
    $p1 link $r1
    $p2 link $r1
    $p2 link $r2
    $p3 link $r2
    $p4 link $r2
    $p4 link $r3
    $p5 link $r3

    list [enumToNames [$p1 testDeepConnectedPorts [java::null] ]] \
            [enumToNames [$p2 testDeepConnectedPorts [java::null] ]] \
            [enumToNames [$p3 testDeepConnectedPorts [java::null] ]] \
            [enumToNames [$p4 testDeepConnectedPorts [java::null] ]] \
            [enumToNames [$p5 testDeepConnectedPorts [java::null] ]]
} {{P3 P5} P1 {P1 P5} P5 {P1 P3}}


######################################################################
####
#
test ComponentPort-9.1 {Check _deepInsidePorts on opaque ports} {
    # _deepInsidePorts is a deprecated protected method in ComponentPort
    # that needs testing anyway
    set e1 [java::new ptolemy.kernel.ComponentEntity]
    $e1 setName E1
    set p1 [java::new ptolemy.kernel.test.TestComponentPort $e1 P1]
    enumToFullNames [$p1 testDeepInsidePorts [java::null] ]
} {}

######################################################################
####
#
test Port-10.1 {Test unlinkAllInside} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 P1]
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1 E2]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set e3 [java::new ptolemy.kernel.ComponentEntity $e2 E3]
    set p3 [java::new ptolemy.kernel.ComponentPort $e3 P3]

    set r1 [java::new ptolemy.kernel.ComponentRelation $e1 R1]
    set r2 [java::new ptolemy.kernel.ComponentRelation $e2 R2]
    $p1 link $r1
    $p2 link $r1
    $p2 link $r2
    $p3 link $r2

    set result1 [list [_testPortLinkedRelations $p1 $p2 $p3] [_testPortInsideRelations $p1 $p2 $p3]]
    $p2 unlinkAll
    set result2 [list [_testPortLinkedRelations $p1 $p2 $p3] [_testPortInsideRelations $p1 $p2 $p3]]
    $p2 unlinkAllInside
    set result3 [list [_testPortLinkedRelations $p1 $p2 $p3] [_testPortInsideRelations $p1 $p2 $p3]]
    list $result1 $result2 $result3
} {{{{} R1 R2} {R1 R2 {}}} {{{} {} R2} {R1 R2 {}}} {{{} {} R2} {R1 {} {}}}}

test Port-10.2 {Test removePort} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 P1]
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1 E2]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set e3 [java::new ptolemy.kernel.ComponentEntity $e2 E3]
    set p3 [java::new ptolemy.kernel.ComponentPort $e3 P3]

    set r1 [java::new ptolemy.kernel.ComponentRelation $e1 R1]
    set r2 [java::new ptolemy.kernel.ComponentRelation $e2 R2]
    $p1 link $r1
    $p2 link $r1
    $p2 link $r2
    $p3 link $r2

    set result1 [list [_testPortLinkedRelations $p1 $p2 $p3] [_testPortInsideRelations $p1 $p2 $p3]]
    $p2 setContainer [java::null]
    set result3 [list [_testPortLinkedRelations $p1 $p2 $p3] [_testPortInsideRelations $p1 $p2 $p3]]
    list $result1 $result3
} {{{{} R1 R2} {R1 R2 {}}} {{{} {} R2} {R1 {} {}}}}

test ComponentPort-11.1 {test deepConnectedPortList} {
    set e0 [java::new ptolemy.kernel.CompositeEntity]
    $e0 setName E0
    set e1 [java::new ptolemy.kernel.ComponentEntity $e0 E1]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e0 E2]
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set r [java::new ptolemy.kernel.ComponentRelation $e0 R0]
    $p1 link $r
    $p2 link $r
    listToFullNames [$p2 deepConnectedPortList]
} {.E0.E1.P1}

test ComponentPort-11.2 {test deepConnectedPortList} {
    # NOTE: expands on the above.
    set e3 [java::new ptolemy.kernel.ComponentEntity $e0 E3]
    set p3 [java::new ptolemy.kernel.ComponentPort $e3 P3]
    $p3 link $r
    listToFullNames [$p2 deepConnectedPortList]
} {.E0.E1.P1 .E0.E3.P3}

test ComponentPort-11.3 {test deepConnectedPortList} {
    # NOTE: expands on the above.
    set e4 [java::new ptolemy.kernel.CompositeEntity $e0 E4]
    set p4 [java::new ptolemy.kernel.ComponentPort $e4 P4]
    $p4 link $r
    listToFullNames [$p2 deepConnectedPortList]
} {.E0.E1.P1 .E0.E3.P3}

test ComponentPort-11.4 {test deepConnectedPortList} {
    # NOTE: expands on the above.
    set e5 [java::new ptolemy.kernel.ComponentEntity $e4 E5]
    set p5 [java::new ptolemy.kernel.ComponentPort $e5 P5]
    set r5 [java::new ptolemy.kernel.ComponentRelation $e4 R5]
    $p5 link $r5
    $p4 link $r5
    listToFullNames [$p2 deepConnectedPortList]
} {.E0.E1.P1 .E0.E3.P3 .E0.E4.E5.P5}

######################################################################
####
#
test ComponentPort-12.1 {Create a composite with three relations} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.kernel.CompositeEntity $w]
    set e1 [java::new ptolemy.kernel.ComponentEntity $toplevel E1]
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 "P1"]
    set r1 [java::new ptolemy.kernel.ComponentRelation $toplevel "R1"]
    set r2 [java::new ptolemy.kernel.ComponentRelation $toplevel "R2"]
    set r3 [java::new ptolemy.kernel.ComponentRelation $toplevel "R3"]
    $p1 link $r1
    $p1 link $r2
    $p1 link $r3
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.0.beta">
    </property>
    <entity name="E1" class="ptolemy.kernel.ComponentEntity">
        <port name="P1" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="R1" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <relation name="R2" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <relation name="R3" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="E1.P1" relation="R1"/>
    <link port="E1.P1" relation="R2"/>
    <link port="E1.P1" relation="R3"/>
</entity>
}

test ComponentPort-12.2 {Test unlink and insertLink} {
    # Uses 12.1 above
    # This illustrates a bug discovered by Hauke Fuhrmannn,
    # where calling unlink and insertLink resulted in
    # erroneous behavior.  The problem was in CrossRefList.
    for {set i 0} {$i < 3} {incr i} {
	set relationName "R[expr {$i + 1}]"
	set relation [$toplevel getRelation $relationName]
	#puts "Unlink $i"
	$p1 {unlink int} $i
	#puts [$e1 description]
	#puts "Link: [$relation getName] at $i"
	$p1 insertLink $i $relation
	#puts [$e1 description]
    }
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.0.beta">
    </property>
    <entity name="E1" class="ptolemy.kernel.ComponentEntity">
        <port name="P1" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="R1" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <relation name="R2" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <relation name="R3" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="E1.P1" relation="R1"/>
    <link port="E1.P1" relation="R2"/>
    <link port="E1.P1" relation="R3"/>
</entity>
}
