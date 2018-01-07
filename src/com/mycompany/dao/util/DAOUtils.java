package com.mycompany.dao.util;

import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
//import com.timeicr.monitor.snmp.Trap;

/** @copyright   2009-2011 mycompany. All rights reserved. */

import org.apache.log4j.Logger;

/** 
 * @file         Utils.java
 * 
 * @description  set of convenience static methods            
 * 
 * @author       Tatiana Stepourska
 * 
 * @version      1.1
 */

public final class DAOUtils  	
{               
	private static final Logger logger = Logger.getLogger(DAOUtils.class);	
	  

/*		private void triggerEMail(HttpSession ss, ApplicationCDR info)
	{
		logger.info("Error email disabled");
		
		//int lg = ENGLISH;
		   String smtpHost = cf.getProperty(MAIL_SMTP_HOST_KEY); 
		   // Get sender address
		   String from     = cf.getProperty(MAIL_FROM_KEY); 
		   // get recipient address
		   logger.info(" CLID id "+ info.getCLID());
		
		   String to       = cf.getProperty(MAIL_TO_KEY);
		   //adding
		   //String toHelp  = cf.getProperty(MAIL_ERROR_TO_KEY);
		   //logger.info("["+ callId + "] internal to  "+ toHelp);
		   
		   logger.info("email address "+ to);
		   //for testing 
		  // String to       = cf.getProperty(MAIL_TO_KEY);
		   // set subject of the mail
		   String subject  = "Error in IVR Util"; //ICommonConstants.EMAIL_SUBJECT;
		   String body     = null;
		   StringBuffer sb = new StringBuffer();
		   
		   
		   switch(lg)
		   {
  				// set the body
		   		default:
		   			sb.append("Error in the Discount Car app. Called from ")
		   			.append(info.getCLID())
		   			//.append(", call ID: ")
		   			//.append(callId)
		   			;		   	
		   }
		   
		  
		   body     = new String(sb);	

		   logger.trace("message: ");
		   logger.trace(body);
		   
		   EMailSend email = new EMailSend();
		   
		   email.setSmtpHost(smtpHost);
		   email.setFrom    (from);
		   email.setTo      (to);
		   email.setSubject (subject);
		   email.setBody    (body);
		  // email.setCallId(callId);
		   
		  // email.setErrorTo(toHelp);
		  // email.setErrorbody(errorBody);
		   
		   email.start();
	}
*/


	public static String[] getDBs(String dblist) {
		logger.trace("getDBs: received: " + dblist);
		String[] dbs = null;
		try {
			dbs = dblist.split(",");
			logger.trace("getDBs: dbs: " + dbs);
		}
		catch(Exception e) {
			
			logger.error("getDBs: Error building array of conn strings: " + e.getMessage());
		}
		logger.trace("getDBs: returning dbs: " + dbs);
		return dbs;
	}
	
	/** 
	 * Reads configuration from file into Properties object
	 * 
	 * @param filename
	 * 
	 * @return Properties
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public static String getStringFromFile(String filename) {
		String fp = "getStringFromFile: ";
		BufferedReader reader   = null;	
		String line = null;
		String result = " ";
		
		try	{
			logger.trace(fp + "trying to create reader from filename: " + filename);
			reader = new BufferedReader(new FileReader(filename));
		}
		catch(Exception e) {
			logger.error(fp + "error creating file reader");
			return " ";
		}
		
		logger.trace(fp + "trying to read the file");
		try {
			while((line = reader.readLine())!=null) {
				result = result + line;
			}
		}
		catch(Exception exn){
			logger.info(fp + "error reading file: " + exn.getMessage());
		}
		finally	{
			try	{
				//fin.close();
				reader.close();
				logger.trace(fp + "property input closed");
			}
			catch(Exception ec){
				logger.error(fp + "Error closing file stream: " + ec.getMessage());
				StackTraceElement[] trace = ec.getStackTrace();			  
				if(trace!=null)
				{
					for(int i=0;i<trace.length;i++)
					{
						logger.error(trace[i]);
					}
				}
			}
			reader  = null;
			
		}
		logger.trace(fp + "returning result: " + result);
		return result;
	}

	
	/**
	 * Sends trap to alarm server.
	 * 
	 * This method has been moved to a different package!
	 * 
	 * @param  sev  --For the Web server monitoring taskName is the URL
	 * @param errMsg
	 * 
	 * @return boolean
	 */
/*	public static boolean raiseAlarm(String sev, String errMsg)
	{
		boolean success = false;		
		
		Severity severity = null;
		
		if(sev==null || sev.equalsIgnoreCase(DAOConstants.SEVERITY_INFO))
			severity = Severity.INFO;
		else if(sev.equalsIgnoreCase(DAOConstants.SEVERITY_WARNING))
			severity = Severity.WARNING;
		else if(sev.equalsIgnoreCase(DAOConstants.SEVERITY_MAJOR))
			severity = Severity.MAJOR;
		else if(sev.equalsIgnoreCase(DAOConstants.SEVERITY_CRITICAL))
			severity = Severity.CRITICAL;
		else
			severity = Severity.INFO;
		
		if(errMsg==null)
			errMsg = new String("UNKNOWN");
			
		try	{		
			Trap.generateTrap(severity, errMsg);		
			success = true;
		}
		catch(Exception e)	{
			logger.error("Error raising alarm: " + e.getMessage());
			success = false;
		}
		finally
		{

		}
				
		return success;
	}
	*/
	
