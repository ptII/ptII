# Makefile for GR domain quicktime actors library
#
# @Authors: Christopher Hylands (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 1998-2009 The Regents of the University of California.
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

ME =		ptolemy/domains/gr/lib/quicktime

# Rule for index.xml uses this
TITLE = 	"Quicktime 3D actors"

DIRS =

# Root of the Java directory
ROOT =		../../../../..


# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# JAVA3D_JARS is the jar files to use with Java 3D.  Usually, this is
# empty, because most installations have Java 3D in the jdk
# jre/lib/ext directory, but some do not. If --with-java3D was used
# with configure, then JAVA3D_JARS will be set to include the jars.
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)$(CLASSPATHSEPARATOR)$(JAVA3D_JARS)$(CLASSPATHSEPARATOR)$(QTJAVA_ZIP)

# Used to build jar files
PTPACKAGE = 	quicktime
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = \
	MovieViewScreen2D.java \
	MovieViewScreen3D.java

OTHER_FILES_TO_BE_JARED = \
	quicktime.xml \
	quicktime-copyright.htm \
	grquicktimeentities.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = 	$(DIRS) \
	demo 

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	index.xml \
	'MovieViewScreen2D$$1.class' \
	'MovieViewScreen2D$$Painter.class' \
	'MovieViewScreen3D$$1.class' \
	'MovieViewScreen3D$$Painter.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass index.xml
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
