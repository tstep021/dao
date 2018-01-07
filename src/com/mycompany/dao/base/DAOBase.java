package com.mycompany.dao.base;

/** Copyright (c) 2004-2011 mycompany. All Rights Reserved. */

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
//import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;


//import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleOCIFailover;
//import oracle.jdbc.OracleStatement;
//import oracle.jdbc.OraclePreparedStatement;
//import oracle.jdbc.OracleResultSet;

import org.apache.log4j.Logger;

import com.mycompany.dao.util.CriticalException;
import com.mycompany.dao.util.DAOConstants;

/**
 * @file   		DAOBase.java
 * 
 * @description This is a superclass for handling DB calls
 * 
 * @author 		tstepourska
 * 
 * @version 	1.0    	2004
 * 				1.1 	Oct 2007
 * 				1.2 	Nov 2008  DB call error handling moved up to here (as super class) in   
 *                      the invoke method, new abstract method and new member variable status introduced
 *              1.3		Jul 2009  Added Data source and TAF handling, error handling
 *              				  Removed OML related variables
 *              1.4		2011 data source connection with TAF: to retry on db shutdown/startup, if unsuccessfull,
 *              			 connect directly to secondary db, if that is unsuccessfull, raise alarm
 */
public abstract class DAOBase implements OracleOCIFailover
{
	static final protected Logger lo = Logger.getLogger(DAOBase.class);

	/**
	 * For Oracle 9i onwards you should use oracle.jdbc.OracleDriver 
	 * rather than oracle.jdbc.driver.OracleDriver as Oracle have stated 
	 * that oracle.jdbc.driver.OracleDriver is deprecated and support for 
	 * this driver class will be discontinued in the next major release. 
	 */
	//protected oracle.jdbc.driver.OracleDriver driver = null;
	protected oracle.jdbc.OracleDriver driver	= null;
	protected OracleConnection connection            = null;
	protected CallableStatement[] callStmt     = null;
	protected Statement[] stmt                 = null;
	protected PreparedStatement[] pStmt        = null;
	protected ResultSet[] rset                 = null;
	
    protected String[] url      = null;
    protected String user       = null;
    protected String password   = null;
    protected String sql        = null;
    
    protected String[] dataSourceNames = null;
	protected OracleDataSource dataSource = null;
    protected int status			= DAOConstants.STATUS_INIT;
    
    /** Connection retry delay in ms */
    protected long retryDelay         = 100;
    
    /** Number of retry connecting to the same instance.
     * For the TAF use 1
     */
    protected int numberOfRetries     = 2;
    
    /** Maximum number of seconds that data source will wait 
     * while attempting to connect to a database. 
     * A value of zero specifies that the timeout is 
     * the default system timeout if there is one; 
     * otherwise it specifies that there is no timeout. 
     * When a DataSource object is created the login timeout 
     * is initially zero. */
    protected int connectionLoginTimeout = DAOConstants.CONNECTION_LOGIN_TIMEOUT;

    /**
     * Defines the database connection type
     */
	public int dbMode = DAOConstants.DB_MODE_DIRECT;

	/** Variable to check if need to raise alarm */
	private CriticalException critical		= null;
	
	/**
	 * Default constructor. 
	 * Each subclass must implement one of the constructors  
	 * where create all database objects participating in the call 
	 * in the form of arrays
	 */
	public DAOBase() {
		/*
		 * Example declaration, assuming 2 callable statements are 
		 * needed, and result set is being returned and 
		 * processed, so we need to create array of Statements 
		 * with length of 2 elements and array of ResultSets with 
		 * the length of 1 element:
		 * 
		 * 
		 * this.callStmt = new CallableStatement[1];
		 * this.rset = new ResultSet[1];
		 * 
		 * or 
		 * 
		 * this.callStmt = new OracleCallableStatement[1];
		 * this.rset = new OracleResultSet[1];
		 */
	}
	
