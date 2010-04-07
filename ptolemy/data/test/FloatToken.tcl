# Tests for the FloatToken class
#
# @Author: Ben Lickly, based on DoubleToken.tcl by Neil Smyth, contributor: Christopher Brooks
#
# @Version $Id$
#
# @Copyright (c) 1997-2007 The Regents of the University of California.
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

# 
#

######################################################################
####
# 
test FloatToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.FloatToken]
    $p toString
} {0.0f}

######################################################################
####
# 
test FloatToken-1.1 {Create a non-empty instance from an float} {
    set p [java::new {ptolemy.data.FloatToken float} 5.5]
    $p toString
} {5.5f}

######################################################################
####
# 
test FloatToken-1.2 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.FloatToken String} "7.77"]
    $p toString
# We must account for roundoff error in floats.
} {7.7699999809265f}

######################################################################
####
# 
test FloatToken-1.3.1 {NIL} { 
    set nil [java::field ptolemy.data.FloatToken NIL]
    list [$nil toString]
} {nil}

######################################################################
####
# 
test FloatToken-1.3 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.FloatToken String} "7.56E-10"]
    $p toString
# We must account for roundoff error in floats.
} {7.559999848361088E-10f}

######################################################################
####
# 
test FloatToken-1.4 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.FloatToken String} "7.56E10"]
    $p toString
# We must account for roundoff error in floats.
} {7.5600003072E10f}

######################################################################
####
# 
test FloatToken-1.5 {Create a nil Token from a null token} {
    catch {java::new ptolemy.data.FloatToken [java::null]} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Creating a nil token with FloatToken(null) is not supported.  Use FloatToken.NIL, or the nil Constant.}}

######################################################################
####
# 
test FloatToken-1.6 {Create a nil Token from an String} {
    catch {java::new {ptolemy.data.FloatToken String} nil} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Creating a nil token with FloatToken("nil") is not supported.  Use FloatToken.NIL, or the nil Constant.}}

######################################################################
####
# 
test FloatToken-1.7 {Create a bogus Token from a bogus String} {
    catch {java::new {ptolemy.data.FloatToken String} "not-a-number"} \
	errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Failed to parse "not-a-number" as a number.
Because:
For input string: "not-a-number"}}

######################################################################
####
# 
test FloatToken-2.0 {Create a non-empty instance and query its value as a double} {
    set p [java::new {ptolemy.data.FloatToken float} 3.5]
    set res [$p doubleValue]
    list $res
} {3.5}

######################################################################
####
# 
test FloatToken-2.1 {Create a non-empty instance and query its value as a float} {
    set p [java::new {ptolemy.data.FloatToken float} 3.5]
    set res1 [$p floatValue]
    list $res1
} {3.5}

######################################################################
####
# 
test FloatToken-2.2 {Create a non-empty instance and query its value as an int} {
    set p [java::new {ptolemy.data.FloatToken float} 12]
    catch {$p intValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.FloatToken '12.0f' to the type int.}}

######################################################################
####
# 
test FloatToken-2.3 {Create a non-empty instance and query its value as a long} {
    set p [java::new {ptolemy.data.FloatToken float} 12]
   catch {$p longValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.FloatToken '12.0f' to the type long.}}

######################################################################
####
# 
test FloatToken-2.4 {Create a non-empty instance and query its value as a string} {
    set p [java::new {ptolemy.data.FloatToken float} 12.2]
    $p toString
# We must account for roundoff error in floats.
} {12.1999998092651f}

######################################################################
####
# 
test FloatToken-2.5 {Test additive identity} {
    set p [java::new {ptolemy.data.FloatToken float} 12.2]
    set token [$p zero]

    list [$token toString]
} {0.0f}
######################################################################
####
# 
test FloatToken-2.6 {Test multiplicative identity} {
    set p [java::new {ptolemy.data.FloatToken float} 12.2]
    set token [$p one]

    list [$token toString]
} {1.0f}

