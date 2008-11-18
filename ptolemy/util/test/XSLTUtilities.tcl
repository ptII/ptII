# Tests for the XSLTUtilities class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2003-2006 The Regents of the University of California.
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test XSLTUtilities-1.1 {Call main} {
    if [catch {file delete -force out.xml} ignore] {
	puts "deleting out.xml failed, ignoring: $ignore"
    }

    set args [java::new {String[]} {3} {test.xml addMarkers.xsl out.xml}]
    java::call ptolemy.util.XSLTUtilities main $args

    set file [open out.xml r]
    set results [read $file]

    # Close before deleting
    close $file
    file delete -force out.xml

    # Strip out spaces.  between java 1.4.1 and 1.4.2, the
    # output changed.
    regsub -all {[ ]+} $results { } results2
    # Insert newlines between ><
    regsub -all {><} $results2 ">\n<" results3
    # Get rid of leading spaces
    #regsub -all {^( )+} $results3 {} results4
    regsub -all "\n *" $results3 "\n" results4

    list $results4
} {{<?xml version="1.0" encoding="UTF-8"?>
<WMBasicEdit>
<Attributes>
<WMENC_STRING Name="Title"/>
</Attributes>

<RemoveAllMarkers/>
<RemoveAllScripts/>
<Scripts>
<Script Command="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide2.gif" Time="341830000" Type="URL"/>
<Script Command="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide3.gif" Time="816310000" Type="URL"/>
</Scripts>

<Markers>
<Marker Name="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide2.gif" Time="341830000"/>
<Marker Name="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide3.gif" Time="816310000"/>
</Markers>
</WMBasicEdit>}}


######################################################################
####
#
test XSLTUtilities-2.1 {test parse and toString} {
    set inputDocument [java::call ptolemy.util.XSLTUtilities \
			   parse test.xml]
    set inputString [java::call ptolemy.util.XSLTUtilities toString \
			  $inputDocument]

    # Strip out spaces.  between java 1.4.1 and 1.4.2, the
    # output changed.
    regsub -all {[ ]+} $inputString { } results2
    # Insert newlines between ><
    regsub -all {><} $results2 ">\n<" results3
    # Get rid of leading spaces
    #regsub -all {^[ ]*} $results3 {} results4
    regsub -all "\n *" $results3 "\n" results4
    list $results4
} {{<?xml version="1.0" encoding="UTF-8"?>
<WMBasicEdit>
<Attributes>
<WMENC_STRING Name="Title"/>

</Attributes>

<RemoveAllMarkers/>
<RemoveAllScripts/>
<Scripts>
<Script Command="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide2.gif" Time="341830000" Type="URL"/>
<Script Command="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide3.gif" Time="816310000" Type="URL"/>
</Scripts>

</WMBasicEdit>}}

######################################################################
####
#
test XSLTUtilities-3.1 {transform(Document, String) using a copy} {
    # Uses inputString from 1.1 above
    set outputDocument [java::call ptolemy.util.XSLTUtilities \
			    transform $inputDocument copy.xsl]
    set outputString [java::call ptolemy.util.XSLTUtilities toString \
			  $outputDocument]

    # diffText is defined in ptII/util/testsuite/testDefs.tcl
    diffText $inputString $outputString
} {}


######################################################################
####
#
test XSLTUtilities-3.2 {Call transform(Document, List) using local files} {
    set transformList [java::new java.util.LinkedList]
    $transformList add copy.xsl
    $transformList add addMarkers.xsl
    set outputDocument [java::call ptolemy.util.XSLTUtilities \
			    transform $inputDocument $transformList]
    set outputString [java::call ptolemy.util.XSLTUtilities toString \
			  $outputDocument]

    # Strip out spaces.  between java 1.4.1 and 1.4.2, the
    # output changed.
    regsub -all {[ ]+} $outputString { } results2
    # Insert newlines between ><
    regsub -all {><} $results2 ">\n<" results3
    # Get rid of leading spaces
    #regsub -all {^[ ]*} $results3 {} results4
    regsub -all "\n *" $results3 "\n" results4

    list $results4
} {{<?xml version="1.0" encoding="UTF-8"?>
<WMBasicEdit>
<Attributes>
<WMENC_STRING Name="Title"/>
</Attributes>

<RemoveAllMarkers/>
<RemoveAllScripts/>
<Scripts>
<Script Command="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide2.gif" Time="341830000" Type="URL"/>
<Script Command="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide3.gif" Time="816310000" Type="URL"/>
</Scripts>

<Markers>
<Marker Name="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide2.gif" Time="341830000"/>
<Marker Name="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide3.gif" Time="816310000"/>
</Markers>
</WMBasicEdit>}}

######################################################################
####
#
test XSLTUtilities-3.3 {Call transform(Document, List) using files found in the classpath} {
    set transformList [java::new java.util.LinkedList]
    $transformList add ptolemy/util/test/copy.xsl
    $transformList add ptolemy/util/test/addMarkers.xsl
    set outputDocument [java::call ptolemy.util.XSLTUtilities \
			    transform $inputDocument $transformList]
    set outputString [java::call ptolemy.util.XSLTUtilities toString \
			  $outputDocument]
    # Strip out spaces.  between java 1.4.1 and 1.4.2, the
    # output changed.
    regsub -all {[ ]+} $outputString { } results2
    # Insert newlines between ><
    regsub -all {><} $results2 ">\n<" results3
    # Get rid of leading spaces
    #regsub -all {^[ ]*} $results3 {} results4
    regsub -all "\n *" $results3 "\n" results4

    list $results4
} {{<?xml version="1.0" encoding="UTF-8"?>
<WMBasicEdit>
<Attributes>
<WMENC_STRING Name="Title"/>
</Attributes>

<RemoveAllMarkers/>
<RemoveAllScripts/>
<Scripts>
<Script Command="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide2.gif" Time="341830000" Type="URL"/>
<Script Command="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide3.gif" Time="816310000" Type="URL"/>
</Scripts>

<Markers>
<Marker Name="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide2.gif" Time="341830000"/>
<Marker Name="http://10.0.0.1/gsrc/talks/2002/berkeley/01/01/slide3.gif" Time="816310000"/>
</Markers>
</WMBasicEdit>}}
