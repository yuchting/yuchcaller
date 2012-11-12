package com.yuchs.yuchcaller;

import java.io.InputStream;
import java.io.OutputStream;

public class SpecialNumber extends BinSearchNumber{
	
	public int 		m_number;
	public String		m_presents;
	
	//! client search using weight;
	public int			m_searchWeight;
	
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
	
	//! get the string of number to dial 
	public String getDialNumber(){
		if((m_number & 0x80000000) != 0){
			return DbIndex.export800or400Number(m_number);
		}else{
			return Integer.toString(m_number);
		}
	}
	
	public String toString(){
		return m_presents + " (" + getDialNumber() + ")";
	}
}
