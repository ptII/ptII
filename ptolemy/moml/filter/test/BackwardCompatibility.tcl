# Tests for the BackwardCompatibility class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002-2007 The Regents of the University of California.
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

# Increase timeout from the default set in $PTII/util/testsuite/testDefs.tcl
set timeOutSeconds 6000

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}


set constMoml  "$header 
<entity name=\"BackwardCompatibilityConst\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">
  </entity>
</entity>"

######################################################################
####
#
test BackwardCompatibility-1.1 {Const: added an _icon} { 
    # This test is sort of pointless, since we add the Const _icon
    # and then remove it.  If we don't remove, this test will not run under
    # the nightly build
    
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
    java::call ptolemy.moml.MoMLParser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    java::call ptolemy.moml.MoMLParser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]

    set toplevel [$parser parse $constMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityConst" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <entity name="Const" class="ptolemy.actor.lib.Const">
    </entity>
</entity>
}}

set mathFunctionMoml  "$header 
<entity name=\"BackwardCompatibilityMathFunction\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"MathFunction\" class=\"ptolemy.actor.lib.MathFunction\">
  </entity>
</entity>"

test BackwardCompatibility-3.1 {MathFunction} { 
    # This test is sort of pointless, since we add the _icon
    # and then remove it.  If we don't remove, this test will not run under
    # the nightly build
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $mathFunctionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityMathFunction" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <entity name="MathFunction" class="ptolemy.actor.lib.MathFunction">
    </entity>
</entity>
}}


set scaleMoml  "$header 
<entity name=\"BackwardCompatibilityScale\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"Scale\" class=\"ptolemy.actor.lib.Scale\">
  </entity>
</entity>"

test BackwardCompatibility-4.1 {Scale} { 
    # This test is sort of pointless, since we add the _icon
    # and then remove it.  If we don't remove, this test will not run under
    # the nightly build

    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $scaleMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityScale" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <entity name="Scale" class="ptolemy.actor.lib.Scale">
    </entity>
</entity>
}}

set trigFunctionMoml  "$header 
<entity name=\"BackwardCompatibilityTrigFunction\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"TrigFunction\" class=\"ptolemy.actor.lib.TrigFunction\">
  </entity>
</entity>"

test BackwardCompatibility-5.1 {TrigFunction} { 
    # This test is sort of pointless, since we add the _icon
    # and then remove it.  If we don't remove, this test will not run under
    # the nightly build

    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $trigFunctionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityTrigFunction" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <entity name="TrigFunction" class="ptolemy.actor.lib.TrigFunction">
    </entity>
</entity>
}}


######################################################################
####
#

set complexToCartesianMoml  "$header 
<entity name=\"BackwardCompatibilityComplextToCartesian\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"ComplexToCartesian1\" class=\"ptolemy.actor.lib.conversions.ComplexToCartesian\">
        <port name=\"input\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
        </port>
        <port name=\"real\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
        <port name=\"imag\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
    </entity>
    <entity name=\"CartesianToComplex2\" class=\"ptolemy.actor.lib.conversions.CartesianToComplex\">
        <port name=\"real\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
        </port>
        <port name=\"imag\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
        </port>
        <port name=\"output\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
    </entity>
</entity>"

test BackwardCompatibility-6.1 {ComplexToCartesian: port name change} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $complexToCartesianMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityComplextToCartesian" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <entity name="ComplexToCartesian1" class="ptolemy.actor.lib.conversions.ComplexToCartesian">
    </entity>
    <entity name="CartesianToComplex2" class="ptolemy.actor.lib.conversions.CartesianToComplex">
    </entity>
</entity>
}}


######################################################################
####
#

