package com.yuchs.yuchcaller.sync.svr.contact;

import java.io.InputStream;
import java.io.OutputStream;

import com.yuchs.yuchcaller.sync.svr.GoogleAPIData;
import com.yuchs.yuchcaller.sync.svr.GoogleAPISyncData;

public class ContactSyncData extends GoogleAPISyncData {


	public ContactData getData(){return (ContactData)m_APIData;}
	public void setData(ContactData data){m_APIData = data;}
	
	@Override
	protected GoogleAPIData newData() {
		return new ContactData();
	}

	
	@Override
	public void exportGoogleData(Object g, String timeZoneID) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void importGoogleData(Object g) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void input(InputStream in) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void output(OutputStream os, boolean outputData) throws Exception {
		// TODO Auto-generated method stub

	}

}
