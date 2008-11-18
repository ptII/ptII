# Makefile for applications for the Palm Pilot using Waba
#
# @Authors: Christopher Hylands
#
# @Version: : $Id$
#
# @Copyright (c) 2000-2005 The Regents of the University of California.
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

# Please don't use GNU make extensions in this file, such as 'ifdef' or '%'.
# If you really must use an GNU make extension, please label it.
#
# Usually, this makefile is included right at the bottom of a makefile,
# just before ptcommon.mk is included.
#
# See $PTII/doc/waba.htm for more documentation
# See $PTII/ptolemy/waba/demo/ramp/makefile for an example makefile that
# includes this makefile.
#
#
# Standard Ptolemy II external makefile variables that this file uses:
#
# CLASSPATHSEPARATOR   Either : or ; for Unix or Windows.  Usually set in
#		$PTII/mk/ptII.mk by configure
# JAVA		The location of the Java Interpreter.  Set in ptII.mk
# JAVAC		The location of the Java Compiler.  Set in ptII.mk
# WABA_CLASSES	CLASSPATH specification to find the WABA.  Set in ptII.mk
# WABA_DIR	Directory where the WABA is located.  Set in ptII.mk
# ME		Directory where the makefile that includes waba.mk is located.
# ROOT		Location of the PTII directory to the makefile that
#		includes waba.mk.

# WABA specific makefiles variables that need to be set before
# including waba.mk:
#
# SOURCE_SYSTEM_CLASS  Classpath to the source system we are generating
#		code for, for example ptolemy.waba.demo.ramp.RampSystem
# ITERATIONS	Number of iterations, for example 50
# OUTPKG	Output package name for generated code, which also
# 		determines the directory relative to PTII where the
#		code will appear.  If OUTPKG is set to cg.ramp, then
#		the code will appear in $PTII/cg/ramp.
# OUTPKG_DIR	Location of the OUTPKG directory.
#		If OUTPKG is cg.ramp, then OUTPKG_DIR would be cg/ramp
# OUTPKG_ROOT   The relative path from OUTPKG to $PTII.
#		If OUTPKG is cg.ramp, then OUTPKG_ROOT would be ../..
# OUTPKG_MAIN_CLASS The class that contains the main() method
#		For example CG_Main

# Uncomment these to turn on verbosity, or run
# make JAVAC_VERBOSE= -verbose JAVA_VERBOSE= -verbose:class MAKEPALMAPP_VERBOSE=" -v -v" codegen
#JAVAC_VERBOSE =	-verbose
#JAVA_VERBOSE =	-verbose:class
#MAKEPALMAPP_VERBOSE = -v -v

WARP = $(WABA_DIR)/bin/warp
EXEGEN = $(WABA_DIR)/bin/exegen

# Run the demo via the usual method without any codegen.
demo_interpreted: $(PTCLASSJAR)
	CLASSPATH="$(CLASSPATH)" \
		$(JAVA) ptolemy.actor.gui.CompositeActorApplication \
		-class $(SOURCE_SYSTEM_CLASS) \
		-iterations $(ITERATIONS)

codegen: generate_sdf_code compile_codegen build_prc run_codegen waba

# FIXME: JAVASRC_SKELETON_DIR needs to go away.
# It is the location of the java sources and the .skel files
# If you don't have these, copy them from /users/ptII/vendors/sun/src/
# See $PTII/ptolemy/java/lang/makefile
JAVASRC_SKELETON_DIR=$(PTII)/vendors/sun/src

# Read in SOURCE_SYSTEM_CLASS and generate .java files in $PTII/$(OUTPKG)
generate_sdf_code: $(JCLASS) $(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).java
$(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).java:
	@echo "###################################"
	@echo "# Generating code for $(SOURCE_SYSTEM_CLASS) in $PTII/$(OUTPKG)"
	@echo "###################################"
	@if [ ! -d "$(JAVASRC_SKELETON_DIR)" ]; then \
		echo "Warning $(JAVASRC_SKELETON_DIR) does not exist"; \
		echo "Copy the zip file from /users/ptII/vendors/sun/src/"; \
		echo "See $PTII/ptolemy/java/lang/makefile for details"; \
	fi
	CLASSPATH="$(ROOT)$(CLASSPATHSEPARATOR)$(JAVASRC_SKELETON_DIR)" \
	$(JAVA) $(JAVA_VERBOSE) ptolemy.domains.sdf.codegen.SDFCodeGenerator \
		-class $(SOURCE_SYSTEM_CLASS) \
		-iterations $(ITERATIONS) \
		-outdir $(ROOT) -outpkg $(OUTPKG)

# Compile the codegen waba code in $(PTII)/$(OUTPKG)
# Note that we compile without debug as the default
compile_codegen: $(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).class
$(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).class: \
			$(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).java
	@echo "###################################"
	@echo "# Compiling codegen waba *.java files in $(PTII)/$(OUTPKG)"
	@echo "###################################"
	(cd $(ROOT)/$(OUTPKG_DIR); \
	CLASSPATH="$(OUTPKG_ROOT)$(CLASSPATHSEPARATOR)$(WABA_CLASSES)" \
	$(JAVAC) -g:none -O $(JAVAC_VERBOSE) \
		$(OUTPKG_MAIN_CLASS).java)

# Compile the non-codegen waba code in $(PTII)/$(OUTPKG)
# Note that we compile without debug as the default
compile_waba:
	@echo "###################################"
	@echo "# Compiling non-codegen waba *.java files in $(PTII)/$(OUTPKG)"
	@echo "###################################"
	(cd $(ROOT)/$(OUTPKG_DIR); \
	CLASSPATH="$(OUTPKG_ROOT)$(CLASSPATHSEPARATOR)$(WABA_CLASSES)" \
	$(JAVAC) -g:none -O $(JAVAC_VERBOSE) \
		$(OUTPKG_MAIN_CLASS).java)



# Create a Palm binary from the class files in
# $(PTII)/$(OUTPKG)/output/$(OUTPKG_DIR)
# Waba Extras at http://www.cygnus.uwa.edu.au/~rnielsen/wextras/
# has java implementations of warp and exegen
WEXTRAS_DIR=$(PTII)/vendors/misc/waba/wextras130
build_prc:
	@echo "###################################"
	@echo "# Creating Palm executable from classes in"
	@echo "# $(PTII)/$(OUTPKG_DIR)"
	@echo "###################################"
	(cd $(ROOT); \
	CLASSPATH=$(WEXTRAS_DIR)/src \
		$(JAVA) wababin/Warp c $(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS) $(OUTPKG_DIR)/*.class; \
	CLASSPATH=$(WEXTRAS_DIR)/src \
		$(JAVA)	wababin/Exegen \
		$(OUTPKG_MAIN_CLASS) \
		$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS) \
		$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS) \
		)

foo:
	#$(WARP) c /q $(OUTPKG_MAIN_CLASS) *.class; \
	#$(EXEGEN) /q \
	#	$(OUTPKG_MAIN_CLASS) $(OUTPKG_MAIN_CLASS) $(OUTPKG_MAIN_CLASS)

run_codegen:
	(cd $(ROOT)/$(OUTPKG_DIR); \
	CLASSPATH="$(OUTPKG_ROOT)$(CLASSPATHSEPARATOR)$(WABA_CLASSES)" \
		$(JAVA) waba.applet.Applet $(OUTPKG).$(OUTPKG_MAIN_CLASS))

clean_codegen: clean
	rm -rf $(ROOT)/$(OUTPKG_DIR)