	/**
	 * Constructor with parameter dataSourceName. Implies 
	 * dbMode JNDI - 3 
	 * Each subclass must implement one of the constructors  
	 * where create all database objects participating in the call 
	 * in the form of arrays
	 */
	public DAOBase(String dsn) {
		
		this.dbMode 		= DAOConstants.DB_MODE_JNDI; 
		this.dataSourceNames = new String[]{ dsn};
		
		/*
		 * Example declaration, assuming 2 callable statements are 
		 * needed, and 1 result set is being returned and 
		 * processed, so we need to create array of Statements 
		 * with length of 2 elements and array of ResultSets with 
		 * the length of 1 element:
		 * 
		 * 
		 * this.callStmt = new CallableStatement[1];
		 * this.rset = new ResultSet[1];
		 * 
		 * 		 or 
		 * 
		 * this.callStmt = new OracleCallableStatement[1];
		 * this.rset = new OracleResultSet[1];
		 */
		
	}
	
	/**
	 * Constructor with parameter dataSourceNames 
	 * for the secondary db connection. Implies 
	 * dbMode JNDI - 3 
	 * Each subclass must implement one of the constructors  
	 * where create all database objects participating in the call 
	 * in the form of arrays
	 */
	public DAOBase(String[] dsn) {
		
		this.dbMode 		= DAOConstants.DB_MODE_JNDI; 
		this.dataSourceNames = dsn;
		
		/*
		 * Example declaration, assuming 2 callable statements are 
		 * needed, and 1 result set is being returned and 
		 * processed, so we need to create array of Statements 
		 * with length of 2 elements and array of ResultSets with 
		 * the length of 1 element:
		 * 
		 * 
		 * this.callStmt = new CallableStatement[1];
		 * this.rset = new ResultSet[1];
		 * 
		 * 		 or 
		 * 
		 * this.callStmt = new OracleCallableStatement[1];
		 * this.rset = new OracleResultSet[1];
		 */
		
	}
	
	/**
	 * Constructor with parameter dbMode. 
	 * Each subclass must implement one of the constructors  
	 * where create all database objects participating in the call 
	 * in the form of arrays
	 */
	public DAOBase(int m) {
		
		this.dbMode = m; 
		
		/*
		 * Example declaration, assuming 2 callable statements are 
		 * needed, and result set is being returned and 
		 * processed, so we need to create array of Statements 
		 * with length of 2 elements and array of ResultSets with 
		 * the length of 1 element:
		 * 
		 * 
		 * this.callStmt = new CallableStatement[1];
		 * this.rset = new ResultSet[1];
		 * 
		 * 		 or 
		 * 
		 * this.callStmt = new OracleCallableStatement[1];
		 * this.rset = new OracleResultSet[1];
		 */		
	}
	
	/**
	 * Constructor with parameters url, username and password.
	 * Implies dbMode direct - 2 
	 * Each subclass must implement one of the constructors  
	 * where create all database objects participating in the call 
	 * in the form of arrays
	 */
	public DAOBase(String[] ur, String us, String pw) {
		
		this.dbMode 	= DAOConstants.DB_MODE_DIRECT; 
		this.url 		= ur;
		this.user 		= us;
		this.password 	= pw;
		
		/*
		 * Example declaration, assuming 2 callable statements are 
		 * needed, and result set is being returned and 
		 * processed, so we need to create array of Statements 
		 * with length of 2 elements and array of ResultSets with 
		 * the length of 1 element:
		 * 
		 * 
		 * this.callStmt = new CallableStatement[1];
		 * this.rset = new ResultSet[1];
		 */	
	}
	
	public CriticalException getCriticalError() {
		return this.critical;
	}
	
	public void setCriticalError(Throwable t) {
		this.critical = new CriticalException(t);
	}
	