	public static String fixChars(String v) {
		//String qu = "\"";
		//String dtfmt = "yyyyMMdd H:m:s";
		//SimpleDateFormat formatter = new SimpleDateFormat(dtfmt);
		char[] EOL = {13, 10}; //new char[2];
		char[] CR = {13};
		char[] LF = {10};
		String cr = new String(CR);
		String lf = new String(LF);
		String eol = new String(EOL);
		String slash = "\\\\";
		String nullstr = "";
		//String userid = null; // "["+secure.SecureFilt.USERID+"] ";
		//Date dt;
		//if(v.indexOf(eol)>=0) sl("Found eol");
		//if(v.indexOf(cr)>=0) sl("Found cr");
		//if(v.indexOf(lf)>=0) sl("Found lf");

		v = v.replaceAll(slash, nullstr);
		v = v.replaceAll(eol,slash+"n");
		v= v.replaceAll(cr,nullstr);
		//v = v.replaceAll(lf,nullstr);
		v = v.replaceAll(lf,"~~");
		v = v.replaceAll("'", slash+"'");
		v = v.replaceAll("\"", slash+"\"");
		return v;
	}

	public static String getTimestamp(long ms, String format) {
		String ts = null;
		
		//Timestamp t = new Timestamp(ms);
		Date gmt = new Date(ms);   		
	   	 
		SimpleDateFormat fm = new SimpleDateFormat(format);
		ts = fm.format(gmt);
        logger.debug("date: " + ts);
		
		return ts;
	}

	/** 
	 * Reads configuration from file into Properties object
	 * Here it is for test only
	 * 
	 * @param filename
	 * 
	 * @return Properties
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public static Properties getConfigFromFile(String filename) throws FileNotFoundException, IOException, Exception
	{
		//if Properties file available, parse it for parameter settings
		File file               = new File(filename);
		FileInputStream fin     = null;
		Properties config = new Properties();
		Exception ex = null;
		logger.trace("getConfigFromFile: filename: " + filename);
		
		try
		{
			if(!file.exists())
				throw new FileNotFoundException("getConfigFromFile: No property file [" + filename + "] found.");

			logger.trace("getConfigFromFile: loading properties");
			fin = new FileInputStream(file);
			config.load(fin);
			//config.list(System.out);
		
			logger.trace("getConfigFromFile: Properties loaded");
			
			if(logger.isTraceEnabled())
			{
			Enumeration<Object> en = config.keys();
			while(en.hasMoreElements())
			{
				String key = (String)en.nextElement();
				logger.trace(key + "=" + config.getProperty(key));
			}
			}
		}
		catch(FileNotFoundException fnfe)
		{
			logger.info("getConfigFromFile: throwing FileNotFoundException");
			ex = new FileNotFoundException(fnfe.getMessage());
		}
		catch(IOException ioe)
		{
			logger.info("getConfigFromFile: throwing IOException");
			ex = new IOException(ioe.getMessage());
		}
		catch(Exception exn)
		{
			logger.info("getConfigFromFile: throwing Exception");
			ex = new Exception(ex.getMessage());
		}
		finally
		{
			try
			{
				fin.close();
				logger.trace("getConfigFromFile: property input closed");
			}
			catch(Exception ec)
			{
				logger.error("Error closing file stream: " + ec.getMessage());
				StackTraceElement[] trace = ec.getStackTrace();			  
				if(trace!=null)
				{
					for(int i=0;i<trace.length;i++)
					{
						logger.error(trace[i]);
					}
				}
			}
			fin  = null;
			file = null;
			
		}
		
		if(ex!=null)
			throw ex;
		
		return config;
	}

}  // end of Utils