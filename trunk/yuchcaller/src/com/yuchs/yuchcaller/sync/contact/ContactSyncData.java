package com.yuchs.yuchcaller.sync.contact;

import javax.microedition.pim.Contact;
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
	 * export data to PIMItem(Contact)
	 */
	public void exportData(PIMItem item, PIMList list) throws Exception {
		
		Contact contact = (Contact)item;
		
		setStringArrayField(list, contact, Contact.NAME,getRightSizeArr(getData().names,list.stringArraySize(Contact.NAME)));
		
		setStringArrayField(list, contact, Contact.ADDR,Contact.ATTR_HOME,getRightSizeArr(getData().addr_home,list.stringArraySize(Contact.ADDR)));
		setStringArrayField(list, contact, Contact.ADDR,Contact.ATTR_WORK,getRightSizeArr(getData().addr_work,list.stringArraySize(Contact.ADDR)));
		
		if(getData().tel != null){
			for(int i = 0;i < getData().tel.length;i++){
				String telNum = getData().tel[i];
				switch(i){
				case ContactData.TEL_WORK:
					setStringField(list, contact, Contact.TEL, Contact.ATTR_WORK,telNum);
					break;
				case ContactData.TEL_WORK2:
					setStringField(list, contact, Contact.TEL, Contact.ATTR_WORK << 16,telNum);
					break;
				case ContactData.TEL_HOME:
					setStringField(list, contact, Contact.TEL, Contact.ATTR_HOME,telNum);
					break;
				case ContactData.TEL_HOME2:
					setStringField(list, contact, Contact.TEL, Contact.ATTR_HOME << 16,telNum);
					break;
				case ContactData.TEL_MOBILE:
					setStringField(list, contact, Contact.TEL, Contact.ATTR_MOBILE,telNum);
					break;
				case ContactData.TEL_MOBILE2:
					setStringField(list, contact, Contact.TEL, Contact.ATTR_MOBILE << 16,telNum);
					break;
				case ContactData.TEL_PAGER:
					setStringField(list, contact, Contact.TEL, Contact.ATTR_PAGER,telNum);
					break;
				case ContactData.TEL_FAX:
					setStringField(list, contact, Contact.TEL, Contact.ATTR_FAX,telNum);
					break;
				case ContactData.TEL_OTHER:
					setStringField(list, contact, Contact.TEL, Contact.ATTR_OTHER,telNum);
					break;					
				}
			}
		}
		
		if(getData().email != null){
			
			for(int i = 0;i < getData().tel.length;i++){
				String email = getData().tel[i];
				
				switch(i){
				case ContactData.EMAIL_WORK:
					setStringField(list, contact, Contact.EMAIL, Contact.ATTR_WORK,email);
					break;
				case ContactData.EMAIL_HOME:
					setStringField(list, contact, Contact.EMAIL, Contact.ATTR_HOME,email);
					break;				
				case ContactData.EMAIL_OTHER:
					setStringField(list, contact, Contact.EMAIL, Contact.ATTR_OTHER,email);
					break;					
				}
			}
		}
		
		setStringField(list,contact,Contact.ORG,getData().org);
		setStringField(list,contact,Contact.NOTE,getData().note);
		setDateField(list,contact,Contact.BIRTHDAY,getData().birthday);
		setStringField(list,contact,Contact.NICKNAME,getData().nickname);
	}
	
	/**
	 * make sure the array is right size
	 * @param arr
	 * @param rightSize
	 * @return
	 */
	private String[] getRightSizeArr(String[] arr,int rightSize){
		
		if(arr != null){
			if(arr.length != rightSize){
				String[] newArr = new String[rightSize];
				for(int i = 0;i < Math.min(rightSize,arr.length);i++){
					newArr[i] = arr[i];
				}
				
				arr = newArr;
			}
		}
		
		return arr;
	}
	public void importData(PIMItem item, PIMList list) throws Exception {

		Contact contact = (Contact)item;
		
		if(getData() == null){
			setData(new ContactData());
		}else{
			getData().clear();
		}
				
		int[] fieldIds = contact.getFields();
		int id;
		for(int i = 0;i < fieldIds.length;i++){
			id = fieldIds[i];
			
			switch(id){
			case Contact.UID:
				setBBID(getStringField(contact,id));
				break;
			case Contact.NAME:
				getData().names = getStringArrayField(list, contact, id);
				break;
			case Contact.NICKNAME:
				getData().nickname = getStringField(contact,id);
				break;
			case Contact.ADDR:
				getData().addr_work = getStringArrayField(list, contact, id,0);
				getData().addr_home = getStringArrayField(list, contact, id,1);
				break;
			case Contact.TEL:
				getData().tel = new String[list.maxValues(id)];
				int count = contact.countValues(id);
				for(int c = 0;c < count;c++){
					
					int attr		= contact.getAttributes(id, c);
					String value	= contact.getString(id, c);
					
					switch(attr){
					case Contact.ATTR_WORK:
						getData().tel[ContactData.TEL_WORK] = value;
						break;
					case Contact.ATTR_WORK << 16:
						getData().tel[ContactData.TEL_WORK2] = value;
						break;
					case Contact.ATTR_HOME:
						getData().tel[ContactData.TEL_HOME] = value;
						break;
					case Contact.ATTR_HOME << 16:
						getData().tel[ContactData.TEL_HOME2] = value;
						break;
					case Contact.ATTR_MOBILE:
						getData().tel[ContactData.TEL_MOBILE] = value;
						break;
					case Contact.ATTR_MOBILE << 16:
						getData().tel[ContactData.TEL_MOBILE2] = value;
						break;
					case Contact.ATTR_PAGER:
						getData().tel[ContactData.TEL_PAGER] = value;
						break;
					case Contact.ATTR_FAX:
						getData().tel[ContactData.TEL_FAX] = value;
						break;
					case Contact.ATTR_OTHER:
						getData().tel[ContactData.TEL_OTHER] = value;
						break;
					}
				}
				
				break;
				
			case Contact.EMAIL:
				getData().email = new String[ContactData.EMAIL_SIZE];
				int num = contact.countValues(id);
				for(int c = 0;c < num;c++){
					int attr		= contact.getAttributes(id, c);
					String value	= contact.getString(id, c);
					switch(attr){
					case Contact.ATTR_WORK:
						getData().tel[ContactData.EMAIL_WORK] = value;
						break;
					case Contact.ATTR_HOME:
						getData().tel[ContactData.EMAIL_HOME] = value;
						break;
					case Contact.ATTR_OTHER:
						getData().tel[ContactData.EMAIL_OTHER] = value;
						break;
					}					
				}
				break;
			case Contact.ORG:
				getData().org = getStringField(contact,id);
				break;
			case Contact.NOTE:
				getData().note = getStringField(contact,id);
				break;
			case Contact.BIRTHDAY:
				getData().birthday = getDateField(contact,id);
				break;
			}
		}
	}
}

