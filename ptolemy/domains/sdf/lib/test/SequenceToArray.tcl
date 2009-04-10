# Test SequenceToArray.
#
# @Author: Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 1997-2009 The Regents of the University of California.
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

######################################################################
####
#
test SequenceToArray-1.1 {test constructor and clone} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set s2abase [java::new ptolemy.domains.sdf.lib.SequenceToArray $e0 s2abase]
    set s2aclone [java::cast ptolemy.domains.sdf.lib.SequenceToArray \
		      [$s2abase clone [$e0 workspace]]]
    set s2a [java::cast ptolemy.domains.sdf.lib.SDFTransformer $s2aclone]
    $s2a setName s2a
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test SequenceToArray in an SDF model
#
test SequenceToArray-2.1 {test double array, test prefire} {
    set e0 [sdfModel 3]

    # put in a Ramp
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression {-2.0}
    $step setExpression {1.0}
    set rampOut [java::field [java::cast ptolemy.actor.lib.Source $ramp] \
								output]

    # Use clone of s2a to make sure that is ok.
    $s2a setContainer $e0
    set s2aIn [java::field $s2a input]
    set s2aOut [java::field $s2a output]
    [java::field $s2aclone arrayLength] setExpression {2}

    # put in a Recorder
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set recIn [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    [java::cast ptolemy.actor.IORelation [$e0 connect $rampOut $s2aIn]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$e0 connect $s2aOut $recIn]] setWidth 1

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{{-2.0, -1.0}} {{0.0, 1.0}} {{2.0, 3.0}}}

######################################################################
#### Check types of above model
#
test SequenceToArray-2.3 {check types} {
    list [[$rampOut getType] toString] [[$s2aIn getType] toString] \
	[[$s2aOut getType] toString] [[$recIn getType] toString]
} {double double arrayType(double,2) arrayType(double,2)}

######################################################################
#### Test string array
#
test SequenceToArray-2.4 {test string array} {
    $init setExpression {"A"}
    $step setExpression {"B"}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{{"A", "AB"}} {{"ABB", "ABBB"}} {{"ABBBB", "ABBBBB"}}}

######################################################################
#### Check types of above model
#
test SequenceToArray-2.5 {check types} {
    list [[$rampOut getType] toString] [[$s2aIn getType] toString] \
	[[$s2aOut getType] toString] [[$recIn getType] toString]
} {string string arrayType(string,2) arrayType(string,2)}

######################################################################
#### Test cascading SequenceToArray
#
test SequenceToArray-2.6 {test cascading SequenceToArray} {
    set s2a2 [java::cast ptolemy.domains.sdf.lib.SDFTransformer \
		  [$s2a clone [$e0 workspace]]]
    $s2a2 setName s2a2
    $s2a2 setContainer $e0
    set s2a2In [java::field $s2a2 input]
    set s2a2Out [java::field $s2a2 output]

    # insert the new SequenceToArray before the Recorder
    $s2aOut unlinkAll
    $recIn unlinkAll
    [java::cast ptolemy.actor.IORelation [$e0 connect $s2aOut $s2a2In]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$e0 connect $s2a2Out $recIn]] setWidth 1

    $init setExpression {0}
    $step setExpression {1}

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{{{0, 1}, {2, 3}}} {{{4, 5}, {6, 7}}} {{{8, 9}, {10, 11}}}}

######################################################################
#### Check types of above model
#
test SequenceToArray-2.7 {check types} {
    list [[$rampOut getType] toString] [[$s2aIn getType] toString] \
	[[$s2aOut getType] toString] [[$s2a2In getType] toString] \
	[[$s2a2Out getType] toString] [[$recIn getType] toString]
} {int int arrayType(int,2) arrayType(int,2) arrayType(arrayType(int,2),2) arrayType(arrayType(int,2),2)}

######################################################################
#### Test array of array of string
#
test SequenceToArray-2.8 {test array of array of string} {
    $init setExpression {"C"}
    $step setExpression {"D"}

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{{{"C", "CD"}, {"CDD", "CDDD"}}} {{{"CDDDD", "CDDDDD"}, {"CDDDDDD", "CDDDDDDD"}}} {{{"CDDDDDDDD", "CDDDDDDDDD"}, {"CDDDDDDDDDD", "CDDDDDDDDDDD"}}}}

######################################################################
#### Check types of above model
#
test SequenceToArray-2.9 {check types} {
    list [[$rampOut getType] toString] [[$s2aIn getType] toString] \
	[[$s2aOut getType] toString] [[$s2a2In getType] toString] \
	[[$s2a2Out getType] toString] [[$recIn getType] toString]
} {string string arrayType(string,2) arrayType(string,2) arrayType(arrayType(string,2),2) arrayType(arrayType(string,2),2)}

