# Run "auto" tests
#
# @Author: Bert Rodiers
#
# @Version: $Id$
#
# @Copyright (c) 2009 The Regents of the University of California.
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

# Get the value of ptolemy.ptII.isRunningNightlyBuild and save it,
# then reset the property to the empty string.
# If we are running as the nightly build, we usually want to
# throw an exception if the trainingMode parameter is set to true.
# However, while testing the Test actor itself, we want to 
# be able to set the trainingMode parameter to true

set oldIsRunningNightlyBuild \
    [java::call ptolemy.util.StringUtilities getProperty \
     "ptolemy.ptII.isRunningNightlyBuild"]
java::call System setProperty "ptolemy.ptII.isRunningNightlyBuild" ""


proc test_cg {model} {
    set codeGeneratorPath "ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator"
    set workspace [java::new ptolemy.kernel.util.Workspace "workspace"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser reset
    $parser purgeAllModelRecords
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set model1_0 \
	 [java::cast ptolemy.actor.TypedCompositeActor [$parser parseFile $model]]
    set manager [java::new ptolemy.actor.Manager $workspace "manager"]
    $model1_0 setManager $manager
    #$manager execute

    # Get the corrrectValues parameter, which should be {}
    set codeGenerator [java::cast ptolemy.cg.kernel.generic.GenericCodeGenerator [[$model1_0 attributeList [java::call java.lang.Class forName $codeGeneratorPath]] get 0]]

    list [$codeGenerator generateCode]
}

######################################################################
#### 
#
test Auto-1.0 {Testing Repeat} {
	test_cg Repeat.xml
} {0}

test Auto-2.0 {Testing Display} {
	test_cg Display.xml
} {0}


# Reset the isRunningNightlyBuild property
java::call System setProperty "ptolemy.ptII.isRunningNightlyBuild" \
    $oldIsRunningNightlyBuild 

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
    