set htvqEncodeMoml  "$header 
<entity name=\"BackwardCompatibilityComplextToCartesian\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"HTVQEncode1\" class=\"ptolemy.domains.sdf.lib.vq.HTVQEncode\">
        <property name=\"codeBook\" class=\"ptolemy.data.expr.Parameter\" value=\"&quot;/ptolemy/domains/sdf/lib/vq/data/usc_hvq_s5.dat&quot;\">
        </property>
        <property name=\"blockCount\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">
        </property>
        <property name=\"blockWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"4\">
        </property>
        <property name=\"blockHeight\" class=\"ptolemy.data.expr.Parameter\" value=\"2\">
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"244.0, 124.0\">
        </property>
        <port name=\"input\" class=\"ptolemy.domains.sdf.kernel.SDFIOPort\">
            <property name=\"input\"/>
            <property name=\"tokenConsumptionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">
            </property>
            <property name=\"tokenInitProduction\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
            <property name=\"tokenProductionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
        </port>
        <port name=\"output\" class=\"ptolemy.domains.sdf.kernel.SDFIOPort\">
            <property name=\"output\"/>
            <property name=\"tokenConsumptionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
            <property name=\"tokenInitProduction\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
            <property name=\"tokenProductionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">
            </property>
        </port>
    </entity>
</entity>"

test BackwardCompatibility-7.1 {HTVQEncode: Property Class Change} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $htvqEncodeMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityComplextToCartesian" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <entity name="HTVQEncode1" class="ptolemy.domains.sdf.lib.vq.HTVQEncode">
        <property name="codeBook" class="ptolemy.data.expr.Parameter" value="&quot;/ptolemy/domains/sdf/lib/vq/data/usc_hvq_s5.dat&quot;">
        </property>
        <property name="blockCount" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="blockWidth" class="ptolemy.data.expr.Parameter" value="4">
        </property>
        <property name="blockHeight" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="244.0, 124.0">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="tokenConsumptionRate" class="ptolemy.data.expr.Parameter" value="1">
            </property>
            <property name="tokenInitProduction" class="ptolemy.data.expr.Parameter" value="0">
            </property>
            <property name="tokenProductionRate" class="ptolemy.data.expr.Parameter" value="0">
            </property>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
            <property name="tokenProductionRate" class="ptolemy.data.expr.Parameter" value="1">
            </property>
            <property name="tokenConsumptionRate" class="ptolemy.data.expr.Parameter" value="0">
            </property>
            <property name="tokenInitProduction" class="ptolemy.data.expr.Parameter" value="0">
            </property>
        </port>
    </entity>
</entity>
}}



######################################################################
####
#

set expressionMoml  "$header 
<entity name=\"expressionProperty\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"SDFDirector\" class=\"ptolemy.domains.sdf.kernel.SDFDirector\">
        <property name=\"iterations\" class=\"ptolemy.data.expr.Parameter\" value=\"5\">
        </property>
        <property name=\"vectorizationFactor\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"73.0, 25.0\">
        </property>
    </property>
    <entity name=\"slow\" class=\"ptolemy.actor.lib.Ramp\">
        <property name=\"firingCountLimit\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
        </property>
        <property name=\"init\" class=\"ptolemy.data.expr.Parameter\" value=\"0.0\">
        </property>
        <property name=\"step\" class=\"ptolemy.data.expr.Parameter\" value=\"PI/100.0\">
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"63.0, 113.0\">
        </property>
        <port name=\"output\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
        <port name=\"trigger\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
            <property name=\"multiport\"/>
        </port>
        <port name=\"step\" class=\"ptolemy.actor.parameters.ParameterPort\">
            <property name=\"input\"/>
        </port>
    </entity>
    <entity name=\"fast\" class=\"ptolemy.actor.lib.Ramp\">
        <property name=\"firingCountLimit\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
        </property>
        <property name=\"init\" class=\"ptolemy.data.expr.Parameter\" value=\"0.0\">
        </property>
        <property name=\"step\" class=\"ptolemy.data.expr.Parameter\" value=\"PI/10.0\">
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"63.0, 200.0\">
        </property>
        <port name=\"output\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
        <port name=\"trigger\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
            <property name=\"multiport\"/>
        </port>
        <port name=\"step\" class=\"ptolemy.actor.parameters.ParameterPort\">
            <property name=\"input\"/>
        </port>
    </entity>
    <entity name=\"Expression\" class=\"ptolemy.actor.lib.Expression\">
        <property name=\"expression\" class=\"ptolemy.data.expr.Parameter\" value=\"cos(slow) + cos(fast)\">
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"202.0, 191.0\">
        </property>
        <port name=\"output\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
        <port name=\"slow\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
        </port>
        <port name=\"fast\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
        </port>
    </entity>
    <entity name=\"FileWriter\" class=\"ptolemy.actor.lib.FileWriter\">
        <property name=\"filename\" class=\"ptolemy.data.expr.Parameter\" value=\"&quot;&quot;\">
        </property>
        <doc>Write to a file</doc>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"428.0, 205.0\">
        </property>
        <port name=\"input\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
            <property name=\"multiport\"/>
        </port>
    </entity>
    <relation name=\"_R0\" class=\"ptolemy.actor.TypedIORelation\">
    </relation>
    <relation name=\"_R1\" class=\"ptolemy.actor.TypedIORelation\">
    </relation>
    <relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">
    </relation>
    <link port=\"slow.output\" relation=\"_R0\"/>
    <link port=\"fast.output\" relation=\"_R1\"/>
    <link port=\"Expression.output\" relation=\"relation\"/>
    <link port=\"Expression.slow\" relation=\"_R0\"/>
    <link port=\"Expression.fast\" relation=\"_R1\"/>
    <link port=\"FileWriter.input\" relation=\"relation\"/>
