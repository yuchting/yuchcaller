package com.yuchs.yuchcaller;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * phone data to index
 * @author tzz
 *
 */
public class PhoneData 
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