######################################################################
####
# Test addition of floats to Token types below it in the lossless 
# type hierarchy, and with other floats.
test FloatToken-3.0 {Test adding floats.} {
    set p [java::new {ptolemy.data.FloatToken float} 12.2]
    set res1 [$p add $p]
    set res2 [$p addReverse $p]

    list [$res1 toString] [$res2 toString]
# We must account for roundoff error in floats.
} {24.3999996185303f 24.3999996185303f}

######################################################################
####
# 
test FloatToken-3.1 {Test adding floats and shorts.} {
    set tok1 [java::new {ptolemy.data.FloatToken float} 12.2]
    set tok2 [java::new {ptolemy.data.ShortToken short} 2]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
# We must account for roundoff error in floats.
} {14.1999998092651f 14.1999998092651f 14.1999998092651f}

######################################################################
####
# Test division of floats with Token types below it in the lossless 
# type hierarchy, and with other floats.
test FloatToken-4.0 {Test dividing floats.} {
    set p [java::new {ptolemy.data.FloatToken float} 12.2]
    set res1 [$p divide $p]
    set res2 [$p divideReverse $p]

    list [$res1 toString] [$res2 toString]
} {1.0f 1.0f}
######################################################################
####
# 
test FloatToken-4.1 {Test dividing floats and shorts.} {
    set tok1 [java::new {ptolemy.data.FloatToken float} 12.2]
    set tok2 [java::new {ptolemy.data.ShortToken short} 2]
    set res1 [$tok1 divide $tok2]
    set resultToken [java::new {ptolemy.data.FloatToken float} \
	    0.1639344262295]
    set res2 [[$tok1 divideReverse $tok2] isCloseTo $resultToken]

    set res3 [[$tok2 divide $tok1] isCloseTo $resultToken]
 
    list [$res1 toString] [$res2 toString] [$res3 toString]
# We must account for roundoff error in floats.
} {6.0999999046326f true true}

######################################################################
####
# Test isEqualTo operator applied to other floats and Tokens types 
# below it in the lossless type hierarchy.
test FloatToken-5.0 {Test equality between floats.} {
    set tok1 [java::new {ptolemy.data.FloatToken float} 12.2]
    set tok2 [java::new {ptolemy.data.FloatToken float} 2.2]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok1]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]

    list [$res1 toString] [$res2 toString]
} {true false}

