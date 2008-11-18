# Test CartesianToComplex.
#
# @Author: Michael Leung
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
#### Test CartesianToComplex in an SDF model
#
test CartesianToComplex-1.1 {test 1} {
    set e0 [sdfModel 1]
    set const1 [java::new ptolemy.actor.lib.Const $e0 const1]
    set const2 [java::new ptolemy.actor.lib.Const $e0 const2]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set conver [java::new ptolemy.actor.lib.conversions.CartesianToComplex \
                    $e0 conver]

    set value1 [getParameter $const1 value]
    $value1 setToken [java::new {ptolemy.data.DoubleToken double} 5.1]
    set value2 [getParameter $const2 value]
    $value2 setToken [java::new {ptolemy.data.DoubleToken double} 6.2]


    $e0 connect \
          [java::field [java::cast ptolemy.actor.lib.Source $const1] output] \
            [java::field $conver x]

    $e0 connect \
          [java::field [java::cast ptolemy.actor.lib.Source $const2] output] \
            [java::field $conver y]

    $e0 connect \
            [java::field $conver output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]

} {{5.1 + 6.2i}}
