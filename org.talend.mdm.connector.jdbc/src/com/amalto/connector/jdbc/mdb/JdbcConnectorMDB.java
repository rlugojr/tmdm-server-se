package com.amalto.connector.jdbc.mdb;

import javax.resource.ResourceException;
import javax.resource.cci.Record;

import com.amalto.connector.mdb.ConnectorMDB;


/**
 * 
 * @author starkey
 */
public class JdbcConnectorMDB extends ConnectorMDB {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7376079462678692827L;

	/* (non-Javadoc)
	 * @see javax.resource.cci.MessageListener#onMessage(javax.resource.cci.Record)
	 */
	public Record onMessage(Record inputData) throws ResourceException {
		try  {
			return super.onMessage(inputData);
	    } catch (ResourceException e) {
//	    	e.printStackTrace();
	        throw e;
	    } catch (Exception e) {
//	    	e.printStackTrace();
	    	throw new ResourceException(e.getClass().getName()+": "+e.getLocalizedMessage());
	    }
	}
	
}

