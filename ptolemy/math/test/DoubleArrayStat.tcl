# Tests for the DoubleArrayStat Class
#
# @Author: Jeff Tsay
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
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

# NOTE: there is way too much resolution in these numeric tests.
#  The results are unlikely to be the same on all platforms.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

set PI [java::field java.lang.Math PI]

proc javaPrintArray {javaArrayObj} {
    set result {}
    for {set i 0} {$i < [$javaArrayObj length]} {incr i} {
	lappend result [[$javaArrayObj get $i] toString]
    }
    return $result
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

set a0 [java::new {double[]} 0]
set a1 [java::new {double[]} 1 [list 0.043]]
set a2 [java::new {double[]} 5 [list 236.1 -36.21 4826.2 5.0 65.4]]
set a4 [java::new {double[]} 5 [list 23.7 -0.00367 4826.2 5.0 0.000654]]
set l [list 1 2 -3 4.1 0.0 -0.0 +0.0 \
	    [java::field java.lang.Double POSITIVE_INFINITY] \
	    [java::field java.lang.Double NEGATIVE_INFINITY] \
	    [java::field java.lang.Double NaN] \
	    [java::field java.lang.Double MIN_VALUE] \
	    [java::field java.lang.Double MAX_VALUE] \
	    ]
set a3 [java::new {double[]} [llength $l] $l]
set p1 [java::new {double[]} 4 [list 0.3 0.2 0.0 0.5]]
set p2 [java::new {double[]} 4 [list 0.7 0.1 0.0 0.2]]
set p3 [java::new {double[]} 4 [list 0.4 0.4 0.1 0.1]]
set badp3 [java::new {double[]} 4 [list 0.4 0.4 -0.1 0.2]]
set p4 [java::new {double[]} 3 [list 0.4 0.4 0.1]]

####################################################################
test DoubleArrayStat-1.1 {entropy} {
    set r [java::call ptolemy.math.DoubleArrayStat entropy $p1]
    set br [java::call ptolemy.math.SignalProcessing close $r \
            1.485475297]
    list $br
} {1}

####################################################################
test DoubleArrayStat-1.2 {entropy of empty array} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat entropy $a0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.entropy() : input array has length 0.}}

####################################################################
test DoubleArrayStat-1.3 {entropy bad p} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat entropy $badp3]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.entropy() : Negative probability encountered.}}

####################################################################
test DoubleArrayStat-1.3.5 {geometricMean} {
    set r0 [java::call ptolemy.math.DoubleArrayStat geometricMean $a0]
    set r1 [java::call ptolemy.math.DoubleArrayStat geometricMean $a1]
    # r2 has negative values
    set r2 [java::call ptolemy.math.DoubleArrayStat geometricMean $a2]
    
    set r3 [java::call ptolemy.math.DoubleArrayStat geometricMean $p3]
    list $r0 $r1 $r2 $r3
}  {1.0 0.043 NaN 0.2}

####################################################################
test DoubleArrayStat-1.4.1 {min} {
    set r [java::call ptolemy.math.DoubleArrayStat min $a2]
    list $r
} -36.21

####################################################################
test DoubleArrayStat-1.4.2 {min with array with NaN in it} {
    # 20.11.27 of the Java Language Spec says that If either value 
    # passed to Math.min() is NaN, the the result is NaN
    set r [java::call ptolemy.math.DoubleArrayStat min $a3]
    list $r
} -Infinity

####################################################################
test DoubleArrayStat-1.4.3 {min of empty aray} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat min $a0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.minAndIndex() : input array has length 0.}}

####################################################################
test DoubleArrayStat-2.1 {max} {
    set r [java::call ptolemy.math.DoubleArrayStat max $a2]
    list $r
}  4826.2

####################################################################
test DoubleArrayStat-2.2 {max of empty aray} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat max $a0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.maxAndIndex() : input array has length 0.}}
  
####################################################################
test DoubleArrayStat-3.1 {mean} {
    set r [java::call ptolemy.math.DoubleArrayStat mean $a2]
    list $r
}  1019.298

####################################################################
test DoubleArrayStat-3.1.1 {mean of empty array} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat mean $a0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.mean() : input array has length 0.}}

####################################################################
test DoubleArrayStat-3.1.2 {mean of null array, test _nonZeroLength} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat mean \
	    [java::null]]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.mean() : input array is null.}}

####################################################################
test DoubleArrayStat-3.2 {productOfElements} {
    set r [java::call ptolemy.math.DoubleArrayStat productOfElements $a4]
    epsilonDiff [list $r] [list -1.37267422284600]
} {}

####################################################################
test DoubleArrayStat-1.1 {relativeEntropy} {
    set r [java::call ptolemy.math.DoubleArrayStat relativeEntropy $p1 \
           $p2]
    set br [java::call ptolemy.math.SignalProcessing close $r \
            0.49424632141]
    set rl [java::new {double[]} 1 [list $r]]
    epsilonDiff [list $r] [list 0.49424632141]
} {}

