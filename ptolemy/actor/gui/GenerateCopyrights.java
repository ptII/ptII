/* Generate a web page that contains links for the appropriate copyrights

 Copyright (c) 2003-2010 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// GenerateCopyrights

/**
 Generate an HTML file that contains links to the appropriate
 copyrights for entities in the configuration.
 This class looks for particular classes, and if the class is found
 in the classpath, then a corresponding html file is included
 in the list of copyrights.

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class GenerateCopyrights {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate HTML about the copyrights for classes that might
     *  be present in the configuration.  This method contains
     *  a list of classes and corresponding copyrights.  If
     *  a class in the list is present, then we generate html
     *  that contains a link to the copyright.  Note that if the
     *  copyright file need not be present on the local machine,
     *  we generate a link to the copy on the Ptolemy website.
     *
     *  <p>If the configuration contains an _applicationName attribute
     *  then that attributed is used as the name of the application
     *  in the generated text.  If _applicationName is not present,
     *  then the default name is "Ptolemy II".
     *
     *  <p>If the configuration contains an _applicationCopyright
     *  StringAttribute, then the value of that attributed is used
     *  as the location of the copyright html file.  If
     *  _applicationCopyright is not present, then
     *  "ptolemy/configs/doc/copyright.htm" is used.
     *
     *  <p>If the configuration has a parameter called
     *  _applicationCopyrights that is an array of records where
     *  each element is a record
     *  <pre>
     *  {actor="ptolemy.actor.lib.Foo", copyright="foo.htm"}
     *  </pre>
     *  then we add that actor/copyright pair to the list of potential
     *  copyrights.
     *
     *  @param configuration The configuration to look for the
     *  _applicationName and _applicationCopyright attributes in.
     *  @return A String containing HTML that describes what
     *  copyrights are used by Entities in the configuration
     */
    public static String generateHTML(Configuration configuration) {
        // A Map of copyrights, where the key is a URL naming
        // the copyright and the value is a List of entities
        // that use that as a copyright.
        Map copyrightsMap = new HashMap();

        // Add the classnames and copyrights.
        // Alphabetical by className.
        _addIfPresent(copyrightsMap,
                "com.jgoodies.forms.factories.DefaultComponentFactory",
                "com/jgoodies/jgoodies-copyright.htm");

        _addIfPresent(copyrightsMap, "actor.lib.logic.fuzzy.FuzzyLogic",
                "ptolemy/actor/lib/logic/fuzzy/FuzzyEngine-copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.domains.gro.kernel.GRODirector",
                "ptolemy/domains/gro/JOGL-copyright.htm");

        _addIfPresent(copyrightsMap, "jni.GenericJNIActor",
                "jni/launcher/launcher-copyright.htm");

        _addIfPresent(copyrightsMap,
                "org.mlc.swing.layout.LayoutConstraintsManager",
                "org/mlc/mlc-copyright.htm");

        _addIfPresent(copyrightsMap, "caltrop.ptolemy.actors.CalInterpreter",
                "ptolemy/cal/saxon-copyright.htm");

        _addIfPresent(copyrightsMap, "caltrop.ptolemy.actors.CalInterpreter",
                "ptolemy/cal/cup-copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.colt.ColtRandomSource",
                "ptolemy/actor/lib/colt/colt-copyright.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.backtrack.eclipse.ast.TypeAnalyzer",
                "ptolemy/backtrack/eclipse/ast/eclipse-copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.backtrack.util.ClassFileLoader",
                "ptolemy/backtrack/util/gcj-copyright.html");

        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.io.comm.SerialComm",
                "ptolemy/actor/lib/io/comm/copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.jai.JAIImageToken",
                "ptolemy/actor/lib/jai/jai-copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.jmf.JMFImageToken",
                "ptolemy/actor/lib/jmf/jmf-copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.joystick.Joystick",
                "ptolemy/actor/lib/joystick/copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.python.PythonScript",
                "ptolemy/actor/lib/python/copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.x10.X10Interface",
                "ptolemy/actor/lib/x10/x10-copyright.htm");

        _addIfPresent(copyrightsMap, "com.mysql.jdbc.Driver",
                "ptolemy/actor/lib/database/mysql-copyright.htm");

        _addIfPresent(copyrightsMap, "oracle.jdbc.OracleDriver",
                "ptolemy/actor/lib/database/ojdbc-copyright.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.ptolemy.actor.ptalon.PtalonActor",
                "ptolemy/actor/ptalon/ptalon-copyright.html");

        _addIfPresent(copyrightsMap, "ptolemy.copernicus.kernel.KernelMain",
                "ptolemy/copernicus/kernel/soot-copyright.html");

        _addIfPresent(copyrightsMap,
                "ptolemy.domains.gr.lib.quicktime.MovieViewScreen2D",
                "ptolemy/domains/gr/lib/quicktime/quicktime-copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.domains.gr.kernel.GRActor",
                "ptolemy/domains/gr/lib/java3d-copyright.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.domains.psdf.kernel.PSDFScheduler",
                "ptolemy/domains/psdf/mapss-copyright.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.vergil.basic.layout,KielerLayoutGUI",
                "ptolemy/vergil/basic/layout/kieler/kieler-copyright.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.actor.lib.opencv.OpenCVReader",
                "ptolemy/actor/lib/opencv/opencv-copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.matlab.Expression",
                "ptolemy/matlab/copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.vergil.pdfrenderer.PDFAttribute",
                "ptolemy/vergil/pdfrenderer/PDFRenderer-copyright.htm");

        _addIfPresent(copyrightsMap, "vendors.vr.Volume",
                "ptolemy/domains/gr/lib/vr/vr-copyright.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.vergil.basic.itextpdf.ExportPDFAction",
                "ptolemy/vergil/basic/itextpdf/itextpdf-copyright.htm");

        // FIXME: This is really lame needing to add in sub package
        // copyrights for other apps, but it is the best we can do right now.
        _addIfPresent(copyrightsMap, "mescal.domains.mescalPE.kernel.parser",
                "mescal/configs/doc/cup-copyright.htm");

        _addIfPresent(copyrightsMap,
                "org.satlive.jsat.objects.ExternalLiteral",
                "mescal/configs/doc/jsat-copyright.htm");

        // Check for the _applicationCopyrights parameter
        try {
            Parameter applicationCopyrights = (Parameter) configuration
                    .getAttribute("_applicationCopyrights", Parameter.class);

            if (applicationCopyrights != null) {
                ArrayToken copyrightTokens = (ArrayToken) applicationCopyrights
                        .getToken();

                for (int i = 0; i < copyrightTokens.length(); i++) {
                    StringToken actorToken = (StringToken) (((RecordToken) copyrightTokens
                            .getElement(i)).get("actor"));
                    StringToken copyrightToken = (StringToken) (((RecordToken) copyrightTokens
                            .getElement(i)).get("copyright"));
                    _addIfPresent(copyrightsMap, actorToken.stringValue(),
                            copyrightToken.stringValue());
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);

            // This application has no _applicationCopyrights
        }

        StringBuffer htmlBuffer = new StringBuffer(
                generatePrimaryCopyrightHTML(configuration));
        Iterator copyrights = copyrightsMap.entrySet().iterator();

        if (copyrights.hasNext()) {
            // DSP configuration might not include other actors.
            htmlBuffer
                    .append("<p>Below we list features and the "
                            + "corresponding copyright "
                            + " of the package that is used.  If a feature is not "
                            + "listed below, then the "
                            + _getApplicationName(configuration)
                            + " copyright is the "
                            + "only copyright."
                            + "<table>\n"
                            + "  <tr><th>Feature</th>\n"
                            + "      <th>Copyright of package used by the feature</th>\n"
                            + "  </tr>\n");

            while (copyrights.hasNext()) {
                Map.Entry entry = (Map.Entry) copyrights.next();
                String copyrightURL = (String) entry.getKey();
                Set entitiesSet = (Set) entry.getValue();

                StringBuffer entityBuffer = new StringBuffer();
                Iterator entities = entitiesSet.iterator();

                while (entities.hasNext()) {
                    if (entityBuffer.length() > 0) {
                        entityBuffer.append(", ");
                    }

                    String entityClassName = (String) entities.next();

                    // If we have javadoc, link to it.
                    // Assuming that entityClassName contains a dot separated
                    // classpath here.
                    String docName = "doc.codeDoc." + entityClassName;
                    String codeDoc = _findURL(docName.replace('.', '/')
                            + ".html");
                    entityBuffer.append("<a href=\"" + codeDoc + "\">"
                            + entityClassName + "</a>");
                }

                String foundCopyright = _findURL(copyrightURL);

                htmlBuffer.append("<tr><td>" + entityBuffer
                        + "</td>\n    <td> <a href=\"" + foundCopyright
                        + "\"><code>" + _canonicalizeURLToPTII(foundCopyright)
                        + "</code></a></td>\n</tr>\n");
            }

            htmlBuffer.append("</table>\n</p>");
        }

        htmlBuffer.append("<p>Other information <a href=\"about:\">about</a>\n"
                + "this configuration.\n" + "</body>\n</html>");
        return htmlBuffer.toString();
    }

    /** Generate the primary copyright.  Include a link to the other
     *  copyrights.
     *  @param configuration The configuration in which to look for the
     *  _applicationName attribute.
     *  @return A String containing HTML that describes what
     *  copyrights are used by Entities in the configuration
     */
    public static String generatePrimaryCopyrightHTML(
            Configuration configuration) {

        String defaultApplicationCopyright = "ptolemy/configs/doc/copyright.htm";
        String applicationCopyright = defaultApplicationCopyright;

        try {
            StringAttribute applicationCopyrightAttribute = (StringAttribute) configuration
                    .getAttribute("_applicationCopyright",
                            StringAttribute.class);

            if (applicationCopyrightAttribute != null) {
                applicationCopyright = applicationCopyrightAttribute
                        .getExpression();
            }
        } catch (Exception ex) {
            // Ignore and use the default applicationCopyright
        }

        String applicationName = _getApplicationName(configuration);
        String applicationCopyrightURL = _findURL(applicationCopyright);

        String aelfredCopyright = _findURL("com/microstar/xml/README.txt");

        String defaultCSS = _findURL("doc/default.css");
        StringBuffer htmlBuffer = new StringBuffer();
        htmlBuffer.append("<html>\n<head>\n<title>Copyrights</title>\n"
                + "<link href=\"" + defaultCSS + "\" rel=\"stylesheet\""
                + "type=\"text/css\">\n" + "</head>\n<body>\n" + "<h1>"
                + applicationName + "</h1>\n"
                + "The primary copyright for the " + applicationName
                + " System can be\n" + "found in <a href=\""
                + applicationCopyrightURL + "\"><code>"
                + _canonicalizeURLToPTII(applicationCopyrightURL)
                + "</code></a>.\n"
                + "This configuration includes code that uses packages\n"
                + "with the following copyrights.\n");

        if (!applicationCopyright.equals(defaultApplicationCopyright)) {
            // If the Ptolemy II copyright is not the main copyright, add it.
            String ptolemyIICopyright = _findURL(defaultApplicationCopyright);
            htmlBuffer.append("<p>" + applicationName + " uses Ptolemy II "
                    + VersionAttribute.CURRENT_VERSION.getExpression() + ".\n"
                    + "PtolemyII is covered by the copyright in\n "
                    + "<a href=\"" + ptolemyIICopyright + "\"><code>"
                    + _canonicalizeURLToPTII(ptolemyIICopyright)
                    + "</code></a>\n");
        }

        htmlBuffer.append("<p>" + applicationName
                + " uses AElfred as an XML Parser.\n"
                + "AElfred is covered by the copyright in\n " + "<a href=\""
                + aelfredCopyright + "\"><code>"
                + _canonicalizeURLToPTII(aelfredCopyright) + "</code></a>\n");

        return htmlBuffer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // If a className is can be found, then add the className
    // and copyrightPath to copyrightsMap
    private static void _addIfPresent(Map copyrightsMap, String className,
            String copyrightPath) {
        try {
            Class.forName(className);

            Set entitiesSet = (Set) copyrightsMap.get(copyrightPath);

            if (entitiesSet == null) {
                // This is the first time we've seen this copyright,
                // add a key/value pair to copyrights, where the key
                // is the URL of the copyright and the value is Set
                // of entities that correspond with that copyright.
                entitiesSet = new HashSet();

                entitiesSet.add(className);
                copyrightsMap.put(copyrightPath, entitiesSet);
            } else {
                // Other classes are using this copyright, so add this
                // one to the list.
                entitiesSet.add(className);
            }
        } catch (Throwable throwable) {
            // Ignore, this just means that the classname could
            // not be found, so we need not include information
            // about the copyright.
        }
    }

    // Truncate a jarURL so that the very long jar:file:...! is
    // converted to $PTII.  If the string does not start with jar:file
    // or if it startes with jar:file but does not contain a !, then
    // it is returned unchanged.  This method is used to truncate
    // the very long paths that we might see under Web Start.
    private static String _canonicalizeURLToPTII(String path) {
        if (!path.startsWith("jar:file")) {
            return path;
        } else {
            int index = path.lastIndexOf("!");

            if (index == -1) {
                return path;
            } else {
                return "$PTII" + path.substring(index + 1, path.length());
            }
        }
    }

    // Look for the localURL, and if we cannot find it, refer
    // to the url on the website that corresponds with this version of
    // Ptolemy II
    private static String _findURL(String localURL) {
        try {
            URL url = Thread.currentThread().getContextClassLoader()
                    .getResource(localURL);
            return url.toString();
        } catch (Exception ex) {
            // Ignore it and use the copyright from the website
            // Substitute in the first two tuples of the version
            // If the version is 3.0-beta-2, we end up with 3.0
            StringBuffer majorVersionBuffer = new StringBuffer();

            Iterator tuples = VersionAttribute.CURRENT_VERSION.iterator();

            // Get the first two tuples and separate them with a dot.
            if (tuples.hasNext()) {
                majorVersionBuffer.append((String) tuples.next());

                if (tuples.hasNext()) {
                    majorVersionBuffer.append(".");
                    majorVersionBuffer.append((String) tuples.next());
                }
            }

            String majorVersion = majorVersionBuffer.toString();
            return "http://ptolemy.eecs.berkeley.edu/ptolemyII/" + "ptII"
                    + majorVersion + "/ptII" + majorVersion + "/" + localURL;
        }
    }

    /** Get the application name from the _applicationName parameter.
     *  If the _applicationName parameter is not set, then return
     *  "Ptolemy II"
     */
    private static String _getApplicationName(Configuration configuration) {
        // Now generate the HTML
        String applicationName = "Ptolemy II";

        try {
            StringAttribute applicationNameAttribute = (StringAttribute) configuration
                    .getAttribute("_applicationName", StringAttribute.class);

            if (applicationNameAttribute != null) {
                applicationName = applicationNameAttribute.getExpression();
            }
        } catch (Exception ex) {
            // Ignore and use the default applicationName
        }
        return applicationName;
    }

}
