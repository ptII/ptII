# Tests for the ActorIndex class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006-2007 The Regents of the University of California.
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Load up the test definitions.
if {[string compare test [info procs removeGraphicalClasses]] == 1} then {
    source [file join $PTII util testsuite removeGraphicalClasses.tcl]
}
# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test ActorIndex-1.1 {Run the ActorIndex on some test files } {
    # This test will fail if the network is not available because
    # testModels.txt refers to 
    # $CLASSPATH/ptolemy/moml/demo/Networked/Networked.xml
    # which downloads an actor from http://ptolemy.eecs.berkeley.edu
    file delete -force codeDoc
    file mkdir codeDoc

    set args [java::new {String[]} 3 [list testNamedObjs.txt testModels.txt codeDoc]]
    java::call ptolemy.moml.filter.ActorIndex main $args
    list \
        [file exists codeDoc/ptolemy/actor/lib/gui/SequencePlotterIdx.htm] \
        [file exists codeDoc/ptolemy/actor/lib/MultiplyDivideIdx.htm]
} {1 1}

######################################################################
####
#
test ActorIndex-2.1 {toString} {
    set filter [java::new ptolemy.moml.filter.NamedObjClassesSeen [java::new java.util.HashMap]]
    $filter toString
} {ptolemy.moml.filter.NamedObjClassesSeen: Create a Set of classes that have been parsed thus far. The classes extend NamedObj. This filter does not modify the model. }
