# procs useful for Tcl Blend
#
# @Author: Christopher Hylands, John Reekie
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

# This file contains procs that may be useful when using Tcl Blend.
# Print the JDK property list
proc jdkProperties {} {
    set props [java::call System getProperties]
    set names [$props propertyNames]
    while { [$names hasMoreElements] } {
	set name [$names nextElement]
	puts "$name=[$props getProperty $name]"
    }
}

# Get Runtime information
proc jdkRuntimeStatistics {} {
    # Ptolemy 1.2 and later has this method
    #puts "$prefix [java::call ptolemy.actor.Manager timeAndMemory -1]"

    # Ptolemy 1.0.1 does not have Manager.timeAndMemory, so we use this:
    set runtime [java::call Runtime getRuntime]
    set totalMemory [expr {[$runtime totalMemory] /1024}]
    set freeMemory [expr {[$runtime freeMemory] /1024}]
    set percent [expr { round ( (double($freeMemory) / double($totalMemory)) * 100 )}]
    return "Memory: ${totalMemory}K Free: ${freeMemory}K ${percent}%"
}

# Return the classpath separator charactor
proc jdkClassPathSeparator {} {
    return [java::field java.io.File pathSeparator]
}

# Print JDK version info
proc jdkVersion {} {
    global tcl_version tcl_patchLevel env
    if [info exists env(CLASSPATH)] {
	puts "env(CLASSPATH):   $env(CLASSPATH)"
    } else {
	puts "env(CLASSPATH) is not set"
    }

    puts "java.class.path property:\
            [java::call System getProperty "java.class.path"]\n"
    puts -nonewline "jdk version: [java::call System getProperty \
	    "java.version"]"
    if [info exists ::java::patchLevel] {
	puts "   Tcl Blend patch level: $::java::patchLevel"
    } else {
	puts ""
    }
    puts "tcl version: $tcl_version    \
	    tcl patch level: $tcl_patchLevel"
    puts "java package: [package versions java]   \
	    info loaded: [info loaded]"
    puts [jdkRuntimeStatistics]
}

# Capture output to System.out
proc jdkCapture {script varName} {
    upvar $varName output
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set stdout [java::field System out]
    java::call System setOut $printStream
    set result [uplevel $script]
    java::call System setOut $stdout
    $printStream flush
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    return $result
}

# Capture output to System.err
proc jdkCaptureErr {script varName} {
    upvar $varName output
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set stderr [java::field System err]
    java::call System setErr $printStream
    set result [uplevel $script]
    java::call System setErr $stderr
    $printStream flush
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    return $output
}

# Capture output to System.out and System.err
proc jdkCaptureOutAndErr {script outputVarName errVarName} {
    upvar $outputVarName output
    upvar $errVarName err

    set outputStream [java::new java.io.ByteArrayOutputStream]
    set outputPrintStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $outputStream]
    set stdout [java::field System out]
    java::call System setOut $outputPrintStream

    set errorStream [java::new java.io.ByteArrayOutputStream]
    set errorPrintStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $errorStream]
    set stderr [java::field System err]
    java::call System setErr $errorPrintStream

    set errMsg ""
    catch {set result [uplevel $script]} errMsg

    java::call System setOut $stdout
    java::call System setErr $stderr

    $errorPrintStream flush
    $outputPrintStream flush
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
	        [$outputStream toString] "\n" output
    regsub -all [java::call System getProperty "line.separator"] \
	        "[$errorStream toString]$errMsg" "\n" err
    
    return "$errMsg$output$err"
}

# Print the most recent Java stack trace
# Here's an example:
# Create a String
#   set s [java::new {String java.lang.String} "123"]
# Try to get a character beyond the end of the array
#   catch {$s charAt 4} err
#   puts "The error was:\n$err"
#   puts "The stack was:\n[jdkStackTrace]"
proc jdkStackTrace {} {
    global errorCode errorInfo
    if { [string match {JAVA*} $errorCode] } {
	set exception [lindex $errorCode 1]
	set stream [java::new java.io.ByteArrayOutputStream]
	set printWriter [java::new \
		{java.io.PrintWriter java.io.OutputStream} $stream]
	$exception {printStackTrace java.io.PrintWriter} $printWriter
	$printWriter flush

	puts "[$exception getMessage]"

	puts "    while executing"
	set stack [$stream toString]
	if { [string length $stack] > 12500 } {
	    puts "[string range $stack 0 12500] . . ."
	} else {
	    puts "$stack"
	}
	puts "    while executing"
    }
    puts $errorInfo
}

# Turn on and off instruction tracing
# Turn on instruction tracing with
# jdkTraceInstructions True
proc jdkTraceInstructions { traceboolean } {
    set runtime [java::call Runtime getRuntime]
    $runtime traceInstructions $traceboolean
}

proc jdkTraceMethodCalls { traceboolean } {
    set runtime [java::call Runtime getRuntime]
    $runtime traceMethodCalls $traceboolean
}


# Print info about threads, see also $PTII/util/testsuite/PrintThreads.java
proc jdkThreads {} {
    set currentThread [java::call Thread currentThread]
    set parent [$currentThread getThreadGroup]
    puts "ThreadGroup:      [$parent toString]"

    # Figure out the root ThreadGroup
    while { ! [java::isnull $parent] } {
	set rootGroup $parent
	set parent2 [$parent getParent]
	set parent $parent2
    }
    puts "Root ThreadGroup: [$rootGroup toString]"

    set threads [java::new {Thread[]} [$rootGroup activeCount]]
    $rootGroup enumerate $threads

    puts "Current Threads are:"
    for { set i 0} { $i < [$threads length]} {incr i} {
	set thread [$threads get $i]
	set currentThreadString ""
	if {$thread == $currentThread} {
	    set currentThreadString "          (Current Thread)"
	} 
	puts "$i. [$thread toString] $currentThreadString"
    }
    puts "To get a stack trace for each thread:"
    puts " Under Unix, try 'kill -3 pid', where pid is the process id from ps"
    puts " Under Windows, try Control-Break"
}

