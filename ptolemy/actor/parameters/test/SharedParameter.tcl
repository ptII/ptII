# Tests for the SharedParameter class
#
# @Author: Christopher Brooks, based on Parameter.tcl by Neil Smyth
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#

######################################################################
####
# 
test SharedParameter-2.0 {Check constructors} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    #set tok [java::new  {ptolemy.data.DoubleToken double} 4.5]
    set ws [java::new ptolemy.kernel.util.Workspace workspace]

    #set param1 [java::new ptolemy.actor.parameters.SharedParameter]
    #set param2 [java::new ptolemy.actor.parameters.SharedParameter $ws]
    set param4 [java::new ptolemy.actor.parameters.SharedParameter $e id1]
    set param3 [java::new ptolemy.actor.parameters.SharedParameter $e id2 [java::null]]
    set param5 [java::new ptolemy.actor.parameters.SharedParameter $e id3 [java::null] "4.5"]
    
    #set name1 [$param1 getFullName]
    #set name2 [$param2 getFullName]    
    set name3 [$param3 getFullName]
    set name4 [$param4 getFullName]
    set name5 [$param5 getFullName]
    set value3 [java::isnull [$param3 getToken]] 
    set value5 [[$param5 getToken] toString]
    #list $name1 $name2 $name3 $name4 $value3 
    list $name3 $name4 $name5 $value3 $value5
} {.entity.id2 .entity.id1 .entity.id3 1 4.5}

#################################
####
# This needs to extended to test type checking
test SharedParameter-3.1 {Check setting the contained Token with another Token} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    #set tok1 [java::new  {ptolemy.data.DoubleToken double} 4.5]

    set param1 [java::new ptolemy.actor.parameters.SharedParameter $e id1 [java::null] "4.5"]
    set name1 [$param1 getFullName]
    set value1 [[$param1 getToken] toString]

    # Now put a new token into the Param
    set tok2 [java::new  {ptolemy.data.DoubleToken double} 7.3]
    $param1 setToken $tok2
    
    set name2 [$param1 getFullName]
    set value2 [[$param1 getToken] toString]

    list $name1 $value1 $name2 $value2
} {.entity.id1 4.5 .entity.id1 7.3}

#################################
####
#
test SharedParameter-3.2 {Check that type changes with new Token type} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    set tok1 [java::new  {ptolemy.data.IntToken int} 11]

    set param1 [java::new ptolemy.actor.parameters.SharedParameter $e id1 [java::null] "11"]
    set name1 [$param1 getFullName]
    set value1 [[$param1 getToken] toString]

    # Now put a new token into the Param
    set tok2 [java::new  {ptolemy.data.DoubleToken double} 7.3]
    set type1 [[$param1 getType] toString]
    $param1 setToken $tok2
    set type2 [[$param1 getType] toString]

    set name2 [$param1 getFullName]
    set value2 [[$param1 getToken] toString]

    list $name1 $value1 $type1 $type2
} {.entity.id1 11 int double}

#################################
####
#
test SharedParameter-3.3 {Check type constraints: ok to put int in a double} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    set tok1 [java::new  {ptolemy.data.DoubleToken double} 4.4]

    set param1 [java::new ptolemy.actor.parameters.SharedParameter $e id1 [java::null] "4.4"]
    set name1 [$param1 getFullName]
    set value1 [[$param1 getToken] toString]

    # Now put a new token of a lower type into the Param
    set tok2 [java::new  {ptolemy.data.IntToken int} 7]
    $param1 setToken $tok2
    
    set name2 [$param1 getFullName]
    set value2 [[$param1 getToken] toString]

    list $name1 $value1 $name2 $value2 
} {.entity.id1 4.4 .entity.id1 7}

test SharedParameter-3.4 {Check setting type constraints (conversion)} {
    set doubleClass [java::field ptolemy.data.type.BaseType DOUBLE]
    $param1 setTypeEquals $doubleClass
    [$param1 getToken] toString
} {7.0}

