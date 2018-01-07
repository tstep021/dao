package com.mycompany.dao.base; 

/** Copyright 2012-2013, mycompany Communications */

//import java.util.
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import com.mycompany.dao.util.CriticalException;
import com.mycompany.dao.util.DAOConstants;
import com.mycompany.dao.util.DAOUtils;

//import oracle.jdbc.
import oracle.jdbc.pool.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author tatiana.stepourska
 * 
 * @version 1.0
 */
public class DBFactory 
{	
	static final Logger logger = Logger.getLogger(DBFactory.class);
	
	public static int    db_mode						= DAOConstants.DB_MODE_DIRECT;
	public static String db_password					= null;
	public static String[] db_url						= null;
	public static String db_user						= null;
	public static String db_driver						= "oracle.jdbc.OracleDriver";
	public static String[] db_data_source_names			= null;
	public static OracleDataSource dataSource 			= null;
	public static ArrayList<DB> dbList					= null;
	
	private static long retryDelay						= 150;

    /** Maximum number of seconds that data source will wait 
     * while attempting to connect to a database. 
     * A value of zero specifies that the timeout is 
     * the default system timeout if there is one; 
     * otherwise it specifies that there is no timeout. 
     * When a DataSource object is created the login timeout 
     * is initially zero. */
	public static int connectionLoginTimeout			= DAOConstants.CONNECTION_LOGIN_TIMEOUT;
	
	/**
	 * 
	 */
	public static final OracleDataSource getDataSource(int attempt) throws Exception {		
		return dataSource;
	}
	
	private static final Connection getDataSourceConnection() throws Exception {
		//Connection c = dataSource.getConnection();
		//if(logger.isDebugEnabled())
		//logger.debug("got data source connection: " + c);
		return dataSource.getConnection();
	}
	
	public static final Connection getConnection() throws Exception {
		switch(db_mode){
		case DAOConstants.DB_MODE_POOL:
			return getPooledConnection();
		case DAOConstants.DB_MODE_DIRECT:
			return getConnection(db_url,db_user,db_password);
		case DAOConstants.DB_MODE_JNDI:
			return getDataSourceConnection();
		default:
			return getConnection(db_url,db_user,db_password);
		}
	}
	
	private static final Connection getPooledConnection() throws Exception {
		String fp = "getPooledConnection: ";
		Connection c = null;
		
		try {
			//try primary (recreating cache and retry included)
			c = dbList.get(0).getConnection();
			if(c == null)
				throw new Exception("Failed to get connection to the primary database. ");
		}
		catch(Exception e){
			logger.error(fp+"Error: " + e.getMessage()+", looking for secondary");
			if(dbList.size()<=1){
				logger.info(fp+"No secondary configuration available");
				throw new Exception(e.getMessage() + "No secondary configuration available");
			}
			//try secondary (recreating cache and retry included)
			c = dbList.get(1).getConnection();
			if(c == null)
				throw new Exception("Failed to get connection to the secondary database. ");
		}
		if(logger.isTraceEnabled())
			logger.trace(fp+"got connection: " + c);
		return c; 
	}

	private static final void initConnectionPool(Properties props) throws Exception {
		String fp = "initConnectionPool: ";
		logger.trace(fp + "started");
		int index = -1;
		String cacheName = null;
		dbList		= new ArrayList<DB>();
		DB tmp = null;
		String dsn = null;

		for(int i=0;i<db_data_source_names.length;i++){
			try {
				dsn = db_data_source_names[i];
				try {
					index = dsn.indexOf("/");
					if(index<0)
						index=0;
					//dataSource.setConnectionCacheName(db_data_source_names[0].substring(index)+"_cache");
					cacheName = dsn.substring(index)+"_cache";
				}
				catch(Exception e){
					//dataSource.setConnectionCacheName("SecureCache");
					cacheName = "SecureCache";
				}
				tmp = new DB(props, cacheName, dsn);
				dbList.add(tmp);
			}
			catch(Exception e){
				logger.error("Error creating data source for " + db_data_source_names[i]);
			}
		}
	}

