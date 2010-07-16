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
package ptdb.kernel.bl.migration.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.XMLDBModel;
import ptdb.kernel.bl.migration.MigrateModelsManager;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////
//// TestMigrateModelsManager

/**
 * JUnit test for MigrateModelManager class.
 * 
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 * 
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest( { MigrateModelsManager.class })
@SuppressStaticInitializationFor( { "ptdb.common.util.DBConnectorFactory" })
public class TestMigrateModelsManager {


    //////////////////////////////////////////////////////////////////////
    ////		public methods 					  ////

    /**
     * Test the migrateModels() method in the case when the given path is
     * correct and it contains only the models with no sub directories.
     * 
     * @exception Exception Thrown by PowerMock if error occurs in the testing.
     */
    @Test
    public void testMigrateModelsFirstLevel() throws Exception {

        String directoryPath = createDirectory(0, 3);

        MigrateModelsManager migrateModelsManager = new MigrateModelsManager();

        try {
            String csvFilePath = migrateModelsManager
                    .migrateModels(directoryPath);

            assertTrue(csvFilePath.equals(directoryPath + "\\migrationResults.csv"));
            
            

        } catch (Exception e) {

            fail("Failed to migrate models - " + e.getMessage());

        } finally {
            
            deleteDirectory(directoryPath);
            
        }
    }

    /**
     * Test the migrateModels() method in the case when the given path is
     * correct and it contains models at the first level and models inside a
     * directory which is inside the current directory.
     * 
     * @exception Exception Thrown by PowerMock if error occurs in the testing.
     */
    @Test
    public void testMigrateModelsDepth() throws Exception {

        String directoryPath = createDirectory(2, 2);

        MigrateModelsManager migrateModelsManager = new MigrateModelsManager();

        try {

            String csvFilePath = migrateModelsManager
                    .migrateModels(directoryPath);

            assertTrue(csvFilePath.equals(directoryPath
                    + "\\migrationResults.csv"));
            



        } catch (Exception e) {
            fail("Failed to migrate models - " + e.getMessage());
        } finally {
            
            deleteDirectory(directoryPath);
            
        }

    }

    /**
     * Test the migrateModels() method in the case when the given path is
     * correct but it does not contain any models in it.
     * 
     * @exception Exception Thrown by PowerMock if error occurs in the testing.
     */
    @Test
    public void testMigrateModelsNoModels() throws Exception {

        String directoryPath = createDirectory(3, 0);

        MigrateModelsManager migrateModelsManager = new MigrateModelsManager();

        try {

            String csvFilePath = migrateModelsManager
                    .migrateModels(directoryPath);

            assertTrue(csvFilePath.equals(directoryPath
                    + "\\migrationResults.csv"));
            

        } catch (Exception e) {
            fail("Failed to migrate models - " + e.getMessage());
        } finally {
            
            deleteDirectory(directoryPath);
            
        }
    }

    /**
     * Test the migrateModels() method in the case when the given path is
     * incorrect.
     * 
     * @exception Exception Thrown by PowerMock if error occurs in the testing.
     */
    @Test
    public void testMigrateModelsIncorrectPath() throws Exception {

        String directoryPath = "incorrect Path";

        MigrateModelsManager migrateModelsManager = new MigrateModelsManager();

        try {

            String csvFilePath = migrateModelsManager
                    .migrateModels(directoryPath);

            fail("The Migration operation did not report any error when the path was incorrect.");

        } catch (Exception e) {
            assertTrue(true);
        }
    }


    //////////////////////////////////////////////////////////////////////
    ////		private methods 				  ////

    /**
     * Create a directory with levels of depth and models.
     * 
     * @param levels The number of levels of depth required to be created.
     * @param models The number of models to be created in each folder created.
     * @return The parent directory path of the directories created.
     * @exception IOException Thrown if the operation failed to create the folders or the models inside them.
     * 
     */
    private String createDirectory(int levels, int models) throws IOException {

        String fileName = "";

        String fileContent = "<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\"  "
                + "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + " <entity name=\"Test\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + " <property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + " </property>"
                + " <property name=\"_windowProperties\" class=\"ptolemy.actor.gui.WindowPropertiesAttribute\" value=\"{bounds={271, 127, 823, 514}, maximized=false}\">"
                + " </property>"
                + " <property name=\"_vergilSize\" class=\"ptolemy.actor.gui.SizeAttribute\" value=\"[600, 400]\">"
                + " </property>"
                + " <property name=\"_vergilZoomFactor\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"1.0\">"
                + " </property>"
                + " <property name=\"_vergilCenter\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"{300.0, 200.0}\">"
                + " </property>"
                + " <entity name=\"TypeComp1\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + " <entity name=\"AddSubtract\" class=\"ptolemy.actor.lib.AddSubtract\">"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{240, 150}\">"
                + "</property>" + "</entity>" + "<property name=\""
                + XMLDBModel.DB_MODEL_ID_ATTR
                + "\" value=\"ModelX\">"
                + "  </property>"
                + "  <property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" value=\"TRUE\">  </property>"
                + "  <property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{240, 150}\">"
                + " </property>"
                + "</entity>"
                + " <entity name=\"TypeComp2\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<entity name=\"AddSubtract\" class=\"ptolemy.actor.lib.AddSubtract\">"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{240, 150}\">"
                + "</property>"
                + "</entity>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{240, 150}\">"
                + " </property>"
                + "  <property name=\""
                + XMLDBModel.DB_MODEL_ID_ATTR
                + "\" value=\"ModelX\">"
                + "  </property>"
                + "  <property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" value=\"TRUE\">  </property>" + "</entity></entity>";

        String directoryPath = StringUtilities.preferencesDirectory()
                + "topDirectory";

        File directory = new File(directoryPath);

        directory.mkdir();

        for (int i = 0; i < models; i++) {
            FileWriter writer = new FileWriter(directoryPath + "\\testModel"
                    + i + ".xml");

            writer.write(fileContent);

            writer.flush();
            writer.close();
        }

        for (int i = 0; i < levels; i++) {

            String subPath = directoryPath + "\\sub" + i;

            File sub = new File(subPath);

            sub.mkdir();

            for (int j = 0; j < models; j++) {
                FileWriter writer = new FileWriter(sub + "\\testModel" + i + j
                        + ".xml");
                writer.write(fileContent);

                writer.flush();
                writer.close();
            }
        }

        return directoryPath;
    }

    /**
     * Delete a directory and all of its content.
     * @param filepath The path for the directory or file to be deleted.
     */
    private void deleteDirectory(String filepath) {
        File path = new File(filepath);

        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i].getAbsolutePath());
                } else {
                    files[i].delete();
                }
            }
        }
    }

}