test SharedParameter-3.5 {Check that we can't convert down} {
    set intClass [java::field ptolemy.data.type.BaseType INT]
    catch {$param1 setTypeEquals $intClass} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: The currently contained token ptolemy.data.DoubleToken(7.0) is not compatible with the desired type int
  in .entity.id1}}

test SharedParameter-3.6 {Check that a new token is converted} {
    set int [java::new ptolemy.data.IntToken 0]
    $param1 setToken $int
    [$param1 getToken] toString
} {0.0}

test SharedParameter-3.7 {Check that a new expression is converted} {
    $param1 setExpression {1}
    [$param1 getToken] toString
} {1.0}

#################################
####
#
test SharedParameter-4.0 {Check setting the contained Token from a String or another Token} {
    set e [java::new {ptolemy.kernel.Entity String} parent]
    set param1 [java::new ptolemy.actor.parameters.SharedParameter $e id1 ]
    $param1 setExpression "1.6 + 8.3"
    set name1 [$param1 getFullName]
    set value1 [[$param1 getToken] toString]

    # Now put a new token into the SharedParameter
    set tok1 [java::new  {ptolemy.data.DoubleToken double} 7.7]
    $param1 setToken $tok1    
    set value2 [[$param1 getToken] toString]

    # Now set the Token contained from a String
    $param1 setExpression "-((true) ? 5.5 : 7)" 
    set value3 [[$param1 getToken] toString]

    # Now put a new token into the Param
    set tok2 [java::new {ptolemy.data.DoubleToken double} 3.3]
    $param1 setToken $tok2    
    set value4 [[$param1 getToken] toString]

    list $name1 $value1 $value2 $value3 $value4
} {.parent.id1 9.9 7.7 -5.5 3.3}

#################################
####
#
test SharedParameter-5.0 {Check reseting the SharedParameter to its original String} {
    set e [java::new {ptolemy.kernel.Entity String} parent]
    set param1 [java::new ptolemy.actor.parameters.SharedParameter $e id1 ]
    $param1 setExpression "1.6 + 8.3"
    set name1 [$param1 getFullName]
    set value1 [[$param1 getToken] toString]

    # Now put a new token into the Param
    set tok1 [java::new  {ptolemy.data.DoubleToken double} 7.7]
    $param1 setToken $tok1    
    set value2 [[$param1 getToken] toString]

    # Now reset the Token 
    $param1 reset
    set value3 [[$param1 getToken] toString]

    # Put a new Token in the SharedParameter from a String
    $param1 setExpression "((true) ? 5.5 : \"string\")" 
    set value4 [[$param1 getToken] toString]
    
    # Reset the Token 
    $param1 reset
    set value5 [[$param1 getToken] toString]

    list $name1 $value1 $value2 $value3 $value4 $value5 
} {.parent.id1 9.9 7.7 9.9 {"5.5"} 9.9}


#################################
####
#
test SharedParameter-5.1 {Check reseting the SharedParameter to its original Token} {
    set e [java::new {ptolemy.kernel.Entity String} parent]
    #set tok1 [java::new {ptolemy.data.DoubleToken double} 9.9]
    set param1 [java::new ptolemy.actor.parameters.SharedParameter $e id1 [java::null] "9.9"]
    set name1 [$param1 getFullName]
    set value1 [[$param1 getToken] toString]

    # Put a new token into the SharedParameter from a String 
    $param1 setExpression "((true) ? 7.7 : \"string\")" 
    set value2 [[$param1 getToken] toString]
    
    # Reset the Token 
    $param1 reset
    set value3 [[$param1 getToken] toString]

    # Put a new Token in the Param from a Token
    set tok1 [java::new  {ptolemy.data.DoubleToken double} 5.5]
    $param1 setToken $tok1    
    set value4 [[$param1 getToken] toString]
    
    # Reset the Token 
    $param1 reset
    set value5 [[$param1 getToken] toString]

    list $name1 $value1 $value2 $value3 $value4 $value5 
} {.parent.id1 9.9 {"7.7"} 9.9 5.5 9.9}