    /**
	* Prepares new database ad-hoc connection 
	* 
	* @param ur
	* @param us
	* @param pw
	* 
	* @throws SQLException
	*/
    private static Connection getConnection(String[] ur, String us, String pw) throws CriticalException, Exception {
    	String fp = "getConnection: ";
    	Connection c = null;
    	logger.info(fp + "ur: " + ur + ", this.url: " + ur);
        if(ur==null || ur.length<=0 ) {
        	logger.error(fp + "ur is null: " + ur + ". No database connection configuration found" ); //- raising alarm...");
        		
    		throw new CriticalException("Failed to find database: No database connection configuration found");
        }
        	
        StackTraceElement[] trace = null;
        
    	try {
    		Class.forName("oracle.jdbc.driver.OracleDriver");
    		//this.driver = new oracle.jdbc.driver.OracleDriver();
    		//this.driver = new oracle.jdbc.OracleDriver();
    	   // DriverManager.registerDriver(this.driver);
    	}
    	catch(Exception e) {
    		throw new CriticalException("Failed to initialize OracleDriver!");
    	}
    	
    	try {
    		c = DriverManager.getConnection(ur[0], us, pw);
    	    logger.info(fp + "got connection to " + ur[0]);
    	}
    	catch(ArrayIndexOutOfBoundsException aoe) {
    		//this.status = DAOConstants.STATUS_ERROR;
    		String errMsg = "Exception connecting to database: " + aoe.getMessage() + "url array out of bounds, please check configuration";
    	    logger.error(fp + errMsg);
    	   
    		throw new CriticalException(errMsg);
    	}
    	catch(Exception e) {
    		//this.status = DAOConstants.STATUS_ERROR;
    	    logger.error(fp + "Exception connecting to " + ur[0] + ": " + e.getMessage());
    	    trace = e.getStackTrace();			  
    		if(trace!=null) {
    			for(int i=0;i<trace.length;i++)	{
    				logger.error(trace[i]);
    			}
    		}
    	    logger.info(fp + "Retrying in " + retryDelay + " ms..");
    	    Thread.sleep(retryDelay);
    				
    	    try	{
    	    	c = DriverManager.getConnection(ur[0], us, pw);
    	    	logger.info(fp + "2nd attempt: got connection to " + ur[0]);
    	    }
    	    catch(Exception ex) {
    	    	logger.error(fp + "Exception connecting to " + ur[0] + ": " + ex.getMessage());
    	    	trace = ex.getStackTrace();			  
    	    	if(trace!=null) {
    	    		for(int i=0;i<trace.length;i++) {
    	    			logger.error(trace[i]);
    	    		}
    	    	}
    		    logger.info(fp + "Trying secondary db.. ");
    		    	
    		    try {
    	    		c = DriverManager.getConnection(ur[1], us, pw);
    	    		//this.status = DAOConstants.STATUS_SUCCESS;
    	    		logger.info(fp + "got connection to " + ur[1]);
    	    	}
    		    catch(ArrayIndexOutOfBoundsException aoe) {
    			    logger.error(fp + "Exception connecting to secondary database - no url configured!");
    			    
    				throw new CriticalException("Failed to find database");
    			}
    	    	catch(Exception exn) {
    	    		logger.error(fp + "Exception connecting to " + ur[1] + ": " + exn.getMessage());
    	    		trace = exn.getStackTrace();
    	    		if(trace!=null) {
    		    		for(int i=0;i<trace.length;i++)	{
    		    			logger.error(trace[i]);
    		    		}
    		    	}
    	    	    logger.info(fp + "Retrying in " + retryDelay + " ms..");
    	    	    Thread.sleep(retryDelay);
    	    	    		
    	    	    try   	{
    	    	    	c =DriverManager.getConnection(ur[1], us, pw);
    	    	    	logger.info(fp + "2nd attempt: got connection to " + ur[1]);
    	    	    }
    	    	    catch(Exception exnx) {
    	    	    	logger.error(fp + "Exception connecting to " + ur[1] + ": " + exnx.getMessage());
    	    	    	trace = exnx.getStackTrace();
    	    	    	if(trace!=null) {
    	    		    	for(int i=0;i<trace.length;i++) {
    	    		    		logger.error(trace[i]);
    	    		    	}
    	    		    }
    	    	    	
    	    			throw new CriticalException("Failed to find database: " + exnx.getMessage());
    	    	    }
    	    	}
    	    }
    	}
    	
    	if(c==null || c.isClosed())
    		throw new CriticalException("Failed to create Connection");
    	return c;
    }

