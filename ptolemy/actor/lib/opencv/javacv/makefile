# Makefile for Ptolemy II Open CV actors that use JavaCV
#
# @Authors: Christopher Brooks, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2010 The Regents of the University of California.
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

ME =		ptolemy/actor/lib/opencv/javacv

DIRS = 		demo #test

# Root of the Java directory
ROOT =		../../../../..

# JAVACV_JAR is set by configure if JavaCV is found.
# JavaCV may be found at http://code.google.com/p/javacv/
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(OPENCV_JAR)$(CLASSPATHSEPARATOR)$(JAVACV_JAR)$(CLASSPATHSEPARATOR)$(JNA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	javacv
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	AwtImageToIplImage.java \
	CameraReader.java \
	ChooseSeq.java \
	DrawResultSeq.java \
	ImageConvertColor.java \
	ImageCopy.java \
	ImageEqualizeHist.java \
	ImageFlip.java \
	ImageReader.java \
	ImageResize.java \
	ImageRotAlternate.java \
	ImageRotate.java \
	ImageRotateOpenCV.java \
	ImageSmooth.java \
	ImageSobel.java \
	IplImageToAWTImage.java \
	ObjectDetect.java

OTHER_FILES_TO_BE_JARED = \
	javacv.xml \
	javacv-copyright.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	#test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	$(PTCLASSJAR) \

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
