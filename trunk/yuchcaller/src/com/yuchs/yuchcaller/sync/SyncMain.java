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
package com.yuchs.yuchcaller.sync;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.compress.GZIPInputStream;
import net.rim.device.api.compress.GZIPOutputStream;
import net.rim.device.api.crypto.MD5Digest;
import net.rim.device.api.io.http.HttpProtocolConstants;

import com.yuchs.yuchcaller.ConnectorHelper;
import com.yuchs.yuchcaller.YuchCaller;
import com.yuchs.yuchcaller.sync.calendar.CalendarSync;
import com.yuchs.yuchcaller.sync.contact.ContactSync;

public class SyncMain {
		
	/**
	 * former error prompt 
	 */
	public final String[]		mErrorStr	= new String[AbsSync.fsm_syncTypeString.length];
	
	/**
	 * former information string
	 */
	public final String[]		mInfoStr	= new String[AbsSync.fsm_syncTypeString.length];
	
	/**
	 * main yuchcaller app context
	 */
	public YuchCaller	m_mainApp;
	
	private boolean	m_isSyncing = false;
	
	// calendar sync
	private final CalendarSync mCalendarSync;
	
	// contact sync
	private final ContactSync mContactSync;

	// calendar re-load thread
	private Thread mReadBbCalendarThread = null;
	
	public SyncMain(YuchCaller _mainApp){
		m_mainApp		= _mainApp;		
		mCalendarSync	= new CalendarSync(this);
		mContactSync	= new ContactSync(this);
	}
	
	/**
	 * read bb calendar event data if former days changed
	 */
	public void readBBCalendarAgain(){
		
		if(mCalendarSync != null && mReadBbCalendarThread == null){
			
			synchronized(this){
				mReadBbCalendarThread = (new Thread(){
					public void run(){
						mCalendarSync.readBBSyncData();
						
						synchronized(SyncMain.this){
							mReadBbCalendarThread = null;
						}
					}
				});
				
				mReadBbCalendarThread.start();				
			}
		}
	}
	
	/**
	 * get the md5 string
	 * @param _org
	 * @return
	 */
	public static String md5(String _org){
		
		byte[] bytes = null;
		try{
			bytes = _org.getBytes("UTF-8");
		}catch(Exception e){
			bytes = _org.getBytes();
		}
		
		return md5(bytes);
		
	}
	
	/**
	 * get the md5
	 * @param _data
	 * @return
	 */
	public static String md5(byte[] _data){
		MD5Digest digest = new MD5Digest();
		
		digest.update(_data, 0, _data.length);

		byte[] md5 = new byte[digest.getDigestLength()];
		digest.getDigest(md5, 0, true);
		
		return convertToHex(md5);
	}

