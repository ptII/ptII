# Tests for the InstantiableNamedObj class
#
# @Author: Christopher Hylands, Edward A. Lee
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
#
test InstantiableNamedObj-1.0 {Constructor tests} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.InstantiableNamedObj]
    set e2 [java::new ptolemy.kernel.InstantiableNamedObj "e2"]
    set e3 [java::new ptolemy.kernel.InstantiableNamedObj $w]
    set e4 [java::new ptolemy.kernel.InstantiableNamedObj $w "e4"]
    list [$e1 getName] [$e2 getName] [$e3 getName] [$e4 getName]
} {{} e2 {} e4}

######################################################################
####
#
test InstantiableNamedObj-1.1 {exportMoML} {
    set e1 [java::new ptolemy.kernel.InstantiableNamedObj "e1"]
    $e1 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="e1" class="ptolemy.kernel.InstantiableNamedObj">
</entity>
}

######################################################################
####
#
test InstantiableNamedObj-1.2 {instantiate} {
    set e1 [java::new ptolemy.kernel.InstantiableNamedObj "e1"]
    catch {set e2 [$e1 instantiate [java::null] {e2}]} msg
    string range $msg 0 52
} {ptolemy.kernel.util.IllegalActionException: Cannot in}

######################################################################
####
#
test InstantiableNamedObj-1.3 {instantiate} {
    $e1 setClassDefinition true
    set e2 [java::cast ptolemy.kernel.InstantiableNamedObj [$e1 instantiate [java::null] {e2}]]
    $e2 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="e2" class=".e1">
</entity>
}

######################################################################
####
#
test InstantiableNamedObj-1.4 {getPrototypeList} {
    [$e2 getPrototypeList] size
} {1}


