# Makefile for Ptolemy II Continuous Time domain demos
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
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

ME =		ptolemy/domains/ct/demo

# PTJSPACES_DIR is set by configure to jspaces if Jini was found
DIRS = 		BouncingBall Brownian \
		CarTracking CartPendulum ComparedClocks ContinuousClock \
		CTInsideDE \
		Helicopter HierarchicalExecution HybridPlant Hysteresis \
		LevelCrossingDetector \
		LevelCrossingDetectorDetectsGlitches \
		Lorenz NewtonsCradle Pendulum3D\
		SampledClocks Satellite SigmaDelta SinuBouncingBall \
		Spontaneous SquareWave Starmac StickyMasses Switch \
		Transmission Thermostat V2V \
		Corba \
		#$(PTJSPACES_DIR)

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	demo
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
# We don't include Corba/Corba.jar because we don't ship Corba.
PTCLASSALLJARS = \
	BouncingBall/BouncingBall.jar \
	Brownian/Brownian.jar \
	CarTracking/CarTracking.jar \
	CartPendulum/CartPendulum.jar \
	ComparedClocks/ComparedClocks.jar \
	ContinuousClock/ContinuousClock.jar \
	CTInsideDE/CTInsideDE.jar \
	Helicopter/Helicopter.jar \
	HierarchicalExecution/HierarchicalExecution.jar \
	HybridPlant/HybridPlant.jar \
	Hysteresis/Hysteresis.jar \
	LevelCrossingDetector/LevelCrossingDetector.jar \
	LevelCrossingDetectorDetectsGlitches/LevelCrossingDetectorDetectsGlitches.jar \
	Lorenz/Lorenz.jar \
	NewtonsCradle/NewtonsCradle.jar \
	Pendulum3D/Pendulum3D.jar \
	SampledClocks/SampledClocks.jar \
	Satellite/Satellite.jar \
	SigmaDelta/SigmaDelta.jar \
	SinuBouncingBall/SinuBouncingBall.jar \
	Spontaneous/Spontaneous.jar \
	Starmac/Starmac.jar \
	SquareWave/SquareWave.jar \
	StickyMasses/StickyMasses.jar \
	Switch/Switch.jar \
	Transmission/Transmission.jar \
	Thermostat/Thermostat.jar \
	V2V/V2V.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

EXTRA_SRCS = index.htm

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	Agilent

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run all the demos, run 'make demo'"

install: all jars

demo: subdemo

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