######################################################################
####
# 
test FloatToken-5.1 {Test equality between floats and shorts.} {
    set tok1 [java::new {ptolemy.data.FloatToken float} 12]
    set tok2 [java::new {ptolemy.data.ShortToken short} 12]
    set tok3 [java::new {ptolemy.data.FloatToken float} 2]
    set tok4 [java::new {ptolemy.data.ShortToken short} 2]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok4]

    set res3 [$tok2 {isEqualTo ptolemy.data.Token} $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {true false true}

######################################################################
####
# Test isCloseTo operator applied to other floats and Tokens types 
# below it in the lossless type hierarchy.
test FloatToken-5.5 {Test closeness between floats. \
    This test should be the same as the similar FloatToken-5.0 \
    isEquals test. \
} {
    set tok1 [java::new {ptolemy.data.FloatToken float} 12.2]
    set tok2 [java::new {ptolemy.data.FloatToken float} 2.2]

    set res1 [$tok1 {isCloseTo ptolemy.data.Token} $tok1]
    set res2 [$tok1 {isCloseTo ptolemy.data.Token} $tok2]

    list [$res1 toString] [$res2 toString]
} {true false}
######################################################################
####
# 
test FloatToken-5.6 {Test closeness between floats and shorts. \
    This test should be the same as the similar FloatToken-5.0 \
    isEquals test. \
} {
    set tok1 [java::new {ptolemy.data.FloatToken float} 12]
    set tok2 [java::new {ptolemy.data.ShortToken short} 12]
    set tok3 [java::new {ptolemy.data.FloatToken float} 2]
    set tok4 [java::new {ptolemy.data.ShortToken short} 2]

    set res1 [$tok1 {isCloseTo ptolemy.data.Token} $tok2]
    set res2 [$tok1 {isCloseTo ptolemy.data.Token} $tok4]

    set res3 [$tok2 {isCloseTo ptolemy.data.Token} $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {true false true}

######################################################################
####
# 
test FloatToken-5.7 {Test closeness between floats} {
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set token1 [java::new {ptolemy.data.FloatToken float} 12.0]
    set notCloseToken1 [java::new {ptolemy.data.FloatToken float} \
	    [expr {12.0 + $epsilon*100.0} ] ]
    set closeToken1 [java::new {ptolemy.data.FloatToken float} \
	    [expr {12.0 - (0.9 * $epsilon)}] ]

    #puts "5.7: $epsilon [$token1 toString] [$notCloseToken1 toString] \
    #	    [$closeToken1 toString]"
    set res1 [$token1 {isCloseTo ptolemy.data.Token} $notCloseToken1]
    set res2 [$token1 {isCloseTo ptolemy.data.Token} $closeToken1]
    set res3 [$notCloseToken1 {isCloseTo ptolemy.data.Token} $token1]
    set res4 [$closeToken1 {isCloseTo ptolemy.data.Token} $token1]

    java::field ptolemy.math.Complex EPSILON $oldEpsilon

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {false true false true}

test FloatToken-5.8 {Test closeness between floats around 0} {
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set token1 [java::new {ptolemy.data.FloatToken float} 0.0]
    set notCloseToken1 [java::new {ptolemy.data.FloatToken float} \
	    [expr {0.0 + 1.1 * $epsilon} ] ]
    set anotherNotCloseToken1 [java::new {ptolemy.data.FloatToken float} \
	    [expr {0.0 - 1.1 * $epsilon} ] ]
    set closeToken1 [java::new {ptolemy.data.FloatToken float} \
	    [expr {0.0 - 0.9 * $epsilon}] ]

    set res1 [$token1 {isCloseTo ptolemy.data.Token} $notCloseToken1]
    set res2 [$token1 {isCloseTo ptolemy.data.Token} $anotherNotCloseToken1]
    set res3 [$token1 {isCloseTo ptolemy.data.Token} $closeToken1]
    set res4 [$notCloseToken1 {isCloseTo ptolemy.data.Token} $token1]
    set res5 [$anotherNotCloseToken1 {isCloseTo ptolemy.data.Token} $token1]
    set res6 [$closeToken1 {isCloseTo ptolemy.data.Token} $token1]

    java::field ptolemy.math.Complex EPSILON $oldEpsilon

    list [$res1 toString] [$res2 toString] [$res3 toString] \
	    [$res4 toString] [$res5 toString] [$res6 toString]
} {false false true false false true}

test FloatToken-5.9 {Test closeness between a float and a String} {
    set FloatToken [java::new {ptolemy.data.FloatToken float} 12.0]
    set stringToken [java::new ptolemy.data.StringToken "12.0f"]
    catch {[$FloatToken {isCloseTo ptolemy.data.Token} $stringToken] toString} errMsg1
    catch {[$stringToken {isCloseTo ptolemy.data.Token} $FloatToken] toString} errMsg2
    list [lrange $errMsg2 0 10] [lrange $errMsg2 0 10]
} {true true}

test FloatToken-5.10 {Test closeness between floats and shorts.} {
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set tok1 [java::new {ptolemy.data.FloatToken float} \
	      [expr {12.0 + 0.5 * $epsilon} ]]
    set tok2 [java::new {ptolemy.data.ShortToken short} 12]

    set res1 [$tok1 {isCloseTo ptolemy.data.Token} $tok2]
    set res2 [$tok2 {isCloseTo ptolemy.data.Token} $tok1]

    java::field ptolemy.math.Complex EPSILON $oldEpsilon

    list [$res1 toString] [$res2 toString]
} {true true}


######################################################################
####
# Test modulo operator between floats and ints.
test FloatToken-6.0 {Test modulo between floats.} {
    set tok1 [java::new {ptolemy.data.FloatToken float} 12.2]
    set tok2 [java::new {ptolemy.data.FloatToken float} 2.2]

    set res1 [$tok1 modulo $tok1]
    set res2 [$tok1 moduloReverse $tok2]

    list [$res1 toString] [$res2 toString]
# We must account for roundoff error in floats.
} {0.0f 2.2000000476837f}
######################################################################
####
# 
test FloatToken-6.1 {Test modulo operator between floats and shorts.} {
    set tok1 [java::new {ptolemy.data.FloatToken float} 12.2]
    set tok2 [java::new {ptolemy.data.ShortToken short} 3]
    
    set res1 [$tok1 modulo $tok2]
    set res2 [$tok1 moduloReverse $tok2]

    set res3 [$tok2 modulo $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
# We must account for roundoff error in floats.
} {0.1999998092651f 3.0f 3.0f}

######################################################################
####
# Test multiply operator between floats and ints.
test FloatToken-7.0 {Test multiply operator between floats.} {
    set tok1 [java::new {ptolemy.data.FloatToken float} 12.2]
    set tok2 [java::new {ptolemy.data.FloatToken float} 2.2]

    set res1 [$tok1 multiply $tok1]
    set res2 [$tok1 multiplyReverse $tok2]

    list [$res1 toString] [$res2 toString]
# We must account for roundoff error in floats.
} {148.8399963378906f 26.8400001525879f}
######################################################################
####
# 
test FloatToken-7.1 {Test multiply operator between floats and shorts.} {
    set tok1 [java::new {ptolemy.data.FloatToken float} 12.2]
    set tok2 [java::new {ptolemy.data.ShortToken short} 3]
    
    set res1 [$tok1 multiply $tok2]
    set res2 [$tok1 multiplyReverse $tok2]

    set res3 [$tok2 multiply $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
# We must account for roundoff error in floats.
} {36.5999984741211f 36.5999984741211f 36.5999984741211f}

######################################################################
####
# Test subtract operator between floats.
test FloatToken-8.0 {Test subtract operator between floats.} {
    set tok1 [java::new {ptolemy.data.FloatToken float} 12.2]
    set tok2 [java::new {ptolemy.data.FloatToken float} 2.2]

    set res1 [$tok1 subtract $tok1]
    set res2 [$tok1 subtractReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {0.0f -10.0f}
######################################################################
####
# 
test FloatToken-8.1 {Test subtract operator between floats and shorts.} {
    set tok1 [java::new {ptolemy.data.FloatToken float} 12.2]
    set tok2 [java::new {ptolemy.data.ShortToken short} 3]
    
    set res1 [$tok1 subtract $tok2]
    set res2 [$tok1 subtractReverse $tok2]

    set res3 [$tok2 subtract $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
# We must account for roundoff error in floats.
} {9.1999998092651f -9.1999998092651f -9.1999998092651f}

######################################################################
####
# 
test FloatToken-11.0 {Test equals} {
    set t1 [java::new {ptolemy.data.FloatToken float} 3.5]
    set t2 [java::new {ptolemy.data.FloatToken float} 3.5]
    set t3 [java::new {ptolemy.data.FloatToken float} -8.0]
    list [$t1 equals $t1] [$t1 equals $t2] [$t1 equals $t3]
} {1 1 0}


######################################################################
####
# 
test FloatToken-11.1 {Test equals on nil} {
    set tu [java::field ptolemy.data.FloatToken NIL]
    set t2 [java::new ptolemy.data.FloatToken 2]
    set t [java::field ptolemy.data.Token NIL]
    list [$tu equals $tu] [$tu equals $t2] [$t2 equals $tu] \
	[$t2 equals $t2] [$t equals $tu] [$tu equals $t]
} {0 0 0 1 0 0} 

######################################################################
####
# 
test FloatToken-12.0 {Test hashCode} {
    set t1 [java::new {ptolemy.data.FloatToken float} 3.5]
    set t2 [java::new {ptolemy.data.FloatToken float} 3.5]
    set t3 [java::new {ptolemy.data.FloatToken float} -8.0]
    list [$t1 hashCode] [$t2 hashCode] [$t3 hashCode]
} {3 3 -8}

######################################################################
####
# 
test FloatToken-13.0 {Test convert from BooleanToken} {
    set t [java::new {ptolemy.data.BooleanToken boolean} false]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FloatToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.BooleanToken 'false' to the type float because the type of the token is higher or incomparable with the given type.}}

test FloatToken-13.1 {Test convert from UnsignedByteToken} {
    set t [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FloatToken convert $t] toString]} msg
    list $msg
} {1.0f}

test FloatToken-13.2 {Test convert from ComplexToken} {
    set o [java::new {ptolemy.math.Complex} 1.0 1.0]
    set t [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $o]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FloatToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.ComplexToken '1.0 + 1.0i' to the type float because the type of the token is higher or incomparable with the given type.}}

test FloatToken-13.3 {Test convert from FloatToken} {
    set t [java::new {ptolemy.data.FloatToken float} 1.0]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FloatToken convert $t] toString]} msg
    list $msg
} {1.0f}

test FloatToken-13.4 {Test convert from FixToken} {
    set t [java::new {ptolemy.data.FixToken java.lang.String} "fix(1.0,8,4)"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FloatToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.FixToken 'fix(1.0,8,4)' to the type float because the type of the token is higher or incomparable with the given type.}}

test FloatToken-13.5 {Test convert from IntToken} {
    set t [java::new {ptolemy.data.IntToken int} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FloatToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.IntToken '1' to the type float because the type of the token is higher or incomparable with the given type.}}

test FloatToken-13.6 {Test convert from LongToken} {
    set t [java::new {ptolemy.data.LongToken long} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FloatToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.LongToken '1L' to the type float because the type of the token is higher or incomparable with the given type.}}

test FloatToken-13.7 {Test convert from ShortToken} {
    set t [java::new {ptolemy.data.ShortToken short} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FloatToken convert $t] toString]} msg
    list $msg
} {1.0f}

