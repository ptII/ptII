# Tests HTMLAbout
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006-2009 The Regents of the University of California.
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

# Tycho test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
test HTMLAbout-1.0 {Read in the configuration} { 
    global configuration
    set configurationURL [java::call ptolemy.util.FileUtilities nameToURL \
			      {$CLASSPATH/ptolemy/actor/gui/test/testConfiguration.xml} \
			      [java::null] \
			      [java::null]]

    if {[info vars configuration] == ""} {
	set configuration [java::call ptolemy.actor.gui.MoMLApplication readConfiguration $configurationURL]
    }
    $configuration getFullName
} {.configuration}


######################################################################
####
#
test HTMLAbout-2.0 {about} {
    set about [java::call ptolemy.actor.gui.HTMLAbout about $configuration ]
    list $about
} {{<html><head><title>About Ptolemy II</title></head><body><h1>About Ptolemy II</h1>
The HTML Viewer in Ptolemy II handles the <code>about:</code>
tag specially.
<br>The following urls are handled:
<ul>
<li><a href="about:configuration"><code>about:configuration</code></a> Expand the configuration (good way to test for missing classes).
<li><a href="about:expandLibrary"><code>about:expandLibrary</code></a> Open a model and expand library tree (good way to test for missing classes, check standard out).
<li><a href="about:copyright"><code>about:copyright</code></a>  Display information about the copyrights.
<li><a href="about:checkCompleteDemos"><code>about:checkCompleteDemos</code></a> Check that each of the demos listed in the individual files is present in <code>ptolemy/configs/doc/completeDemos.htm</code>.
</ul>
<table>
<tr rowspan=4><center><b>Full</b></center></tr>
  <tr>
    <code>ptolemy/configs/doc/completeDemos.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/completeDemos.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/completeDemos.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/completeDemos.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
  <tr>
    <code>ptolemy/configs/doc/demos.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/demos.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/demos.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/demos.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
  <tr>
    <code>ptolemy/configs/doc/whatsNew8.1.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/whatsNew8.1.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/whatsNew8.1.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/whatsNew8.1.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
  <tr>
    <code>ptolemy/configs/doc/whatsNew8.0.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/whatsNew8.0.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/whatsNew8.0.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/whatsNew8.0.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
  <tr>
    <code>ptolemy/configs/doc/whatsNew7.0.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/whatsNew7.0.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/whatsNew7.0.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/whatsNew7.0.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
  <tr>
    <code>ptolemy/configs/doc/whatsNew6.0.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/whatsNew6.0.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/whatsNew6.0.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/whatsNew6.0.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
  <tr>
    <code>ptolemy/configs/doc/whatsNew5.1.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/whatsNew5.1.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/whatsNew5.1.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/whatsNew5.1.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
  <tr>
    <code>ptolemy/configs/doc/whatsNew5.0.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/whatsNew5.0.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/whatsNew5.0.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/whatsNew5.0.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
  <tr>
    <code>ptolemy/configs/doc/whatsNew4.0.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/whatsNew4.0.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/whatsNew4.0.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/whatsNew4.0.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
  <tr>
    <code>ptolemy/configs/doc/whatsNew3.0.2.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/whatsNew3.0.2.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/whatsNew3.0.2.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/whatsNew3.0.2.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
<tr rowspan=4><center><b>HyVisual</b></center></tr>
  <tr>
    <code>ptolemy/configs/hyvisual/intro.htm</code>
    <td><a href="about:demos#ptolemy/configs/hyvisual/intro.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/hyvisual/intro.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/hyvisual/intro.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
<tr rowspan=4><center><b>Ptiny</b></center></tr>
  <tr>
    <code>ptolemy/configs/doc/completeDemosPtiny.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/completeDemosPtiny.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/completeDemosPtiny.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/completeDemosPtiny.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
  <tr>
    <code>ptolemy/configs/doc/demosPtiny.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/demosPtiny.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/demosPtiny.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/demosPtiny.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
  <tr>
    <code>doc/mainVergilPtiny.htm</code>
    <td><a href="about:demos#doc/mainVergilPtiny.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#doc/mainVergilPtiny.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#doc/mainVergilPtiny.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
<tr rowspan=4><center><b>Ptiny for Kepler</b></center></tr>
  <tr>
    <code>ptolemy/configs/doc/completeDemosPtinyKepler.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/completeDemosPtinyKepler.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/completeDemosPtinyKepler.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/completeDemosPtinyKepler.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
  <tr>
    <code>ptolemy/configs/doc/demosPtinyKepler.htm</code>
    <td><a href="about:demos#ptolemy/configs/doc/demosPtinyKepler.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/doc/demosPtinyKepler.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/doc/demosPtinyKepler.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
<tr rowspan=4><center><b>VisualSense</b></center></tr>
  <tr>
    <code>ptolemy/configs/visualsense/intro.htm</code>
    <td><a href="about:demos#ptolemy/configs/visualsense/intro.htm">&nbsp;Open the .xml&nbsp;</a></td>
    <td><a href="about:links#ptolemy/configs/visualsense/intro.htm">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>
    <td><a href="about:checkModelSizes#ptolemy/configs/visualsense/intro.htm">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>
  </tr>
</table>
</body>
</html>
}}

######################################################################
####
#
test HTMLAbout-2.1 {generateLinks for testDemos.htm} {
    set links [java::call ptolemy.actor.gui.HTMLAbout generateLinks \
		   {ptolemy/actor/gui/test/testDemos.htm} {.*.xml$} \
		   $configuration]
    list [string range [$links toString] [expr {[string length [$links toString]] - 36}] [string length [$links toString]]]
} {ptolemy/actor/gui/test/testDemos.htm}

######################################################################
####
#
test HTMLAbout-3.0 {GenerateCopyrights.generateCopyrights} {
    set copyrights [java::call ptolemy.actor.gui.GenerateCopyrights generateHTML $configuration]
    # Just check for a few strings because the configuration
    # changes on different machines.
    list [regexp {com/microstar/xml/README.txt} $copyrights]
} {1}
