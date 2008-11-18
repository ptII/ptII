# Test CodeGeneratorUtilities
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006 The Regents of the University of California.
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

#####
test CodeGeneratorUtilities-1.1 {openAsFileOrURL} {
    # Increase Code Coverage
    catch { java::call ptolemy.codegen.kernel.CodeGeneratorUtilities \
	openAsFileOrURL "DoesNotExist"} errMsg
    list [string range $errMsg 0 42]
} {{java.io.FileNotFoundException: DoesNotExist}}

#####
test CodeGeneratorUtilities-2.1 {Test out subsitute(String, NamedObj)} {
    set namedObj [java::new ptolemy.kernel.util.NamedObj myNamedObj]
    set p1 [java::new ptolemy.data.expr.Parameter $namedObj \
	testParameter [java::new ptolemy.data.StringToken "myTestParameter"]]
    set p2 [java::new ptolemy.data.expr.Parameter $namedObj \
	anotherTestParameter [java::new ptolemy.data.StringToken "myOtherTestParameter"]]
    java::call ptolemy.codegen.kernel.CodeGeneratorUtilities \
	substitute ptolemy/codegen/kernel/test/substitute.in $namedObj
} {This is a myTestParameter, followed by
myOtherTestParameter.
}

#####
test CodeGeneratorUtilities-2.2 {Test out subsitute(String, NamedObj)} {
    # Uses 2.1 above
    catch { java::call ptolemy.codegen.kernel.CodeGeneratorUtilities \
		substitute NotAFile $namedObj } errMsg
    list $errMsg
} {{java.io.FileNotFoundException: Failed to find 'NotAFile' as a resource}}

#####
test CodeGeneratorUtilities-6.1 {Test out subsitute(inputFileName, substituteMap, outputFileName)} {
    file delete -force substitute.out
    set r1 [file exists substitute.out]
    set substituteMap [java::new java.util.HashMap]
    $substituteMap put "@testParameter@" "myTestParam"
    $substituteMap put "@anotherTestParameter@" "myOtherTestParam"
    java::call ptolemy.codegen.kernel.CodeGeneratorUtilities \
	substitute ptolemy/codegen/kernel/test/substitute.in $substituteMap \
	substitute.out
    set fin [open substitute.out r ]
    set r2 [read $fin]
    close $fin
    list $r1 $r2
} {0 {This is a myTestParam, followed by
myOtherTestParam.
}}
