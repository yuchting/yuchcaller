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
import java.lang.reflect.Method;
import java.security.MessageDigest;

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
	 * the result of sync 
	 */
	protected byte[] mResult						= null;
	
	/**
	 * the http transport for google API
	 */
	protected HttpTransport mHttpTransport = new NetHttpTransport();
	
	/**
	 *  the jackson factory for google API 
	 */
	protected JacksonFactory mJsonFactory = new JacksonFactory(); 
	
	
	public GoogleAPISync(InputStream in,Logger _logger)throws Exception{
		
		String tAccessToken		= sendReceive.ReadString(in);
		String tRefreshToken	= sendReceive.ReadString(in);
		
		mYuchAcc				= sendReceive.ReadString(in);
		mTimeZoneID				= sendReceive.ReadString(in);
		
		mAllClientSyncDataMD5 = sendReceive.ReadString(in);
		
		mGoogleCredential = new GoogleCredential.Builder()
							    .setClientSecrets(getGoogleAPIClientId(), getGoogleAPIClientSecret())
							    .setJsonFactory(mJsonFactory).setTransport(mHttpTransport).build()
							    .setRefreshToken(tRefreshToken)
							    .setAccessToken(tAccessToken);
		
		mLogger.LogOut("Google Credential Succ! " + mYuchAcc + " TimeZone:" + mTimeZoneID);
	}
	
	/**
	 * get the result json string to process client request
	 * @return
	 */
	public byte[] getResult(){
		return mResult;
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
    
        byte[] byteArray = messageDigest.digest();  
  
        StringBuffer md5StrBuff = new StringBuffer();  
  
        for (int i = 0; i < byteArray.length; i++) {              
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)  
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));  
            else  
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));  
        }  
  
        return md5StrBuff.toString();
    } 
}
