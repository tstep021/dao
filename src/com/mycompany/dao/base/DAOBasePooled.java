package com.mycompany.dao.base;

/** Copyright (c) 2004-2013 mycompany. All Rights Reserved. */

import java.sql.*;
import org.apache.log4j.*;

import com.mycompany.dao.util.CriticalException;
import com.mycompany.dao.util.DAOConstants;

public abstract class DAOBasePooled  {
    private static final Logger lo = Logger.getLogger(DAOBasePooled.class);   

	/**
	 * For Oracle 9i onwards you should use oracle.jdbc.OracleDriver 
	 * rather than oracle.jdbc.driver.OracleDriver as Oracle have stated 
	 * that oracle.jdbc.driver.OracleDriver is deprecated and support for 
	 * this driver class will be discontinued in the next major release. 
	 */
	protected CallableStatement[] callStmt  = null;
	protected Statement[] stmt              = null;
	protected PreparedStatement[] pStmt     = null;
	protected ResultSet[] rset              = null;
    protected String sql        			= null;
    protected int status					= DAOConstants.STATUS_INIT;

	/** Variable to check if need to raise alarm */
	private CriticalException critical		= null;
	
	/**
	 * Default constructor. 
	 * Each subclass must implement one of the constructors  
	 * where create all database objects participating in the call 
	 * in the form of arrays
	 */
	public DAOBasePooled() {
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
	
	public CriticalException getCriticalError() {
		return this.critical;
	}
	
	public void setCriticalError(Throwable t) {
		this.critical = new CriticalException(t);
	}

	public String conToString(Connection c) {
		StringBuffer result = new StringBuffer();
		try {
		    DatabaseMetaData md = c.getMetaData();

			result
			.append("Connected to ").append(md.getURL())
			.append(" as ").append(md.getUserName())
			//.append(" with ").append(md.getDriverName())
			;
		}
		catch(Exception e) {
			String err = "ERROR printing connection metadata: " + e.getMessage();
		    //lo.error(err);
		    result.append(err);
		}
		
		return result.toString();
	}

	/**
	 * Disposes all database resources
	 * @throws SQLException
	 */
	protected void cleanUp(Connection c) throws SQLException {

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
		// close DB connection
		if (c != null) {
			c.close();			
		}}
		catch(Exception e) {
			lo.error("Error closing connection: " + e.getMessage());
		}
		//c = null;
	}
	
    /**
	* Sets the DB SQL string
	*
	* @param s  --DB SQL string
	*/
    public void setSqlString(String s)   {
	   this.sql = s;
    }

    /**
	* Gets the DB SQL string
	*
	* @return String sqlString  --DB SQL string
	*/
 
   public String getSqlString()  {
	   return this.sql;
    }

    /**
	* Sets the execution status
	*
	* @param m     --The status of the DB call execution
	*/
    public void setStatus(int m)    {
	   this.status = m;
    }

    /**
	* Returns the execution status
	*
	* @return int  --The status of the DB call execution 
	*/
    public int getStatus()    {
	   return this.status;
    }

    public void destroy() {
    	//this.filterConfig = null;
    }

    public int invoke()  {
    	Connection conn = null;  
    	try {
    		conn=DBFactory.getConnection();
    		lo.info("invoke: got a connection: " +conn);
    		if(conn==null)
    			throw new Exception("Failed to get a connection!");
    		
    		lo.info("invoke: got a connection: " + conToString(conn));

    		//try{
    			status=processDatabaseCall(conn);	
    		/*}
    		catch(Exception e){
    			lo.error("Exception during statement execution: " + this.status +" - "+e.getMessage());
    		}*/
    	}
    	catch( SQLException sqle)   {
    		this.status = sqle.getErrorCode();
    		lo.error("SQLException during statement execution: " + this.status +" - "+sqle.getMessage());
    		
    		if(this.status==4068){	//stored procedure package has been recompiled, retry
    			try {
    				lo.trace("SQLException 4068, calling processDatabaseCall 2nd time, conn closed - " + conn.isClosed());
    			status=processDatabaseCall(conn);	
    			lo.trace("SQLException 4068: after 2nd try status : "  + this.status);
    			}
    			catch(SQLException sql2){
    				this.status = sqle.getErrorCode();
    				lo.error("SQLException 4068: SQLException during 2nd try statement execution: " + this.status +" - "+sql2.getMessage());
    			}
    			catch(Exception e2)   {
    	    		status = DAOConstants.STATUS_ERROR;
    	    		lo.error("SQLException 4068: Exception during 2nd statement execution: " + this.status +" - "+e2.getMessage());
    	    	}
    		}
    	}
    	catch(Exception e)   {
    		status = DAOConstants.STATUS_ERROR;
    		lo.error("Exception during statement execution: " + this.status +" - "+e.getMessage());
    	}
    	finally {
    		try	 {
    			this.cleanUp(conn);
    			lo.trace("DB clean up complete");
    		}
    		catch (Exception e) {
    			lo.error("EXCEPTION closing DB resources: " + e.getMessage());
    		}			 
    	}
   		return status;
    }   

    public abstract int processDatabaseCall(Connection c) throws SQLException, Exception;

}