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
	public int			m_phoneNumberEnd;
	public byte		m_carrier;
	
	public void Read(InputStream in)throws Exception{
		super.Read(in);
		
		m_phoneNumberEnd	= sendReceive.ReadInt(in);
		m_carrier		= (byte)in.read(); 
	}
	
	public void Write(OutputStream os)throws Exception{
		super.Write(os);
		
		sendReceive.WriteInt(os,m_phoneNumberEnd);
		os.write(m_carrier);
	}
}
