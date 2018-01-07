package com.mycompany.dao.util;

/**
 * This class is to capture critical case for raising alarm with severity CRITICAL
 * 
 * @author tatiana.stepourska
 *
 */
public class CriticalException extends Exception {

	private static final long serialVersionUID = -4152706665859040821L;

	private String severity = null;
	
	/**
	 * Returns severity for alarm
	 * @return String
	 */
	public String getSeverity() {
		return this.severity;
	}
	
	/**
	 * Sets severity for alarm
	 * @param s
	 */
	public void setSeverity(String s) {
		this.severity = s;
	}
	
	public CriticalException() {
		// TODO Auto-generated constructor stub
	}

	public CriticalException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public CriticalException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public CriticalException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public CriticalException(String message, Throwable cause, String sev) {
		super(message, cause);
		setSeverity(sev);	
	}
	
	/**
	 * Sets severity for generating alarm
	 * 
	 * @param message
	 * @param s
	 */
	public CriticalException(String message, String sev) {
		super(message);
		setSeverity(sev);	
	}

}
