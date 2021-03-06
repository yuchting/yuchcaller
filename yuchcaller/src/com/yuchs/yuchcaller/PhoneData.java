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
package com.yuchs.yuchcaller;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * phone data to index
 * @author tzz
 *
 */
public class PhoneData extends BinSearchNumber
{
	public int			m_phoneNumber;
	public byte		m_province;
	public short		m_city;
	
	public void Read(InputStream in)throws Exception{
		m_phoneNumber	= sendReceive.ReadInt(in);
		m_province		= (byte)in.read();
		m_city			= (short)sendReceive.ReadShort(in);
	}
	
	public void Write(OutputStream os)throws Exception{
		sendReceive.WriteInt(os,m_phoneNumber);
		os.write(m_province);
		sendReceive.WriteShort(os, m_city);
	}
	
	//! compare with a number for bineary search
	public int Compare(int _number){
		if(m_phoneNumber < _number){
			return -1;
		}else if(m_phoneNumber > _number){
			return 1;
		}else{
			return 0;
		}		
	}
}
