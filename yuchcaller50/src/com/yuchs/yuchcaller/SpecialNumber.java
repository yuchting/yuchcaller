package com.yuchs.yuchcaller;

import java.io.InputStream;
import java.io.OutputStream;

public class SpecialNumber extends BinSearchNumber{
	
	int 		m_number;
	String		m_presents;
	
	public void Read(InputStream in)throws Exception{
		m_number	= sendReceive.ReadInt(in);
		m_presents	= sendReceive.ReadString(in); 
	}
	
	public void Write(OutputStream os)throws Exception{		
		sendReceive.WriteInt(os,m_number);
		sendReceive.WriteString(os, m_presents);
	}	

	//! compare with a number for bineary search
	public int Compare(int _number){
		
		if(m_number < _number){
			return -1;
		}else if(m_number > _number){
			return 1;
		}else{
			return 0;
		}		
	}
}
