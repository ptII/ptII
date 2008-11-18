# Tests for the FuzzyDoubleComparator class
#
# @Author: Jie Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998-2006 The Regents of the University of California.
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


######################################################################
#### Constructor and default threshold
#
test FuzzyDoubleComparator-2.1 {Construct a FuzzyDoubleComparator} {
    set fdc [java::new ptolemy.actor.util.FuzzyDoubleComparator]
    list [$fdc getThreshold]
} {1e-10}

test FuzzyDoubleComparator-2.2 {Construct with threshold} {
    set fdc [java::new \
	    ptolemy.actor.util.FuzzyDoubleComparator 1e-7]
    list [$fdc getThreshold]
} {1e-07}



######################################################################
#### set and get threshold
#
test FuzzyDoubleComparator-3.1 {Construct a FuzzyDoubleComparator} {
    $fdc setThreshold 1e-5
    list [$fdc getThreshold]
} {1e-05}

######################################################################
#### compare
#
test FuzzyDoubleComparator-4.1 {Construct a FuzzyDoubleComparator} {
    set zero [java::new java.lang.Double 0.0]
    set one [java::new java.lang.Double 1.0]
    set minusone [java::new java.lang.Double -1.0]
    set little [java::new java.lang.Double 1e-6]
    list [$fdc compare $minusone $zero] [$fdc compare $one $zero] \
	    [$fdc compare $little $zero]
} {-1 1 0}
