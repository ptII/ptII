package ptdb.common.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;

import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.exception.DBConnectionException;
import ptdb.kernel.database.AsynchronousDBConnection;
import ptdb.kernel.database.DBConnection;

/** 
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 * @author abijwe
 * This is a factory class that deals with the creation of different types of connections.
 * This class is the interface that will be used to create connections to XML database
 *   
 * @author abijwe
 *
 */
public class DBConnectorFactory {

    
    /** Name for the property for XML database class in the config/ptdb-properties file
     */
    public static String _DB_CLASS_NAME = "DB_Class_Name";

    /** Name for the property for XML database URL/Location in the config/ptdb-properties file
     */
    public static String _DB_URL = "DB_Url";

    /** Name for the property for XML database container name in the config/ptdb-properties file
     */
    public static String _XMLDB_CONTAINER_NAME = "DB_Container_Name";

    /** Name for the property for PtolemyDB cache container name in the config/ptdb-properties file
     */
    public static String _CACHE_CONTAINER_NAME = "Cache_Container_Name";

    /** This API is used to get a synchronous connection to the database. A synchronous connection is a connection that 
     *  executes the query as soon as the executeQuery is called on the connection. A synchronous connection supports both 
     *  execution of queries with transaction and without transactions.
     *  
     * @param isTransactionRequired - Boolean to specify whether this connection needs a transaction or not.
     * @return DBConnection - The executes queries as and when the execute query is called.
     * @throws DBConnectionException - Whenever we face a problem while creating a database connection. These problems could be that 
     *                                  configured connection class does not exist, the path for the database is not found, the container name 
     *                                  is incorrect, the connection to the database could not be established etc. 
     */
    public static DBConnection getSyncConnection(boolean isTransactionRequired)
            throws DBConnectionException {
        return _createConnection(_dbContainerName, isTransactionRequired);
    }

    /** Get an asynchronous connection to the database. An asynchronous connection is a connection that 
     *  enqueues the query as soon as the executeQuery is called on the connection and executes them asynchronously. 
     *  An asynchronous connection should be used for the execution of queries that require a transaction and do not return data from the database.  
     * 
     * @return DBConnection - That enqueues queries as and when the execute query is called and executes them asynchronously
     * @throws DBConnectionException - Whenever we face a problem while creating a database connection. These problems could be that 
     *                                  configured connection class does not exist, the path for the database is not found, the container name 
     *                                  is incorrect, the connection to the database could not be established etc.
     */
    public static DBConnection getAsyncConnection()
            throws DBConnectionException {
        return new AsynchronousDBConnection();
    }

    /** Get a synchronous connection to the cache database. A synchronous connection is a connection that 
     *  executes the query as soon as the executeQuery is called on the connection. A synchronous connection supports both 
     *  execution of queries with transaction and without transactions.
     *  
     * @param isTransactionRequired - Boolean to specify whether this connection needs a transaction or not.
     * @return DBConnection - That executes queries as and when the execute query is called.
     * @throws DBConnectionException - Whenever we face a problem while creating a database connection. These problems could be that 
     *                                  configured connection class does not exist, the path for the database is not found, the container name 
     *                                  is incorrect, the connection to the database could not be established etc. 
     */
    public static DBConnection getCacheConnection(boolean isTransactionRequired)
            throws DBConnectionException {
        return _createConnection(_cacheContainerName, isTransactionRequired);
    }

