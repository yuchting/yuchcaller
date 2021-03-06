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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

public abstract class GoogleAPISync {

	public static String getGoogleAPIClientId(){
		
		try{
			Class<?> clazz = Class.forName("com.yuchs.yuchcaller.sync.svr.PrivateConfig");
			Method method = clazz.getMethod("getGoogleAPIClientId",new Class[0]);
			return method.invoke(null, null).toString();
		}catch(Exception e){
			return "";
		}
	}
	
	public static String getGoogleAPIClientSecret(){
		
		try{
			Class<?> clazz = Class.forName("com.yuchs.yuchcaller.sync.svr.PrivateConfig");
			Method method = clazz.getMethod("getGoogleAPIClientSecret",new Class[0]);
			return method.invoke(null, null).toString();
		}catch(Exception e){
			return "";
		}
	}
	
	/**
	 * whether is valid email
	 * @param _email
	 * @return
	 */
	public static boolean isValidEmail(String _email){
		return _email.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
	}
	

	/**
	 * buffered Hash map for reduce Google API refresh
	 */
	protected static ConcurrentHashMap<String,BufferedEvents> smEventHashMap = new ConcurrentHashMap<String,BufferedEvents>();
	
	/**
	 * the google credential for some google service 
	 */
	protected GoogleCredential mGoogleCredential = null;
	
	/**
	 * logger
	 */
	protected Logger		mLogger;
	
	/**
	 * yuch account
	 */
	protected String		mYuchAcc;
	
	/**
	 * time zone ID
	 */
	protected String		mTimeZoneID;
	
	/**
	 * all client sync data md5 
	 */
	protected String	mAllClientSyncDataMD5		= null;
	
	/**
	 * all server sync data md5
	 */
	protected String mAllSvrSyncDataMD5		 	= null;
	
	/**
	 * min time want to sync
	 */
	protected long		mMinTimeToSync				= 0;

	/**
	 * the result of sync 
	 */
	protected byte[] mResult						= null;
	
	/**
	 * the list of received from client
	 */
	protected Vector<GoogleAPISyncData>	mClientSyncDataList = null;
		
	/**
	 * google api data list
	 */
	protected Vector<Object>				mSvrSyncDataList = null;
	
	/**
	 * different type
	 * 
	 * -1: sync type
	 * 0 : diff type
	 * 1 : need list type
	 */
	protected int	mDiffType					= -1;
		
	/**
	 * the http transport for google API
	 */
	protected HttpTransport mHttpTransport = new NetHttpTransport();
	
	/**
	 *  the jackson factory for google API 
	 */
	protected JacksonFactory mJsonFactory = new JacksonFactory(); 
	
	
	public GoogleAPISync(InputStream in,Logger _logger)throws Exception{
		mLogger = _logger;
		
		mMinTimeToSync			= sendReceive.ReadLong(in);
		
		String tRefreshToken	= sendReceive.ReadString(in);
		String tAccessToken		= sendReceive.ReadString(in);
				
		mYuchAcc				= sendReceive.ReadString(in);
		mTimeZoneID				= sendReceive.ReadString(in);
		
		mAllClientSyncDataMD5 = sendReceive.ReadString(in);

		
		mGoogleCredential = new GoogleCredential.Builder()
							    .setClientSecrets(getGoogleAPIClientId(), getGoogleAPIClientSecret())
							    .setJsonFactory(mJsonFactory).setTransport(mHttpTransport).build()
							    .setRefreshToken(tRefreshToken)
							    .setAccessToken(tAccessToken);
		
		// read the client sync data and type
		readClientSyncData(in);
		
		mLogger.LogOut(mYuchAcc + " Google Credential Succ! TimeZone:" + mTimeZoneID);
	}
	
