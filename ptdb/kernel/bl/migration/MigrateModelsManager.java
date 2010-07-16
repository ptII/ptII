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
package ptdb.kernel.bl.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ptdb.common.dto.XMLDBModel;
import ptdb.kernel.bl.save.SaveModelManager;
import ptolemy.util.StringUtilities;


///////////////////////////////////////////////////////////////
//// MigrateModelsManager

/**
 * 
 * Handle the migration of models from the file system to the database at the 
 * business layer. 
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */

public class MigrateModelsManager {


    //////////////////////////////////////////////////////////////////////
    ////		public methods 					  ////
    
    /**
     * Migrate models from the file system stored in the given path to the database.
     * 
     * @param directoryPath The path on the file system where the models exist.
     * @return A string representing the path to the CSV file the contains the results of the migration.
     * @exception IOException Thrown if there is an error reading or writing files.
     */
    public String  migrateModels(String directoryPath) throws IOException {
        
        String csvFilePath = directoryPath + "\\migrationResults.csv";
        
        // Check if the application has write access to the csv file path.
        try {
            
            File csvFile = new File(csvFilePath);
            csvFile.createNewFile();
            
        } catch (Exception e) {
            
            csvFilePath = StringUtilities.preferencesDirectory() + "migrationResults.csv";
        }
        
        _csvFileWriter = new FileWriter(csvFilePath);
        
        //write the header for the csv file.
        _csvFileWriter.write("Model Name" + "," + "Migration Status" 
                + "," + "Error Messages" + "\n");
        
        File directoryFile = new File(directoryPath);
        
        _readFiles(directoryFile); 
        
        _csvFileWriter.flush();
        
        _csvFileWriter.close();
        
        return csvFilePath;
        
    }

    //////////////////////////////////////////////////////////////////////
    ////		private methods 				////
    
    /**
     * Reads the models in the given directory and store them in the database.
     * 
     * @param directory A file that represents the directory that contains the models.
     * @exception IOException Thrown if there is an error while reading or writing files.
     */
    private void _readFiles(File directory) throws IOException {
      
        // If the path sent is a file, try to create a model in the database out of it.
        if (directory.isFile()) {
            
            String modelName = "";
            
            if (directory.getName().indexOf(".") > 0) {
                
                modelName = directory.getName().substring(0, directory.getName().indexOf("."));
                
            } else {
                
                modelName = directory.getName();
            }
                
            _createDBModel(modelName, _getContent(directory));
            
            
        } else if (directory.isDirectory()) {
            // If the path is a directory, get the list of files and call 
            // this method recursively on each of the files. 
            
            File[] listOfFiles = directory.listFiles();
      
            if (listOfFiles != null) {
                
                for (int i = 0; i < listOfFiles.length; i++) {
                    
                    _readFiles(listOfFiles[i]); 
                    
                }     
            }
        }
    }
    
    
    /**
     * Read the content of a given file and return it to the caller as a string.
     * @param file The file that we need to read its content.
     * @return A string representation of the content of the model.
     * @exception IOException Thrown if there is an error reading the content of the file.
     */
    private String _getContent(File file) throws IOException {
        
        StringBuilder contents = new StringBuilder();
          
        BufferedReader input =  new BufferedReader(new FileReader(file));
          
        try {
            
            String line = null; 

            while (( line = input.readLine()) != null) {
              
                contents.append(line);
              
                contents.append(System.getProperty("line.separator"));
            
            }
        } finally {
            
            input.close();
          
        }
        
        return contents.toString();
        
    }
    
    
    /**
     * create a new model in the database based on the parameters passed.
     * 
     * @param modelName The name of the model to be created.
     * @param modelContent The content of the model to be created.
     * @exception IOException Thrown if there is an error writing the result to the CSV file.
     */
    private void _createDBModel(String modelName, String modelContent) 
            throws IOException {
        
        
        XMLDBModel xmlDBModel = new XMLDBModel(modelName);
        
        xmlDBModel.setModel(modelContent);
        
        xmlDBModel.setIsNew(true);
        
        SaveModelManager saveModelManager = new SaveModelManager();
        
        boolean isSuccessful = false;
        
        try {
            
            saveModelManager.save(xmlDBModel);
            isSuccessful = true;
            
        } catch (Exception e) {
            
            isSuccessful = false;
            _csvFileWriter.write(modelName + "," + "Failed" 
                    + "," + e.getMessage() + "\n");
            
        }
        
        if (isSuccessful == true) {
            
            _csvFileWriter.write(modelName + "," + "Successful" 
                    + "," + " " + "\n");
            
        }
    }

    //////////////////////////////////////////////////////////////////////
    ////		private variables				////
    /** File writer to handle writing the migration result to CSV file. */
    private FileWriter _csvFileWriter; 

}
