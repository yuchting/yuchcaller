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

import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;

import com.yuchs.yuchcaller.sendReceive;

public abstract class AbsSyncData {
	
	// the bb system calendar UID
	protected String bID = null;
	
	// the google calendar UID
	protected String gID = null;
	
	// last modified time
	protected long lastMod = 0;
	
	// calendar/contact/task data
	protected AbsData	m_data = null;
	
	public void setBBID(String _bID){bID = _bID;}
	public String getBBID(){return bID;}
	
	public void setGID(String _id){gID = _id;}
	public String getGID(){	return gID;}
	
	public void setLastMod(long _mod){lastMod = _mod;}
	public long getLastMod(){return lastMod;}
	
	/**
	 * new a data class
	 * @return
	 */
	protected abstract AbsData newData();
	
	/**
	 * need calculate md5 by minTime
	 * @param minTime
	 * @return
	 */
	protected abstract boolean needCalculateMD5(long minTime);
	
	/**
	 * ouput data to the stream
	 * @param _os
	 * @param _outputData
	 * @throws Exception
	 */
	public void output(OutputStream os,boolean _outputData)throws Exception{
		sendReceive.WriteString(os,getBBID());
		sendReceive.WriteString(os,getGID());
		sendReceive.WriteLong(os,getLastMod());
		
		if(m_data != null && _outputData){
			sendReceive.WriteBoolean(os, true);
			m_data.outputData(os);
		}else{
			sendReceive.WriteBoolean(os, false);
		}
	}
	
	/**
	 * input data from the stream
	 * @param _in
	 * @throws Exception
	 */
	public void input(InputStream in)throws Exception{
		setBBID(sendReceive.ReadString(in));
		setGID(sendReceive.ReadString(in));
		setLastMod(sendReceive.ReadLong(in));
		
		boolean tHasData = sendReceive.ReadBoolean(in);
		if(tHasData){

			if(m_data == null){
				m_data = newData();
			}
			
			m_data.inputData(in);
		}
	}
	
	/**
	 * get the blackberry event string 
	 * @param _item
	 * @param _id
	 * @return
	 */
	public static String getStringField(PIMItem _item,int _id){
		int tCount = _item.countValues(_id);
		if(tCount > 0){
			return _item.getString(_id, 0);
		}
		
		return "";
	}
	
	/**
	 * set the event id by string
	 * @param _list 
	 * @param _item
	 * @param _id
	 * @param _value
	 */
	public static void setStringField(PIMList _list,PIMItem _item,int _id,String _value){
		try{
			if(_list.isSupportedField(_id)){
				
				if(_item.countValues(_id) > 0){
					if(_value != null && _value.length() > 0){
						_item.setString(_id,0,PIMItem.ATTR_NONE,_value);
					}else{
						_item.removeValue(_id,0);
					}
				}else{
					if(_value != null && _value.length() > 0){
						_item.addString(_id,PIMItem.ATTR_NONE,_value);
					}				
				}
			}
		}catch(Exception e){
			System.out.println("Fuck!");
		}
		
	}
	
	/**
	 * get the the long(for date) field
	 * @param _item
	 * @param _id
	 * @return
	 */
	public static long getDateField(PIMItem _item,int _id){
		int tCount = _item.countValues(_id);
		if(tCount > 0){
			return _item.getDate(_id, 0);
		}
		
		return 0;
	}
	
	/**
	 * set the date field for event
	 * @param _list
	 * @param _item
	 * @param _id
	 * @param _value
	 */
	public static void setDateField(PIMList _list,PIMItem _item,int _id,long _value){
		try{
		if(_list.isSupportedField(_id)){
			if(_item.countValues(_id) > 0){
				_item.setDate(_id,0,PIMItem.ATTR_NONE,_value);
			}else{
				_item.addDate(_id,PIMItem.ATTR_NONE,_value);
			}
			
		}
		
	}catch(Exception e){
		System.out.println("Fuck!");
	}
	
	}
	