####################################################################
test DoubleArrayStat-1.2 {relativeEntropy two empty arrays} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat relativeEntropy $a0 \
	    $a0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.relativeEntropy() : input array has length 0.}}

####################################################################
test DoubleArrayStat-1.3 {relativeEntropy unequally sized arrays} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat relativeEntropy $p3 \
	    $p4]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.relativeEntropy() : input arrays must have the same length, but the first array has length 4 and the second array has length 3.}}

####################################################################
test DoubleArrayStat-1.4 {relativeEntropy bad p} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat relativeEntropy \
$badp3 $p3]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.relativeEntropy() : Negative probability encountered.}}

####################################################################
test DoubleArrayStat-1.5 {relativeEntropy bad q} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat relativeEntropy $p3 \
	    $badp3]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.relativeEntropy() : Negative probability encountered.}}

####################################################################
test DoubleArrayStat-3.1 {standardDeviation} {
    set r [java::call ptolemy.math.DoubleArrayStat standardDeviation $a2]
    set er [java::call ptolemy.math.DoubleArrayStat standardDeviation $a2 false]
    epsilonDiff [list $r] [list $er]
} {}

####################################################################
test DoubleArrayStat-3.1 {standardDeviation sample true} {
    set r [java::call ptolemy.math.DoubleArrayStat standardDeviation $a2 true]
    epsilonDiff [list $r] {2130.652535614383}
} {}

####################################################################
test DoubleArrayStat-3.2 {standardDeviation sample false} {
    set r [java::call ptolemy.math.DoubleArrayStat standardDeviation $a2 false]
    epsilonDiff [list $r] {1905.713562426421}
} {}

####################################################################
test DoubleArrayStat-3.3 {standardDeviation sample true, empty array} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat standardDeviation $a0 true]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.variance() : input array has length 0.}}

####################################################################
test DoubleArrayStat-3.4 {standardDeviation sample true, length 1 array} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat standardDeviation $a1 true]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.variance() : sample variance and standard deviation of an array of length less than 2 are not defined.}}

####################################################################
test DoubleArrayStat-3.5 {standardDeviation sample false, length 1 array} {
    set r [java::call ptolemy.math.DoubleArrayStat standardDeviation $a1 false]
    epsilonDiff [list $r] {0.0}
} {}

####################################################################
test DoubleArrayStat-3.1 {sumOfElements} {
    set r [java::call ptolemy.math.DoubleArrayStat sumOfElements $a2]
    list $r
} 5096.49

####################################################################
test DoubleArrayStat-3.1 {sumOfElements empty array} {
    set r [java::call ptolemy.math.DoubleArrayStat sumOfElements $a0]
    list $r
} 0.0

####################################################################
test DoubleArrayStat-3.1 {variance sample} {
    set r [java::call ptolemy.math.DoubleArrayStat variance $a2]
    set er [java::call ptolemy.math.DoubleArrayStat variance $a2 false] 
    epsilonDiff [list $r] [list $er]
} {}

####################################################################
test DoubleArrayStat-3.1 {variance sample true} {
    set r [java::call ptolemy.math.DoubleArrayStat variance $a2 true]
    epsilonDiff [list $r] {4539680.227519999}
} {}

####################################################################
test DoubleArrayStat-3.2 {variance sample false} {
    set r [java::call ptolemy.math.DoubleArrayStat variance $a2 false]
    epsilonDiff [list $r] {3631744.182016002}
} {}

####################################################################
test DoubleArrayStat-3.3 {variance sample true, empty array} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat variance $a0 true]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.variance() : input array has length 0.}}

####################################################################
test DoubleArrayStat-3.4 {variance sample true, length 1 array} {
    catch {set r [java::call ptolemy.math.DoubleArrayStat variance $a1 true]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayStat.variance() : sample variance and standard deviation of an array of length less than 2 are not defined.}}

####################################################################
test DoubleArrayStat-3.5 {variance sample false, length 1 array} {
    set r [java::call ptolemy.math.DoubleArrayStat variance $a1 false]
    epsilonDiff [list $r] {0.0}
} {}