test FloatToken-13.8 {Test convert from StringToken} {
    set t [java::new {ptolemy.data.StringToken java.lang.String} "One"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FloatToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.StringToken '"One"' to the type float because the type of the token is higher or incomparable with the given type.}}
    
######################################################################
####
# 
#test FloatToken-13.8 {Test convert from PetiteToken} {
#    set t [java::new {ptolemy.data.PetiteToken double} 0.1]
#    set msg {}
#    set result {}
#    catch {set result [[java::call ptolemy.data.FloatToken convert $t] toString]} msg
#    list $msg
#} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.PetiteToken '0.1p' to the type float.}}


######################################################################
####
# 
test FloatToken-16.0 {call leftShift and get coverage in the parent class} {
    set p [java::new {ptolemy.data.FloatToken float} 16.16]
    catch {$p leftShift 1} errMsg
    list $errMsg
# We must account for roundoff error in floats.
} {{ptolemy.kernel.util.IllegalActionException: leftShift operation not supported between ptolemy.data.FloatToken '16.1599998474121f' and ptolemy.data.IntToken '1'}}

######################################################################
####
# 
test FloatToken-17.0 {call logicalRightShift and get coverage in the parent class} {
    set p [java::new {ptolemy.data.FloatToken float} 16.16]
    catch {$p logicalRightShift 1} errMsg
    list $errMsg
# We must account for roundoff error in floats.
} {{ptolemy.kernel.util.IllegalActionException: logicalRightShift operation not supported between ptolemy.data.FloatToken '16.1599998474121f' and ptolemy.data.IntToken '1'}}

######################################################################
####
# 
test FloatToken-18.0 {call rightShift and get coverage in the parent class} {
    set p [java::new {ptolemy.data.FloatToken float} 16.16]
    catch {$p rightShift 1} errMsg
    list $errMsg
# We must account for roundoff error in floats.
} {{ptolemy.kernel.util.IllegalActionException: rightShift operation not supported between ptolemy.data.FloatToken '16.1599998474121f' and ptolemy.data.IntToken '1'}}
