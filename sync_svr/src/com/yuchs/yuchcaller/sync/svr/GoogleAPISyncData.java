package com.yuchs.yuchcaller.sync.svr;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class GoogleAPISyncData {
	
	// the bb system calendar UID
	protected String bID = null;
	
	// the google calendar UID
	protected String gID = null;
	
	// this sync solt data md5
	protected long lastMod = 0;
	
	// calendar data
	protected GoogleAPIData	m_APIData = null;
	
	public void setBBID(String _bID){bID = _bID;}
	public String getBBID(){return bID;}
	
	public void setGID(String _id){gID = _id;}
	public String getGID(){	return gID;}
	
	public void setLastMod(long _mod){lastMod = _mod;}
	public long getLastMod(){return lastMod;}
	
	/**
	 * create the new data
	 * @return
	 */
	protected abstract GoogleAPIData newData();
	
	/**
	 * import the Google data
	 * @param g
	 * @throws Exception
	 */
	public abstract void importGoogleData(Object g)throws Exception;
	
	/**
	 * export GoogleAPI sync data to real google data
	 * @param g
	 * @param _timeZoneID
	 * @throws Exception
	 */
	public abstract void exportGoogleData(Object g,String _timeZoneID)throws Exception;
	
	/**
	 * input data from a byte stream
	 * @param in
	 * @throws Exception
	 */
	public abstract void input(InputStream in)throws Exception;
	
	/**
	 * output data to a bytes stream
	 * @param os
	 * @param _outputData output data detail or NOT
	 * @throws Exception
	 */
	public abstract void output(OutputStream os,boolean _outputData)throws Exception;
	
}
