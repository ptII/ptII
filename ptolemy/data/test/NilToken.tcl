# Tests for the nil tokens
#
# @Author: Christopher Brooks
#
# @Version $Id$
#
# @Copyright (c) 2006-2008 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
# 


# Test Token, which does not support non-nil one and zero

set types [list Token DoubleToken IntToken LongToken UnsignedByteToken]
foreach type $types {
    puts -nonewline "$type"
    # Perform binary operations on nil types
    # bitwiseAnd bitwiseOr bitwiseXor are excluded, they only work on scalars?
    set binaryOperations \
	[list add addReverse \
	     divide divideReverse modulo moduloReverse \
	     multiply multiplyReverse subtract subtractReverse ]

    foreach binaryOperation $binaryOperations {
	test "$type-$binaryOperation" "Test $binaryOperation binary op on $type" {

	    puts -nonewline " $binaryOperation"
	    #set nil [java::new ptolemy.data.$type [java::null]] 
	    set nil [java::field ptolemy.data.Token NIL]
	    set typeToken [java::new ptolemy.data.$type] 
	    if { "$type" == "Token" } {
		set one $typeToken
   	    } else {
	        set one [java::cast ptolemy.data.$type [$typeToken one]]
            }

	    # Try the binaryop with both args nil
	    set results [java::cast ptolemy.data.Token \
			     [$nil $binaryOperation $nil]]
	    set resultsClassName [[$results getClass] getName] 

	    # Try the binaryop with one arg nil
	    set results1 [java::cast ptolemy.data.Token \
			     [$nil $binaryOperation $one]]
	    set results1ClassName [[$results1 getClass] getName] 

	    # Try the binaryop with the other arg nil
	    set results2 [java::cast ptolemy.data.Token \
			     [$one $binaryOperation $nil]]
	    set results2ClassName [[$results2 getClass] getName] 


	    list [list \
		      [$results toString] \
		      [$results isNil] \
		      [[$results getType] toString]] \
		[list \
		     [$results1 toString] \
		     [$results1 isNil] \
		     [[$results getType] toString]] \
		[list \
		     [$results2 toString] \
		     [$results2 isNil] \
		     [[$results getType] toString]] \
	} {{nil 1 niltype} {nil 1 niltype} {nil 1 niltype}}
    }


    
    if { "$type" != "Token" } {
	# Perform unary operations on nil types
	set unaryOperations \
	    [list absolute]
	foreach unaryOperation $unaryOperations {
	    test "$type-$unaryOperation" "Test $unaryOperation unary op on $type" {
		puts -nonewline " $unaryOperation"
		#set nil [java::new ptolemy.data.$type [java::null]] 
		set nil [java::field ptolemy.data.$type NIL]
		set results [$nil $unaryOperation]
		set resultsClassName [[$results getClass] getName] 
		list [$results toString] \
		    [$results isNil] \
		    [expr {"$resultsClassName" == "ptolemy.data.$type"}]
	    } {nil 1 1}
	}

	# Perform isEqualTo on nil types.  isEqualTo always returns false
	set relationalOperations [list isEqualTo]
	foreach relationalOperation $relationalOperations {
	    test "$type-$relationalOperation" "Test $relationalOperation on $type" {
		puts -nonewline " $relationalOperation"
		#set nil [java::new ptolemy.data.$type [java::null]] 
		set nil [java::field ptolemy.data.Token NIL]
		set typeToken [java::new ptolemy.data.$type] 
		set result [$nil $relationalOperation $nil]
		set result2 [$one $relationalOperation $nil]
		set result3 [$nil $relationalOperation $one]
		set result4 [$one $relationalOperation $one]
		list [$result toString] [$result2 toString] \
		    [$result3 toString] [$result4 toString]
	    } {false false false true}
	}
    }

#    # Perform isLessThan on nil types. isLessThan throws an exception
#    set relationalOperations [list isLessThan]
#    foreach relationalOperation $relationalOperations {
#	test "$type-$relationalOperation" "Test $relationalOperation on $type" {
#
#	    puts -nonewline " $relationalOperation"
#	    #set nil [java::new ptolemy.data.$type [java::null]] 
#	    set nil [java::field ptolemy.data.Token NIL]
#	    set typeToken [java::new ptolemy.data.$type] 
#	    set one [java::cast ptolemy.data.$type [$typeToken one]]
#	    catch {$one $relationalOperation $nil} msg
#	    regsub -all $type $msg "XXXToken" result
#	    list $result
#	} {{ptolemy.kernel.util.IllegalActionException: isLessThan operation not supported between ptolemy.data.XXXToken 'nil' and ptolemy.data.XXXToken 'nil' because one or the other is nil}}
#    }
#


    puts "."
}

# Check out MatrixToken operations.
# FIXME: FixToken.NIL does not exist
set types [list Boolean Complex Double Int Long]
foreach type $types {
    puts -nonewline "${type}MatrixToken "

    # Perform convert(ScalarToken)
    set operation convert
    test "$type-$operation" "Test $operation on ${type}MatrixToken" {
	    puts -nonewline " ${operation}(nil)"
	    #set nil [java::new ptolemy.data.$type [java::null]] 
	    set nil [java::field ptolemy.data.${type}Token NIL]
	    set matrixToken [java::new ptolemy.data.${type}MatrixToken] 
   	    catch {java::call ptolemy.data.${type}MatrixToken $operation $nil} msg
	    regsub -all $type $msg "XXX" result
	    regsub -all {\[[a-zA-Z].*\]}  $result "xxx" result2
	    list $result2
	} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.XXXToken 'nil' to the type xxx.}}


    if { $type == "Int" || $type == "Long" } {
    # Perform convert(ScalarToken, int)
    set operation convert
    test "$type-$operation" "Test $operation on ${type}MatrixToken" {
	    puts -nonewline " ${operation}(nil, 2)"
	    #set nil [java::new ptolemy.data.$type [java::null]] 
	    set nil [java::field ptolemy.data.${type}Token NIL]
	    set matrixToken [java::new ptolemy.data.${type}MatrixToken] 
	    catch {java::call ptolemy.data.${type}MatrixToken $operation $nil 2} msg
	    regsub -all $type $msg "XXX" result
	    regsub -all {\[[a-zA-Z].*\]}  $result "xxx" result2
	    list $result2
	} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.XXXToken 'nil' to the type xxx.}}
    }

    puts "."
}
