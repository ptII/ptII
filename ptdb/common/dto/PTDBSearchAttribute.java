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

import ptolemy.data.expr.Variable;

///////////////////////////////////////////////////////////////
//// PTDBSearchAttribute

/**
 * The attribute to wrap the attributes information to be searched in the
 * database. It indicates that this attribute is wrapped from the DB Search
 * frame.
 *
 * @author Alek Wang, Lyle Holsinger
 * @version $Id:$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating
 * @Pt.AcceptedRating
 *
 */
public class PTDBSearchAttribute extends Variable {

    /**
     * Default constructor of PTDBSearchAttribute.
     */
    public PTDBSearchAttribute() {
        
        super();
        _isGenericAttribute = false;
        
    }

    /**
     * Constructor for a generic attribute PTDBSearchAttribute.
     * @param genericClassName Name of the Attribute class.
     */
    public PTDBSearchAttribute(String genericClassName) {
        
        super();
        _isGenericAttribute = true;
        _genericClassName = genericClassName;
        
    }
    
    ///////////////////////////////////////////////////////////////////
    //                    public methods                           ////
    
    /** Get an indication if the attribute is generic.
     * @return Indication if the attribute is generic.
     * @see #setGenericAttribute()
     */
    public boolean isGenericAttribute(){
        
        return _isGenericAttribute;
        
    }
    
    /** Set the attribute as generic or not generic.
     * 
     * @param isGenericAttribute Set the attribute as generic (true) or 
     * not generic (false)
     * 
     */
    public void setGenericAttribute(boolean isGenericAttribute){
        
        _isGenericAttribute = isGenericAttribute;
        if(!_isGenericAttribute) _genericClassName = "";
        
    }
    
    /** Get the class name for a generic attribute
     * @return the class name.
     * @see #setGenericClassName()
     */
    public String getGenericClassName(){
        
        return _genericClassName;
        
    }
    
    /** Set the attribute class name for a generic attribute.
     * 
     * @param genericClassName The attribute class name
     * 
     */
    public void setGenericClassName(String genericClassName){
        
        _genericClassName = genericClassName;
        if(_genericClassName.length()>0){
            
            _isGenericAttribute = true;
        
        }
        
    }
    
    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////
    
    private boolean _isGenericAttribute = false;
    private String _genericClassName = "";
    
}
