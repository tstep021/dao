package com.mycompany.dao.util; 

/** Copyright 2007-2013, mycompany. All rights reserved */

/** 
 * @file        DAOConstants.java	
 * 
 * @description Interface contains a list of DB specific constants.
 * 
 * @author      Tatiana Stepourska
 * 
 * @version      1.0
 */
public interface DAOConstants
{ 	
	public static final String DB_DELIM							= ",";
	/** Database mode - accessing db using data source with cache */
	public static final int  DB_MODE_POOL	                    = 0;
	/** Database mode - accessing db using Test configuration */
	//public static final int  DB_MODE_TEST                       = 1;	
	/** Database mode - accessing db directly */
	public static final int  DB_MODE_DIRECT                   	= 2;
	/** Database mode - accessing db via jndi data source with no cache */
	public static final int  DB_MODE_JNDI                   	= 3;	
	
	/** Context look up key */
	public static final String KEY_CONTEXT_LOOKUP	      		= "java:/comp/env";
	public static final String KEY_DATA_SRC_NAMES	       		= "data_src_names";

	/** Key for db connection string */
	public static final String KEY_DB_URL             			= "db_url";
	/** Key for db connection string location */
	public static final String KEY_DB_URL_LOCATION     			= "db_url_location";
	/** Key for db password */
	public static final String KEY_DB_PASSWORD        			= "db_password";
	/** Key for db user */
	public static final String KEY_DB_USER           			= "db_user";

	/**
	* Key for getting class name of the db driver
	*/
	public static final String KEY_DB_DRIVER			    	= "db_driver";
	/**
	 * Key for setting database connection mode
	 * DB_MODE_POOL  	 = 0 using data source with cache
	 * DB_MODE_TEST      = 1 legacy, not in use
	 * DB_MODE_DIRECT    = 2 ad-hoc, conn created every time
	 * DB_MODE_JNDI      = 3 using tomcat data source with no cache
	 */
	public static final String  KEY_DB_MODE           			= "db_mode";
	
    /** Maximum number of seconds that data source will wait 
     * while attempting to connect to a database. 
     * A value of zero specifies that the timeout is 
     * the default system timeout if there is one; 
     * otherwise it specifies that there is no timeout. 
     * When a DataSource object is created the login timeout 
     * is initially zero. */
	public static final int CONNECTION_LOGIN_TIMEOUT				= 5;
	
	/**
	 * Use CRITICAL when a component is down or cannot be reached.
	 */	
	public static final String SEVERITY_CRITICAL 	= "CRITICAL";
	
	/**
	 * Use MAJOR when a call cannot proceed any further.
	 */
	public static final String SEVERITY_MAJOR 		= "MAJOR";
	
	/**
	 * Use WARNING when a call can only be serviced with limited functionality.
	 */
	public static final String SEVERITY_WARNING 	= "WARN";
	
	/**
	 * Use INFO to indicate startup and shutdown of components.
	 */
	public static final String SEVERITY_INFO 		= "INFO";

	public static final String SP_TAFTEST						= "{call taftest(?)}";
	
	public static final String SQL_TEST_QUERY					= "SELECT to_char(sysdate, 'yyyy-mm-dd hh24:mi:ss') FROM dual";
	
	/** Numeric value for status error */
	public static final int STATUS_ERROR                			= 9;
	/** Numeric value for the initializing status variable */
	public static final int STATUS_INIT								= -99;
	/** Numeric value for status not found */
	public static final int STATUS_NO_DATA_FOUND      				= 69;//TODO find ORA error code
	/** Execution result code - success */
	public static final int STATUS_SUCCESS                  		= 0;
	
	/**
	 * Cache property
	 * 7.4.1.3 MaxStatementsLimit
	 * Sets the maximum number of statements that a connection keeps open. 
	 * When a cache has this property set, reinitializing the cache or closing 
	 * the datasource automatically closes all cursors beyond the specified 
	 * MaxStatementsLimit.
	 * Default: 0
	 */
	public static final String CACHE_MAX_STATEMENT_LIMIT	= "MaxStatementsLimit";
	