	/**
	 * set the date field for event by data value
	 * @param _list
	 * @param _item
	 * @param _id
	 * @param _value
	 */
	public static void setDateField(PIMList _list,PIMItem _item,int _id,String _value){
		
		try{
			long v = Long.parseLong(_value);
			setDateField(_list,_item,_id,v);
		}catch(Exception e){}
	}
	
	
	/**
	 * get the integer field for the event
	 * @param _item
	 * @param _id
	 * @return
	 */
	public static int getIntField(PIMItem _item,int _id){
		int tCount = _item.countValues(_id);
		if(tCount > 0){
			return _item.getInt(_id, 0);
		}
		
		return 0;
	}
	
	/**
	 * set the int value of this event
	 * @param _list
	 * @param _item
	 * @param _id
	 * @param _value
	 */
	public static void setIntField(PIMList _list,PIMItem _item,int _id,int _value){
		try{
		if(_list.isSupportedField(_id)){
			if(_item.countValues(_id) > 0){
				_item.setInt(_id,0,PIMItem.ATTR_NONE,_value);
			}else{
				_item.addInt(_id,PIMItem.ATTR_NONE,_value);
			}
		}
	}catch(Exception e){
		System.out.println("Fuck!");
	}
	
	}
	
	/**
	 * set the int field by int string
	 * @param _list
	 * @param _item
	 * @param _id
	 * @param _value
	 */
	public static void setIntField(PIMList _list,PIMItem _item,int _id,String _value){
		
		try{
			int v = Integer.parseInt(_value);
			setIntField(_list,_item,_id,v);
		}catch(Exception e){}
	}
	
	/**
	 * get the boolean field for the event
	 * @param _item
	 * @param _id
	 * @return
	 */
	public static boolean getBooleanField(PIMItem _item,int _id){
		int tCount = _item.countValues(_id);
		if(tCount > 0){
			return _item.getBoolean(_id, 0);
		}
		
		return false;
	}
	
	/**
	 * set the boolean field
	 * @param _list
	 * @param _item
	 * @param _id
	 * @param _value
	 */
	public static void setBooleanField(PIMList _list,PIMItem _item,int _id,boolean _value){
		try{
		if(_list.isSupportedField(_id)){
			if(_item.countValues(_id) > 0){
				_item.setBoolean(_id, 0, PIMItem.ATTR_NONE, _value);
			}else{
				_item.addBoolean(_id, PIMItem.ATTR_NONE, _value);
			}
		}
	}catch(Exception e){
		System.out.println("Fuck!");
	}
	
	}
	
	/**
	 * get the string array field 
	 * @param _item
	 * @param _id
	 * @return
	 */
	public static String[] getStringArrayField(PIMList _list,PIMItem _item,int _id){
		int tCount = _item.countValues(_id);
		if(tCount > 0){
			if(_list.getFieldDataType(_id) == PIMItem.STRING_ARRAY){
				
				return _item.getStringArray(_id, 0);
				
			}else if(_list.getFieldDataType(_id) == PIMItem.STRING){

				String[] tResult = new String[tCount];
				for(int i = 0 ;i < tCount;i++){
					tResult[i] = _item.getString(_id, i);
				}
				
				return tResult;
			}
		}
		
		return null;
	}
	
	/**
	 * set the String array field
	 * @param _list
	 * @param _item
	 * @param _id
	 * @param _value
	 */
	public static void setStringArrayField(PIMList _list,PIMItem _item,int _id,String[] _value){
		if(_list.isSupportedField(_id)){
			
			int count = _item.countValues(_id);
			
			if(_list.getFieldDataType(_id) == PIMItem.STRING_ARRAY){

				if(count > 0){
					if(_value != null && _value.length > 0){
						_item.setStringArray(_id, 0, PIMItem.STRING_ARRAY, _value);
					}else{
						_item.removeValue(_id,0);
					}				
				}else{
					if(_value != null && _value.length > 0){
						_item.addStringArray(_id, PIMItem.STRING_ARRAY, _value);
					}				
				}
				
			}else if(_list.getFieldDataType(_id) == PIMItem.STRING){
				
				if(count > 0){
					for(int i = 0;i < count;i++){
						_item.removeValue(_id,0);
					}
				}
				
				if(_value != null && _value.length > 0){
					for(int i = 0 ;i < _value.length;i++){
						_item.addString(_id, PIMItem.ATTR_NONE, _value[i]);
					}
				}
			}
		}
	}
}
