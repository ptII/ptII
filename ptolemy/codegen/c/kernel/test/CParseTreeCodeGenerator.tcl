# Run tests on CParseTreeCodeGenerator class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005-2009 The Regents of the University of California.
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

proc parseTreeTest {expression} {
    set ptParser [java::new ptolemy.data.expr.PtParser]
    set parseTree [$ptParser generateParseTree $expression]
    set model [sdfModel]
    set codeGenerator \
    [java::new ptolemy.codegen.c.kernel.CCodeGenerator \
    $model "myCodeGenerator"]

    set parseTreeCodeGenerator \
	[java::new ptolemy.codegen.c.kernel.CParseTreeCodeGenerator $codeGenerator]
    # We have to eval the parse tree first, though we ignore the value
    set token [$parseTreeCodeGenerator evaluateParseTree $parseTree]
    # return [list [$token toString] [$parseTreeCodeGenerator generateFireCode]]
    return [list [$parseTreeCodeGenerator generateFireCode]]
}

test CParseTreeCodeGenerator-1.1 {Simple tests of ParseTreeCodeGenerator} {
    parseTreeTest {1+3}
} {{($add_Int_Int($convert_Int_Int(1), 3))}}

test CParseTreeCodeGenerator-1.2 {A more complex example} {
    parseTreeTest {((3+4)+(1|2)+232)}
} {{($add_Int_Int($add_Int_Int($convert_Int_Int(($add_Int_Int($convert_Int_Int(3), 4))), (1|2)), 232))}}

test CParseTreeCodeGenerator-2.1 {Define a variable in a regular parse tree } {
    # This test uses the regular (non codegen) parse tree
    # We need to do something similar for codegen
    # See ptII/ptolemy/data/expr/test/PtParser.tcl
    set namedList [java::new ptolemy.kernel.util.NamedList]
    set variableA [java::new ptolemy.data.expr.Variable]
    $variableA setName "foo"
    $variableA setExpression "42"
    $namedList prepend $variableA
    set scope [java::new ptolemy.data.expr.ExplicitScope $namedList]    
    set evaluator [java::new ptolemy.data.expr.ParseTreeEvaluator]

    #set res1  [ $evaluator evaluateParseTree $root1 $scope]
    set ptParser [java::new ptolemy.data.expr.PtParser]
    set root [ $ptParser generateStringParseTree {1+$foo} ]
    set results  [ $evaluator evaluateParseTree $root $scope]
    list [$results toString]

} {{"1+42"}}


proc parseTreeTraceTest {expression} {
    set ptParser [java::new ptolemy.data.expr.PtParser]
    set model [sdfModel]
    set codeGenerator \
    [java::new ptolemy.codegen.c.kernel.CCodeGenerator \
    $model "myCodeGenerator"]
    set parseTreeCodeGenerator \
    [java::new ptolemy.codegen.c.kernel.CParseTreeCodeGenerator $codeGenerator]

    set parseTree [$ptParser generateParseTree $expression ]
    set tree [$parseTreeCodeGenerator traceParseTreeEvaluation \
		$parseTree [java::null]]
    return [list \
		$tree \
		[$parseTreeCodeGenerator generateFireCode]]
}

test CParseTreeCodeGenerator-10.1 {traceParseTreeEvaluation} {
    parseTreeTraceTest {10+1}
} {{Entering node ptolemy.data.expr.ASTPtSumNode
  Entering node ptolemy.data.expr.ASTPtLeafNode
  Node ptolemy.data.expr.ASTPtLeafNode evaluated to 10
  Entering node ptolemy.data.expr.ASTPtLeafNode
  Node ptolemy.data.expr.ASTPtLeafNode evaluated to 1
Node ptolemy.data.expr.ASTPtSumNode evaluated to 10
} {($add_null_null($convert_null_null(10), 1))}}

test CParseTreeCodeGenerator-11.1 {visitFunctionDefinitionNode} {
    parseTreeTraceTest {function(x:double) x*5.0}
} {{Entering node ptolemy.data.expr.ASTPtFunctionDefinitionNode
Node ptolemy.data.expr.ASTPtFunctionDefinitionNode evaluated to (function(x:double) (x*5.0))
} {}}

test CParseTreeCodeGenerator-12.1 {visitFunctionalIfNode} {
    parseTreeTraceTest { 1==0 ? 0.5: 1.5 }
} {{Entering node ptolemy.data.expr.ASTPtFunctionalIfNode
  Entering node ptolemy.data.expr.ASTPtRelationalNode
    Entering node ptolemy.data.expr.ASTPtLeafNode
    Node ptolemy.data.expr.ASTPtLeafNode evaluated to 1
    Entering node ptolemy.data.expr.ASTPtLeafNode
    Node ptolemy.data.expr.ASTPtLeafNode evaluated to 0
  Node ptolemy.data.expr.ASTPtRelationalNode evaluated to null
  java.lang.NullPointerException
} {(1 == 0)}} {Why is this null}