	/** Cache property 
	 * 7.4.1.4 MinLimit
	 * Sets the minimum number of connections the cache maintains. 
	 * This guarantees that the cache will not shrink below this minimum limit.
	 * Setting the MinLimit property does not initialize the cache to contain 
	 * the minimum number of connections. To do this, use the InitialLimit 
	 * property. See "InitialLimit".
	 * Default: 0
	 */
	public static final String CACHE_MIN_LIMIT				= "MinLimit";
	
	/** Cache property 
	 * 7.4.1.2 MaxLimit
	 * Sets the maximum number of connection instances the cache can hold. 
	 * The default value is Integer.MAX_VALUE, meaning that there is no limit 
	 * enforced by the connection cache, so that the number of connections is 
	 * limited only by the number of database sessions configured for the database.
	 * Default: Integer.MAX_VALUE (no limit)
	 */
	public static final String CACHE_MAX_LIMIT				= "MaxLimit";
	
	/** Cache property 
	 * 7.4.1.1 InitialLimit
	 * Sets how many connections are created in the cache when it is created 
	 * or reinitialized. When this property is set to an integer value greater 
	 * than 0, creating or reinitializing the cache automatically creates the 
	 * specified number of connections, filling the cache in advance of need.
	 * Default: 0
	 */
	public static final String CACHE_INITIAL_LIMIT			= "InitialLimit";
	
	/** Cache property:
	 * 7.4.3.3 ConnectionWaitTimeout 
	 * Specifies cache behavior when a connection is requested and there are 
	 * already MaxLimit connections active. If ConnectionWaitTimeout is greater 
	 * than zero (0), each connection request waits for the specified number of seconds, 
	 * or until a connection is returned to the cache. If no connection is returned 
	 * to the cache before the timeout elapses, the connection request returns null.
	 */
	public static final String CACHE_CONNECTION_WAIT_TIMEOUT= "ConnectionWaitTimeout";
	
	/** Cache property 
	 * 7.4.3.5 ValidateConnection
	 * Setting ValidateConnection to true causes the connection cache to test every
	 * connection it retrieves against the underlying database.
	 * Default: false
	 */
	public static final String CACHE_VALIDATE_CONNECTION	= "ValidateConnection";
	
	/** Cache property 
	 * 7.4.2.1 InactivityTimeout
	 * Sets the maximum time a physical connection can remain idle in a connection cache. 
	 * An idle connection is one that is not active and does not have a logical handle 
	 * associated with it. When InactivityTimeout expires, the underlying physical 
	 * connection is closed. However, the size of the cache is not allowed to shrink 
	 * below minLimit, if has been set.
	 * Default: 0 (no timeout in effect)
	 */
	public static final String CACHE_INACTIVITY_TIMEOUT		= "InactivityTimeout";
	
	/**
	 * 7.4.2.2 TimeToLiveTimeout
	 * Sets the maximum time in seconds that a logical connection can remain open. 
	 * When TimeToLiveTimeout expires, the logical connection is unconditionally 
	 * closed, the relevant statement handles are canceled, and the underlying 
	 * physical connection is returned to the cache for reuse.
	 * Default: 0 (no timeout in effect)
	 * 
	 * 7.4.2.3 AbandonedConnectionTimeout
	 * Sets the maximum time that a connection can remain unused before the connection 
	 * is closed and returned to the cache. A connection is considered unused if 
	 * it has not had SQL database activity.
	 * When AbandonedConnectionTimeout is set, JDBC monitors SQL database activity 
	 * on each logical connection. For example, when stmt.execute() is invoked 
	 * on the connection, a heartbeat is registered to convey that this connection 
	 * is active. The heartbeats are set at each database execution. If a connection 
	 * has been inactive for the specified amount of time, the underlying connection 
	 * is reclaimed and returned to the cache for reuse.
	 * Default: 0 (no timeout in effect)
	 * 
	 * 7.4.2.4 PropertyCheckInterval
	 * Sets the time interval at which the cache manager inspects and enforces all 
	 * specified cache properties. PropertyCheckInterval is set in seconds.
	 * Default: 900 seconds (15 minutes)
	 */
}//end of DAOConstants 