/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
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
	
	public GoogleAPIData getAPIData(){return m_APIData;}
	public void setAPIData(GoogleAPIData _data){m_APIData = _data;}
	
	/**
	 * create the new data
	 * @return
	 */
	protected abstract GoogleAPIData newData();
	
	/**
	 * import the Google data
	 * @param g
	 * @param _timeZoneID
	 * @throws Exception
	 */
	public abstract void importGoogleData(Object g,String _timeZoneID)throws Exception;
	
	/**
	 * export GoogleAPI sync data to real google data
	 * @param g
	 * @param _timeZoneID
	 * @throws Exception
	 */
	public abstract void exportGoogleData(Object g,String _timeZoneID)throws Exception;
	
	/**
	 * input the data from the byte stream
	 * @param in
	 * @throws Exception
	 */
	public void input(InputStream in)throws Exception{
		setBBID(sendReceive.ReadString(in));
		setGID(sendReceive.ReadString(in));
		setLastMod(sendReceive.ReadLong(in));
		
		boolean tHasData = sendReceive.ReadBoolean(in);
		if(tHasData){

			if(getAPIData() == null){
				m_APIData = newData();
			}
			
			getAPIData().inputData(in);
		}		
	}
	
	/**
	 * output the data to the byte stream
	 * @param os
	 * @param _outputData output data detail or NOT
	 * @throws Exception
	 */
	public void output(OutputStream os,boolean _outputData)throws Exception{
		sendReceive.WriteString(os,getBBID());
		sendReceive.WriteString(os,getGID());
		sendReceive.WriteLong(os,getLastMod());
		
		if(getAPIData() != null && _outputData){
			sendReceive.WriteBoolean(os, true);
			getAPIData().outputData(os);
		}else{
			sendReceive.WriteBoolean(os, false);
		}
	}
	
}
