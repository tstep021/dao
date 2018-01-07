package com.test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * @file   		TestDB.java
 * 
 * @description 
 * 
 * @author 		Tatiana Stepourska
 * 
 * @version 	1.0   
 */
public class TestDB
{
	static final protected Logger lo = Logger.getLogger(TestDB.class);

	protected oracle.jdbc.driver.OracleDriver driver = null;
	protected Connection connection            = null;
	
	//array of database objects
	protected CallableStatement[] callStmt     = null;
	protected Statement[] stmt                 = null;
	protected PreparedStatement[] pStmt        = null;
	protected ResultSet[] rset                 = null;
	
	//connection properties
    protected String url      = null;
    protected String user       = null;
    protected String password   = null;
    protected String sql        = null;

    protected int status			= -99;

	/**
	 * Default constructor. 
	 * Each subclass must implement one of the constructors  
	 * where create all database objects participating in the call 
	 * in the form of arrays
	 */
	public TestDB() {
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
		
		this.pStmt = new PreparedStatement[1];
		this.rset = new ResultSet[1];
	}
	

	/**
	 * Constructor with parameters url, username and password.
	 * Implies dbMode direct - 2 
	 * Each subclass must implement one of the constructors  
	 * where create all database objects participating in the call 
	 * in the form of arrays
	 */
	public TestDB(String ur, String us, String pw) {
		
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
		this.pStmt = new PreparedStatement[1];
		this.rset = new ResultSet[1];
	}

	/**
	 * Gets the database connection according to DB mode and 
	 * other supplied information
	 * 
	 * @throws SQLException
	 * @throws Exception
	 */
	public void getConnection() throws Exception, Error  {
		
	    this.getDirectConnection(this.url,this.user, this.password);
	
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
	* Prepares new database ad-hoc connection 
	* 
	* @param ur
	* @param us
	* @param pw
	* 
	* @throws SQLException
	*/
    private void getDirectConnection(String ur, String us, String pw) throws Exception {
    	String fp = "getDirectConnection: ";
     	
        if(ur==null) {
        		
    		throw new Exception("Failed to find database: No database connection configuration found");
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

    		this.driver = new oracle.jdbc.driver.OracleDriver();
    	    DriverManager.registerDriver(this.driver);
    	  
    	}
    	catch(Exception e) {
			this.status = -1;
    		throw new Exception("Failed to initialize OracleDriver!");
    	}
    	
    	try {
    		this.connection = DriverManager.getConnection(ur, us, pw);
    		this.status = 0;
    	    lo.info(fp + "got connection to " + ur);
    	}
    	catch(Exception e) {
    		this.status = -1;
    	    lo.error(fp + "Exception connecting to " + ur + ": " + e.getMessage());
    	    trace = e.getStackTrace();			  
    		if(trace!=null) {
    			for(int i=0;i<trace.length;i++)	{
    				lo.error(trace[i]);
    			}
    		}
    	}
    }
        
    /**
     * Sets database connection
     * 
     * @param c
     */
    protected void setConnection(Connection c) throws SQLException   {  	
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
			this.connection = null;
		}}
		catch(Exception e) {
			lo.error("Error closing connection: " + e.getMessage());
		}
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
    public void setUrl(String u)
    {
	   this.url = u;
    }

    /**
	* Gets the DB URL
	*
	* @return String url  --DB URL
	*/
    public String getUrl()
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
	    }
		catch(Exception e)	{
			errMsg = "Generic DataAccessObject Exception: "+ e.toString() + "::" + e.getMessage();
			lo.error(errMsg);
			trace = e.getStackTrace();			  
			if(trace!=null) {
				for(int i=0;i<trace.length;i++)	{
					lo.error(trace[i]);
				}
			}
			this.status = -1;			
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
	 * 
	 * @return int
	 * 
	 * @exception SQLException
	 * @exception Exception
	 */
    public int processDatabaseCall() throws SQLException, Exception {
    	this.status = 0;
		this.sql = "SELECT to_char(sysdate, 'yyyy-mm-dd hh24:mi:ss') FROM dual";
	
		//String param1 = "whatever";
		String returnValue = null;
		
		this.pStmt[0] = this.connection.prepareStatement(this.sql);
		lo.trace("prepared for " + this.sql);
		
		//setting IN parameters
		//this.pStmt[0].setString(1, param1);
		this.rset[0] = this.pStmt[0].executeQuery();
		
		//register OUT parameters
		//this.callStmt[0].registerOutParameter(2, OracleTypes.CURSOR);
		//for non-queries and stored procedures:
		//this.callStmt[0].execute();
		
		while(this.rset[0].next()) {
			returnValue = this.rset[0].getString(1);
			//returnValue = this.rset[0].getString("COLUMN_NAME");
			lo.info("returnValue: " + returnValue);
		}
		
		return this.status;
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
     
		//connection string for thin driver
		String ur = "jdbc:oracle:thin:@/000.000.000.000:1521/database.mycompany.com";
        String us = "user";
        String pw = "password";
        
        TestDB test = new TestDB();
        test.setUrl(ur);
        test.setUser(us);
        test.setPassword(pw);
       
        int status = test.invoke();
        lo.info("status: " + status);   
	}
    
}	//end of class