</entity>"

test BackwardCompatibility-7.2 {Expression: Property Class Change} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $expressionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="expressionProperty" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="7.2.devel">
    </property>
    <property name="SDFDirector" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="5">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="73.0, 25.0">
        </property>
    </property>
    <entity name="slow" class="ptolemy.actor.lib.Ramp">
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="init" class="ptolemy.data.expr.Parameter" value="0.0">
        </property>
        <property name="step" class="ptolemy.actor.parameters.PortParameter" value="PI/100.0">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="63.0, 113.0">
        </property>
    </entity>
    <entity name="fast" class="ptolemy.actor.lib.Ramp">
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="init" class="ptolemy.data.expr.Parameter" value="0.0">
        </property>
        <property name="step" class="ptolemy.actor.parameters.PortParameter" value="PI/10.0">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="63.0, 200.0">
        </property>
    </entity>
    <entity name="Expression" class="ptolemy.actor.lib.Expression">
        <property name="expression" class="ptolemy.kernel.util.StringAttribute" value="cos(slow) + cos(fast)">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="202.0, 191.0">
        </property>
        <port name="slow" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="fast" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
    </entity>
    <entity name="FileWriter" class="ptolemy.actor.lib.FileWriter">
        <property name="filename" class="ptolemy.data.expr.Parameter" value="&quot;&quot;">
        </property>
        <doc>Write to a file</doc>
        <property name="_location" class="ptolemy.kernel.util.Location" value="428.0, 205.0">
        </property>
    </entity>
    <relation name="_R0" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="_R1" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="slow.output" relation="_R0"/>
    <link port="fast.output" relation="_R1"/>
    <link port="Expression.output" relation="relation"/>
    <link port="Expression.slow" relation="_R0"/>
    <link port="Expression.fast" relation="_R1"/>
    <link port="FileWriter.input" relation="relation"/>
</entity>
}}

######################################################################
####
#

set editorFactoryMoml  "$header 
<entity name=\"BackwardCompatibilityEditorFactor\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"lambda\" class=\"ptolemy.data.expr.Parameter\" value=\"25.0\">
        <property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\">
        </property>
        <property name=\"lambda\" class=\"ptolemy.vergil.icon.ValueIcon\">
        </property>
        <property name=\"_smallIconDescription\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">
            <configure>
      <svg>
        <text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\">-P-</text>
      </svg>
    </configure>
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"355.0, 200.0\">
        </property>
    </property>
</entity>"

test BackwardCompatibility-8.1 {Is a parameter, does not have _editorFactory} { 

    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $editorFactoryMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityEditorFactor" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <property name="lambda" class="ptolemy.data.expr.Parameter" value="25.0">
        <property name="_hideName" class="ptolemy.kernel.util.SingletonAttribute">
        </property>
        <property name="lambda" class="ptolemy.kernel.util.Attribute">
        </property>
        <property name="_smallIconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
      <svg>
        <text x="20" style="font-size:14; font-family:SansSerif; fill:blue" y="20">-P-</text>
      </svg>
    </configure>
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="355.0, 200.0">
        </property>
        <property name="_editorFactory" class="ptolemy.kernel.util.Attribute">
        </property>
    </property>
</entity>
}}

