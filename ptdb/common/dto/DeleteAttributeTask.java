/*
@Copyright (c) 2010 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


*/
package ptdb.common.dto;

///////////////////////////////////////////////////////////////////
//// DeleteAttributeTask

/**
 * A task request to delete an attribute from the database.
 *
 * <p>It is used as a data transfer object and hold the attribute
 * with its getter and setter method.</p>
 *
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */

public class DeleteAttributeTask {

    /**
     * Construct an instance of the object and set the attribute to be deleted
     * from the database.
     *
     * @param attribute the attribute to be deleted from the database.
     */
    public DeleteAttributeTask(XMLDBAttribute attribute) {

        _xmlDBAttribute = attribute;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the attribute to be deleted from the database.
     *
     * @return The attribute to be deleted from the database.
     * @see #setXMLDBAttribute
     */
    public XMLDBAttribute getXMLDBAttribute() {
        return _xmlDBAttribute;
    }

    /**
     * Set the attribute to be deleted from the database.
     *
     * @param attribute the attribute to be deleted from the database.
     * @see #getXMLDBAttribute
     */
    public void setXMLDBAttribute(XMLDBAttribute attribute) {
        _xmlDBAttribute = attribute;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The attribute to be deleted from the database. */
    private XMLDBAttribute _xmlDBAttribute;

}
