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
package com.yuchs.yuchcaller.sync.contact;

import javax.microedition.pim.Contact;
import javax.microedition.pim.PIMItem;

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
	public void exportData(PIMItem item) throws Exception {
		
		Contact contact = (Contact)item;
		
		setStringArrayField( contact, Contact.NAME,makeRightSizeArr(getData().names,contact.getPIMList().stringArraySize(Contact.NAME)));
		
		setStringArrayField( contact, Contact.ADDR,Contact.ATTR_WORK,makeRightSizeArr(getData().addr_work,contact.getPIMList().stringArraySize(Contact.ADDR)));
		setStringArrayField( contact, Contact.ADDR,Contact.ATTR_HOME,makeRightSizeArr(getData().addr_home,contact.getPIMList().stringArraySize(Contact.ADDR)));		
		
		if(getData().tel != null){

			for(int i = 0;i < getData().tel.length;i++){
				String telNum = getData().tel[i];
				switch(i){
				case ContactData.TEL_WORK:
					setStringField( contact, Contact.TEL, Contact.ATTR_WORK,telNum);
					break;
				case ContactData.TEL_WORK2:
					setStringField( contact, Contact.TEL, Contact.ATTR_WORK << 16,telNum);
					break;
				case ContactData.TEL_HOME:
					setStringField( contact, Contact.TEL, Contact.ATTR_HOME,telNum);
					break;
				case ContactData.TEL_HOME2:
					setStringField( contact, Contact.TEL, Contact.ATTR_HOME << 16,telNum);
					break;
				case ContactData.TEL_MOBILE:
					setStringField( contact, Contact.TEL, Contact.ATTR_MOBILE,telNum);
					break;
				case ContactData.TEL_MOBILE2:
					setStringField( contact, Contact.TEL, Contact.ATTR_MOBILE << 16,telNum);
					break;
				case ContactData.TEL_PAGER:
					setStringField( contact, Contact.TEL, Contact.ATTR_PAGER,telNum);
					break;
				case ContactData.TEL_FAX:
					setStringField( contact, Contact.TEL, Contact.ATTR_FAX,telNum);
					break;
				case ContactData.TEL_OTHER:
					setStringField( contact, Contact.TEL, Contact.ATTR_OTHER,telNum);
					break;					
				}
			}
		}
			
		if(getData().email != null){
			
			clearPIMItemFields(item, Contact.EMAIL);
			
			for(int i = 0;i < getData().email.length;i++){
				String email = getData().email[i];
								
				switch(i){
				case ContactData.EMAIL_OTHER:
					setStringField( contact, Contact.EMAIL, Contact.ATTR_OTHER,i,email);
					break;	
				case ContactData.EMAIL_WORK:
					setStringField( contact, Contact.EMAIL, Contact.ATTR_WORK,i,email);
					break;
				case ContactData.EMAIL_HOME:
					setStringField( contact, Contact.EMAIL, Contact.ATTR_HOME,i,email);
					break;
								
				}
			}
		}

		setStringField(contact,Contact.ORG,getData().org);
		setStringField(contact,Contact.NOTE,getData().note);
		setDateField(contact,Contact.BIRTHDAY,getData().birthday);
		setStringField(contact,Contact.NICKNAME,getData().nickname);
	}
	
	/**
	 * make sure the array is right size
	 * @param arr
	 * @param rightSize
	 * @return
	 */
	private String[] makeRightSizeArr(String[] arr,int rightSize){
		
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
	
	public void importData(PIMItem item) throws Exception {

		Contact contact = (Contact)item;
		
		if(getData() == null){
			setData(new ContactData());
		}else{
			getData().clear();
		}
				
		int[] fieldIds = contact.getFields();
		int id;
		int count;
		for(int i = 0;i < fieldIds.length;i++){
			id = fieldIds[i];
			
			switch(id){
			case Contact.UID:
				setBBID(getStringField(contact,id));
				break;
			case Contact.NAME:
				getData().names = getStringArrayField(contact, id);
				break;
			case Contact.NICKNAME:
				getData().nickname = getStringField(contact,id);
				break;
			case Contact.ADDR:
				getData().addr_work = getStringArrayField(contact, id,Contact.ATTR_WORK);
				getData().addr_home = getStringArrayField(contact, id,Contact.ATTR_HOME);
				break;
			case Contact.TEL:
				getData().tel = new String[ContactData.TEL_SIZE];
				count = contact.countValues(id);
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
					case Contact.ATTR_NONE:
						getData().tel[ContactData.TEL_OTHER] = value;
						break;
					}
				}
				
				break;
				
			case Contact.EMAIL:
				getData().email = new String[ContactData.EMAIL_SIZE];
				count = contact.countValues(id);
				
				for(int c = 0;c < count;c++){
					int attr		= contact.getAttributes(id, c);
					String value	= contact.getString(id, c);
					
					if(attr == Contact.ATTR_NONE){
						getData().email[c] = value;
					}else{
						switch(attr){
						case Contact.ATTR_OTHER:
							getData().email[ContactData.EMAIL_OTHER] = value;
							break;
						case Contact.ATTR_WORK:
							getData().email[ContactData.EMAIL_WORK] = value;
							break;
						case Contact.ATTR_HOME:
							getData().email[ContactData.EMAIL_HOME] = value;
							break;						
						}
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