set annotationMoml  "$header 
<entity name=\"BackwardCompatibilityEditorFactor\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"0:annotation1\" class=\"ptolemy.kernel.util.Attribute\">
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"426.0, 80.0\">
        </property>
        <property name=\"_iconDescription\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">
            <configure>
           <svg>
           <text x=\"20\" style=\"font-size:14; font-family:sanserif; fill:blue\" y=\"20\">This model shows a nonlinear feedback
system that exhibits chaotic behavior.
It is modeled in continuous time. The
CT director uses a sophisticated
ordinary differential equation solver
to execute the model. This particular
model is known as a Lorenz attractor.</text>
           </svg>
           </configure>
        </property>
    </property>
</entity>
"

test BackwardCompatibility-9.1 {annotation named annotation1 without a _hideName} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $annotationMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityEditorFactor" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <property name="0:annotation1" class="ptolemy.kernel.util.Attribute">
        <property name="_location" class="ptolemy.kernel.util.Location" value="426.0, 80.0">
        </property>
        <property name="_iconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
           <svg>
           <text x="20" style="font-size:14; font-family:sanserif; fill:blue" y="20">This model shows a nonlinear feedback
system that exhibits chaotic behavior.
It is modeled in continuous time. The
CT director uses a sophisticated
ordinary differential equation solver
to execute the model. This particular
model is known as a Lorenz attractor.</text>
           </svg>
           </configure>
        </property>
        <property name="_hideName" class="ptolemy.data.expr.SingletonParameter" value="true">
        </property>
    </property>
</entity>
}}


set pnDirectorMoml  "$header 
<entity name=\"PnDirectoryMoML\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"Process Network Director\" class=\"ptolemy.domains.pn.kernel.PNDirector\">
        <property name=\"Initial_queue_capacity\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">
        </property>
    </property>
</entity>
"


test BackwardCompatibility-10.1 {PNDirectory parameter named Initial_queue_capacity} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $pnDirectorMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="PnDirectoryMoML" class="ptolemy.actor.TypedCompositeActor">
    <property name="Process Network Director" class="ptolemy.domains.pn.kernel.PNDirector">
        <property name="initialQueueCapacity" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </property>
</entity>
}}

test BackwardCompatiblity-11.1 {Call toString on all the filters} {
    set filters [java::call ptolemy.moml.MoMLParser getMoMLFilters]
    # listToStrings is defined in  util/testsuite/enums.tcl
    # The toString output is rather voluminous, so we just check that
    # it is more than 1000 chars.
    expr {[string length [listToStrings $filters]] > 1000}
} {1}

test BackwardCompatiblity-11.2 {Call BackwardCompatibility.toString} {
    # This is a little strange because when we call 
    # BackwardCompatibility.allFilters(), we add the individual filters 
    # so when we call toString in the test above, we never actually
    # call BackwardCompatibility.toString()

    # Ideally, we would like to make toString() static, but we
    # can't do that because Object.toString() is not static
    set bc [java::new ptolemy.moml.filter.BackwardCompatibility]
    # The toString output is rather voluminous, so we just check that
    # it is more than 1000 chars.
    expr {[string length [$bc toString]] > 1000}
} {1}

set testMoML "$header
<entity name=\"testMoML\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"test\" class=\"ptolemy.actor.lib.Test\">
        <port name=\"input\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
            <property name=\"multiport\"/>
        </port>
    </entity>
</entity>"

test BackwardCompatibility-12.1 {Setting a multiport can result in a null container}  {
    # The MoML fragment above came from actor/lib/test/Test.tcl,
    # where we created a model that had a Ramp connected to a Test actor
    # exporting the MoML and then importing it resulted in a null pointer
    # exception in GRColorChanges.  
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $testMoML]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="testMoML" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="4.1">
    </property>
    <entity name="test" class="ptolemy.actor.lib.Test">
    </entity>
</entity>
}}