#################################
####
#

test SharedParameter-8.0 {Check that previous dependencies are cleared when a new Token or expression is placed in the SharedParameter.} {
    set e [java::new {ptolemy.kernel.Entity String} parent]
    set param1 [java::new ptolemy.actor.parameters.SharedParameter $e id1 ]
    $param1 setExpression "10"

    set param2 [java::new ptolemy.actor.parameters.SharedParameter $e id2 ]
    $param2 setExpression "id1"

    # This should clear the previous dependence on param2
    $param2 setExpression "20"

    # This should be ok as there is no dependency loop
    $param1 setExpression "id2"
    
    set value2 [[$param1 getToken] toString]
    
    list $value2
} {20}
#################################
####
# 
test SharedParameter-9.0 {Check that notification works properly when a SharedParameter is removed} {
    set top [java::new ptolemy.kernel.CompositeEntity]
    $top setName topLevel
    set bottom [java::new ptolemy.kernel.CompositeEntity $top bottomLevel]
    set param1 [java::new ptolemy.actor.parameters.SharedParameter $top clock ]
    $param1 setExpression "11"

    set param2 [java::new ptolemy.actor.parameters.SharedParameter $bottom clock ]
    $param2 setExpression "66"

    set param3 [java::new ptolemy.actor.parameters.SharedParameter $bottom newFreq ]
    $param3 setExpression "clock * 100"

    set res1 [[$param3 getToken] toString]

    # This should remove clack parameter from the bottom entity and make
    # the clock parameter from the top entity visible.
    $param2 setContainer [java::null]

    set res2 [[$param3 getToken] toString]
    	
    # With a regular Parameter, res2 would be 1100	
    list $res1 $res2 [[$param1 getToken] toString]
} {6600 6600 66}


#################################
####
#
test SharedParameter11.0 {Check that variables are in the scope of variables.} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    set a [java::new ptolemy.data.expr.Variable $e a]
    set b [java::new ptolemy.data.expr.Variable $e b]
    set tok [java::new {ptolemy.data.IntToken int} 1]
    $b setToken $tok
    $a setExpression "b"
    set ra [$a getToken]
    $ra toString
} {1}

#################################
####
# NOTE: This test verifies a "misfeature" that it would be nice to find
# a way to fix.  The returned value would ideally be 3.5, but this seems
# impossible to do without having the side effect of almost always
# triggering the evaluation of expressions as soon as they are set.
test SharedParameter12.0 {Check that notification does not occur when dependents change} {
    set e [java::new ptolemy.data.expr.test.AttributeChanged entity]
    set a [java::new ptolemy.data.expr.Variable $e a]
    set param [java::field $e param]
    $param setExpression {a}
    $a setExpression {4.5}
    $param getToken
    $a setExpression {3.5}
    $e getParamValue
} {4.5}

######################################################################
####
#
test SharedParameter-13.0 {Test exportMoML} {
    set n [java::new ptolemy.kernel.util.Workspace "N"]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    set a1 [java::new ptolemy.actor.parameters.SharedParameter $a "A1"]
    $a exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="A" class="ptolemy.kernel.util.NamedObj">
    <property name="A1" class="ptolemy.actor.parameters.SharedParameter">
    </property>
</entity>
}

test SharedParameter-13.1 {Test exportMoML} {
    $a1 setExpression {3}
    $a exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="A" class="ptolemy.kernel.util.NamedObj">
    <property name="A1" class="ptolemy.actor.parameters.SharedParameter" value="3">
    </property>
</entity>
}

test SharedParameter-13.2 {Test exportMoML} {
    $a1 setExpression {"Test String"}
    $a exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="A" class="ptolemy.kernel.util.NamedObj">
    <property name="A1" class="ptolemy.actor.parameters.SharedParameter" value="&quot;Test String&quot;">
    </property>
</entity>
}

