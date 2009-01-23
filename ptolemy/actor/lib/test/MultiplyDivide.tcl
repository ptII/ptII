# Test MultiplyDivide.
#
# @Author: Edward A. Lee
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

######################################################################
####
#
test MultiplyDivide-1.1 {test constructor and clone} {
    set e0 [sdfModel 2]
    set muldiv [java::new ptolemy.actor.lib.MultiplyDivide $e0 muldiv]
    set newObject [java::cast ptolemy.actor.lib.MultiplyDivide \
		       [$muldiv clone [$e0 workspace]]]
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test MultiplyDivide in an SDF model
#
test MultiplyDivide-2.1 {test multiply alone} {
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression {1.0}
    $step setExpression {1.0}
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set value [getParameter $const value]
    $value setExpression {-2.0}
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set multiply [java::field $muldiv multiply]
    set r1 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
       $multiply]
    set r2 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
       [java::field $muldiv multiply]]
    $e0 connect \
       [java::field $muldiv output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {-2.0 -4.0}

test MultiplyDivide-2.2 {test multiply and divide} {
    set divide [java::field $muldiv divide]
    $multiply unlink $r2
    $divide link $r2
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {-0.5 -1.0}

test MultiplyDivide-2.3 {test divide only} {
    $multiply unlink $r1
    $divide link $r1
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {-0.5 -0.25}

######################################################################
#### Test with string type
#
test MultiplyDivide-3.1 {test with run-time type error} {
    $init setExpression {"a"}
    $step setExpression {"b"}
    $divide unlinkAll
    $multiply link $r1
    $multiply link $r2
    catch {[$e0 getManager] execute} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.StringToken '"a"' and ptolemy.data.StringToken '"-2.0"'
  in .top.muldiv
Because:
multiply operation not supported between ptolemy.data.StringToken '"a"' and ptolemy.data.StringToken '"-2.0"'}}

######################################################################
#### Test with run-time type error
#
test MultiplyDivide-3.3 {test with run-time type error: double and a long } {
    $init setExpression {1L}
    $step setExpression {3L}
    $value setExpression {42.0}
    catch {[$e0 getManager] execute} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.LongToken '1L' and ptolemy.data.DoubleToken '42.0' because the types are incomparable.
  in .top.muldiv
Because:
multiplyReverse method not supported between ptolemy.data.LongToken '1L' and ptolemy.data.DoubleToken '42.0' because the types are incomparable.}}
