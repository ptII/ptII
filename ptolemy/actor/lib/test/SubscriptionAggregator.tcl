# Test SubscriptionAggregator
#
# @Author: Christopher Brooks
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

######################################################################
####
#
test SubscriptionAggregator-1.1 {Simple Test of SubscriptionAggregator} {
    set workspace [java::new ptolemy.kernel.util.Workspace "subaggWS"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set url [[java::new java.io.File "auto/SubscriptionAggregator3.xml"] toURL]
    $parser purgeModelRecord $url
    set model [java::cast ptolemy.actor.TypedCompositeActor \
		   [$parser {parse java.net.URL java.net.URL} \
			[java::null] $url]]
    set manager [java::new ptolemy.actor.Manager $workspace "subaggManager"]
    $model setManager $manager 
    $manager execute
} {}

test SubscriptionAggregator-1.2 {Change one actor} {
    # Uses 1.1 above
    set actor [$model getEntity SubscriptionAggregator2]
    #set channel [$actor getAttribute channel]
    set channel [getParameter $actor channel]

    # Changing the channel should not change the output
    $channel setToken [java::new ptolemy.data.StringToken "channel1"]
    $manager execute
} {}

test SubscriptionAggregator-2.0 {No Publisher} {
    set e0 [sdfModel 5]
    set subAgg [java::new ptolemy.actor.lib.SubscriptionAggregator $e0 subagg]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    [java::cast ptolemy.actor.IORelation [$e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Subscriber $subAgg] \
		 output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]]] setWidth 1
    catch {[$e0 getManager] execute} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Subscriber has no matching Publisher, channel was "channel1" which evaluated to "channel1".
  in .top.subagg}}

test SubscriptionAggregator-3.0 {Debugging messages} {
    set e3 [sdfModel 5]

    set const [java::new ptolemy.actor.lib.Const $e3 const]
    set publisher [java::new ptolemy.actor.lib.Publisher $e3 publisher]
    set channelP [getParameter $publisher channel]
    $channelP setExpression "channel42"
    $publisher attributeChanged $channelP

    [java::cast ptolemy.actor.IORelation [$e3 connect \
	[java::field [java::cast ptolemy.actor.lib.Source $const] \
	     output] \
	[java::field $publisher input]]] setWidth 1

    set subAgg [java::new ptolemy.actor.lib.SubscriptionAggregator $e3 subagg]
    set channelS [getParameter $subAgg channel]
    $channelS setExpression "channel42"
    $subAgg attributeChanged $channelS

    set rec [java::new ptolemy.actor.lib.Recorder $e3 rec]
    [java::cast ptolemy.actor.IORelation [$e3 connect \
	[java::field [java::cast ptolemy.actor.lib.Subscriber $subAgg] \
	     output] \
	[java::field [java::cast ptolemy.actor.lib.Sink $rec] input]]] setWidth 1

    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.StreamListener $printStream]
    $subAgg addDebugListener $listener

    [$e3 getManager] execute
    $subAgg removeDebugListener $listener
    $printStream flush
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    list $output \
	[enumToTokenValues [$rec getRecord 0]]
} {{Connections changed on port: input
Called preinitialize()
Connections changed on port: input
Called stopFire()
Added attribute firingsPerIteration to .top.subagg
Called initialize()
Called iterate(1)
Called prefire()
Called fire()
Called postfire()
Called iterate(1)
Called prefire()
Called fire()
Called postfire()
Called iterate(1)
Called prefire()
Called fire()
Called postfire()
Called iterate(1)
Called prefire()
Called fire()
Called postfire()
Called iterate(1)
Called prefire()
Called fire()
Called postfire()
Called wrapup()
} {1 1 1 1 1}}


test SubscriptionAggregator-4.0 {7*9*11 SubscriptionAggregators} {
    set e3 [sdfModel 5]

    set ramp [java::new ptolemy.actor.lib.Ramp $e3 ramp]
    set publisher [java::new ptolemy.actor.lib.Publisher $e3 publisher]
    set channelP [getParameter $publisher channel]
    $channelP setExpression "channel42"
    $publisher attributeChanged $channelP

    [java::cast ptolemy.actor.IORelation [$e3 connect \
	[java::field [java::cast ptolemy.actor.lib.Source $ramp] \
	     output] \
	[java::field $publisher input]]] setWidth 1

    set rec [java::new ptolemy.actor.lib.Recorder $e3 rec]

    for {set k 0} {$k < 7} {incr k} {
	set ek [java::new ptolemy.actor.TypedCompositeActor $e3 ek-$k]        

	# Create an output port
	set pk [java::new ptolemy.actor.TypedIOPort $ek Pk false true]
	$pk setMultiport true

	for {set j 0} {$j < 9} {incr j} {
   	    puts -nonewline .

	    set ej [java::new ptolemy.actor.TypedCompositeActor $ek ej-$j]        
	    # Create an output port
	    set pj [java::new ptolemy.actor.TypedIOPort $ej Pj false true]
	    $pj setMultiport true

	    for {set i 0} {$i < 11} {incr i} {
		set subAgg [java::new ptolemy.actor.lib.SubscriptionAggregator \
				$ej subagg-$i]
		set channelS [getParameter $subAgg channel]
		$channelS setExpression "channel.*"
		$subAgg attributeChanged $channelS

		set operation [getParameter $subAgg operation]
		$operation setExpression "multiply"
		$subAgg attributeChanged $operation

		[java::cast ptolemy.actor.IORelation [$ej connect \
		    [java::field [java::cast ptolemy.actor.lib.Subscriber $subAgg] \
			 output] \
		    $pj]] setWidth 1

	    }
	    [java::cast ptolemy.actor.IORelation [$ek connect \
		$pj \
		$pk]] setWidth 1
	}
	[java::cast ptolemy.actor.IORelation [$e3 connect \
		$pk \
		[java::field [java::cast ptolemy.actor.lib.Sink $rec] input]]] setWidth 1
    }

    [$e3 getManager] execute
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    set count [$rec getCount]
    list $count \
	[enumToTokenValues [$rec getRecord 0]] \
	[enumToTokenValues [$rec getRecord [expr {$count/5 - 1}]]] \
	[enumToTokenValues [$rec getRecord [expr {$count/5}]]]
} {35 {0 1 2 3 4} {0 1 2 3 4} {{"_"} {"_"} {"_"} {"_"} {"_"}}} 

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
