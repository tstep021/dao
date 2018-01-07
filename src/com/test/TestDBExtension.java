package com.test;

/** @copyright (c) 2009-2011 mycompany. All rights reserved. */

import java.sql.*;

import oracle.jdbc.*;

import org.apache.log4j.Logger;

import com.mycompany.dao.base.DAOBase;
import com.mycompany.dao.util.DAOConstants;

/**
 * @file        TestDBExtension.java
 * 
 * @description 
 * @author      
 * @date        2009-2011
 *
 * @version     1.0
 */

public class TestDBExtension extends DAOBase 
{
	static final Logger logger = Logger.getLogger(TestDBExtension.class);
	
	private String myParam1 = null;
	public String result = null;

	/**
	 * Constructor must create arrays of objects 
	 * required for processing DB call, declared 
	 * in the base class
	 */
	public TestDBExtension() {
		// 1) depending on the number and type of statements to run
		//create an array of statements:
		
		//callable statements (stored procedure)
		//this.callStmt 	= new OracleCallableStatement[1];
		
		//statements
		//this.stmt 	= new OracleStatement[1];
		
		//prepared statements 
		this.pStmt 	= new OraclePreparedStatement[1];
		
		// 2) depending on the number and type of result sets
		//create an array of result sets
		this.rset 		= new OracleResultSet[1];
	}
	
	//if any additional IN parameters to supply
	public void setMyParam1(String p1) {
		this.myParam1 = p1;
	}
	
	public String getMyParam1() {
		return this.myParam1;
	}
	// end of additional IN parameters to supply
	
	/**
	 * Calls SQL/PL SQL statement. Note that connection has been acquired 
	 * in the superclass invoke() method before the current method call
	 * All errors are being handled and all resources disposed in a 
	 * superclass
	 */
	public int processDatabaseCall()  throws SQLException, Exception
	{
		this.status = DAOConstants.STATUS_INIT;
		this.sql = DAOConstants.SQL_TEST_QUERY;

		//this.callStmt[0] = this.connection.prepareCall(this.sql);	
		this.pStmt[0] = this.connection.prepareStatement(this.sql);	
		logger.debug("Prepared for " + this.sql);
			
		//status of the stored procedure function
		//this.callStmt[0].registerOutParameter(1, OracleTypes.INTEGER);
		
		//set IN params
		//this.callStmt[0].setString(2, OracleTypes.VARCHAR);
		
		//register OUT params
		//stored procedure result set:
		//this.callStmt[0].registerOutParameter(3, OracleTypes.CURSOR);
		logger.debug("Parameters set for " + this.sql);
		
		//stored procedure execution
		//this.callStmt[0].execute();
		// OR 
		//prepared statement execution
		this.rset[0] = this.pStmt[0].executeQuery();
		logger.debug("executed");
				
		//stored procedure only/////////
		//this.status = this.callStmt[0].getInt(1);
		//logger.info("status: " + status);
		//if(this.status!=DAOConstants.STATUS_SUCCESS) {
			//handling for non-successful execution or Oracle error		
		//	throw new Exception("Error processing statement: " + this.status);
		//}
		////end of stored procedure only//////
		
		//parse result set
		while(this.rset[0].next()) {
			String r = rset[0].getString(1);
			logger.info("result: " + r);			
			
			//only for prepared statement
			this.status = DAOConstants.STATUS_SUCCESS;
		}
		
	    return status;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//array of urls for thin connections
		//dev database
		String[] urldev = {"jdbc:oracle:thin:@//000.000.000.000:1521/database.mycompany.com"};
		
        String password = "password";
        String user 	= "user";
        //default db mode
       // int dbmode = DAOConstants.DB_MODE_DIRECT;
        //data source connection db mode
       // int dbmode = DAOConstants.DB_MODE_JNDI; 
        
        TestDBExtension test = new TestDBExtension();
        test.setUrl(urldev);
        test.setUser(user);
        test.setPassword(password);
       // test.setDbMode(dbmode);

        //if any additional IN parameters to supply
        String param1 = "my test";
        test.setMyParam1(param1);
        
        int status = test.invoke();
        logger.info("status: " + status);   
	}

}