/*

 Copyright (c) 2008 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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
package ptolemy.actor.gt.controller;

import java.net.URL;

import ptolemy.data.ArrayToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// ReadMoML

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ReadModel extends GTEvent {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public ReadModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        modelFile = new FileParameter(this, "modelFile");
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ReadModel newObject = (ReadModel) super.clone(workspace);
        newObject._parser = null;
        return newObject;
    }

    public RefiringData fire(ArrayToken arguments) throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        if (_parser == null) {
            _parser = new MoMLParser();
        } else {
            _parser.reset();
        }

        CompositeEntity model;
        URL url = modelFile.asURL();
        try {
            model = (CompositeEntity) _parser.parse(url, url);
        } catch (Exception e) {
            throw new IllegalActionException(this, e, "Unable to parse the " +
                    "model from file \"" + modelFile.stringValue().trim() +
                    "\" as a CompositeEntity.");
        } finally {
            MoMLParser.purgeModelRecord(url);
        }
        getModelParameter().setModel(model);

        return data;
    }

    public FileParameter modelFile;

    private MoMLParser _parser;
}