	/**
	 * convert bytes to hex string
	 * @param data
	 * @return
	 */
	public static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        
        return buf.toString();
    }
	

	/**
	 * spliter string
	 * @param s
	 * @param split
	 * @return
	 */
	public static Vector splitStr(String s,char split){
		
		Vector tResult = new Vector();
		
		StringBuffer sb = new StringBuffer();
		
		for(int i = 0;i < s.length();i++){
			char c = s.charAt(i);		
			if(c == split){
				if(sb.length() == 0){
					continue;
				}
				
				tResult.addElement(sb.toString());
				
				sb = new StringBuffer();
			}else{
				sb.append(c);
			}
		}
		
		if(sb.length() > 0){
			tResult.addElement(sb.toString());
		}
		
		return tResult;
	}
		
	public void startSync(){
		
		if(m_isSyncing){
			return ;
		}
		
		if(m_mainApp.getProperties().getYuchRefreshToken().length() == 0){
			return;
		}
		
		(new Thread(){
			public void run(){
				if(m_isSyncing){
					return;
				}
				
				synchronized(SyncMain.this){
					m_isSyncing = true;
				}
				
				startSyncImpl();
				
				synchronized(SyncMain.this){
					m_isSyncing = false;
				}
			}
		}).start();
	}
	
	private void startSyncImpl(){
		
		clearReport();
		
		//mCalendarSync.startSync();
		
		mContactSync.startSync();
	}
	
	//! report error
	public void reportError(String error,int _type){
		
		if(error != null && error.length() > 0){
			error = AbsSync.fsm_syncTypeString[_type] + " Error: " + error;		
			m_mainApp.SetErrorString(error);
		}
		
		mErrorStr[_type] = error;
				
		if(m_mainApp.m_configManager != null){
			m_mainApp.m_configManager.refreshSyncPrompt();
		}
	}
	
	//! report error
	public void reportError(String errorLabel,Exception e,int _type){
		reportError(errorLabel + " " + e.getMessage() + " " + e.getClass().getName(),_type);
	}
	
	// report the information
	public void reportInfo(String info,int _type){
		if(info != null && info.length() > 0){
			info = AbsSync.fsm_syncTypeString[_type] + " Info: " + info;
			m_mainApp.SetErrorString(info);
		}
		
		mInfoStr[_type] = info;
		
		if(m_mainApp.m_configManager != null){
			m_mainApp.m_configManager.refreshSyncPrompt();
		}
	}
	
	/**
	 * clear report error and information
	 */
	public void clearReport(){
		for(int i = 0;i < mInfoStr.length;i++){
			mInfoStr[i] 	= null;
			mErrorStr[i]	= null;
		}
		
		if(m_mainApp.m_configManager != null){
			m_mainApp.m_configManager.refreshSyncPrompt();
		}
	}
	
	/**
	 * request the url via POST
	 * @param _url
	 * @param _paramsName
	 * @param _paramsValue
	 * @param _gzip
	 * @return
	 * @throws Exception
	 */
	public static String requestPOSTHTTP(String _url,String[] _paramsName,String[] _paramsValue)throws Exception{
		
		if(_paramsName == null || _paramsValue == null || _paramsName.length != _paramsValue.length){
			throw new IllegalArgumentException("_paramsName == null || _paramsValue == null || _paramsName.length != _paramsValue.length");
		}
		
		StringBuffer tParam = new StringBuffer();
		for(int i = 0;i < _paramsName.length;i++){
			if(tParam.length() != 0){
				tParam.append('&');
			}
			
			tParam.append(_paramsName[i]).append('=').append(_paramsValue[i]);
		}
		
		return new String(requestPOSTHTTP(_url,tParam.toString().getBytes("UTF-8"),false,true),"UTF-8");
		
	}
	
	/**
	 * post the http request directly by content
	 * @param _url
	 * @param _content
	 * @param _gzip
	 * @return
	 * @throws Exception
	 */
	public static byte[] requestPOSTHTTP(String _url,byte[] _content,boolean _gzip)throws Exception{
		return requestPOSTHTTP(_url,_content,_gzip,false);
	}
	
	/**
	 * post the http request directly by content
	 * @param _url
	 * @param _content
	 * @param _gzip
	 * @return _www_form add HTTP header "Content-Type: application/x-www-form-urlencoded"
	 * @throws Exception
	 */
	public static byte[] requestPOSTHTTP(String _url,byte[] _content,boolean _gzip,boolean _www_form)throws Exception{
		
		byte[] tParamByte = _content;
		
		// Attempt to gzip the data
		if(_gzip){
			ByteArrayOutputStream zos = new ByteArrayOutputStream();
			try{
				GZIPOutputStream zo = new GZIPOutputStream(zos,6);
				try{
					zo.write(tParamByte);
				}finally{
					zo.close();
					zo = null;
				}
				
				byte[] tZipByte = zos.toByteArray();
				if(tZipByte.length < tParamByte.length){
					tParamByte = tZipByte;
				}else{
					_gzip = false;
				}
			}finally{
				zos.close();
				zos = null;
			}
		}
		
		HttpConnection conn = (HttpConnection)ConnectorHelper.open(_url,Connector.READ_WRITE,30000);
		try{
			
			conn.setRequestMethod(HttpConnection.POST);
			conn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_LENGTH,String.valueOf(tParamByte.length));

			if(_www_form){
				conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			}
			
			if(_gzip){
				conn.setRequestProperty("Content-Encoding","gzip");
			}
			
			OutputStream out = conn.openOutputStream();
			try{
				out.write(tParamByte);
				out.flush();
			}finally{
				out.close();
				out = null;
			}
			
			int rc = conn.getResponseCode();
		    if(rc != HttpConnection.HTTP_OK){
		    	throw new IOException("HTTP response code: " + rc);
		    }
		    
		    InputStream in = conn.openInputStream();
		    try{
		    	int length = (int)conn.getLength();
		    	int ch;
		    	byte[] result;
		    	
		    	if (length != -1){
		    		
		    		result = new byte[length];
		    		in.read(result);
		    		
		    	}else{
		    		
		    		ByteArrayOutputStream os = new ByteArrayOutputStream();
		    		try{

				        while ((ch = in.read()) != -1){
				        	os.write(ch);
				        }
				        
				        result = os.toByteArray();
				        
		    		}finally{
		    			os.close();
		    			os = null;
		    		}
			    }
		    	
		    	if(conn.getHeaderField("Content-Encoding") != null){
		    		
		    		ByteArrayInputStream gin = new ByteArrayInputStream(result);
					try{
						GZIPInputStream zi	= new GZIPInputStream(gin);
						try{
							ByteArrayOutputStream os = new ByteArrayOutputStream();
				    		try{

								while((ch = zi.read()) != -1){
									os.write(ch);
								}
								result = os.toByteArray();
								
				    		}finally{
				    			os.close();
				    			os = null;
				    		}
						}finally{
							zi.close();
						}
					}finally{
						gin.close();
					}
		        }
		    	
		    	return result;

		    }finally{
		    	in.close();
		    	in = null;
		    }

		}finally{
			conn.close();
			conn = null;
		}
	}

}
