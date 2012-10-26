package com.yuchs.yuchcaller;

import java.io.InputStream;
import java.util.Vector;

import local.yuchcallerlocalResource;

public class DbIndex {

	//! phone data index list
	private Vector		m_phoneDataList = new Vector();
	
	//! cell phone data index list
	private Vector		m_cellPhoneDataList = new Vector();
	
	//! the version of db index
	private int		m_dbIndexVersion	= 0;
	
	//! index the phone number
	public PhoneData findPhoneData(String _number){
		
	}
	
	/**
	 * read the idx file by InputStream
	 * @param in
	 */
	public void ReadIdxFile(InputStream in)throws Exception{
		if(in.read() != 'y' || in.read() != 'u' || in.read() != 'c' || in.read() != 'h'){
			throw new Exception(YuchCaller.sm_local.getString(yuchcallerlocalResource.PHONE_ERROR_DB_INDEX_FILE));
		}
		
		
	}
}