####################################################################
test DoubleArrayStat-4.1 {randomGaussian should be different} {
    # Zoltan Kemenczy writes:
    # The behaviour of randomGaussian() et al. functions in
    # ptolemy.math.DoubleArrayStat depends on how much time elapses between
    # subsequent calls...  Specifically if multiple calls are made within a java
    # system time "unit", the same result will be returned since the seed for
    # java.util.Random() is the same.
    # 
    # Here is a small java example that illustrates this problem:
    # ---------------------------------------
    # import ptolemy.math.DoubleArrayStat;
    #
    #class tmp {
    #    public static void main(String[] args) {
    #        double[] v1 = DoubleArrayStat.randomGaussian(0.0,1.0,10);
    #        double[] v2 = DoubleArrayStat.randomGaussian(0.0,1.0,10);
    #        for (int i = 0; i < v1.length; i++)
    #            System.out.print(v1[i]);
    #        System.out.println();
    #        for (int i = 0; i < v1.length; i++)
    #            System.out.print(v2[i]);
    #    }
    #   }
    #  ----------------------------------------
    #
    #   It would be maybe better if there was a single:
    #   
    #   static Random random = new Random();
    # 
    # in ptolemy.math.DoubleArrayStat and all DoubleArrayStat randomXXX()
    # functions referred to it?


    # Note that this bug only appears under Windows?
    set v1 [java::call ptolemy.math.DoubleArrayStat randomGaussian 0.0 1.0 10]
    set v2 [java::call ptolemy.math.DoubleArrayStat randomGaussian 0.0 1.0 10]
    set r1 [$v1 getrange 0]
    set r2 [$v2 getrange 0]
    expr {$r1 != $r2}
} {1}


####################################################################
test DoubleArrayStat-5.1 {crosscorrelation} {
    set v1 [java::call ptolemy.math.DoubleArrayStat crossCorrelation \
	    $p1 $p2 4 0 0]
    set r1 [$v1 getrange ]
    set v2 [java::call ptolemy.math.DoubleArrayStat crossCorrelation \
	    $p1 $p2 10 2 3]
    set r2 [$v2 getrange ]
    list $r1 $r2 
} {0.33 {0.04 0.06}}


####################################################################
test DoubleArrayStat-5.1 {crosscorrelationAt, no start lag} {
    set r1 [java::call ptolemy.math.DoubleArrayStat crossCorrelationAt \
	    $p1 $p2 4 0]
    set r2 [java::call ptolemy.math.DoubleArrayStat crossCorrelationAt \
	    $p1 $p2 10 3]
    list $r1 $r2 
} {0.33 0.06}


####################################################################
test DoubleArrayStat-6.1.1 {autoCorrelation} {
    set v1 [java::call ptolemy.math.DoubleArrayStat autoCorrelation $p1 4 0 0]
    set r1 [$v1 getrange ]
    set v2 [java::call ptolemy.math.DoubleArrayStat autoCorrelation $p1 10 2 3]
    set r2 [$v2 getrange ]
    list $r1 $r2 
} {0.38 {0.1 0.15}}

####################################################################
test DoubleArrayStat-6.1.2 {autoCorrelationAt without startlag and endlag} {
    set v1 [java::call ptolemy.math.DoubleArrayStat autoCorrelationAt $p1 4 0]
    list $v1 
} {0.38}


####################################################################
test DoubleArrayStat-7.1 {randomBernoulli} {
    set a1 [java::call ptolemy.math.DoubleArrayStat randomBernoulli 0.5 100]
    set a2 [java::call ptolemy.math.DoubleArrayStat randomBernoulli 0.5 100]
    # 100 randoms should not be the same
    list [$a1 length] \
	[$a2 length] \
	[expr {[string compare [$a1 getrange] [$a2 getrange]] == 0}]
} {100 100 0}

####################################################################
test DoubleArrayStat-8.1 {randomExponential} {
     set a1 [java::call ptolemy.math.DoubleArrayStat randomExponential 0.5 100]
     set a2 [java::call ptolemy.math.DoubleArrayStat randomExponential 0.5 100]
     # 100 randoms should not be the same
     list [$a1 length] \
	[$a2 length] \
	[expr {[string compare [$a1 getrange] [$a2 getrange]] == 0}]
} {100 100 0}

####################################################################
test DoubleArrayStat-9.1 {randomPoisson} {
    set a1 [java::call ptolemy.math.DoubleArrayStat randomPoisson 0.5 100]
    set a2 [java::call ptolemy.math.DoubleArrayStat randomPoisson 0.5 100]
    # 100 randoms should not be the same
    list [$a1 length] \
	[$a2 length] \
	[expr {[string compare [$a1 getrange] [$a2 getrange]] == 0}]
} {100 100 0}

####################################################################
test DoubleArrayStat-10.1 {randomUniform} {
    set a1 [java::call ptolemy.math.DoubleArrayStat randomUniform \
	-1.0 100.0 100]
    set a2 [java::call ptolemy.math.DoubleArrayStat randomUniform \
	-1.0 100.0 100]
    # 100 randoms should not be the same
    list [$a1 length] \
	[$a2 length] \
	[expr {[java::call ptolemy.math.DoubleArrayStat max $a1] <= 100.0}] \
	[expr {[java::call ptolemy.math.DoubleArrayStat min $a1] >= -1.0}] \
	[expr {[java::call ptolemy.math.DoubleArrayStat max $a2] <= 100.0}] \
	[expr {[java::call ptolemy.math.DoubleArrayStat min $a2] >= -1.0}] \
	[expr {[string compare [$a1 getrange] [$a2 getrange]] == 0}]
} {100 100 1 1 1 1 0}
