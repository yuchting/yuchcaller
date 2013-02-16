package com.yuchs.yuchcaller.sync;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.compress.GZIPInputStream;
import net.rim.device.api.compress.GZIPOutputStream;
import net.rim.device.api.crypto.MD5Digest;
import net.rim.device.api.io.http.HttpProtocolConstants;

import com.yuchs.yuchcaller.ConnectorHelper;
import com.yuchs.yuchcaller.YuchCaller;
import com.yuchs.yuchcaller.YuchCallerProp;
import com.yuchs.yuchcaller.sendReceive;

public class SyncMain {
	
	public YuchCaller	m_mainApp;
	
	private boolean	m_isSyncing = false;
	
	// calendar sync
	private final CalendarSync mCalendarSync;
		
	public SyncMain(YuchCaller _mainApp){
		m_mainApp = _mainApp;
		mCalendarSync = new CalendarSync(this);
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
		
		m_mainApp.invokeLater(new Runnable() {
			
			// started in own YuchCaller context
			//
			public void run() {

				(new Thread(){
					public void run(){
						m_isSyncing = true;
						startSyncImpl();
						m_isSyncing = false;
					}
				}).start();	
			}
		});
	}
	
	/**
	 * write the account information to a OutputStream
	 * @param os
	 * @param type
	 * @param md5		sync data md5
	 * @param diffType 	of sync
	 * @throws Exception
	 */
	public void writeAccountInfo(OutputStream os,String type,String md5,long minSyncTime,int diffType)throws Exception{
		
		YuchCallerProp tProp = m_mainApp.getProperties();
		
		// write the version
		sendReceive.WriteShort(os,(short)0);
		sendReceive.WriteString(os, type);
		
		// send the min time for sync
		sendReceive.WriteLong(os, minSyncTime);
		
		//sendReceive.WriteString(os,tProp.getYuchRefreshToken());
		//sendReceive.WriteString(os,tProp.getYuchAccessToken());
		
		sendReceive.WriteString(os,"1/VQPrbZhyWhXrYP6eVNnwkQwj2RQK3Gyc1q-3k08sKxE");
		sendReceive.WriteString(os,"ya29.AHES6ZQr1KFYYlAqCoU0H6ag1q9EI7kwOcMNynpIYXsxtJlqUAe9");
		
		sendReceive.WriteString(os,tProp.getYuchAccount());
		
		sendReceive.WriteString(os,TimeZone.getDefault().getID());
		sendReceive.WriteString(os,md5);
		
		// write the diff type
		os.write(diffType);
	}
	
	private void startSyncImpl(){
		
		// read yuch account 
//		if(!readYuchAccount()){
//			return;
//		}
		
		mCalendarSync.startSync();
	}
	
	//! report error
	public void reportError(String error){
		error = "SyncError: " + error;
		System.err.println(error);
		
		m_mainApp.SetErrorString(error);
	}
	
	//! report error
	public void reportError(String errorLabel,Exception e){
		System.err.println(errorLabel + ": " + e.getClass().getName() + " " + e.getMessage());
		
		m_mainApp.SetErrorString(errorLabel, e);
	}
	
	// report the information
	public void reportInfo(String info){
		info = "SyncInfo: " + info;
		
		System.out.println(info);
		m_mainApp.SetErrorString(info);
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
			
			tParam.append(_paramsName[i]).append('=').append(_paramsValue);
		}
		
		return new String(requestPOSTHTTP(_url,tParam.toString().getBytes("UTF-8"),false),"UTF-8");
		
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
//			
//			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");			
//			conn.setRequestProperty("User-Agent","Profile/MIDP-2.0 Configuration/CLDC-1.0");
//			conn.setRequestProperty("Keep-Alive","60000");
//			conn.setRequestProperty("Connection","keep-alive");
						
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
