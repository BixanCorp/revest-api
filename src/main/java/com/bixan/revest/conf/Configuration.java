/**
 * 
 */
package com.bixan.revest.conf;

import org.apache.commons.configuration2.XMLConfiguration;

import com.bixan.revest.core.Constant.Encryption;
import com.bixan.revest.core.Constant.ThreadPool;
import com.bixan.revest.core.Constant.UserDb;

/**
 * This is an utility class to get application variables read from the xml configuration. However,
 * the client can also get the XMLConfiguration object by calling getXMLConfiguration() to retrieve 
 * parameters by path.
 * 
 * @author sambit.bixan@gmail.com
 *
 */
public class Configuration {
	private static XMLConfiguration xml = null;
	
	Configuration(XMLConfiguration xml) {
		this.xml = xml;
	}
	
	/**
	 * Returns the configuation as XMLConfiguration
	 * 
	 * @return XMLConfiguration
	 */
	public XMLConfiguration getXMLConfiguration() {
		return xml;
	}
	
	public String getUserDbServer() {
		return xml.getString(UserDb.BASE_PATH + "." + UserDb.SERVER_PARAM);
	}
	
	public String getUserDbPort() {
		return xml.getString(UserDb.BASE_PATH + "." + UserDb.PORT_PARAM);
	}
	
	public String getUserDbName() {
		return xml.getString(UserDb.BASE_PATH + "." + UserDb.NAME_PARAM);
	}
	
	public String getUserDbUser() {
		return xml.getString(UserDb.BASE_PATH + "." + UserDb.USER_PARAM);
	}
	
	public String getUserDbPwd() {
		return xml.getString(UserDb.BASE_PATH + "." + UserDb.PWD_PARAM);
	}
	
	public int getEncryptionIteration() {
		return xml.getInt(Encryption.BASE_PATH + "." + Encryption.ITERATION_PARAM, 1000);
	}
	
	public String getEncryptAlgo() {
		return xml.getString(Encryption.BASE_PATH + "." + Encryption.ALGORITHM_PARAM);
	}
	
	public String getValidationWord() {
		return xml.getString(Encryption.BASE_PATH + "." + Encryption.VALWORD_PARAM);
	}
	
	public int getPoolSize() {
		return xml.getInt(ThreadPool.BASE_PATH + "." + ThreadPool.CORE_SIZE_PARAM, ThreadPool.CORE_SIZE_DEFAULT);
	}

	public int getMaxPoolSize() {
		return xml.getInt(ThreadPool.BASE_PATH + "." + ThreadPool.MAX_SIZE_PARAM, ThreadPool.MAX_SIZE_DEFAULT);
	}
	
	public long getPoolKeepalive() {
		return xml.getLong(ThreadPool.BASE_PATH + "." + ThreadPool.KEEPALIVE_PARAM, ThreadPool.KEEPALIVE_DEFAULT);
	}
}