# If you create a DirectoryListing actor in Ptolemy II 4.0.1 and
# enabling displaying port names, then you get something like:
set testMoML "$header
<entity name=\"testMoML\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"test\" class=\"ptolemy.actor.lib.io.DirectoryListing\">
    <port name=\"directoryOrURL\" class=\"ptolemy.actor.TypedIOPort\">
        <property name=\"input\"/>
        <property name=\"_showName\" class=\"ptolemy.kernel.util.SingletonAttribute\">
            </property>
    </port>
    </entity>
</entity>"

test BackwardCompatibility-13.1 {DirectoryListing}  {
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $testMoML]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="testMoML" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="5.0-alpha">
    </property>
    <entity name="test" class="ptolemy.actor.lib.io.DirectoryListing">
        <port name="directoryOrURL" class="ptolemy.actor.parameters.ParameterPort">
            <property name="input"/>
            <property name="_showName" class="ptolemy.kernel.util.SingletonAttribute">
            </property>
        </port>
    </entity>
</entity>
}}

set testMoML "$header
<entity name=\"testMoML\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"ModelReference\" class=\"ptolemy.actor.lib.hoc.ModelReference\">
        <property name=\"modelFileOrURL\" class=\"ptolemy.data.expr.FileParameter\" value=\"\$PTII/ptolemy/actor/lib/test/auto/Ramp1.xml\">
        </property>
    </entity>
    <entity name=\"TestRamp\" class=\"ptolemy.moml.filter.test.TestRamp\">
    </entity>
</entity>"

test BackwardCompatibility-13.2 {ModelReference modelFileOrURL parameter change and an Icon: check that the modified flag is set}  {

    # The problem here is that loading an Icon calls setContext() which
    # calls reset() which sets the modified flag to false.
    # Thus, loading a model that has a compatibility change and
    # an icon results in the model not being marked as modified

    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers

    # Test out the modified flag
    $parser reset
    set modified [java::call ptolemy.moml.MoMLParser isModified]

    set toplevel [$parser parse $testMoML]
    set newMoML [$toplevel exportMoML]

    list $modified [java::call ptolemy.moml.MoMLParser isModified] $newMoML
} {0 1 {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="testMoML" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="5.1-devel">
    </property>
    <entity name="ModelReference" class="ptolemy.actor.lib.hoc.ModelReference">
        <property name="modelFileOrURL" class="ptolemy.actor.parameters.FilePortParameter" value="$PTII/ptolemy/actor/lib/test/auto/Ramp1.xml">
        </property>
    </entity>
    <entity name="TestRamp" class="ptolemy.moml.filter.test.TestRamp">
    </entity>
</entity>
}}


set testMoML "$header
<entity name=\"testMoML\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"ModelReference\" class=\"ptolemy.actor.lib.hoc.ModelReference\">
        <property name=\"modelFileOrURL\" class=\"ptolemy.data.expr.FileParameter\" value=\"\$PTII/ptolemy/actor/lib/test/auto/Ramp1.xml\">
        </property>
    </entity>
    <input source=\"TestRampIcon.xml\"/>
</entity>"

test BackwardCompatibility-13.3 {The input directive also calls setContext() which overwrites the modified flag}  {

    # The problem here is that the input line calls setContext() which
    # calls reset() which sets the modified flag to false.
    # Thus, loading a model that has a compatibility change and
    # an icon results in the model not being marked as modified

    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers

    # Test out the modified flag
    $parser reset
    set modified [java::call ptolemy.moml.MoMLParser isModified]

    set toplevel [$parser parse $testMoML]
    set newMoML [$toplevel exportMoML]

    list $modified [java::call ptolemy.moml.MoMLParser isModified] $newMoML
} {0 1 {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="testMoML" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="5.1-devel">
    </property>
    <property name="TestRampIcon" class="ptolemy.kernel.attributes.VersionAttribute">
    </property>
    <entity name="ModelReference" class="ptolemy.actor.lib.hoc.ModelReference">
        <property name="modelFileOrURL" class="ptolemy.actor.parameters.FilePortParameter" value="$PTII/ptolemy/actor/lib/test/auto/Ramp1.xml">
        </property>
    </entity>
</entity>
}}

