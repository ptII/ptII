/*
 Copyright (c) 2003-2007 THALES.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL THALES BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE
 OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THALES HAS BEEN
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 THALES SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THALES HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

 Created on 8 juil. 2003

 */
package thales.vergil;

import java.net.URL;

import javax.swing.UIManager;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.VergilApplication;
import thales.actor.gui.SingleWindowHTMLViewer;

//////////////////////////////////////////////////////////////////////////
//// SingleWindowApplication

/**
 Main entry point for the SingleWindow mode.

 @author J&eacute;r&ocirc;me Blanc & Benoit Masson, Thales Research and technology, 12 nov. 2003
 @version $Id$
 @since
 @Pt.ProposedRating Yellow (jerome.blanc)
 @Pt.AcceptedRating Red (cxh)
 */
public class SingleWindowApplication extends VergilApplication {

    /** Construct a single window application.
     * @param args Arguments to pass to
     * {@link ptolemy.vergil.VergilApplication#VergilApplication(String[])}.
     * @exception Exception
     */
    public SingleWindowApplication(String[] args) throws Exception {
        super(args);
    }

    /** Create a single window application.
     * @param args Arguments to pass to
     * {@link ptolemy.vergil.VergilApplication#VergilApplication(String[])}.
     */
    public static void main(String[] args) {
        try {
            new SingleWindowApplication(args);
        } catch (Exception ex) {
            MessageHandler.error("Command failed", ex);
            System.exit(0);
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }

            System.exit(0);
        }
    }

    /** The main frame. */
    public static SingleWindowHTMLViewer _mainFrame;

    /* (non-Javadoc)
     * @see ptolemy.actor.gui.MoMLApplication#_createDefaultConfiguration()
     */
    protected Configuration _createDefaultConfiguration() throws Exception {
        return readConfiguration(specToURL("thales/configs/singleWindow/singleWindowConfiguration.xml"));
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.gui.MoMLApplication#_createEmptyConfiguration()
     */
    protected Configuration _createEmptyConfiguration() throws Exception {
        Configuration configuration = _createDefaultConfiguration();

        try {
            UIManager.setLookAndFeel(System.getProperty("swing.defaultlaf"));
        } catch (Exception e) {
            // Ignore exceptions, which only result in the wrong look and feel.
        }

        // FIXME: This code is Dog slow for some reason.
        URL inurl = specToURL("thales/configs/singleWindow/singleWindowWelcomeWindow.xml");
        _parser.reset();
        _parser.setContext(configuration);
        _parser.parse(inurl, inurl);

        Effigy doc = (Effigy) configuration.getEntity("directory.doc");
        URL idurl = specToURL("ptolemy/configs/full/intro.htm");
        doc.identifier.setExpression(idurl.toExternalForm());

        if (_mainFrame != null) {
            _mainFrame.setConfiguration(configuration);
        }

        return configuration;
    }
}