######################################################################
####
#
test SharedParameter-14.0 {Test the mechanism for extending scope} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setModelErrorHandler \
            [java::new ptolemy.kernel.util.BasicModelErrorHandler]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set ext1 [java::new ptolemy.data.expr.ScopeExtendingAttribute $e1 "ext1"]
    set p1 [java::new ptolemy.actor.parameters.SharedParameter $ext1 "p"]
    set ext2 [java::new ptolemy.data.expr.ScopeExtendingAttribute $e2 "ext2"]
    set p2 [java::new ptolemy.actor.parameters.SharedParameter $ext2 "p"]
    set p3 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p3"]
    set p4 [java::new ptolemy.actor.parameters.SharedParameter $e2 "p4"]

    $p1 setExpression "5"
    $p2 setExpression "0"

    $p3 setExpression "p"
    $p4 setExpression "p"

    set r1 [$p3 getToken]
    set r2 [$p4 getToken]

    $ext2 setContainer [java::null]

    set r3 [$p4 getToken]

    catch {$ext1 setContainer [java::null]} msg1

    catch {$p4 getToken} msg2

    # With regular parameters, we would have 5 0 5
    list [$r1 toString] [$r2 toString] [$r3 toString] $msg1 $msg2
} {0 0 0 {ptolemy.kernel.util.IllegalActionException: Error evaluating expression: p
  in .<Unnamed Object>.p3
Because:
The ID p is undefined.
-------------- and --------------
Error evaluating expression: p
  in .<Unnamed Object>.e2.p4
Because:
The ID p is undefined.} {ptolemy.kernel.util.IllegalActionException: Error evaluating expression: p
  in .<Unnamed Object>.e2.p4
Because:
The ID p is undefined.}}

######################################################################
####
#
test SharedParameter-15.0 {Insert a new parameter shadows same-named parameter at higher level} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set p1 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p"]
    set p3 [java::new ptolemy.actor.parameters.SharedParameter $e2 "p3"]

    $p1 setExpression "5"
    $p3 setExpression "p"
    
    set r1 [$p3 getToken]

    set p2 [java::new ptolemy.actor.parameters.SharedParameter $e2 "p"]
    $p2 setExpression "10"

    set r2 [$p3 getToken]

    list [$r1 toString] [$r2 toString]
} {5 10}

test SharedParameter-15.1 {Removing an actor that contains a parameter should not throw an exception if that parameter depends on other parameters} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setModelErrorHandler \
            [java::new ptolemy.kernel.util.BasicModelErrorHandler]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set p1 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p1"]
    set p3 [java::new ptolemy.actor.parameters.SharedParameter $e2 "p3"]

    $p1 setExpression "5"
    $p3 setExpression "p1"
    
    set r1 [$p3 getToken]

    catch {$e2 setContainer [java::null]} msg
    $p1 setExpression "6"
	$p1 validate

    list [$r1 toString] $msg
} {5 {}}

test SharedParameter-15.2 {Failure to Shadow, including scopeeEtending} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set e3 [java::new ptolemy.data.expr.ScopeExtendingAttribute $e2 "e3"]
    set p1 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p"]
    set p3 [java::new ptolemy.actor.parameters.SharedParameter $e2 "p3"]

    $p1 setExpression "5"
    $p3 setExpression "p"
    
    set r1 [$p3 getToken]

    set p2 [java::new ptolemy.actor.parameters.SharedParameter $e3 "p"]
    $p2 setExpression "10"

    set r2 [$p3 getToken]

    list [$r1 toString] [$r2 toString]
} {5 10}

test SharedParameter-15.3 {Changing container of parameter depends on scope.} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1 "e2"]
    set e3 [java::new ptolemy.kernel.ComponentEntity $e2 "e3"]
 
    set p1 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p"]
    set p2 [java::new ptolemy.actor.parameters.SharedParameter $e2 "p"]
    set p3 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p3"]

    $p1 setExpression "5"
    $p2 setExpression "7"
    $p3 setExpression "p"
    
    set r1 [$p3 getToken]

    $p3 setContainer $e2

    set r2 [$p3 getToken]

    # With a regular parameter, r1 toString would be 5
    list [$r1 toString] [$r2 toString]
} {7 7}