test CParseTreeCodeGenerator-13.1 {visitMethodCallNode} {
    parseTreeTraceTest {[1, 2; 3, 4; 5, 6].getRowCount()}
} {{Entering node ptolemy.data.expr.ASTPtMethodCallNode
  Entering node ptolemy.data.expr.ASTPtMatrixConstructNode
    Entering node ptolemy.data.expr.ASTPtLeafNode
    Node ptolemy.data.expr.ASTPtLeafNode evaluated to 1
    Entering node ptolemy.data.expr.ASTPtLeafNode
    Node ptolemy.data.expr.ASTPtLeafNode evaluated to 2
    Entering node ptolemy.data.expr.ASTPtLeafNode
    Node ptolemy.data.expr.ASTPtLeafNode evaluated to 3
    Entering node ptolemy.data.expr.ASTPtLeafNode
    Node ptolemy.data.expr.ASTPtLeafNode evaluated to 4
    Entering node ptolemy.data.expr.ASTPtLeafNode
    Node ptolemy.data.expr.ASTPtLeafNode evaluated to 5
    Entering node ptolemy.data.expr.ASTPtLeafNode
    Node ptolemy.data.expr.ASTPtLeafNode evaluated to 6
  Node ptolemy.data.expr.ASTPtMatrixConstructNode evaluated to [1, 2; 3, 4; 5, 6]
Node ptolemy.data.expr.ASTPtMethodCallNode evaluated to [1, 2; 3, 4; 5, 6]
} {($new(Matrix(3, 2, 6, $new(Int(1)), $new(Int(2)), $new(Int(3)), $new(Int(4)), $new(Int(5)), $new(Int(6)), TYPE_Int)))->getRowCount()}}

test CParseTreeCodeGenerator-14.1 {visitPowerNode} {
    parseTreeTraceTest {2^3}
} {{Entering node ptolemy.data.expr.ASTPtPowerNode
  Entering node ptolemy.data.expr.ASTPtLeafNode
  Node ptolemy.data.expr.ASTPtLeafNode evaluated to 2
  Entering node ptolemy.data.expr.ASTPtLeafNode
  Node ptolemy.data.expr.ASTPtLeafNode evaluated to 3
Node ptolemy.data.expr.ASTPtPowerNode evaluated to 2
} {(pow(2, 3))}}


test CParseTreeCodeGenerator-15.1 {visitShiftNode} {
    parseTreeTraceTest {16<<1} 
} {{Entering node ptolemy.data.expr.ASTPtShiftNode
  Entering node ptolemy.data.expr.ASTPtLeafNode
  Node ptolemy.data.expr.ASTPtLeafNode evaluated to 16
  Entering node ptolemy.data.expr.ASTPtLeafNode
  Node ptolemy.data.expr.ASTPtLeafNode evaluated to 1
Node ptolemy.data.expr.ASTPtShiftNode evaluated to null
} {(16 << 1)}}

test CParseTreeCodeGenerator-16.1 {visitFunctionApplicationNode} {
    parseTreeTraceTest {(function(x:double) x*5.0) (10.0)}
} {} {Why does this have null 10.0}

# I've commented this out. The expected test result 
# "$new(IntArray(3, 3, 0, 2, 3))" is not correct, and the 
# CParseTreeCodeGenerator doesn't currently support map(). -- Jackie
#
# test CParseTreeCodeGenerator-16.2 {visitFunctionApplicationNode} {
#    parseTreeTraceTest {map(function(x:int) x+3, {0, 2, 3})}
# } {{Entering node ptolemy.data.expr.ASTPtFunctionApplicationNode
#  Entering node ptolemy.data.expr.ASTPtFunctionDefinitionNode
#  Node ptolemy.data.expr.ASTPtFunctionDefinitionNode evaluated to (function(x:int) (x+3))
#  Entering node ptolemy.data.expr.ASTPtArrayConstructNode
#    Entering node ptolemy.data.expr.ASTPtLeafNode
#    Node ptolemy.data.expr.ASTPtLeafNode evaluated to 0
#    Entering node ptolemy.data.expr.ASTPtLeafNode
#    Node ptolemy.data.expr.ASTPtLeafNode evaluated to 2
#    Entering node ptolemy.data.expr.ASTPtLeafNode
#    Node ptolemy.data.expr.ASTPtLeafNode evaluated to 3
#  Node ptolemy.data.expr.ASTPtArrayConstructNode evaluated to {0, 2, 3}
# Node ptolemy.data.expr.ASTPtFunctionApplicationNode evaluated to {0, 2, 3}
# } {$new(IntArray(3, 3, 0, 2, 3))}}


