# Makefile for the ptolemy/vergil/basic/export/html package
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2011 The Regents of the University of California.
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


ME =		ptolemy/vergil/basic/export/html

DIRS =		demo javascript test

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	html
PTCLASSJAR =
PTCLASSALLJAR = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	javascript/javascript.jar

JSRCS = \
	AreaEventType.java \
	DefaultIconLink.java \
	DefaultIconScript.java \
	DefaultTitle.java \
	ExportHTMLAction.java \
	ExportParameters.java \
	ExportToWeb.java \
	HTMLExportable.java \
	HTMLText.java \
	HTMLTextPosition.java \
	IconLink.java \
	IconScript.java \
	LinkTarget.java \
	LinkToOpenTableaux.java \
	ParameterDisplayIconScript.java \
	Title.java \
	WebContent.java \
	WebExportable.java \
	WebExportParameters.java \
	WebExportable.java \
	WebExporter.java

OTHER_FILES_TO_BE_JARED = \
	utilityWebExport.xml \
	webExportConfiguration.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	javascript test

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc \
	$(PTCLASSJAR) \
	'ExportHTMLAction$$DirectoryFilter.class' \
	'ExportHTMLAction$$IconVisibleLocation.class' \
	'ExportHTMLAction$$1.class' \
	'ExportToWeb$$1.class' \
	'ExportToWeb$$2.class' \
	'ExportToWeb$$3.class'

JCLASS= $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
