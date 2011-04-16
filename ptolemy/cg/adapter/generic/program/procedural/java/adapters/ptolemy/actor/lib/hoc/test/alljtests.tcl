# CAUTION: automatically generated file by a rule in ptcommon.mk
# This file will source all the Tcl files that use Java. 
# This file will source the tcl files list in the
# makefile SIMPLE_JTESTS and GRAPHICAL_JTESTS variables
# This file is different from all.itcl in that all.itcl
# will source all the .itcl files in the current directory
#
# Set the following to avoid endless calls to exit
if {![info exists reallyExit]} {set reallyExit 0}
# Exiting when there are no more windows is wrong
#::tycho::TopLevel::exitWhenNoMoreWindows 0
# If there is no update command, define a dummy proc.  Jacl needs this
if {[info command update] == ""} then { 
    proc update {} {}
}
#Do an update so that we are sure tycho is done displaying
update
set savedir "[pwd]"
if {"dummy.tcl" != ""} {foreach i [list dummy.tcl] {puts $i; cd "$savedir"; if [ file exists $i ] { if [ catch {source $i} msg] {puts "
WARNING: Sourcing $i resulted in an error,
so we are incrementing the error count.
The error was: $msg
"; incr FAILED}}}}
puts stderr dummy.tcl
cd "$savedir"
if [ file exists dummy.tcl ] { if [catch {source dummy.tcl} msg] {puts "
WARNING: Sourcing dummy.tcl resulted in an error,
so we are incrementing the error count
The error was $msg
"; incr FAILED}}
catch {doneTests}
exit