test SharedParameter-15.4 {Changing container of parameter that depends on scope to an invalid scope.} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    # e2 has to be of the same class as e1 for the parameters to be shared.
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1 "e2"]
    $e1 setModelErrorHandler \
            [java::new ptolemy.kernel.util.BasicModelErrorHandler]

    set e3 [java::new ptolemy.kernel.ComponentEntity]
    $e3 setModelErrorHandler \
            [java::new ptolemy.kernel.util.BasicModelErrorHandler]

    set p1 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p"]
    set p2 [java::new ptolemy.actor.parameters.SharedParameter $e2 "p"]
    set p3 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p3"]

    $p1 setExpression "5"
    $p2 setExpression "7"
    $p3 setExpression "p"
    
    set msg1 [[$p3 getToken] toString]

    catch {$p3 setContainer $e3} msg2

    catch {set msg3 [[$p3 getToken] toString]} msg3
    
    # With a regular parameter, msg1 would be 5 
    list $msg1 $msg2 $msg3
} {7 {ptolemy.kernel.util.IllegalActionException: Error evaluating expression: p
  in .<Unnamed Object>.p3
Because:
The ID p is undefined.} {ptolemy.kernel.util.IllegalActionException: Error evaluating expression: p
  in .<Unnamed Object>.p3
Because:
The ID p is undefined.}}

test SharedParameter-15.5 {Changing container of parameter depends on scope.} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setModelErrorHandler \
            [java::new ptolemy.kernel.util.BasicModelErrorHandler]
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1 "e2"]
    set e3 [java::new ptolemy.kernel.ComponentEntity $e2 "e3"]
 
    set p1 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p"]
    set p2 [java::new ptolemy.actor.parameters.SharedParameter $e2 "p"]
    set p3 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p3"]

    $p1 setExpression "5"
    $p2 setExpression "7"
    $p3 setExpression "p"
    
    catch {set msg1 [[$p3 getToken] toString]} msg1

    catch {$p3 setContainer [java::null]} msg2

    catch {set msg3 [[$p3 getToken] toString]} msg3
    
    # With a regular parameter, msg1 would be 5 
    list $msg1 $msg2 $msg3
} {7 {} {ptolemy.kernel.util.IllegalActionException: Error evaluating expression: p
  in .p3
Because:
The ID p is undefined.}}

test SharedParameter-15.6 {Changing container of container of parameter that depends on outside scope from valid scope to valid scope} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1 "e2"]
    set e3 [java::new ptolemy.kernel.ComponentEntity $e2 "e3"]
 
    set p1 [java::new ptolemy.data.expr.Parameter $e1 "p"]
    set p2 [java::new ptolemy.data.expr.Parameter $e2 "p"]
    set p3 [java::new ptolemy.data.expr.Parameter $e3 "p3"]

    $p1 setExpression "5"
    $p2 setExpression "7"
    $p3 setExpression "p"
    
    set r1 [$p3 getToken]

    $e3 setContainer $e1

    set r2 [$p3 getToken]


    set sp1 [java::new ptolemy.actor.parameters.SharedParameter $e1 "sp"]
    set sp2 [java::new ptolemy.actor.parameters.SharedParameter $e2 "sp"]
    set sp3 [java::new ptolemy.actor.parameters.SharedParameter $e3 "sp3"]

    $sp1 setExpression "55"
    $sp2 setExpression "77"
    $sp3 setExpression "sp"
    
    set r3 [$sp3 getToken]

    $e3 setContainer $e1

    set r4 [$sp3 getToken]

    # When we change the container of a Parameter, we get different
    # values, but not when we change the container of a SharedParmeter
    list [$r1 toString] [$r2 toString] [$r3 toString] [$r4 toString]
} {7 5 77 77}