    /** 
     * 
     * @param containerName
     * @param isTransactionRequired
     * @return
     * @throws DBConnectionException
     */
    private static DBConnection _createConnection(String containerName,
            boolean isTransactionRequired) throws DBConnectionException {
        /**
         * 
         */
        if (_dbClassName == null)
            throw new DBConnectionException(
                    "DBConnection class for ptdb is undefined. Please provide valid classname in ptdb-params.properties");

        if (!_isDBSetupDone)
            throw new DBConnectionException(
                    "XML Database Connection is not configured. Please provide details in ptdb-params.properties");

        DBConnection xmlDBConnection = null;
        DBConnectionParameters dbConnParams = new DBConnectionParameters(
                _dbUrl, containerName, isTransactionRequired);

        /**
         * 
         */
        try {

            Class xmlDBClass = Class.forName(_dbClassName);
            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = DBConnectionParameters.class;
            Constructor<DBConnection> xmlDBConstructor = xmlDBClass
                    .getConstructor(parameterTypes);

            xmlDBConnection = xmlDBConstructor.newInstance(dbConnParams);

        } catch (ClassNotFoundException e) {

            throw new DBConnectionException(
                    "PTDB database connector class not specified in ptdb-params.properties",
                    e);

        } catch (SecurityException e) {

            throw new DBConnectionException("PTDB security exception - "
                    + e.getMessage(), e);

        } catch (NoSuchMethodException e) {

            throw new DBConnectionException(
                    "PTDB database connector class does not have the constructor DBConnection(DBConnectionParameters p)",
                    e);

        } catch (IllegalArgumentException e) {

            throw new DBConnectionException(
                    "PTDB illegal argument exception - " + e.getMessage(), e);

        } catch (InstantiationException e) {

            throw new DBConnectionException("PTDB instantiation exception - "
                    + e.getMessage(), e);

        } catch (IllegalAccessException e) {

            throw new DBConnectionException("PTDB illegal access exception - "
                    + e.getMessage(), e);

        } catch (InvocationTargetException e) {

            throw new DBConnectionException(
                    "PTDB invocation target exception - " + e.getMessage(), e);

        }
        return xmlDBConnection;

    }
    
    /** Container name for XML database cache for Ptolemy DB.
     *  The name is stored in config/ptdb.properties file as Cache_Container_Name Property
     */
    private static String _cacheContainerName;
    
    /** Class name for the class that will be used by the database framework to manage connections and transaction to the actual XML database
     *  The name is stored in config/ptdb.properties file as DB_Class_Name Property 
     *  This class should be of type DBConnection
     */
    private static String _dbClassName;

    /** Container name for XML database Ptolemy DB.
     *  The name is stored in config/ptdb.properties file as DB_Container_Name Property
     */
    private static String _dbContainerName;
    
    /** Path for the location where the XML database file is located.
     *  The name is stored in config/ptdb.properties file as DB_Url Property
     */
    private static String _dbUrl;
    
    /** This boolean is set when the database properties are read from the config/ptdb.properties file. This signifies if the database setup 
     *  for Ptolemy XML Database is done or not. 
     *  
     *   Set to true if the setup is done
     *   Set to false if the setup is incomplete
     * 
     */
    private static boolean _isDBSetupDone;
    
    /** Whenever the class is loaded, we load the properties from config/ptdb.properties and set them for use during creating connections to the 
     *  XML database.  
     *  
     *  This is also used to ascertain if the database setup has been done and if the properties for the database have been set. 
     *  
     *  If the config/ptdb.properties file is not found then it will throw an exception.
     */
    static {
        Properties props = new Properties();
        URL url = ClassLoader.getSystemResource("ptdb-params");

        try {
            if (url == null) {
                throw new ExceptionInInitializerError("Did not find ptdb-params property.");
            }
            
            props.load(url.openStream());
            
            _dbClassName = props.getProperty(_DB_CLASS_NAME);
            _dbUrl = props.getProperty(_DB_URL);

            if (_dbUrl != null && !"".equals(_dbUrl)) {

                _dbContainerName = props.getProperty(_XMLDB_CONTAINER_NAME);
                _cacheContainerName = props.getProperty(_CACHE_CONTAINER_NAME);

            } else {
                _isDBSetupDone = false;
            }

        } catch (IOException e) {

            _isDBSetupDone = false;
            throw new ExceptionInInitializerError("Did not find ptdb-params property file.");            
        }

    }
}