	public void setConnectionLoginTimeout(int t) {
		this.connectionLoginTimeout = t;
	}
	public int getConnectionLoginTimeout() {
		return this.connectionLoginTimeout;
	}
    /**
    * Implements interface method
    * 
    * 	// Possible Failover Types
	*	public static final int FO_SESSION = 1;
	*   public static final int FO_SELECT  = 2;
	*	public static final int FO_NONE    = 3;
	*	public static final int;
	*
	*	// Possible Failover events registered with callback
	*	public static final int FO_BEGIN  = 1;
	*	public static final int FO_END    = 2;
	*	public static final int FO_ABORT  = 3;
	*	public static final int FO_REAUTH = 4;
	*	public static final int FO_ERROR  = 5;
	*	public static final int FO_RETRY  = 6;
	*	public static final int FO_EVENT_UNKNOWN = 7;
	*  
	*  @param Connection conn
	*  @param Object ctxt
	*  @param int type
	*  @param int event
    */
    public int callbackFn (Connection conn,
            Object ctxt, //any thing the user wants to save
            int type, //one of the above possible Failover Types
            int event ) //One of the above possible Failover Events
    {
    	String fp = "callbackFn: ";
    	lo.debug(fp + "started");
    	
    	String foType[] = { "FO_SESSION", "FO_SELECT", "FO_NONE" };
    	String foEvent[] = { "FO_BEGIN", "FO_END", "FO_ABORT", "FO_REAUTH", "FO_ERROR", "FO_RETRY", "FO_EVENT_UNKNOWN" };
    	try {
    		lo.debug(fp + "The connection for which the failover occurred is :"
    		                 + this.connection.getMetaData().toString());
    	} catch (SQLException se) {
    		lo.error(fp + se.getMessage() + " - " + se.getErrorCode());
    	}
    		   
    	if(lo.isDebugEnabled()) {
    		lo.debug(fp + "FAILOVER TYPE is :  " + foType[type-1]);
    		lo.debug(fp + "FAILOVER EVENT is : " + foEvent[event-1]);
    	}        
    		        
    	switch (event) {
    	    case FO_BEGIN:
    	        lo.info(fp + "Failover event is BEGIN");
    	        break;
    	    case FO_END:            	
    	        lo.info(fp + "Failover event is FO_END");
    	        break;
    	    case FO_ABORT:
    	    	lo.info(fp + "Failover event is FO_ABORT");
    	        break;
    	    case FO_REAUTH:
    	    	lo.info(fp + "Failover event is FO_REAUTH");
    	        break;
    	    case FO_ERROR:
    	    	lo.info(fp + "Failover event is FO_ERROR");
    	        break;
    	    case FO_RETRY:
    	    	lo.info(fp + "Failover event is FO_RETRY");
    	        break;
    	    case FO_EVENT_UNKNOWN:
    	    	lo.info(fp + "Failover event is FO_EVENT_UNKNOWN");
    	        break;
    	    default:
    	    	lo.info(fp + "Default: " + event);
    	}
    		        
    	lo.debug(fp + "Before returning from the Callback Function. " ) ;
    	return 0;  		       
    }
 
	/**
	 * Gets the database connection according to DB mode and 
	 * other supplied information
	 * 
	 * @throws SQLException
	 * @throws Exception
	 */
	public void getConnection() throws CriticalException, Exception, Error  {
		String fp = "getConnection: ";
		
	    switch(this.dbMode)	{
	    	case DAOConstants.DB_MODE_JNDI:
	    		lo.info(fp + "getting JNDI connection");
	    		this.getDataSourceConnection(this.dataSourceNames);
	    		break;
	    	case DAOConstants.DB_MODE_DIRECT:
	    		lo.info(fp + "getting DIRECT connection");
	    		this.getDirectConnection(this.url,this.user, this.password);
	    		break;
	    	default:
	    		lo.info(fp + "Unknown DB mode received, executing default DIRECT connection");
	    		this.getDirectConnection(this.url,this.user, this.password);
	    }
	   
	    lo.info(this.conToString());
    }
	
