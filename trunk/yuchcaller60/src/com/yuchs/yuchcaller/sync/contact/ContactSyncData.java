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
import javax.microedition.pim.PIMItem;

import net.rim.blackberry.api.pdap.BlackBerryContact;

import com.yuchs.yuchcaller.sync.AbsData;
import com.yuchs.yuchcaller.sync.AbsSyncData;

public class ContactSyncData extends AbsSyncData {
	
	/**
	 * get/set the ContactData
	 * @return
	 */
	public ContactData getData(){return (ContactData)m_data;}	
	public void setData(ContactData d){m_data = d;}

	/**
	 * need calculate md5 by minTime and by index
	 * @param minTime
	 * @param idx  index of current AbsSyncData
	 * @return
	 */
	protected boolean needCalculateMD5(long minTime,int idx){
		
		// names is empty contact don't need calculate MD5
		//
		if(getData() != null && getData().names == null){
			return false;			
		}
		
		return true;
	}

	protected AbsData newData() {
		return new ContactData();
	}
	
	/**
	 * export data to PIMItem(Contact)
	 */
	public void exportData(PIMItem item) throws Exception {
		
		BlackBerryContact contact = (BlackBerryContact)item;
		
		clearPIMItemFields(contact, BlackBerryContact.NAME);
		setStringArrayField( contact, BlackBerryContact.NAME,makeRightSizeArr(getData().names,contact.getPIMList().stringArraySize(BlackBerryContact.NAME)));
		
		clearPIMItemFields(contact, BlackBerryContact.ADDR);
		setStringArrayField( contact, BlackBerryContact.ADDR,BlackBerryContact.ATTR_WORK,makeRightSizeArr(getData().addr_work,contact.getPIMList().stringArraySize(BlackBerryContact.ADDR)));
		setStringArrayField( contact, BlackBerryContact.ADDR,BlackBerryContact.ATTR_HOME,makeRightSizeArr(getData().addr_home,contact.getPIMList().stringArraySize(BlackBerryContact.ADDR)));		

		clearPIMItemFields(contact, BlackBerryContact.TEL);
		if(getData().tel != null){

			for(int i = 0;i < getData().tel.length;i++){
				String telNum = getData().tel[i];
				switch(i){
				case ContactData.TEL_WORK:
					setStringField( contact, BlackBerryContact.TEL, BlackBerryContact.ATTR_WORK,telNum);
					break;
				case ContactData.TEL_WORK2:
					setStringField( contact, BlackBerryContact.TEL, BlackBerryContact.ATTR_WORK2,telNum);
					break;
				case ContactData.TEL_HOME:
					setStringField( contact, BlackBerryContact.TEL, BlackBerryContact.ATTR_HOME,telNum);
					break;
				case ContactData.TEL_HOME2:
					setStringField( contact, BlackBerryContact.TEL, BlackBerryContact.ATTR_HOME2,telNum);
					break;
				case ContactData.TEL_MOBILE:
					setStringField( contact, BlackBerryContact.TEL, BlackBerryContact.ATTR_MOBILE,telNum);
					break;
				case ContactData.TEL_MOBILE2:
					setStringField( contact, BlackBerryContact.TEL, BlackBerryContact.ATTR_MOBILE << 16,telNum);
					break;
				case ContactData.TEL_PAGER:
					setStringField( contact, BlackBerryContact.TEL, BlackBerryContact.ATTR_PAGER,telNum);
					break;
				case ContactData.TEL_FAX:
					setStringField( contact, BlackBerryContact.TEL, BlackBerryContact.ATTR_FAX,telNum);
					break;
				case ContactData.TEL_OTHER:
					setStringField( contact, BlackBerryContact.TEL, BlackBerryContact.ATTR_OTHER,telNum);
					break;					
				}
			}
		}
			
		clearPIMItemFields(item, BlackBerryContact.EMAIL);
		if(getData().email != null){
			for(int i = 0;i < getData().email.length;i++){
				String email = getData().email[i];
								
				switch(i){
				case ContactData.EMAIL_OTHER:
					setStringField( contact, BlackBerryContact.EMAIL, BlackBerryContact.ATTR_OTHER,i,email);
					break;	
				case ContactData.EMAIL_WORK:
					setStringField( contact, BlackBerryContact.EMAIL, BlackBerryContact.ATTR_WORK,i,email);
					break;
				case ContactData.EMAIL_HOME:
					setStringField( contact, BlackBerryContact.EMAIL, BlackBerryContact.ATTR_HOME,i,email);
					break;
								
				}
			}
		}

		setStringField(contact,BlackBerryContact.ORG,getData().org);
		setStringField(contact,BlackBerryContact.TITLE,getData().title);
		setStringField(contact,BlackBerryContact.NOTE,getData().note);
		setDateField(contact,BlackBerryContact.BIRTHDAY,getData().birthday);
		setStringField(contact,BlackBerryContact.NICKNAME,getData().nickname);
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

		BlackBerryContact contact = (BlackBerryContact)item;
		
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
			case BlackBerryContact.UID:
				setBBID(getStringField(contact,id));
				break;
			case BlackBerryContact.NAME:
				getData().names = getStringArrayField(contact, id,ContactData.NAME_SIZE);
				break;
			case BlackBerryContact.NICKNAME:
				getData().nickname = getStringField(contact,id);
				break;
			case BlackBerryContact.ADDR:
				getData().addr_work = getStringArrayField(contact, id,BlackBerryContact.ATTR_WORK,ContactData.ADDR_SIZE);
				getData().addr_home = getStringArrayField(contact, id,BlackBerryContact.ATTR_HOME,ContactData.ADDR_SIZE);
				break;
			case BlackBerryContact.TEL:
				getData().tel = new String[ContactData.TEL_SIZE];
				count = contact.countValues(id);
				for(int c = 0;c < count;c++){
					
					int attr		= contact.getAttributes(id, c);
					String value	= contact.getString(id, c);
					
					switch(attr){
					case BlackBerryContact.ATTR_WORK:
						getData().tel[ContactData.TEL_WORK] = value;
						break;
					case BlackBerryContact.ATTR_WORK2:
						getData().tel[ContactData.TEL_WORK2] = value;
						break;
					case BlackBerryContact.ATTR_HOME:
						getData().tel[ContactData.TEL_HOME] = value;
						break;
					case BlackBerryContact.ATTR_HOME2:
						getData().tel[ContactData.TEL_HOME2] = value;
						break;
					case BlackBerryContact.ATTR_MOBILE:
						getData().tel[ContactData.TEL_MOBILE] = value;
						break;
					case BlackBerryContact.ATTR_MOBILE << 16:
						getData().tel[ContactData.TEL_MOBILE2] = value;
						break;
					case BlackBerryContact.ATTR_PAGER:
						getData().tel[ContactData.TEL_PAGER] = value;
						break;
					case BlackBerryContact.ATTR_FAX:
						getData().tel[ContactData.TEL_FAX] = value;
						break;
					case BlackBerryContact.ATTR_OTHER:
					case BlackBerryContact.ATTR_NONE:
						getData().tel[ContactData.TEL_OTHER] = value;
						break;
					}
				}
				
				break;
				
			case BlackBerryContact.EMAIL:
				getData().email = new String[ContactData.EMAIL_SIZE];
				count = contact.countValues(id);
				
				for(int c = 0;c < count;c++){
					int attr		= contact.getAttributes(id, c);
					String value	= contact.getString(id, c);
					
					if(attr == BlackBerryContact.ATTR_NONE){
						getData().email[c] = value;
					}else{
						switch(attr){
						case BlackBerryContact.ATTR_OTHER:
							getData().email[ContactData.EMAIL_OTHER] = value;
							break;
						case BlackBerryContact.ATTR_WORK:
							getData().email[ContactData.EMAIL_WORK] = value;
							break;
						case BlackBerryContact.ATTR_HOME:
							getData().email[ContactData.EMAIL_HOME] = value;
							break;						
						}
					}
										
				}
				break;
			case BlackBerryContact.ORG:
				getData().org = getStringField(contact,id);
				break;
			case BlackBerryContact.TITLE:
				getData().title = getStringField(contact,id);
				break;
			case BlackBerryContact.NOTE:
				getData().note = getStringField(contact,id);
				break;
			case BlackBerryContact.BIRTHDAY:
				getData().birthday = getDateField(contact,id);				 
				break;
			}
		}
	}
}