test CParseTreeCodeGenerator-16.3 {visitFunctionApplicationNode} {
    set ptParser [java::new ptolemy.data.expr.PtParser]
    set model [sdfModel]
    set codeGenerator \
    [java::new ptolemy.codegen.c.kernel.CCodeGenerator \
    $model "myCodeGenerator"]
    set parseTreeCodeGenerator \
    [java::new ptolemy.codegen.c.kernel.CParseTreeCodeGenerator $codeGenerator]
    
    set parseTree [$ptParser generateParseTree \
		       {f = function(x:double) x*5.0}]
    set tree [$parseTreeCodeGenerator traceParseTreeEvaluation \
		$parseTree [java::null]]
    set r1 [list \
		$tree \
		[$parseTreeCodeGenerator generateFireCode]]
    set parseTree [$ptParser generateParseTree {f(10.0)}
    set tree [$parseTreeCodeGenerator traceParseTreeEvaluation \
    		$parseTree [java::null]]
    set r2 [list \
    		$tree \
    		[$parseTreeCodeGenerator generateFireCode]]
    list $r1 $r2
} {} {known failure}

test CParseTreeCodeGenerator-16.4 {visitFunctionApplicationNode} {
    parseTreeTraceTest {iterate(function(x:int) x+3, 5, 0)}
} {} {Why does this have iterate(, 5, 0)}


test CParseTreeCodeGenerator-16.5 {visitFunctionApplicationNode, cover _evaluateArrayIndex} {
    parseTreeTraceTest {{1,2}(1)}
} {{null(1$new(Array(2, 2, $new(Int(1)), $new(Int(2))))}} \
{why does this have null in it}

test CParseTreeCodeGenerator-16.5 {visitFunctionApplicationNode, cover _evaluateMatrixIndex} {
    parseTreeTraceTest {[1, 2; 3, 4](0,0)}
} {   {null(0, 0((Token*) $new(Matrix(2, 2, $new(Int(1)), TYPE_Int, $new(Int(2)), TYPE_Int, $new(Int(3)), TYPE_Int, $new(Int(4)), TYPE_Int)))}
} {Why Does this have null in it}


test CParseTreeCodeGenerator-17.1 {visitRecordConstructNode} {
    parseTreeTraceTest {{a=1,b=2}}
} {{Entering node ptolemy.data.expr.ASTPtRecordConstructNode
  Entering node ptolemy.data.expr.ASTPtLeafNode
  Node ptolemy.data.expr.ASTPtLeafNode evaluated to 1
  Entering node ptolemy.data.expr.ASTPtLeafNode
  Node ptolemy.data.expr.ASTPtLeafNode evaluated to 2
Node ptolemy.data.expr.ASTPtRecordConstructNode evaluated to {a = 1, b = 2}
} 12}


test CParseTreeCodeGenerator-17.2 {Construct arrays with newline strings in them} {
    parseTreeTraceTest {{"this is
 a test", "test two", "\\(regex\\)"}}
} {{Entering node ptolemy.data.expr.ASTPtArrayConstructNode
  Entering node ptolemy.data.expr.ASTPtLeafNode
  Node ptolemy.data.expr.ASTPtLeafNode evaluated to "this is\n a test"
  Entering node ptolemy.data.expr.ASTPtLeafNode
  Node ptolemy.data.expr.ASTPtLeafNode evaluated to "test two"
  Entering node ptolemy.data.expr.ASTPtLeafNode
  Node ptolemy.data.expr.ASTPtLeafNode evaluated to "\\(regex\\)"
Node ptolemy.data.expr.ASTPtArrayConstructNode evaluated to {"this is\n a test", "test two", "\\(regex\\)"}
} {$new(StringArray(3, 3, "this is\\n a test", "test two", "\\\\(regex\\\\)"))}}

# I've commented this out. The ParseTreeCodeGenerator should not be 
# tested this way. -- Gang
#
#test CParseTreeCodeGenerator-2.2 {Define a variable in a codegen parse tree } {
#    # We need to do something similar for codegen
#    # See ptII/ptolemy/data/expr/test/PtParser.tcl
#    set namedList [java::new ptolemy.kernel.util.NamedList]
#    set variableA [java::new ptolemy.data.expr.Variable]
#    $variableA setName "foo"
#    $variableA setExpression "42"
#    $namedList prepend $variableA
#    set scope [java::new ptolemy.data.expr.ExplicitScope $namedList]    
#    #set evaluator [java::new ptolemy.data.expr.ParseTreeEvaluator]
#    set evaluator \
#  	[java::new ptolemy.codegen.c.kernel.CParseTreeCodeGenerator]
#
#    #set res1  [ $evaluator evaluateParseTree $root1 $scope]
#    set ptParser [java::new ptolemy.data.expr.PtParser]
#    set root [ $ptParser generateStringParseTree {1+$foo} ]
#    set results  [ $evaluator evaluateParseTree $root $scope]
#    list [$evaluator generateFireCode]
#
#} {{"1+42"}}