test SharedParameter-15.7 {Removing parameter invalidate dependants.} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setModelErrorHandler \
            [java::new ptolemy.kernel.util.BasicModelErrorHandler]
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1 "e2"]
 
    set p1 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p"]
    set p3 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p3"]

    $p1 setExpression "5"
    $p3 setExpression "p"
    
    catch {set msg1 [[$p3 getToken] toString]} msg1

    catch {$p1 setContainer [java::null]} msg2

    catch {set msg3 [[$p3 getToken] toString]} msg3

    list $msg1 $msg2 $msg3
} {5 {ptolemy.kernel.util.IllegalActionException: Error evaluating expression: p
  in .<Unnamed Object>.p3
Because:
The ID p is undefined.} {ptolemy.kernel.util.IllegalActionException: Error evaluating expression: p
  in .<Unnamed Object>.p3
Because:
The ID p is undefined.}}

test SharedParameter-15.8 {Changing container of container of parameter that depends on outside scope from valid scope to invalid scope} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setModelErrorHandler \
            [java::new ptolemy.kernel.util.BasicModelErrorHandler]
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1 "e2"]
    set e3 [java::new ptolemy.kernel.ComponentEntity $e2 "e3"]
 
    set p2 [java::new ptolemy.actor.parameters.SharedParameter $e2 "p"]
    set p3 [java::new ptolemy.actor.parameters.SharedParameter $e3 "p3"]

    $p2 setExpression "7"
    $p3 setExpression "p"
    
    catch {set msg1 [[$p3 getToken] toString]} msg1

    catch {$p3 setContainer $e1} msg2

    catch {set msg3 [[$p3 getToken] toString]} msg3

    list $msg1 $msg2 $msg3
} {7 {ptolemy.kernel.util.IllegalActionException: Error evaluating expression: p
  in .<Unnamed Object>.p3
Because:
The ID p is undefined.} {ptolemy.kernel.util.IllegalActionException: Error evaluating expression: p
  in .<Unnamed Object>.p3
Because:
The ID p is undefined.}}

test SharedParameter-15.9 {Changing container of container of parameter that depends on outside scope from valid scope to invalid null scope} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setModelErrorHandler \
            [java::new ptolemy.kernel.util.BasicModelErrorHandler]
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1 "e2"]
    set e3 [java::new ptolemy.kernel.ComponentEntity $e2 "e3"]
 
    set p2 [java::new ptolemy.actor.parameters.SharedParameter $e2 "p"]
    set p3 [java::new ptolemy.actor.parameters.SharedParameter $e3 "p3"]

    $p2 setExpression "7"
    $p3 setExpression "p"
    
    catch {set msg1 [[$p3 getToken] toString]} msg1

    catch {$p3 setContainer [java::null]} msg2

    catch {set msg3 [[$p3 getToken] toString]} msg3

    list $msg1 $msg2 $msg3
} {7 {} {ptolemy.kernel.util.IllegalActionException: Error evaluating expression: p
  in .p3
Because:
The ID p is undefined.}}

######################################################################
####
#
test SharedParameter-16.0 {Test array and matrix references} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set p1 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p1"]
    set p2 [java::new ptolemy.actor.parameters.SharedParameter $e2 "p2"]
    $p1 setExpression "\{1, 2, 3\}"
    $p2 setExpression "p1(0)"
    set r1 [[$p2 getToken] toString]
    $p2 setExpression "p1(10)"
    catch {$p2 getToken} msg1

    $p1 setExpression "\[1, 2; 3, 4\]"
    $p2 setExpression "p1(0, 0)"
    set r2 [[$p2 getToken] toString]
    $p2 setExpression "p1(4, 5)"
    catch {$p2 getToken} msg2
    list $r1 $r2 $msg1 $msg2
} {1 1 {ptolemy.kernel.util.IllegalActionException: Error evaluating expression: p1(10)
  in .<Unnamed Object>.e2.p2
Because:
The index '10' is out of bounds on the array '{1, 2, 3}'.} {ptolemy.kernel.util.IllegalActionException: Error evaluating expression: p1(4, 5)
  in .<Unnamed Object>.e2.p2
Because:
The index (4,5) is out of bounds on the matrix '[1, 2; 3, 4]'.}}


