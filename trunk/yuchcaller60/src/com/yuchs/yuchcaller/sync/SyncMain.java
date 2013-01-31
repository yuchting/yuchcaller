package com.yuchs.yuchcaller.sync;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.pim.PIM;

import org.json.me.JSONArray;
import org.json.me.JSONObject;

import net.rim.blackberry.api.pdap.BlackBerryEvent;
import net.rim.blackberry.api.pdap.BlackBerryEventList;
import net.rim.device.api.compress.GZIPInputStream;
import net.rim.device.api.compress.GZIPOutputStream;
import net.rim.device.api.crypto.MD5Digest;
import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.util.TimeZoneUtilities;

import com.yuchs.yuchcaller.YuchCaller;
import com.yuchs.yuchcaller.YuchCallerProp;
import com.yuchs.yuchcaller.sendReceive;

public class SyncMain {
	
	public YuchCaller	m_mainApp;
	
	private boolean	m_isSyncing = false;
	
	// calender sync list
	private Hashtable	mCalenderSyncList = new Hashtable();
	
	// mark whether read the calender list
	private boolean	mReadCalenderList = false;
	
	public SyncMain(YuchCaller _mainApp){
		m_mainApp = _mainApp;
		
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
	
	public void startSync(){
		
		if(m_isSyncing){
			return ;
		}
		
		(new Thread(){
			public void run(){
				m_isSyncing = true;
				startSyncImpl();
				m_isSyncing = false;
			}
		}).start();
	}
	
	private void startSyncImpl(){
		
		// read yuch account 
		if(!readYuchAccount()){
			return;
		}
		
		// read the calender information in bb database
		readBBCalender();
		
		// read sync file (gID for bID)
		readWriteSyncFile(true);
		
		syncRequest();
	}
	
	//! report
	private void reportError(String error){
		
	}
	
	//! read yuch account and request the Refresh/Access token
	private boolean readYuchAccount(){
		
		YuchCallerProp tProp = m_mainApp.getProperties();
		
		if(tProp.getYuchAccount().length() == 0 || tProp.getYuchPass().length() == 0){
			return false;
		}
		
		if(tProp.getYuchAccessToken().length() == 0 || tProp.getYuchRefreshToken().length() == 0){
			
			// request the yuch server
			
			String url = "http://192.168.10.7:8888/f/login/";
			//String url = "http://www.yuchs.com/f/login/";
			
			url += YuchCaller.getHTTPAppendString();
			
			String[] tParamName = {
				"acc",	"pass",	"type",
			};
			
			String[] tParamValue = {
				tProp.getYuchAccount(),
				tProp.getYuchPass(),				
				"yuchcaller",
			};
		
			try{
				String tResult = requestPOSTHTTP(url,tParamName,tParamValue);
				
				if(tResult.startsWith("<Error>")){
					reportError(tResult.substring(7));
				}else{
					int tSpliter = tResult.indexOf("|");
					
					if(tSpliter != -1){
						tProp.setYuchRefreshToken(tResult.substring(0,tSpliter));
						tProp.setYuchAccessToken(tResult.substring(tSpliter + 1));
						
						tProp.save();
						
						m_mainApp.SetErrorString("sync: read Yuch done!");
						
						return true;
						
					}else{
						reportError("Unkown:" + tResult);
					}					
				}
				
			}catch(Exception e){
				// network problem
				
			}			
		}
		
		return false;
	}
	
	/**
	 * read the calender information from bb calender
	 */
	private void readBBCalender(){
		
		if(mReadCalenderList){
			return ;
		}
		
		try{
			
			BlackBerryEventList t_events = (BlackBerryEventList)PIM.getInstance().openPIMList(PIM.EVENT_LIST,PIM.READ_ONLY);

			Enumeration t_allEvents = t_events.items();
			
			Vector t_eventList = new Vector();
		    if(t_allEvents != null){
			    while(t_allEvents.hasMoreElements()) {
			    	t_eventList.addElement(t_allEvents.nextElement());
			    }
		    }
		    
		    synchronized(mCalenderSyncList){
			    mCalenderSyncList.clear();
			    
			    for(int i = 0;i < t_eventList.size();i++){
			    	
			    	BlackBerryEvent event = (BlackBerryEvent)t_eventList.elementAt(i);
			    	
			    	CalenderSyncData syncData = new CalenderSyncData();
			    	syncData.importData(event);
			    	
			    	mCalenderSyncList.put(syncData.getBBID(), syncData);
			    }
		    }
		    
		    mReadCalenderList = true;
		    
		}catch(Exception e){
			m_mainApp.SetErrorString("RBBC", e);
		}
	}

	/**
	 * the calender data file
	 */
	static final String fsm_calenderFilename 		= YuchCallerProp.fsm_rootPath_back + "YuchCaller/calender.data";

	/**
	 * the version fo sync file
	 */
	static final int SyncFileVersion = 0;
	
	/**
	 * check the sync file
	 */
	private void readWriteSyncFile(boolean _read){
		
		m_mainApp.getProperties().preWriteReadIni(_read, fsm_calenderFilename);
				
		try{
			FileConnection fc = (FileConnection) Connector.open(fsm_calenderFilename,Connector.READ_WRITE);
			try{
				if(_read){
					if(!fc.exists()){
						fc.create();					
					}
					
					InputStream in = fc.openInputStream();
					int tVersion = in.read();
					
					int num = sendReceive.ReadInt(in);
					for(int i= 0;i < num;i++){
						String bid = sendReceive.ReadString(in);
						String gid = sendReceive.ReadString(in);
												
						CalenderSyncData syncData = (CalenderSyncData)mCalenderSyncList.get(bid);
						if(syncData != null){
							syncData.setGID(gid);
						}
					}
					
				}else{
					OutputStream os = fc.openOutputStream();
					os.write(SyncFileVersion);
					
					synchronized(mCalenderSyncList){
						sendReceive.WriteInt(os, mCalenderSyncList.size());
						
						Enumeration enum = mCalenderSyncList.elements();
						while(enum.hasMoreElements()){
							CalenderSyncData syncData = (CalenderSyncData)enum.nextElement();
							
							sendReceive.WriteString(os, syncData.getBBID());
							sendReceive.WriteString(os, syncData.getGID());
						}
					}
					
				}			
				
			}finally{
				fc.close();
				fc = null;
			}
		}catch(Exception e){
			m_mainApp.SetErrorString("RWSF",e);
		}
		
		m_mainApp.getProperties().postWriteReadIni(fsm_calenderFilename);
	}
	
	/**
	 * sync request
	 */
	private void syncRequest(){
		try{

			// construct the json data 
			JSONObject tJson = new JSONObject();
			
			YuchCallerProp tProp = m_mainApp.getProperties();
					
			tJson.put("RefreshToken", tProp.getYuchRefreshToken());
			tJson.put("AccessToken", tProp.getYuchAccessToken());
			
			tJson.put("YuchAcc", tProp.getYuchAccount());
			tJson.put("TimeZone", TimeZone.getDefault().getID());
			
			tJson.put("Type", "calender");
			
			JSONArray tJsonDataList = new JSONArray();
			
			synchronized(mCalenderSyncList){
				
				Enumeration enum = mCalenderSyncList.elements();
				while(enum.hasMoreElements()){
					CalenderSyncData syncData = (CalenderSyncData)enum.nextElement();
					
					JSONObject data = new JSONObject();
					data.put("bid", syncData.getBBID());
					data.put("gid", syncData.getGID());
					data.put("md5", syncData.getMD5());
					
					if(syncData.getGID() == null || syncData.getGID().length() == 0){
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						try{
							Base64OutputStream base64os = new Base64OutputStream(os);
							try{
								syncData.getData().outputData(base64os);
							}finally{
								base64os.close();
								base64os = null;
							}
							
							data.put("data",os.toString());
							
						}finally{
							os.close();
							os = null;
						}
						
					}
					
					tJsonDataList.put(data);
				}
			}
			
			tJson.put("DataList", tJsonDataList);
			
			try{
				
			}finally{
				
			}
			
		}catch(Exception e){
			// sync request failed
			
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
	private static String requestPOSTHTTP(String _url,String[] _paramsName,String[] _paramsValue)throws Exception{
		
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
		
		return requestPOSTHTTP(_url,tParam.toString().getBytes("UTF-8"),false);
		
	}
	
	/**
	 * post the http request directly by content
	 * @param _url
	 * @param _content
	 * @param _gzip
	 * @return
	 * @throws Exception
	 */
	private static String requestPOSTHTTP(String _url,byte[] _content,boolean _gzip)throws Exception{
		
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
		
		HttpConnection conn = (HttpConnection)Connector.open(_url);
		try{
			
			conn.setRequestMethod(HttpConnection.POST);
			conn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_LENGTH,String.valueOf(tParamByte.length));
//			
//			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");			
//			conn.setRequestProperty("User-Agent","Profile/MIDP-2.0 Configuration/CLDC-1.0");
//			conn.setRequestProperty("Keep-Alive","60000");
//			conn.setRequestProperty("Connection","keep-alive");
						
//			if(_gzip){
//				conn.setRequestProperty("Accept-Encoding","gzip,deflate");
//			}
			
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
		    	
		    	if(_gzip){
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
		    	
		    	return new String(result,"UTF-8");

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