	/**
	 * 
	 * @param dsn
	 * @return
	 * @throws Exception
	 */
	//private static final OracleConnectionPoolDataSource initDataSource(String dsn) throws Exception {
	private static final OracleDataSource initDataSource(String dsn) throws Exception {		
		Context ictx = new InitialContext();
		Context ctx = (Context) ictx.lookup("java:comp/env");
		//OracleConnectionPoolDataSource ds = (OracleConnectionPoolDataSource)ctx.lookup(dsn);	
		OracleDataSource ds = (OracleDataSource)ctx.lookup(dsn);	    
		ds.setUser(db_user);
	    ds.setPassword(db_password);
		return ds;
	}

	/**
	 * 
	 * @param props
	 * @throws Exception
	 */
	public static final void initDB(Properties props) throws Exception {
		String fp = "initDB: ";
		String key 				= null;
		String value 			= null;
		Enumeration<Object> en = props.keys();
		//logger.info(fp + "Listing properties");
			
		while(en.hasMoreElements()) {
			key = (String)en.nextElement();
			value = props.getProperty(key);
			//logger.info(fp + "key: " + key + ", value:" + value);
				
			//check critical properties, if fail
			if(key.compareTo(DAOConstants.KEY_DB_PASSWORD)==0)	{
				logger.info(fp + "key: " + key + ", value:" + value);
				db_password			= value;
				//props.remove(key);
			}
			else if(key.compareTo(DAOConstants.KEY_DB_URL_LOCATION)==0)	{	
				logger.info(fp + "key: " + key + ", value:" + value);
				//Utils.getDBs(value)
				db_url = DAOUtils.getDBs(DAOUtils.getStringFromFile(value));
				if(db_url!=null) {
					int len=db_url.length;
					
					for(int i=0;i<len;i++) {
						logger.info(fp + "db_url: " + db_url[i]);
					}
				}
				//props.remove(key);
			}
			else if(key.compareTo(DAOConstants.KEY_DB_USER)==0)	{
				logger.info(fp + "key: " + key + ", value:" + value);
				db_user				= value; 
				//props.remove(key);
			}
			else if(key.compareTo(DAOConstants.KEY_DB_DRIVER)==0)	{
				logger.info(fp + "key: " + key + ", value:" + value);
				db_driver			= value; 
				//props.remove(key);
			}
			else if(key.compareTo(DAOConstants.KEY_DB_MODE)==0)	{
				logger.info(fp + "key: " + key + ", value:" + value);
				try {
					db_mode			= Integer.parseInt(value);	
					//props.remove(key);
				}
				catch(Exception e) {
					logger.error(fp + "could not parse db mode, using default - " + db_mode);
				}
			}	
			else if(key.compareTo(DAOConstants.KEY_DATA_SRC_NAMES)==0){
				logger.info(fp + "key: " + key + ", value:" + value);
				try{
					db_data_source_names = value.split(",");
					logger.info("db_data_source_names[0]: " + db_data_source_names[0]);
									
					//props.remove(key);
				}
				catch(Exception e){
					logger.info("Error getting data  source names or initializing data source: " + e.getMessage());
				}
			}
		}
		logger.info("Done loading properties");
		
		switch(db_mode){
			case DAOConstants.DB_MODE_POOL:
				logger.info("Calling initConnectionPool");
				initConnectionPool(props);
				break;
			case DAOConstants.DB_MODE_JNDI:
				//OracleDataSource tmp = null;
			//	dataSourceList		= new ArrayList<OracleDataSource>();
				for(int i=0;i<db_data_source_names.length;i++){
					try {
						dataSource = DBFactory.initDataSource(db_data_source_names[i]);
						dataSource.setLoginTimeout(connectionLoginTimeout);
						logger.info("Created data source for " + db_data_source_names[i]);						
					}
					catch(Exception e){
						logger.error("Error creating data source for " + db_data_source_names[i]);
					}
				}
				break;
			default:	
		}
	}

}//end of class