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
 * cell phone data
 * @author tzz
 *
 */
public class CellPhoneData extends PhoneData
{
	public short			m_phoneNumberEnd;
	public byte			m_carrier;
	
	public void Read(InputStream in)throws Exception{
		super.Read(in);
		
		m_phoneNumberEnd	= sendReceive.ReadShort(in);
		m_carrier			= (byte)in.read(); 
	}
	
	public void Write(OutputStream os)throws Exception{
		super.Write(os);
		
		sendReceive.WriteShort(os,m_phoneNumberEnd);
		os.write(m_carrier);
	}
	
	//! compare with a number for bineary search
	public int Compare(int _number){
		
		if((m_phoneNumber + m_phoneNumberEnd) < _number){
			return -1;
		}else if(m_phoneNumber > _number){
			return 1;
		}else{
			return 0;
		}		
	}
}
