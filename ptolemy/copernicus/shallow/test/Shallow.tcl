# Tests for the MoMLCompiler class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2005 The Regents of the University of California.
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

# Ptolemy II bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs sootCodeGeneration] == "" } then { 
    source [file join $PTII util testsuite codegen.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# Generate code for all the xml files in a directory.
proc autoShallowCG {autoDirectory} {
    global PTII
    # We attempt to gather speed stats
    set builtinPercentageSum 0
    set execPercentageSum 0
    set numberOfModels 0
    foreach file [glob $autoDirectory/*.xml] {
	puts "---- testing $file"
	#set time [java::new Long [java::call System currentTimeMillis]]
	test "Auto" "Automatic test in file $file" {
	    set elapsedTime [time {set results [sootCodeGeneration $PTII $file]}]]
	    puts "soot took [expr {[lindex $elapsedTime 0] / 1000000.0}] seconds for $file"
	    puts "$results"
	    #incr builtinPercentageSum [lindex $results 7]
	    #incr execPercentageSum [lindex $results 11]
	    #incr numberOfModels
	    list {}
	} {{}}
	java::call System gc
	#puts "[java::call ptolemy.actor.Manager timeAndMemory [$time longValue]]"
    }
    if {$numberOfModels == 0} {
	set summary "Number of Models was 0 !"
    } else {
	set summary "Percentages for $autoDirectory: $numberOfModels models \
		Builtin Interp/Shallow: \ 
	[expr {$builtinPercentageSum / $numberOfModels}] % \
		Exec Interp/Shallow: \
		[expr {$execPercentageSum / $numberOfModels}] $"
    }
    puts $summary
    return $summary
}

######################################################################
####
#

#test Shallow-1.1 {Compile and run the Orthocomm test} {
#    set result [sootShallowCodeGeneration \
#  	    [file join $relativePathToPTII ptolemy actor lib test auto \
#	    RecordUpdater.xml]]
#ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom]
#    lrange $result 0 9
#} {2 4 6 8 10 12 14 16 18 20}

# Do a SDF and a DE test just be sure things are working
test Shallow-1.2 {Compile and run the SDF IIR test} {
    global PTII
    set result [sootCodeGeneration $PTII \
	    [file join $relativePathToPTII ptolemy actor lib test auto \
	    MultiplexorDE.xml]]
    puts $result
    list {}
} {{}}

test Shallow-1.2 {Compile and run the SDF IIR test} {
    global PTII
    set result [sootCodeGeneration $PTII \
	    [file join $relativePathToPTII ptolemy actor lib test auto \
	    IIR.xml]]
    puts $result
    list {}
} {{}}


test Shallow-1.3 {Compile and run the DE Counter test} {
    global PTII
    set result [sootCodeGeneration $PTII \
	    [file join $relativePathToPTII ptolemy actor lib test auto \
	    Counter.xml]]
    puts $result
    list {}
} {{}}
 
test Shallow-1.3 {Compile and run the MathFunction test, which tends to hang} {
    global PTII
    set result [sootCodeGeneration $PTII \
	    [file join $relativePathToPTII ptolemy actor lib test auto \
	    MathFunction.xml]]
    puts $result
    list {}
} {{}}


# Now try to generate code for all the tests in the auto directories.
autoShallowCG [file join $relativePathToPTII ptolemy actor lib comm test auto]
autoShallowCG [file join $relativePathToPTII ptolemy actor lib conversions test auto]
autoShallowCG [file join $relativePathToPTII ptolemy actor lib hoc test auto]
autoShallowCG [file join $relativePathToPTII ptolemy actor lib security test auto]
autoShallowCG [file join $relativePathToPTII ptolemy actor lib string test auto]
autoShallowCG [file join $relativePathToPTII ptolemy actor lib test auto]
autoShallowCG [file join $relativePathToPTII ptolemy actor lib xslt test auto]
#autoShallowCG [file join $relativePathToPTII ptolemy actor lib javasound test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains ct kernel test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains ct lib test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains ct test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains ddf kernel test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains ddf lib test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains ddf test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains de lib test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains dt kernel test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains fsm kernel test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains fsm test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains giotto kernel test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains giotto test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains hdf kernel test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains pn kernel test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains pn test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains psdf kernel test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains sdf kernel test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains sdf lib test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains sdf lib vq test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains sdf test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains sr kernel test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains sr lib test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains tm test auto]
autoShallowCG [file join $relativePathToPTII ptolemy domains wireless test auto]

# Print out stats
#doneTests

