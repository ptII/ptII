/* Interface for parameters that provide web export content.

 Copyright (c) 2011 The Regents of the University of California.
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

package ptolemy.vergil.basic.export.html;

import java.awt.Frame;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.TypedActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.domains.modal.kernel.State;
import ptolemy.gui.ImageExportable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Instantiable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;


///////////////////////////////////////////////////////////////////
//// LinkToOpenTableaux
/**
 * A parameter specifying default hyperlink to associate
 * with icons in model. Putting this into a model causes a hyperlink
 * to be associated with each icon (as specified by the <i>include</i>
 * and <i>instancesOf</i> parameters) that is associated to an
 * open Tableau.
 * If the the frame associated with the tableau implements
 * HTMLExportable, then this is an ordinary link to
 * the HTML exported by the frame. If it instead
 * implements ImageExportable, then this a link that
 * brings up the image in a lightbox.
 * <p>
 * This parameter is designed to be included in a Configuration file
 * to specify global default behavior for export to Web. Just put
 * it in the top level of the Configuration, and this hyperlink
 * will be provided by default.
 * <p>
 * Note that this class works closely with {@link ExportHTMLAction}.
 * It will not work if the {@link WebExporter} provided to its
 * methods is not an instance of ExportHTMLAction.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class LinkToOpenTableaux extends DefaultIconLink {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public LinkToOpenTableaux(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This overrides the base class to ensure that each class
     *  definition is exported only once.
     *  @throws IllegalActionException If a subclass throws it.
     */
    public void provideContent(WebExporter exporter) throws IllegalActionException {
        try {
            _exportedClassDefinitions = new HashSet<NamedObj>();
            super.provideContent(exporter);
        } finally {
            _exportedClassDefinitions = null;
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Override the base class to generate a web page or an image
     *  file for the specified object, if appropriate, and to provide
     *  the href, target, and class attributes to the area attribute
     *  associated with the object.
     *  @param exporter The exporter.
     *  @param object The Ptolemy II object.
     *  @throws IllegalActionException If evaluating parameters fails.
     */
    protected void _provideOutsideContent(WebExporter exporter, NamedObj object)
            throws IllegalActionException {
        
        // Create a table of effigies associated with any
        // open submodel or plot.
        Map<NamedObj, PtolemyEffigy> openEffigies = new HashMap<NamedObj, PtolemyEffigy>();
        Tableau myTableau = exporter.getFrame().getTableau();
        Effigy myEffigy = (Effigy) myTableau.getContainer();
        List<PtolemyEffigy> effigies = myEffigy.entityList(PtolemyEffigy.class);
        for (PtolemyEffigy effigy : effigies) {
            openEffigies.put(effigy.getModel(), effigy);
        }

        PtolemyEffigy effigy = openEffigies.get(object);
        // The hierarchy of effigies does not always follow the model hierarchy
        // (e.g., a PlotEffigy will be contained by the top-level effigy for the
        // model for some reason), so if the effigy is null, we search nonetheless
        // for an effigy.
        if (effigy == null) {
            Effigy candidate = Configuration.findEffigy(object);
            if (candidate instanceof PtolemyEffigy) {
                effigy = (PtolemyEffigy)candidate;
            }
        }
        try {
            if (effigy != null) {
                // _linkTo() recursively calls writeHTML();
                _linkTo(exporter, effigy, object, object, exporter.getExportParameters());
            } else {
                // If the object is a State, we still have work to do.
                if (object instanceof State) {
                    // In a ModalModel, object is a State
                    // inside the _Controller.  But the effigy is stored
                    // under the refinements of that state, which have the
                    // same container as the _Controller.
                    try {
                        TypedActor[] refinements = ((State) object).getRefinement();
                        // FIXME: There may be more
                        // than one refinement. How to open all of them?
                        // We have only one link. For now, just open the first one.
                        if (refinements != null && refinements.length > 0) {
                            effigy = openEffigies.get((NamedObj) refinements[0]);
                            if (effigy != null) {
                                // _linkTo() recursively calls writeHTML();
                                _linkTo(exporter, effigy, (NamedObj) refinements[0],
                                        (NamedObj) refinements[0], exporter.getExportParameters());
                            }
                        }
                    } catch (IllegalActionException e) {
                        // Ignore errors here. Just don't export this refinement.
                    }
                } else if (object instanceof Instantiable) {
                    // There is no open effigy, but the object might
                    // be an instance of a class where the class definition
                    // is open. Look for that.
                    Instantiable parent = ((Instantiable) object).getParent();
                    if (parent instanceof NamedObj) {
                        // Avoid doing the export of a class definition multiple times.
                        if (_exportedClassDefinitions.contains(parent)) {
                            // Have already exported the class definition. Just
                            // need to add the hyperlink.
                            exporter.defineAreaAttribute(object, "href", parent.getName() + "/index.html", true);
                        } else {
                            // Have not exported the class definition. Do so now.
                            _exportedClassDefinitions.add((NamedObj) parent);
                            Effigy classEffigy = Configuration
                                    .findEffigy((NamedObj) parent);
                            if (classEffigy instanceof PtolemyEffigy) {
                                // _linkTo() recursively calls writeHTML();
                                _linkTo(exporter, (PtolemyEffigy)classEffigy, object, 
                                        (NamedObj) parent, exporter.getExportParameters());
                            }
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to generate sub-web-page. ");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** For the specified effigy, define the relevant href, target,
     *  and class area attributes
     *  if the effigy has any open tableaux and those have frames
     *  that implement either HTMLExportable or ImageExportable.
     *  As a side effect, this may generate HTML or image
     *  files or subdirectories in the directory given in the specified
     *  parameters.
     *  @param exporter The exporter.
     *  @param effigy The effigy.
     *  @param sourceObject The source Ptolemy II object (link from).
     *  @param destinationObject The destination object (link to, same as sourceObject,
     *   or alternatively, a class definition for sourceObject).
     *  @param parameters The parameters of the web export that requires this link.
     *  @exception IOException If unable to create required HTML files.
     *  @exception PrinterException If unable to create required HTML files.
     *  @throws IllegalActionException If something goes wrong.
     */
    private void _linkTo(WebExporter exporter, PtolemyEffigy effigy, 
            NamedObj sourceObject, NamedObj destinationObject, ExportParameters parameters)
            throws IOException, PrinterException, IllegalActionException {
        File gifFile;
        // Look for any open tableaux for the object.
        List<Tableau> tableaux = effigy.entityList(Tableau.class);
        // If there are multiple tableaux open, use only the first one.
        if (tableaux.size() > 0) {
            String name = destinationObject.getName();
            Frame frame = tableaux.get(0).getFrame();
            // If it's a composite actor, export HTML.
            if (frame instanceof HTMLExportable) {
                File directory = parameters.directoryToExportTo;
                File subDirectory = new File(directory, name);
                if (subDirectory.exists()) {
                    if (!subDirectory.isDirectory()) {
                        // Move file out of the way.
                        File backupFile = new File(directory, name + ".bak");
                        if (!subDirectory.renameTo(backupFile)) {
                            throw new IOException("Failed to rename \""
                                    + subDirectory + "\" to \""
                                    + backupFile + "\""); 
                        }
                    }
                } else if (!subDirectory.mkdir()) {
                    throw new IOException("Unable to create directory "
                            + subDirectory);
                }
                ExportParameters newParameters = new ExportParameters(subDirectory, parameters);
                ((HTMLExportable) frame).writeHTML(newParameters);
                exporter.defineAreaAttribute(sourceObject, "href", name + "/index.html", true);
                // Add to table of contents file if we are using the Ptolemy website infrastructure.
                boolean usePtWebsite = Boolean.valueOf(StringUtilities.getProperty("ptolemy.ptII.exportHTML.usePtWebsite"));
                if (usePtWebsite) {
                    exporter.addContent("toc.htm", false,
                            " <li><a " + name + "/index.html" + ">"
                            + ExportHTMLAction._getTitleText(destinationObject) + "</a></li>");
                }
            } else if (frame instanceof ImageExportable) {
                gifFile = new File(parameters.directoryToExportTo, name + ".gif");
                OutputStream gifOut = new FileOutputStream(gifFile);
                try {
                    ((ImageExportable) frame).writeImage(gifOut, "gif");
                } finally {
                    gifOut.close();
                }
                // Strangely, the class has to be "iframe".
                // I don't understand why it can't be "lightbox".
                exporter.defineAreaAttribute(sourceObject, "href", name + ".gif", true);
                exporter.defineAreaAttribute(sourceObject, "class", "iframe", true);
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** A set of class definitions for which an export has already occurred. */
    private Set<NamedObj> _exportedClassDefinitions;
}