	/**
	 * fetch the buffered events
	 * @return
	 */
	protected boolean fetchFormerEvent(){
		
		if(mDiffType == 0){
			return false;
		}
		
		BufferedEvents tFormerEvent = smEventHashMap.get(mYuchAcc + getClass().getSimpleName());
		
		if(tFormerEvent != null){
			if(tFormerEvent.mRefreshTime - System.currentTimeMillis() < 2 * 60 * 1000){
				mAllSvrSyncDataMD5	= tFormerEvent.mAllEventMd5;
				mSvrSyncDataList	= tFormerEvent.mGoogleDataList;
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * store former events
	 * @param events
	 */
	protected void storeFormerEvent(){
		// buffered the former event;
		BufferedEvents tFormerEvent = new BufferedEvents();
				
		tFormerEvent.mAllEventMd5	= mAllSvrSyncDataMD5;
		tFormerEvent.mGoogleDataList= mSvrSyncDataList;
		tFormerEvent.mRefreshTime	= System.currentTimeMillis();
		
		smEventHashMap.put(mYuchAcc + getClass().getSimpleName(),tFormerEvent);
	}
	
	/**
	 * read the server google data
	 */
	public abstract void readSvrGoogleData() throws Exception;
	
	/**
	 * upload the google data to the google servers
	 * @param g
	 * @return Google API object (Event Contact)
	 * @throws Exception
	 */
	protected abstract Object uploadGoogleData(GoogleAPISyncData g)throws Exception;
	
	/**
	 * update the google data to google servers
	 * @param o
	 * @param g
	 * @throws Exception
	 */
	protected abstract Object updateGoogleData(Object o,GoogleAPISyncData g)throws Exception;
	
	/**
	 * delegate the goole data from google server
	 * @param g
	 */
	protected abstract void deleteGoogleData(GoogleAPISyncData g)throws Exception;
	
	/**
	 * is frist sync same data (same google data and bb data)
	 * @param o
	 * @param g
	 * @return
	 * @throws Exception
	 */
	protected abstract boolean isFristSyncSameData(Object o,GoogleAPISyncData g)throws Exception;
	
	/**
	 * get the google data last modification time
	 * @param o
	 * @return
	 */
	protected abstract long getGoogleDataLastMod(Object o);
	
	/**
	 * get the google data id
	 * @param o
	 * @return google data unified id
	 */
	protected abstract String getGoogleDataId(Object o);
		
	/**
	 * create new sync data
	 * @return
	 */
	protected abstract GoogleAPISyncData newSyncData();

	/**
	 * read the client sync data via the json object
	 * @param _json
	 * @throws Exception
	 */
	protected void readClientSyncData(InputStream in)throws Exception{
		
		mDiffType = in.read();
		if(mDiffType != 0){
			
			if(mClientSyncDataList == null){
				mClientSyncDataList = new Vector<GoogleAPISyncData>();
			}
			
			int num = sendReceive.ReadInt(in);
			
			for(int i = 0;i < num;i++){
				GoogleAPISyncData data	= newSyncData();
				data.input(in);
				mClientSyncDataList.add(data);
			}
		}		
	}
	
	/**
	 * export the diff list
	 * @return
	 * @throws Exception
	 */
	protected byte[] exportDiffList()throws Exception{
		
		// different list
		//
		Vector<GoogleAPISyncData> tAddList		= null;
		Vector<GoogleAPISyncData> tDelList	 	= null;
		Vector<GoogleAPISyncData> tUploadList	= null;
		Vector<GoogleAPISyncData> tUpdateList	= null;
		Vector<GoogleAPISyncData> tNeedList		= null;
		
		add_total:
		for(Object g : mSvrSyncDataList){
			
			for(GoogleAPISyncData d : mClientSyncDataList){
				
//				Name tName = ((ContactEntry)g).getName();
//				
//				String[] names = new String[]{
//					tName.hasFamilyName() 	? tName.getFamilyName().getValue():null,
//					tName.hasGivenName() 	? tName.getGivenName().getValue():null,
//					tName.hasAdditionalName()? tName.getAdditionalName().getValue():null,
//					tName.hasNamePrefix() 	? tName.getNamePrefix().getValue():null,
//					tName.hasNameSuffix() 	? tName.getNameSuffix().getValue():null,
//				};
//				
//				if(((ContactSyncData)d).getData() != null){
//					test_flag:
//					for(String s : names){
//						ContactSyncData cc = (ContactSyncData)d;
//						
//						for(String s1 : cc.getData().names){
//							if(s != null && s1 != null && s.equals(s1) && s.equals("一帆")){
//								break test_flag;
//							}
//						}
//					}
//				}
				
				
//				if(d instanceof TaskSyncData){
//					Task task = (Task)g;
//					TaskSyncData dd = (TaskSyncData)d;
//					
//					if(task.getTitle().startsWith("再次新建") && dd.getData().summary.startsWith("再次新建")){
//						System.out.println();
//					}
//				}
				
				if(d.getGID().isEmpty() && d.getAPIData() != null
				&& isFristSyncSameData(g,d)){
						
					if(tUploadList == null){
						tUploadList = new Vector<GoogleAPISyncData>();
					}
					
					d.setGID(getGoogleDataId(g));
					d.setLastMod(getGoogleDataLastMod(g));
					
					tUploadList.add(d);
					
					// remove this avoid next compare
					mClientSyncDataList.remove(d);
					
					continue add_total;
					
				}else{

					if(d.getGID().equals(getGoogleDataId(g))){
						
						if(d.getLastMod() == -1){
							try{
								deleteGoogleData(d);
							}catch(Exception e){
								// export problem exception
								exportHexString(d);
								throw e;
							}
							
						}
						
						continue add_total;
					}
				}
			}
			
			if(tAddList == null){
				tAddList = new Vector<GoogleAPISyncData>();
			}
			
			GoogleAPISyncData data = newSyncData();
			data.importGoogleData(g,mTimeZoneID);
			
			tAddList.add(data);
		}
		
		del_total:
		for(GoogleAPISyncData d : mClientSyncDataList){
			
			if(d.getLastMod() != -1 && !d.getGID().isEmpty()){
				
				for(Object g : mSvrSyncDataList){
					
					if(d.getGID().equals(getGoogleDataId(g))){
						
						long tLastMod = getGoogleDataLastMod(g);
						
						if(d.getLastMod() > tLastMod){
							// client is updated
							// need list
							//
							if(tNeedList == null){
								tNeedList = new Vector<GoogleAPISyncData>();
							}
							tNeedList.add(d);
							
						}else if(d.getLastMod() < tLastMod){
							
							// server's updated
							// update list
							//
							if(tUpdateList == null){
								tUpdateList = new Vector<GoogleAPISyncData>();
							}
							
							d.importGoogleData(g,mTimeZoneID);
							tUpdateList.add(d);
						}
						
						continue del_total;
					}
				}
			}
			
			
			if(!d.getGID().isEmpty()){

				// delete the client calender
				if(tDelList == null){
					tDelList = new Vector<GoogleAPISyncData>();
				}
				tDelList.add(d);
				
			}else{
				
				// upload a new event
				if(tUploadList == null){
					tUploadList = new Vector<GoogleAPISyncData>();
				}
				
				try{

					// upload data to google
					Object g = uploadGoogleData(d);
					
					d.setGID(getGoogleDataId(g));
					d.setLastMod(getGoogleDataLastMod(g));
					
					tUploadList.add(d);
					
				}catch(Exception e){
					// export the problem Google API sync data
					exportHexString(d);					
					throw e;
				}
			}
		}
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try{
			
			if(tAddList != null){
				sendReceive.WriteInt(os, tAddList.size());
				for(GoogleAPISyncData d : tAddList){
					d.output(os, true);
				}
			}else{
				sendReceive.WriteInt(os, 0);
			}
			
			if(tDelList != null){
				sendReceive.WriteInt(os, tDelList.size());
				
				for(GoogleAPISyncData d : tDelList){
					sendReceive.WriteString(os,d.getBBID());
				}
			}else{
				sendReceive.WriteInt(os,0);
			}
			
			if(tUpdateList != null){
				sendReceive.WriteInt(os, tUpdateList.size());
				
				for(GoogleAPISyncData d : tUpdateList){
					d.output(os, true);
				}
			}else{
				sendReceive.WriteInt(os,0);
			}
			
			if(tUploadList != null){
				sendReceive.WriteInt(os, tUploadList.size());
				
				for(GoogleAPISyncData d : tUploadList){
					sendReceive.WriteString(os,d.getBBID());
					sendReceive.WriteString(os,d.getGID());
					sendReceive.WriteLong(os,d.getLastMod());
				}
				
			}else{
				sendReceive.WriteInt(os,0);
			}
			
			if(tNeedList != null){
				sendReceive.WriteInt(os,tNeedList.size());
				for(GoogleAPISyncData d : tNeedList){
					sendReceive.WriteString(os,d.getBBID());
				}
			}else{
				sendReceive.WriteInt(os,0);
			}
		
			return os.toByteArray();
			
		}finally{
			os.close();
		}
	}
	
	/**
	 * compare the event
	 * sync request
	 * 
	 * work following
	 * 
	 * 		client			server
	 * 		|					|
	 * 		Mod md5------------>md5 compare
	 * 		|					|
	 * 		succ(no change)<----nochange or diff 
	 * 		|					|
	 * 		|					|
	 * 		|					|
	 * 		diff list---------->diff list process ( diff type 0)
	 * 		|					|
	 * 		process<-----------add update upload delete needlist
	 * 		|					|
	 * 		needList---------->updated google calendar  ( diff type 1)
	 * 		|					|
	 * 		succ<--------------mod time list
	 * 
	 */
	public void compareEvent()throws Exception{
					
		if(mDiffType == 0){

			String tResultStr;
			if(mAllClientSyncDataMD5.equals(mAllSvrSyncDataMD5)){
				// same events
				//
				tResultStr = "succ";
			}else{
				tResultStr = "diff";
			}
			
			mResult = tResultStr.getBytes("UTF-8");	
			
			mLogger.LogOut(mYuchAcc + " Sync " + tResultStr);
			
		}else if(mDiffType == 1){
			
			// export the 
			// ADD DEL UPDATE NEED list
			//
			mResult = exportDiffList();
			
		}else if(mDiffType == 2){
			
			// process the NeedList
			//
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try{
				sendReceive.WriteInt(os, mClientSyncDataList.size());
				
				// process the need list's client update 
				//
				for(int i = 0;i < mClientSyncDataList.size();i++){
					
					GoogleAPISyncData client = mClientSyncDataList.get(i);
					
					for(Object svr : mSvrSyncDataList){
						if(client.getGID().equals(getGoogleDataId(svr))){
							mSvrSyncDataList.remove(svr);
							
							try{								
								mSvrSyncDataList.add(updateGoogleData(svr,client));
							}catch(Exception e){
								// export the problem
								exportHexString(client);
								throw e;
							}
							
							sendReceive.WriteString(os, client.getBBID());
							sendReceive.WriteLong(os,client.getLastMod());
							
							break;
						}
					}
				}
				
				mResult = os.toByteArray();
				
				mLogger.LogOut(mYuchAcc + " Sync NeedList OK!");
			}finally{
				os.close();
				os = null;
			}
		}else{
			throw new Exception("Error diff type:" + mDiffType);
		}		
	}
	
	/**
	 * get the result json string to process client request
	 * @return
	 */
	public byte[] getResult(){
		return mResult;
	}
	
	/**
	 * convert data to hex string
	 * @param data
	 * @return
	 */
	public static String convertToHex(byte[] data) {
		
        StringBuffer buf = new StringBuffer();
        
        int halfbyte;
        int two_halfs;
        for (int i = 0; i < data.length; i++) {
        	
            halfbyte = (data[i] >>> 4) & 0x0F;
            two_halfs = 0;
            
            do{
            	
                if ((0 <= halfbyte) && (halfbyte <= 9)){
                    buf.append((char) ('0' + halfbyte));
                }else{
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                
                halfbyte = data[i] & 0x0F;
                
            } while(two_halfs++ < 1);
        }
        
        return buf.toString();
    }
	
	/**
	 * get the MD5
	 * @param str
	 * @return
	 */
	public static String getMD5(String str)throws Exception {  
	
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");  
        messageDigest.reset();
        messageDigest.update(str.getBytes("UTF-8")); 
               
        return convertToHex(messageDigest.digest());
    }
	
	/**
	 * convert the string hex to bytes
	 * @param stringData
	 * @throws Exception
	 */
	public static byte[] convertToBytes(String stringData)throws Exception{
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try{
			for(int i = 0;i < stringData.length();i+=2){
				String hex = Character.toString(stringData.charAt(i)) + stringData.charAt(i+1);
				os.write(Integer.parseInt(hex,16));
			}
			
			return os.toByteArray();
			
		}finally{
			os.close();
		}
	}
	
	/**
	 * export bytes array to log file
	 * @ 
	 */
	protected void exportHexString(GoogleAPISyncData d){
		
		try{
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try{
				d.output(os, true);
				mLogger.LogOut("data:" + convertToHex(os.toByteArray()));
			}finally{
				os.close();
			}
		}catch(Exception e){
			mLogger.PrinterException(e);
		}
	}
	
	/**
	 * buffered events list
	 * @author tzz
	 *
	 */
	public static class BufferedEvents{

		//! former refresh time from the Google API
		public long mRefreshTime;
		
		//! all event md5
		public String mAllEventMd5;
		
		//! the google data List
		public Vector<Object>	mGoogleDataList = null;
		
	}

}
