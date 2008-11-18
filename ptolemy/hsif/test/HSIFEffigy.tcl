# Tests for the HSIFUtilities class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2003-2005 The Regents of the University of California.
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

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#

if {[string compare removeGraphicalClasses [info procs removeGraphicalClasses]] != 0} \
        then {
    source [file join $PTII util testsuite removeGraphicalClasses.tcl]
} {}

test HSIFEffigy.1.1 {Convert the Thermostat demo using the configuration} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset	
    set inURL [java::call ptolemy.actor.gui.MoMLApplication specToURL \
	"HSIFConfiguration.xml"]
    set toplevel [$parser parse $inURL [$inURL openStream]]
    set config [java::cast ptolemy.actor.gui.Configuration $toplevel]

    set inURL [java::call ptolemy.actor.gui.MoMLApplication specToURL \
	"../demo/Thermostat/Thermostat.xml"]
	
    set key [$inURL toExternalForm]	
    jdkCapture {$config openModel $inURL $inURL $key} hsifOutput
    # The date and time at the start of the file will change, so
    # just check the size and that it contains a certain string	
    list [expr {[string length $hsifOutput] > 20000}] \
	[regexp {Generated by Ptolemy II at } $hsifOutput]
} {1 1}

test HSIFEffigy-1.2 {Parse the results from converting the Thermostat HSIF model} {
    # Uses hsifOutput from above	

    # Strip out everything before <$xml
    set start [string first {xml version} $hsifOutput] 
    if {$start == -1 } {
	error "Did not find {xml version} in $hsifOutput"
    }
    set moml [string range $hsifOutput [expr {$start - 2}] end]

    $parser reset    	
    removeGraphicalClasses $parser
    set toplevel [$parser parse $moml]

    set composite [java::cast ptolemy.actor.CompositeActor $toplevel]
    set director [$composite getDirector]	
    list [$director toString]
} {{ptolemy.domains.ct.kernel.CTMixedSignalDirector {.Thermostat.CT Director}}}