set testMoML "$header
    <entity name=\"ModalModel\" class=\"ptolemy.domains.fsm.modal.ModalModel\">
        <property name=\"_tableauFactory\" class=\"ptolemy.kernel.util.Attribute\">
        </property>
    </entity>
"

test BackwardCompatibility-14.1 {ModalModel _tableauFactory changed} {
    # This test is sort of pointless, since we add ModalModel in 
    # in PropertyClassChanges and then remove it in RemoveGraphical
    # classes.
    # However, the parser will be marked as modified even though
    # the result is unchanged.  This is right, since we are running
    # two filters back to back, one of which changes the results,
    # the other which changes it back.

    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers

    # Test out the modified flag
    $parser reset
    set modified [java::call ptolemy.moml.MoMLParser isModified]

    set toplevel [$parser parse $testMoML]
    set newMoML [$toplevel exportMoML]

    list $modified [java::call ptolemy.moml.MoMLParser isModified] $newMoML
} {0 1 {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="ModalModel" class="ptolemy.domains.fsm.modal.ModalModel">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="6.1.devel">
    </property>
    <property name="_tableauFactory" class="ptolemy.kernel.util.Attribute">
    </property>
</entity>
}}


# NonStrictTest reads ptolemy.actor.lib.NonStrictTest.fire.compat 
# and ignores fire() not being called if the property is true.
java::call System setProperty \
	ptolemy.actor.lib.NonStrictTest.fire.compat "true"

test BackwardCompatiblity-20.1 {Try running old models, first check that the makefile created the compat/ directory} { 
    if {! [file exists compat]} {
	error "compat directory does not exist.  This could happen\
		If you do not have access to old Ptolemy II tests"
    } else {
	list 1
    }
} {1} KNOWN_FAILURE

