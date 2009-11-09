# Tests for release management
#
# @Author: Christopher Brooks
#
# $Id$
#
# @Copyright (c) 2009 The Regents of the University of California.
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

# Get rid of any previous lists of .java files etc.
exec make clean

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

test release-1.1 {Check for missing makefiles} {
    exec make --no-print-directory --silent missingMakefiles
} {./config/makefile
./doc/coding/templates/makefile
./ptolemy/backtrack/automatic/ptolemy/actor/lib/makefile
./ptolemy/backtrack/automatic/ptolemy/domains/sdf/lib/makefile
./ptolemy/backtrack/automatic/ptolemy/math/makefile
./ptolemy/backtrack/eclipse/plugin/makefile
./ptolemy/backtrack/eclipse/plugin/actions/makefile
./ptolemy/backtrack/eclipse/plugin/actions/codestyle/makefile
./ptolemy/backtrack/eclipse/plugin/compatibility/makefile
./ptolemy/backtrack/eclipse/plugin/console/makefile
./ptolemy/backtrack/eclipse/plugin/dialogs/makefile
./ptolemy/backtrack/eclipse/plugin/editor/makefile
./ptolemy/backtrack/eclipse/plugin/preferences/makefile
./ptolemy/backtrack/eclipse/plugin/util/makefile
./ptolemy/backtrack/eclipse/plugin/widgets/makefile
./ptolemy/backtrack/ui/makefile
./ptolemy/backtrack/util/java/util/makefile
./ptolemy/plot/servlet/makefile}

test release-2.1 {Check for directories that have java files, but are not in doc/makefile} {
    exec make --no-print-directory --silent missingDocPackages
} {config
contrib.actor.lib.example
diva.util.java2d.svg
doc.coding.templates
doc.tutorial
jni.launcher
jni.test.jni.meaningOfLife
jni.test.jni.Streaming
jni.test.jni.testDeux
jni.test.jni.testTrois
ptolemy.actor.corba
ptolemy.actor.corba.CoordinatorUtil
ptolemy.actor.corba.CorbaIOUtil
ptolemy.actor.corba.util
ptolemy.actor.lib.javasound.test.pitchshift
ptolemy.actor.lib.jxta
ptolemy.actor.lib.tutorial
ptolemy.backtrack.automatic.ptolemy.math
ptolemy.backtrack.eclipse.plugin.actions.codestyle
ptolemy.backtrack.eclipse.plugin.compatibility
ptolemy.backtrack.eclipse.plugin.console
ptolemy.backtrack.eclipse.plugin.dialogs
ptolemy.backtrack.eclipse.plugin.widgets
ptolemy.backtrack.test.array1
ptolemy.backtrack.test.ptolemy.backtrack.test.array1
ptolemy.backtrack.test.ptolemy.backtrack.test.random1
ptolemy.backtrack.test.ptolemy.backtrack.test.test1
ptolemy.backtrack.test.ptolemy.backtrack.test.test2
ptolemy.backtrack.test.random1
ptolemy.backtrack.test.test1
ptolemy.backtrack.test.test2
ptolemy.backtrack.util.java.util
ptolemy.caltrop.ddi.util
ptolemy.chic
ptolemy.component
ptolemy.component.data
ptolemy.component.data.type
ptolemy.component.domains.ptinyos
ptolemy.copernicus.interpreted
ptolemy.copernicus.kernel.fragment
ptolemy.domains.gr.lib.experimental
ptolemy.domains.gr.lib.quicktime
ptolemy.domains.wireless.lib.network
ptolemy.domains.wireless.lib.network.mac
ptolemy.domains.wireless.lib.tinyOS
ptolemy.moml.jxta
ptolemy.plot.servlet}

set currentDirectory [pwd]
test release-3.1 {Run svn status and look for files that should be checked in.  See ptII/adm/bin/svnignoreupdate for a script to fix this} {

    cd "$PTII"
    set result [exec svn status]
    set result1 {M      lib/matlab.jar
?       ptolemy/matlab/META-INF}
    if { $result == $result1 } {
	puts "Result was:\n$result\nWhich is ok"
        set result {}
    } else {
	puts "Result was:\n<$result>\nWhich is not\n<$result1>"        
    }
    list $result
} {{}}
cd "$currentDirectory"