	public String conToString() {
		StringBuffer result = new StringBuffer();
		try {
		    DatabaseMetaData md = this.connection.getMetaData();

			result
			.append("Connected to ").append(md.getURL())
			.append(" as ").append(md.getUserName())
			.append(" with ").append(md.getDriverName());
		}
		catch(Exception e) {
			String err = "ERROR printing connection metadata: " + e.getMessage();
		    //lo.error(err);
		    result.append(err);
		}
		
		return result.toString();
	}
	
    /**
     * Creates data source connection
     * 
     * @param lookupName
     */
	private void getDataSourceConnection(String[] lookupName) throws SQLException, CriticalException, Exception, Error {
		String fp = "getDataSourceConnection: ";
		Context initContext = new InitialContext();
		Context envContext;	
		
		try {
			envContext = (Context) initContext.lookup(DAOConstants.KEY_CONTEXT_LOOKUP);
		}
		catch(Exception e) {
			lo.error("Error creating context!");
			throw new CriticalException("Error creating context!");
		}
		
		try {						
			this.dataSource = (OracleDataSource) envContext.lookup(lookupName[0]);
			this.dataSource.setLoginTimeout(this.connectionLoginTimeout);
			if(lo.isDebugEnabled()){
			lo.debug(fp + "Got dataSource from DSN " + lookupName[0] + ", set timeout: " + this.dataSource.getLoginTimeout());
			}
		}
		catch(NamingException ne) {
			this.status = DAOConstants.STATUS_ERROR;
    		lo.error("Caught NamingException: " + ne.getExplanation());
    		if(lo.isTraceEnabled()) {
    			lo.trace("root cause: " + ne.getRootCause());
    		}
    		if(lookupName.length>1){
    			lo.error("Trying secondary data source..");
    		
    			try {
    			this.dataSource = (OracleDataSource) envContext.lookup(lookupName[1]);
    			this.dataSource.setLoginTimeout(this.connectionLoginTimeout);
    			if(lo.isDebugEnabled()){
    			lo.debug(fp + "Got secondary dataSourse: " + this.dataSource + ", set timeout " + this.dataSource.getLoginTimeout());
    			}
    			//discard first data source name
    			String tmpName = lookupName[1];
    			lookupName = new String[]{tmpName};
    			}
    			catch(Exception e) {
    			throw new CriticalException("Could not create data source!");
    			}
    		}
    		else{
    			throw new CriticalException("Could not create data source!");
    		}
		}
		
		try {				
			this.connection = (OracleConnection)this.dataSource.getConnection();
			lo.trace(fp+"got data source connection");
		}
		//catches SQL exception for the connection retry
    	catch(SQLException sqle)  {
    		this.status = sqle.getErrorCode();
    		lo.error(fp + "SQLException: " + this.status+" - "+sqle.getMessage());

    		//wait 
    		lo.info(fp + "Retrying in " + this.retryDelay + " ms..");
    		try {
    			Thread.sleep(this.retryDelay);
    		}
    		catch(InterruptedException ie) {
    			lo.error(fp + "Error sleeping: " + ie.getMessage());
    		}
  		
    		//Then try to re-connect to the same data source 
    		try	{			 	
    			this.connection = (OracleConnection)this.dataSource.getConnection();
    			this.status = DAOConstants.STATUS_SUCCESS;
    			lo.trace(fp + "got connection after 2nd attempt");
    		}
    		catch(SQLException sqle2)  {
    			this.status = sqle2.getErrorCode();
    			lo.error(fp + "Error again: " + this.status + ": "+sqle2.getMessage());
    			
    			//if second data source is available try to connect to it
    			if(lookupName.length>1){
    				this.dataSource = (OracleDataSource) envContext.lookup(lookupName[1]);
    				this.dataSource.setLoginTimeout(this.connectionLoginTimeout);
    				this.connection = (OracleConnection)this.dataSource.getConnection();
    				lo.info(fp + "Got connection for secondary dataSourse " + lookupName[1] + ", set timeout: " + this.dataSource.getLoginTimeout() + ": " + this.connection);
    			}	
    			else {
    				throw new CriticalException(sqle2);
    			}
    		}
    	}
    	catch(Exception e) {
			throw new CriticalException(e);
		}
    	
   	try {	    
    	    //lo.info("Trying to register TAF callback");
    	    this.connection.registerTAFCallback(this, "FAILOVER"); 
    	    lo.trace("TAF callback registered");
    	}
    	catch(Exception e) {
    		//lo.error("Not an OCI driver, or other error registering TAF callback, : " + e.getMessage());
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
    private void getDirectConnection(String[] ur, String us, String pw) throws CriticalException, Exception {
    	String fp = "getDirectConnection: ";
    	lo.error(fp + "ur: " + ur + ", this.url: " + this.url);
        if(ur==null || ur.length<=0 ) {
        	lo.error(fp + "ur is null: " + ur + ". No database connection configuration found" ); //- raising alarm...");
        		
    		throw new CriticalException("Failed to find database: No database connection configuration found");
        }
        	
        StackTraceElement[] trace = null;
        
    	try {
    		try {
    		//reset
    		if(this.connection!=null) {
    			this.connection.close();
    			this.connection = null;
        	}}
    		catch(Exception e) {
    			
    		}

    		//this.driver = new oracle.jdbc.driver.OracleDriver();
    		this.driver = new oracle.jdbc.OracleDriver();
    	    DriverManager.registerDriver(this.driver);
    	}
    	catch(Exception e) {
			this.status = DAOConstants.STATUS_ERROR;
    		throw new CriticalException("Failed to initialize OracleDriver!");
    	}
    	
    	try {
    		this.connection = (OracleConnection)DriverManager.getConnection(ur[0], us, pw);
    		this.status = DAOConstants.STATUS_SUCCESS;
    	    lo.info(fp + "got connection to " + ur[0]);
    	}
    	catch(ArrayIndexOutOfBoundsException aoe) {
    		this.status = DAOConstants.STATUS_ERROR;
    		String errMsg = "Exception connecting to database: " + aoe.getMessage() + "url array out of bounds, please check configuration";
    	    lo.error(fp + errMsg);
    	   
    		throw new CriticalException(errMsg);
    	}
    	catch(Exception e) {
    		this.status = DAOConstants.STATUS_ERROR;
    	    lo.error(fp + "Exception connecting to " + ur[0] + ": " + e.getMessage());
    	    trace = e.getStackTrace();			  
    		if(trace!=null) {
    			for(int i=0;i<trace.length;i++)	{
    				lo.error(trace[i]);
    			}
    		}
    	    lo.info(fp + "Retrying in " + this.retryDelay + " ms..");
    	    Thread.sleep(this.retryDelay);
    				
    	    try	{
    	    	this.connection = (OracleConnection)DriverManager.getConnection(ur[0], us, pw);
    	    	this.status = DAOConstants.STATUS_SUCCESS;
    	    	lo.info(fp + "2nd attempt: got connection to " + ur[0]);
    	    }
    	    catch(Exception ex) {
    	    	this.status = DAOConstants.STATUS_ERROR;
    	    	lo.error(fp + "Exception connecting to " + ur[0] + ": " + ex.getMessage());
    	    	trace = ex.getStackTrace();			  
    	    	if(trace!=null) {
    	    		for(int i=0;i<trace.length;i++) {
    	    			lo.error(trace[i]);
    	    		}
    	    	}
    		    lo.info(fp + "Trying secondary db.. ");
    		    	
    		    try {
    	    		this.connection = (OracleConnection)DriverManager.getConnection(ur[1], us, pw);
    	    		this.status = DAOConstants.STATUS_SUCCESS;
    	    		lo.info(fp + "got connection to " + ur[1]);
    	    	}
    		    catch(ArrayIndexOutOfBoundsException aoe) {
    		    	this.status = DAOConstants.STATUS_ERROR;
    			    lo.error(fp + "Exception connecting to secondary database - no url configured!");
    			    
    				throw new CriticalException("Failed to find database");
    			}
    	    	catch(Exception exn) {
    	    		this.status = DAOConstants.STATUS_ERROR;
    	    		lo.error(fp + "Exception connecting to " + ur[1] + ": " + exn.getMessage());
    	    		trace = exn.getStackTrace();
    	    		if(trace!=null) {
    		    		for(int i=0;i<trace.length;i++)	{
    		    			lo.error(trace[i]);
    		    		}
    		    	}
    	    	    lo.info(fp + "Retrying in " + this.retryDelay + " ms..");
    	    	    Thread.sleep(retryDelay);
    	    	    		
    	    	    try   	{
    	    	    	this.connection =(OracleConnection) DriverManager.getConnection(ur[1], us, pw);
    	    	    	this.status = DAOConstants.STATUS_SUCCESS;
    	    	    	lo.info(fp + "2nd attempt: got connection to " + ur[1]);
    	    	    }
    	    	    catch(Exception exnx) {
    	    	    	this.status = DAOConstants.STATUS_ERROR;
    	    	    	lo.error(fp + "Exception connecting to " + ur[1] + ": " + exnx.getMessage());
    	    	    	trace = exnx.getStackTrace();
    	    	    	if(trace!=null) {
    	    		    	for(int i=0;i<trace.length;i++) {
    	    		    		lo.error(trace[i]);
    	    		    	}
    	    		    }
    	    	    	
    	    			throw new CriticalException("Failed to find database: " + exnx.getMessage());
    	    	    }
    	    	}
    	    }
    	}
    	
    	try {	    
    		String strFailover = "FAILOVER";
    	    //lo.info("Trying to register TAF callback");
    	    this.connection.registerTAFCallback(this, strFailover); 
    	    lo.trace("TAF callback registered");
    	}
    	catch(Exception e) {
    		//lo.error("Not an OCI driver, or other error registering TAF callback, : " + e.getMessage());
    	}
    }
        
    /**
     * Sets database connection
     * 
     * @param c
     */
    protected void setConnection(OracleConnection c) throws SQLException   {  	
    	if(c!=null && !c.isClosed())  	{
    		try	{
        		if(this.connection!=null)
        			this.connection.close();
        	}
        	catch(Exception e) 	{        		
        	}
        	
			this.connection = c;
    	}            
    }
 
	/**
	 * Disposes all database resources
	 * @throws SQLException
	 */
	protected void cleanUp() throws SQLException {

		// dispose of result sets, if any
		if (this.rset != null) 	{
			for (int i = 0; i < this.rset.length; i++) 	{
				try	{
					if (this.rset[i] != null){
						this.rset[i].close();
					}
				}
				catch(Exception e)	{
					lo.error("Failed to close rs[" + i + "]:" + e.getMessage());
				}
			}
			this.rset = null;
		}

		// dispose of statements, if any
		if (this.stmt != null) {
			for (int i = 0; i < this.stmt.length; i++) {
				if (this.stmt[i] != null) {
					this.stmt[i].close();
				}
			}

			this.stmt = null;
		}

		// dispose of prepared statements, if any
		if (this.pStmt != null) {
			for (int i = 0; i < this.pStmt.length; i++) {
				if (this.pStmt[i] != null) {
					this.pStmt[i].close();
				}
			}

			this.pStmt = null;
		}

		// dispose of callable statements, if any
		if (this.callStmt != null) {
			for (int i = 0; i < this.callStmt.length; i++) {
				if (this.callStmt[i] != null) {
					this.callStmt[i].close();
				}
			}

			this.callStmt = null;
		}

		try {
		// dispose of DB connection
		if (this.connection != null) {
			this.connection.close();			
		}}
		catch(Exception e) {
			lo.error("Error closing connection: " + e.getMessage());
		}
		this.connection = null;
	}

    /**
	* Sets the DB user
	*
	*@param usr  --DB user
	*/
    public void setUser(String usr)
    {
    	this.user = usr;
    }

    /**
	* Gets the DB user
	*
	*@return String user  --DB user
	*/
    public String getUser()
    {
	   return this.user;
    }

    /**
	* Sets the DB password
	*
	* @param pwd  --DB password
	*/
    public void setPassword(String pwd)
    {
	   this.password = pwd;
    }

    /**
	* Gets the DB password
	*
	* @return password  --DB password
	*/
    public String getPassword()
    {
	   return this.password;
    }

    /**
	* Sets the DB URL
	*
	* @param u     --The DB URL
	*/
    public void setUrl(String[] u)
    {
	   this.url = u;
    }

    /**
	* Gets the DB URL
	*
	* @return String url  --DB URL
	*/
    public String[] getUrl()
    {
	   return this.url;
    }

    /**
	* Sets the DB SQL string
	*
	* @param s  --DB SQL string
	*/
    public void setSqlString(String s)
    {
	   this.sql = s;
    }

    /**
	* Gets the DB SQL string
	*
	* @return String sqlString  --DB SQL string
	*/
    public String getSqlString()
    {
	   return this.sql;
    }

    /**
	* Sets Data Source Name
	*
	* @param s  --DB SQL string
	*/
    public void setDataSourceNames(String[] s)
    {
	   this.dataSourceNames = s;
    }

    /**
	* Gets Data Source Name
	*
	* @return String sqlString  --DB SQL string
	*/
    public String[] getDataSourceName()
    {
	   return this.dataSourceNames;
    }

    /**
	* Sets the database connection mode
	*
	* @param m     --The DB connection mode
	*/
    public void setDbMode(int m)
    {
	   this.dbMode = m;
    }
 
    /**
	* Gets the database connection mode
	*
	* @return int 
	*/
    public int getDbMode()
    {
	   return this.dbMode;
    }
      
    /**
	* Sets the execution status
	*
	* @param m     --The status of the DB call execution
	*/
    public void setStatus(int m)
    {
	   this.status = m;
    }

    /**
	* Returns the execution status
	*
	* @return int  --The status of the DB call execution 
	*/
    public int getStatus()
    {
	   return this.status;
    }
        
    /**
	* Sets the connection retry delay
	*
	* @param s     --Connection retry delay time in ms
	*/
    public void setRetryDelay(long s)
    {
	   this.retryDelay = s;
    }

    /**
	* Returns the connection retry delay
	*
	* @return long   --Connection retry delay time in ms 
	*/
    public long getRetryDelay()
    {
	   return this.retryDelay;
    }
    
    /**
	* Sets the number of retries connecting to the same URL
	*
	* @param s     --Number of retries connecting to the same URL
	*/
    public void setNumberOfRetries(int n)
    {
	   this.numberOfRetries = n;
    }

    /**
	* Returns the number of retries connecting to the same URL
	*
	* @return int   --Number of retries connecting to the same URL 
	*/
    public int getNumberOfRetries()   {
	   return this.numberOfRetries;
    }

    /**
     * Performs main flow of the DB call, takes care of error handling and 
     * resources clean up 
     * 
     * @return status
     */
    public int invoke()   {
    	
    	String errMsg 				= null;
    	StackTraceElement[] trace 	= null;
    	
		try	{			 			
			this.getConnection();		
			this.status = processDatabaseCall();
		}
		// catching SQL exception thrown by subclass during statment execution
	    catch(SQLException sqle)  {
	    	this.status = sqle.getErrorCode();
	    	lo.error("SQLException during statement execution: " + this.status +" - "+sqle.getMessage());    	
	    	
	    	//for the following errors need to reconnect and re-execute
	    	if((this.status == 1012)  || // not logged on to Oracle
	        		(this.status == 1033) || // Oracle initialization or shutdown in progress
	        		(this.status == 1034) || // Oracle not available
	        		(this.status == 1089) || // immediate shutdown in progress, no operations are permitted
	        		(this.status == 3113) || // end-of-file on communication channel
	        		(this.status == 3114) || // not connected to Oracle
	        		(this.status == 12203)|| // TNS--unable to connect to destination
	        		(this.status == 12535)|| // ORA-12535: TNS:operation timed out
	        		//////////////////////
	        		(this.status == 12170)|| // ORA-12170: TNS:Connect timeout occurred
	        		/////////////////////
	        		//(errorCode == 12500)|| // TNS--listener failed to start dedicated server process
	        		//(errorCode == 12571)|| // TNS--packet writer failure
	        		//(errorCode == 12514)|| // ORA-12514: TNS:listener could not resolve SERVICE_NAME given in connect descriptor
	        		//(errorCode == 12154)|| // ORA-12154: TNS:could not resolve service name
	        		//(errorCode == 24387)|| // ORA-24387: Invalid attach driver
	        		(this.status == 12571)) // cannot safely replay the call 
	    	{
	    		lo.error("Trying to reconnect and re-execute..");    	
		    	
	    	try {
	    		this.getConnection();
	    		this.status = processDatabaseCall();
	    	}
	    	catch(Exception exn) {
	    		errMsg = "Critical DataAccessObject Exception trying to re-execute the statement: "+ sqle.toString() + "::" + sqle.getMessage();
	    		critical = new CriticalException(errMsg,exn);
	    		
	    		lo.error(errMsg);
				
				trace = sqle.getStackTrace();			  
				if(trace!=null) {
					for(int i=0;i<trace.length;i++)	{
						lo.error(trace[i]);
					}
				}
	    	}
	    	catch(Error err) {
	    		errMsg = "Critical DataAccessObject Error trying to re-execute the statement: "+ err.toString() + "::" + err.getMessage();
	    		critical = new CriticalException(errMsg,err);
	    		lo.error(errMsg);
				
				trace = err.getStackTrace();			  
				if(trace!=null) {
					for(int i=0;i<trace.length;i++)	{
						lo.error(trace[i]);
					}
				}
	    	}
	    	}
	    }
		catch(CriticalException ce) {
			
			errMsg = "Critical DataAccessObject Exception: "+ ce.toString() + "::" + ce.getMessage();
			critical = new CriticalException(errMsg,ce);
			lo.error(errMsg);
			trace = ce.getStackTrace();			  
			if(trace!=null) {
				for(int i=0;i<trace.length;i++)	{
					lo.error(trace[i]);
				}
			}
		}
		catch(Exception e)	{
			errMsg = "Generic DataAccessObject Exception: "+ e.toString() + "::" + e.getMessage();
			critical = new CriticalException(errMsg,e);
			lo.error(errMsg);
			trace = e.getStackTrace();			  
			if(trace!=null) {
				for(int i=0;i<trace.length;i++)	{
					lo.error(trace[i]);
				}
			}
			this.status = DAOConstants.STATUS_ERROR;			
		}
		catch(Error er)	{
			errMsg = "Generic DataAccessObject Error: "+ er.toString() + "::" + er.getMessage();
			critical = new CriticalException(errMsg,er);
			lo.error(errMsg);
			trace = er.getStackTrace();			  
			if(trace!=null) {
				for(int i=0;i<trace.length;i++)	{
					lo.error(trace[i]);
				}
			}
			this.status = DAOConstants.STATUS_ERROR;					
		}
		finally	{
			try	 {
				 this.cleanUp();
				 lo.trace("DB clean up complete");
			 }
			 catch (Exception e) {
				 lo.error("EXCEPTION closing DB resources: " + e.getMessage());
			 }			 
		}		
		
    	return this.status;
    }
       	
	/**
	 * Must be implemented in each subclass to 
	 * create DB call and process custom results.
	 * All custom DB code should be implemented here. 
	 * Result object(s) should be declared in the subclass 
	 * as member variables and set once results received from  
	 * database. Status/error code should be set as 
	 * member variable
	 * 
	 * @return int
	 * 
	 * @exception SQLException
	 * @exception Exception
	 */
    public abstract int processDatabaseCall() throws SQLException, Exception;
    
}	//end of class