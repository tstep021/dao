package com.mycompany.dao.base;

import javax.naming.Context;
import javax.naming.InitialContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.*;
import oracle.jdbc.pool.OracleDataSource;

import org.apache.log4j.*;

import com.mycompany.dao.util.DAOConstants;

/**
 * @author xxxxxxx.xxxxxxxxx	2010-2013
 * 
 * updated by tatiana.stepourska	2013
 *
 */

/**
 * Connection Cache Properties: from
 * http://docs.oracle.com/cd/B28359_01/java.111/b31224/concache.htm#CDEBCBJC
 * 

The connection cache properties govern the characteristics of a connection cache. 
Application set cache properties using the OracleDataSource method setConnectionCacheProperties

Limit Properties
==================
These properties control the size of the cache.

InitialLimit

	Sets how many connections are created in the cache when it is created or 
	reinitialized. When this property is set to an integer value greater than 0, 
	creating or reinitializing the cache automatically creates the specified 
	number of connections, filling the cache in advance of need.

	Default: 0

MaxLimit

	Sets the maximum number of connection instances the cache can hold. The 
	default value is Integer.MAX_VALUE, meaning that there is no limit enforced 
	by the connection cache, so that the number of connections is limited only 
	by the number of database sessions configured for the database.

	Default: Integer.MAX_VALUE (no limit)

	Note:
	If the number of concurrent connections exceeds the maximum number of sessions 
	configured at the database server, then you will get ORA-00018 error. 
	To avoid this error, you must set a value for the MaxLimit property. 
	This value should be less than the value of the SESSIONS parameter 
	configured for the database server.

MaxStatementsLimit

	Sets the maximum number of statements that a connection keeps open. 
	When a cache has this property set, reinitializing the cache or closing 
	the data source automatically closes all cursors beyond the specified 
	MaxStatementsLimit.

	Default: 0

MinLimit

	Sets the minimum number of connections the cache maintains.

	Default: 0

	Note:
	Setting the MinLimit property does not initialize the cache to contain 
	the minimum number of connections. To do this, use the InitialLimit property.

	When InitialLimit is greater than MinLimit, it is possible to have any number 
	of connections specified by InitialLimit up to a value specified by MaxLimit. 
	Therefore, InitialLimit does not depend on MinLimit.

	Connections can fall below the minimum limit set on the connection pool when 
	JDBC Fast Connection Failover DOWN events are processed. The processing removes 
	affected connections from the pool. MinLimit will be honored as requests to the 
	connection pool increase and the number of connections get past the MinLimit value.

TIMEOUT Properties
===================
These properties control the lifetime of an element in the cache.

InactivityTimeout

	Sets the maximum time a physical connection can remain idle in a connection 
	cache. An idle connection is one that is not active and does not have a logical 
	handle associated with it. When InactivityTimeout expires, the underlying 
	physical connection is closed. However, the size of the cache is not allowed 
	to shrink below minLimit, if it has been set.

	Default: 0 (no timeout in effect)

TimeToLiveTimeout

	Sets the maximum time in seconds that a logical connection can remain open. 
	When TimeToLiveTimeout expires, the logical connection is unconditionally closed, 
	the relevant statement handles are canceled, and the underlying physical 
	connection is returned to the cache for reuse.

	Default: 0 (no timeout in effect)

	See Also: TimeToLiveTimeout and AbandonedConnectionTimeout

AbandonedConnectionTimeout

	Sets the maximum time that a connection can remain unused before the 
	connection is closed and returned to the cache. A connection is considered 
	unused if it has not had SQL database activity.

	When AbandonedConnectionTimeout is set, JDBC monitors SQL database activity 
	on each logical connection. For example, when stmt.execute is called on the 
	connection, a heartbeat is registered to convey that this connection is 
	active. The heartbeats are set at each database execution. If a connection 
	has been inactive for the specified amount of time, the underlying connection 
	is reclaimed and returned to the cache for reuse.

	Default: 0 (no timeout in effect)

	See Also: TimeToLiveTimeout and AbandonedConnectionTimeout
	
PropertyCheckInterval

	Sets the time interval at which the Connection Cache Manager inspects and 
	enforces all specified cache properties. PropertyCheckInterval is set in seconds.

	Default: 900 seconds

Other Properties
================
These properties control miscellaneous cache behavior.

AttributeWeights

	AttributeWeights sets the weight for each attribute set on the connection.
	See Also:"AttributeWeights"
	
ClosestConnectionMatch

	ClosestConnectionMatch causes the connection cache to retrieve the connection with the closest approximation to the specified connection attributes.
	See Also:"ClosestConnectionMatch"
	
ConnectionWaitTimeout

	Specifies cache behavior when a connection is requested and there are already 
	MaxLimit connections active. If ConnectionWaitTimeout is equal to zero, then 
	each connection request waits for zero seconds, that is, null connection is 
	returned immediately. If ConnectionWaitTimeout is greater than zero, then each 
	connection request waits for the specified number of seconds or until a connection 
	is returned to the cache. If no connection is returned to the cache before the 
	timeout elapses, then the connection request returns null.

	Default: zero

LowerThresholdLimit

	Sets the lower threshold limit on the cache. The default is 20 percent of the 
	MaxLimit on the connection cache. This property is used whenever a releaseConnection() 
	cache callback method is registered.

ValidateConnection

	Setting ValidateConnection to true causes the connection cache to test every connection 
	it retrieves against the underlying database. If a valid connection cannot be retrieved, 
	then an exception is thrown.

	Default: false
 */
