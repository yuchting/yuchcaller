package com.yuchs.yuchcaller.sync.contact;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;

import com.yuchs.yuchcaller.sync.AbsData;
import com.yuchs.yuchcaller.sync.AbsSyncData;

public class ContactSyncData extends AbsSyncData {
	
	/**
	 * get/set the ContactData
	 * @return
	 */
	public ContactData getData(){return (ContactData)m_data;}	
	public void setData(ContactData d){m_data = d;}

	protected boolean needCalculateMD5(long minTime) {
		// all contact need calculate MD5
		return true;
	}

	protected AbsData newData() {
		return new ContactData();
	}

	/**
	 * import blackberry Contact
	 * @param _contact
	 * @param list		ContactList
	 */
	public void importData(Contact _contact,ContactList _list)throws Exception{
		
	}
	
	/**
	 * export the contact to the blackberry contact
	 * @param event
	 * @throws Exception
	 */
	public void exportData(Contact _contact,ContactList _list)throws Exception{
		
	}
	
	public void exportData(PIMItem item, PIMList list) throws Exception {
		// TODO Auto-generated method stub
		
	}
	public void importData(PIMItem item, PIMList list) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