if {[info procs jdkStackTrace] == 1} then {
    source [file join $PTII util testsuite jdkTools.tcl]
}
# createAndExecute a file with a MoMLFilter
proc createAndExecute {file} {
    global KNOWN_FAILED
    if { "$file" == "compat/testAudioReaderAudioPlayer.xml" \
	    || "$file" == "compat/testAudioReader.xml" \
	    || "$file" == "compat/testAudioPlayer.xml" \
	    || "$file" == "compat/testAudioCapture_AudioPlayer.xml" \
	    || "$file" == "compat/testAudioCapture.xml" \
	    || "$file" == "compat/vqtest1.xml" \
	    || "$file" == "compat/DifferentialSystem.xml" \
	    || "$file" == "compat/MaximumEntropySpectrum.xml" \
	    || "$file" == "compat/TransferFunction.xml" \
	    || "$file" == "compat/ArrayAppend.xml" \
	    || [file tail $file] == "ImageReaderImageDisplay.xml" \
	    || [file tail $file] == "ImageReaderImageRotateImageToString.xml" \
	    || [file tail $file] == "ImageReaderImageToString.xml" \
	    || [file tail $file] == "testAudioReader.xml" \
	    || [file tail $file] == "testAudioPlayer.xml" \
	    || [file tail $file] == "testAudioCapture_AudioPlayer.xml" \
	    || [file tail $file] == "testAudioCapture.xml" \
	    || [file tail $file] == "automataLibrary.xml" \
	    || [file tail $file] == "DifferentialSystem.xml" \
	    || [file tail $file] == "FireAtCT.xml" \
	    || [file tail $file] == "FireAtCT2.xml" \
	    || [file tail $file] == "FireAtDE.xml" \
	    || [file tail $file] == "MaximumEntropySpectrum.xml" \
	    || [file tail $file] == "MethodCallTest.xml" \
	    || [file tail $file] == "MultimodeTest.xml" \
	    || [file tail $file] == "TransferFunction.xml" \
	    || [file tail $file] == "Samplers.xml" \
	    || [file tail $file] == "SampleDelay.xml" \
	    || [file tail $file] == "Sampler.xml" \
	    || [file tail $file] == "SamplerWithDefault.xml" \
	    || [file tail $file] == "SamplerWithDefault1.xml" \
	    || [file tail $file] == "SamplerWithDefault2.xml" \
	    || [file tail $file] == "SimpleTest.xml" \
	    || [file tail $file] == "StateSpace.xml" \
	    || [file tail $file] == "RateLimiter.xml" \
	    || [file tail $file] == "multirate.xml" \
	    || [file tail $file] == "testAudioReaderAudioPlayer.xml" \
	    || [file tail $file] == "VariableFIR2.xml" \
	    || [file tail $file] == "Autocorrelation3.xml" \
	    || [file tail $file] == "test-pn-composite-1.xml" \
	    || [file tail $file] == "FileWriter2.xml" \
	    || [file tail $file] == "ReadLineInPTII.xml" \
	    || [file tail $file] == "function.xml" \
	    || [file tail $file] == "automataActorLibrary.xml" \
	    || [file tail $file] == "ViterbiDecoderSoft.xml" \
	    || [file tail $file] == "record.xml" \
	    || [file tail $file] == "methodCall.xml" \
	    || [file tail $file] == "DatagramReaders.xml" \
	    || [file tail $file] == "DatagramReadersSynced.xml" \
	    || "$file" == "compat4/MobileFunctionTest.xml" \
	    || "$file" == "compat4/StreamLoader.xml" \
	    || [file tail $file] == "arraymem_test.xml" \
	    || [file tail $file] == "arraymem_test2.xml" \
	    || [file tail $file] == "arraymem_test3.xml" \
	    || [file tail $file] == "register_hde.xml" \
	    || [file tail $file] == "statesample_test.xml" \
	    || [file tail $file] == "SOC_FSM_SR_HDE.xml" \
	    || [file tail $file] == "SOC_Sobel_FSM_HDE.xml" \
	    || [file tail $file] == "hde_sdf_fib_two_nums.xml" \
	    || "$file" == "compat4/Signature.xml" \
	    || "$file" == "compat4/XSLTransformerTest.xml" \
	    || "$file" == "compat4/FileReader.xml" \
	    || [string range $file 0 10] == "compat4/JAI" \
	    || "$file" == "compat4/MobileModelTest.xml" \
	    || "$file" == "compat4/ModelReference.xml" \
	    || "$file" == "compat4/ModelReference3.xml" \
	    || "$file" == "compat4/ModelReference4.xml" \
	    || "$file" == "compat4/MovieReader.xml" \
	    || "$file" == "compat4/MovieWriter.xml" \
	    || "$file" == "compat4/MrPtolemyMovieWriter.xml" \
	    || "$file" == "compat4/SDFVQMovieWriter.xml" \
	    || "$file" == "compat4/SDFVQMovieWriter.xml" \
	    || "$file" == "compat4/SDFVQSequenceDisplay.xml" \
	    || "$file" == "compat4/FunctionClosures.xml" \
	    || "$file" == "compat4/FunctionDefinition.xml" \
	    || "$file" == "compat4/Primes.xml" \
	    || "$file" == "compat4/SDFDDI.xml" \
	    || "$file" == "compat4/PSDFinsidePN.xml" \
	    || "$file" == "compat4/SDFDirector1.xml" \
	    || "$file" == "compat4/singleActor.xml" \
	    || "$file" == "compat4/PythonScale.xml" \
	    || "$file" == "compat4/PythonStop.xml" \
	    || "$file" == "compat4/PythonTerminate.xml" \
	    || "$file" == "compat2/Cascade.xml" \
	    || "$file" == "compat2/HierarchyTest.xml" \
	    || "$file" == "compat3/Cascade.xml" \
	    || "$file" == "compat3/Clock5.xml" \
	    || "$file" == "compat3/ControllersTestNonPreemptive.xml" \
	    || "$file" == "compat3/ControllersTestPreemptive.xml" \
	    || "$file" == "compat3/ContinuousClock.xml" \
	    || "$file" == "compat3/HierarchyTest.xml" \
	    || "$file" == "compat3/LLSTest.xml" \
	    || "$file" == "compat3/MultiplexorDE.xml" \
	    || "$file" == "compat3/SwitchTest.xml" \
	    || "$file" == "compat3/sampledClocks.xml" \
	    || "$file" == "compat4/CarTrackingTest.xml" \
	    || "$file" == "compat4/Cascade.xml" \
	    || "$file" == "compat3/Clock3.xml" \
	    || "$file" == "compat4/Clock3.xml" \
	    || "$file" == "compat4/Clock5.xml" \
	    || "$file" == "compat3/ContinuousClockandDiscreteClock.xml" \
	    || "$file" == "compat4/ContinuousClockandDiscreteClock.xml" \
	    || "$file" == "compat4/ControllersTestNonPreemptive.xml" \
	    || "$file" == "compat4/ControllersTestPreemptive.xml" \
	    || "$file" == "compat4/ContinuousClock.xml" \
	    || "$file" == "compat4/CQBug1.xml" \
	    || "$file" == "compat4/HierarchyTest.xml" \
	    || "$file" == "compat4/LevelCrossingDetectorDetectsGlitches.xml" \
	    || "$file" == "compat4/MultiplexorDE.xml" \
	    || "$file" == "compat4/SwitchTest.xml" \
	    || "$file" == "compat4/VectorAssemblerDisassemblerCT.xml" \
	    || "$file" == "compat4/half.xml" \
	    || "$file" == "compat4/sampledClocks.xml" \
	    || "$file" == "compat4/scaler.xml" \
	    || "$file" == "compat4/scaletoHalf.xml" \
	    || "$file" == "compat4/ScheduleCTCompositeActor2.xml" \
	    || "$file" == "compat4/ScheduleCTCompositeActor3.xml" \
	    || "$file" == "compat4/triple.xml" \
	    || "$file" == "compat4/ExecRunDemos.xml" \
	} {
	puts "$file: Skipping Known Failure"
	incr KNOWN_FAILED
	return
    }

    
    #java::new ptolemy.actor.gui.MoMLSimpleApplication $file
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]
    $parser addMoMLFilter \
	    [java::new ptolemy.moml.filter.RemoveGraphicalClasses]

    set namedObj [$parser parseFile $file]
    set toplevel [java::cast ptolemy.actor.CompositeActor $namedObj]

    # DT is a mess, don't bother testing it
    set compositeActor [java::cast ptolemy.actor.CompositeActor $toplevel]
    set director [$compositeActor getDirector]
    if [java::instanceof \
	    $director ptolemy.domains.dt.kernel.DTDirector] {
	puts "$file: Skipping DT tests, marking as Known Failure"
	incr KNOWN_FAILED
	return
    }

    # Look for comp
    set deepEntityList [$compositeActor deepEntityList]
    for {set i 0} {$i < [$deepEntityList size]} {incr i} {
	set containedActor [$deepEntityList get $i]
	if [java::instanceof $containedActor \
		ptolemy.actor.TypedCompositeActor] {
	    set compositeActor [java::cast ptolemy.actor.CompositeActor \
		    $containedActor]
	    set director [$compositeActor getDirector]
	    if [java::instanceof \
		    $director ptolemy.domains.dt.kernel.DTDirector] {
		puts "$file: Skipping tests with DT inside, marking as Known Failure"
		incr KNOWN_FAILED
		return
	    }
	}
    }


    #set newMoML [$toplevel exportMoML]
    #puts $newMoML

    set workspace [$toplevel workspace]
    set manager [java::new ptolemy.actor.Manager \
	    $workspace "compatibilityChecking"]
    
    $toplevel setManager $manager
    $manager execute

}


# Find all the files in the compat directory


#foreach file [list compat/ComplexToCartesianAndBack.xml compat/testAudioReaderAudioPlayer.xml compat/test1.xml compat/FIR1.xml] {

# Use -nocomplain here so that we do not spuriously report an error
# in alljtests.tcl if compat, compat2 or compat3 do not exist.
foreach file [lsort [glob -nocomplain compat/*.xml compat2/*.xml compat3/*.xml compat4/*.xml]] {
    puts "------------------ testing $file"
    test "Auto" "Automatic test in file $file" {
        set application [createAndExecute $file]
        list {}
    } {{}}
    #test "Auto-rerun" "Automatic test rerun in file $file" {
    #	$application rerun
    #	list {}
    #} {{}}
}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]

#doneTests