######################################################################
#### StringParameter tests.

test SharedParameter-17.0 {String mode parameters} {
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p1"]
    $p1 setStringMode true
    $p1 setToken [java::new ptolemy.data.StringToken "noQuotes!"]
    $p1 getExpression
} {noQuotes!}

test SharedParameter-17.1 {String mode parameters} {
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p1"]
    $p1 setStringMode true
    $p1 setExpression "noQuotes!"
    $p1 getExpression
} {noQuotes!}

test SharedParameter-17.2 {String mode parameters} {
    $p1 setExpression {a"}
    $p1 getExpression
} {a"}

test SharedParameter-17.3 {String mode parameters} {
    $p1 setExpression {a\"}
	$p1 getExpression
} {a\"}

test SharedParameter-17.4 {String mode parameters} {
    $p1 setExpression {a}
    set p2 [java::new ptolemy.actor.parameters.SharedParameter $e1 "p2"]
    $p2 setStringMode true
    $p2 setExpression {$p1}
	$p2 getExpression
} {$p1}

test SharedParameter-17.5 {String mode parameters} {
    set t [$p2 getToken]
    list [$t toString] [[java::cast ptolemy.data.StringToken $t] stringValue]
} {{"a"} a}

test SharedParameter-17.6 {String mode parameters} {
	$p2 setExpression {a $p1 $(p1) a ${p1} a $$a}
    set t [$p2 getToken]
    [java::cast ptolemy.data.StringToken $t] stringValue
} {a a a a a a $a}

test SharedParameter-18.0 {simple setExpression} {
    set c18 [java::new ptolemy.kernel.CompositeEntity] 
    set c18_1 [java::new ptolemy.kernel.CompositeEntity $c18 c18_1] 
    set c18_2 [java::new ptolemy.kernel.CompositeEntity $c18 c18_2] 
    set param1 [java::new ptolemy.actor.parameters.SharedParameter $c18_1 param1 \
	[$c18_2 getClass] 1]
    set param2 [java::new ptolemy.actor.parameters.SharedParameter $c18_2 param1]
    set r1 [$param1 getExpression]
    set r2 [$param2 getExpression]

    $param1 setExpression 2
    set r3 [$param1 getExpression]
    set r4 [$param2 getExpression]

    $param2 setExpression 3
    set r5 [$param1 getExpression]
    set r6 [$param2 getExpression]
    list $r1 $r2 $r3 $r4 $r5 $r6

} {1 1 2 2 3 3}

test SharedParameter-18.1.1 {simple setExpression with TestSharedParameter} {
	# FIXME: This test seems to mainly count how many times methods are called.
	# Why does this matter?
    set c18 [java::new ptolemy.kernel.CompositeEntity] 
    set c18_1 [java::new ptolemy.kernel.CompositeEntity $c18 c18_1] 
    set c18_2 [java::new ptolemy.kernel.CompositeEntity $c18 c18_2] 
    set c18_3 [java::new ptolemy.kernel.CompositeEntity $c18 c18_3] 
    set param1 [java::new ptolemy.actor.parameters.test.TestSharedParameter $c18_1 param1 \
	[$c18_2 getClass] 1]
    set param2 [java::new ptolemy.actor.parameters.test.TestSharedParameter $c18_2 param1]
    set param3 [java::new ptolemy.actor.parameters.test.TestSharedParameter $c18_3 param1]

    set r1 [$param1 getExpression]
    set r2 [$param2 getExpression]
    set r3 [$param3 getExpression]

    $param1 setExpression 2
    set r4 [$param1 getExpression]
    set r5 [$param2 getExpression]
    set r6 [$param3 getExpression]

    $param2 setExpression 3
    set r7 [$param1 getExpression]
    set r8 [$param2 getExpression]
    set r9 [$param3 getExpression]

    # List all the counts just to be sure we are not bogus
    list [list $r1 $r2 $r3 $r4 $r5 $r6 $r7 $r8 $r9] "\n" \
	[$param1 getCounts] "\n" \
	[$param2 getCounts] "\n" \
	[$param3 getCounts]
} {{1 1 1 2 2 2 3 3 3} {
} {inferValueFromContextCount: 1
isSuppressingPropagationCount: 0
setExpressionCount: 3
sharedParameterSetCount: 1
validateCount: 0
propagateValueCount: 0} {
} {inferValueFromContextCount: 1
isSuppressingPropagationCount: 0
setExpressionCount: 3
sharedParameterSetCount: 1
validateCount: 0
propagateValueCount: 0} {
} {inferValueFromContextCount: 1
isSuppressingPropagationCount: 0
setExpressionCount: 3
sharedParameterSetCount: 0
validateCount: 0
propagateValueCount: 0}} 

test SharedParameter-18.1.2 {call validateSettables} {
    # Uses 18.1 above
    set r1 [$param1 getCounts]
    set r2 [$param2 getCounts]
    set r3 [$param3 getCounts]

    $c18 validateSettables

    set r4 [$param1 getCounts]
    set r5 [$param2 getCounts]
    set r6 [$param3 getCounts]

    # Formerly, r2 would have these values 
    #  sharedParameterSetCount: 3
    #  validateCount: 4
    # Now, with changes
    # to {CompositeEntity,Entity,NamedObj}.validateSettables()
    # we get 
    #  sharedParameterSetCount: 4
    #  validateCount: 2

    list "r1=" $r1 "\nr2=" $r2 "\nr3=" $r3 "\n...\nr4=" \
	$r4 "\nr5=" $r5 "\nr6=" $r6

} {r1= {inferValueFromContextCount: 1
isSuppressingPropagationCount: 0
setExpressionCount: 3
sharedParameterSetCount: 1
validateCount: 0
propagateValueCount: 0} {
r2=} {inferValueFromContextCount: 1
isSuppressingPropagationCount: 0
setExpressionCount: 3
sharedParameterSetCount: 1
validateCount: 0
propagateValueCount: 0} {
r3=} {inferValueFromContextCount: 1
isSuppressingPropagationCount: 0
setExpressionCount: 3
sharedParameterSetCount: 0
validateCount: 0
propagateValueCount: 0} {
...
r4=} {inferValueFromContextCount: 1
isSuppressingPropagationCount: 0
setExpressionCount: 3
sharedParameterSetCount: 2
validateCount: 1
propagateValueCount: 0} {
r5=} {inferValueFromContextCount: 1
isSuppressingPropagationCount: 0
setExpressionCount: 3
sharedParameterSetCount: 1
validateCount: 1
propagateValueCount: 0} {
r6=} {inferValueFromContextCount: 1
isSuppressingPropagationCount: 0
setExpressionCount: 3
sharedParameterSetCount: 0
validateCount: 1
propagateValueCount: 0}}

#################################
####
#
test SharedParameter-19.2 {Check that type changes with new Token type} {
    set c19 [java::new ptolemy.kernel.CompositeEntity] 
    set c19_1 [java::new ptolemy.kernel.CompositeEntity $c19 c19_1] 
    set c19_2 [java::new ptolemy.kernel.CompositeEntity $c19 c19_2] 

    set param1 [java::new ptolemy.actor.parameters.SharedParameter $c19_1 id1 [java::null] "11"]
    set param2 [java::new ptolemy.actor.parameters.SharedParameter $c19_2 id1 [java::null] "22"]

	# Now put a new token into the SharedParameter
    set tok1 [java::new  {ptolemy.data.DoubleToken double} 7.3]
	$param1 setToken $tok1

    set value1 [[$param1 getToken] toString]
    set value2 [[$param2 getToken] toString]

	# They show the same value.
    list $value1 $value2

} {7.3 7.3}