////////////////////////////////////////////////////////////////////////////////
public class DB  //implements Runnable
////////////////////////////////////////////////////////////////////////////////
{
	private static final Logger logger = Logger.getLogger(DB.class);   

	Connection conn;
	oracle.jdbc.pool.OracleDataSource dp=null;

	private Properties props;
	private String instancecachename;
	private String dataSourceName;

	DB(Properties p, String cachename, String dsn)	throws Exception {
		//if(props==null||dsn==null)
		//	throw new Exception("Failed to construct DB instance: dsn is null, or properties is null");
		props = p;
		instancecachename = cachename;
		dataSourceName = dsn;
		logger.trace("constructor: calling createCache");
		CreateCache(cachename);
	}
	
	public void setPassword(String s){
		if(this.dp!=null)
			this.dp.setPassword(s);
	}
	public void setUser(String s){
		if(this.dp!=null)
			this.dp.setUser(s);
	}

	public String getUser(){
		if(this.dp!=null)
			return this.dp.getUser();
		
		return null;
	}

	private void CreateCache(String cachename) throws Exception {
		String fp = "CreateCache: ";

		logger.trace(fp+"cachename: "+cachename+",dataSourceName: "+this.dataSourceName);
		Context ictx = new InitialContext();
		Context ctx = (Context) ictx.lookup("java:comp/env");
		logger.trace(fp+"ctx: "+ctx);
		dp = null;
		
		dp=(OracleDataSource)ctx.lookup(dataSourceName);
		logger.info(fp + "created data source for dsn "+dataSourceName+": " + dp);
    	dp.setUser(props.getProperty(DAOConstants.KEY_DB_USER));
    	dp.setPassword(props.getProperty(DAOConstants.KEY_DB_PASSWORD));
    	dp.setConnectionCachingEnabled(true); 
        dp.setConnectionCacheName(cachename); 
        Properties cacheProps = new Properties();
        String tmp = null;
        try {
        	tmp =  props.getProperty(DAOConstants.CACHE_MIN_LIMIT).trim();
        	cacheProps.setProperty(DAOConstants.CACHE_MIN_LIMIT,tmp);
        }catch(Exception e){
        	cacheProps.setProperty(DAOConstants.CACHE_MIN_LIMIT, "1");
        }
        try{
        	tmp = props.getProperty(DAOConstants.CACHE_MAX_LIMIT).trim();
        	cacheProps.setProperty(DAOConstants.CACHE_MAX_LIMIT, tmp); 
        }catch(Exception e){
        	cacheProps.setProperty(DAOConstants.CACHE_MAX_LIMIT, "4"); 
        }
        try{
        	tmp = props.getProperty(DAOConstants.CACHE_INITIAL_LIMIT).trim();
        	cacheProps.setProperty(DAOConstants.CACHE_INITIAL_LIMIT, tmp); 
        }catch(Exception e){
        	cacheProps.setProperty(DAOConstants.CACHE_INITIAL_LIMIT, "1");
        }
        try{
        	tmp = props.getProperty(DAOConstants.CACHE_CONNECTION_WAIT_TIMEOUT).trim();
        	cacheProps.setProperty(DAOConstants.CACHE_CONNECTION_WAIT_TIMEOUT, tmp);
        }catch(Exception e){
        	cacheProps.setProperty(DAOConstants.CACHE_CONNECTION_WAIT_TIMEOUT, "30");
        }
        try{
        	tmp = props.getProperty(DAOConstants.CACHE_VALIDATE_CONNECTION).trim();
        	cacheProps.setProperty(DAOConstants.CACHE_VALIDATE_CONNECTION, tmp);
        }catch(Exception e){
        	cacheProps.setProperty(DAOConstants.CACHE_VALIDATE_CONNECTION, "true");
        }
        try{
        	tmp = props.getProperty(DAOConstants.CACHE_INACTIVITY_TIMEOUT).trim();
        	cacheProps.setProperty(DAOConstants.CACHE_INACTIVITY_TIMEOUT, tmp);
        }catch(Exception e){
        	cacheProps.setProperty(DAOConstants.CACHE_INACTIVITY_TIMEOUT, "270");
        }
        /*
        cacheProps.setProperty("MinLimit", "0");
        cacheProps.setProperty("MaxLimit", "4"); 
        cacheProps.setProperty("InitialLimit", "1"); 
        cacheProps.setProperty("ConnectionWaitTimeout", "50");
        cacheProps.setProperty("ValidateConnection", "true");
        cacheProps.setProperty("InactivityTimeout", "270");
        */
    	dp.setConnectionCacheProperties(cacheProps);
    	logger.info(fp + "set data source properties");
	}

	public Connection getConnection() {
		Connection c = null;
		try	{
			c = dp.getConnection();
			if(c==null)
				throw new SQLException("Connection is null");
		}
		catch(Exception sqle) {
			sle("Getting connection", sqle);
		 
			//recreate cache and retry
			try {
				logger.info("getConnection: calling createCache");
				CreateCache(instancecachename);
				c = dp.getConnection();
				if(c==null)
					throw new SQLException("Connection is null");
			}
			catch(SQLException sqle2) {
				sle("Getting connection", sqle2);
				c = null;
			}	
			catch(Exception e2)	 {
				sle("Getting connection", e2);
				c = null;
			}
		}
		finally	{
		
		}
		return c;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb
		.append("dataSource: ").append(this.dataSourceName);
		
		try{
		sb
		.append("\nurl: ").append(dp.getURL());
		}
		catch(SQLException e){}
		
		sb.append("\nuser: ").append(dp.getUser())
		.append("\ncache name: ").append(instancecachename)
		;
		
		try {
		Properties cp = dp.getConnectionCacheProperties();
		
		Enumeration<Object> keys = cp.keys();
		while(keys.hasMoreElements()){
			String key= (String)keys.nextElement();
			String val = props.getProperty(key);
			sb.append("\ncache property: ").append(key).append("=").append(val);
		}
		}
		catch(Exception e){
			
		}
		return sb.toString();
	}

private void sle(String str, Exception e)
{
	String mess = str+" ... " + e.getMessage();
	logger.error(mess+"\n"+ frmtStackTrace(e));
}

private String frmtStackTrace(Exception e)
{
StringWriter sw = new StringWriter();
e.printStackTrace(new PrintWriter(sw));
String stacktrace = sw.toString();
return stacktrace;
}

}
