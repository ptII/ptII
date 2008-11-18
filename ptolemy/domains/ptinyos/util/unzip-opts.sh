#!/bin/sh

# @Version: $Id$
# @Author: Elaine Cheong
#
# @Copyright (c) 2005-2006 The Regents of the University of California.
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
#                                                 PT_COPYRIGHT_VERSION_2
#                                                 COPYRIGHTENDKEY


# FIXME: move this file to tinyos-1.x?

# This script will unzip the opts.tar.gz file into the tinyos-1.x
# directory.  The opts.tar.gz file contains files named "opts"
# containing necessary ncc compiler options for the subdirectories
# where the opts files are located.

###########################################################################
#   SETTINGS
###########################################################################

# Name of the .tar.gz file to open
SRC_FILENAME=opts.tar.gz

# Directory in which the .tar.gz file is located.
SRC_DIR=$PTII/vendors/ptinyos/tinyos-1.x/contrib/ptII

# Output directory (root of tinyos-1.x directory).
OUTPUT_DIR=$PTII/vendors/ptinyos/tinyos-1.x

###########################################################################

# Don't use tar -C or tar -z here, don't use gtar
(cd $OUTPUT_DIR; cat $SRC_DIR/$SRC_FILENAME | gunzip | tar -xvf -)

echo "Extracted $SRC_DIR/$SRC_FILENAME"
