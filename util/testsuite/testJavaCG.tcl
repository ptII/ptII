# Proc that Runs ptolemy.cg Java codegen
#
# @Author: Bert Rodiers
#
# @Version: $Id$
#
# @Copyright (c) 2009-2010 The Regents of the University of California.
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


# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

proc testJavaCG {model} {
    testJavaCGInline $model true
    testJavaCGInline $model false
}

proc testJavaCGInline {model inline} {
    global PTII	
    set relativeFilename \
	    [java::call ptolemy.util.StringUtilities substituteFilePrefix \
	    $PTII $model {$PTII}]
    puts "------------------ JavaCG \$PTII/bin/ptcg -language java -inline $inline $relativeFilename"
    test "Auto" "Automatic JavaCG \$PTII/bin/ptcg -language java -inline $inline $relativeFilename" {
	# Remove files from ~/cg so as to force building
	foreach classFile [glob -nocomplain [java::call System getProperty "user.home"]/cg/*.class] { file delete -force $classFile}
	foreach javaFile [glob -nocomplain [java::call System getProperty "user.home"]/cg/*.java] { file delete -force $javaFile}
	foreach mkFile [glob -nocomplain [java::call System getProperty "user.home"]/cg/*.mk] { file delete -force $mkFile}
	set parser [java::new ptolemy.moml.MoMLParser]
	$parser reset
	$parser purgeAllModelRecords

	set args [java::new {String[]} 5 \
		  [list "-language" "java" "-inline" $inline $model]]

	set timeout 500000
	puts "testJavaCG.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
	set watchDog [java::new util.testsuite.WatchDog $timeout]

	set returnValue 0
	if [catch {set returnValue \
		       [java::call ptolemy.cg.kernel.generic.GenericCodeGenerator \
			    generateCode $args]} errMsg] {
	    $watchDog cancel
	    error "$errMsg\n[jdkStackTrace]"
	} else {
	    $watchDog cancel
	}
	list $returnValue
    } {0}
}
