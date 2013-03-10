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
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;

import net.rim.blackberry.api.pdap.PIMListListener;

import com.yuchs.yuchcaller.sync.AbsSync;
import com.yuchs.yuchcaller.sync.AbsSyncData;
import com.yuchs.yuchcaller.sync.SyncMain;

public class ContactSync extends AbsSync implements PIMListListener{

	public ContactSync(SyncMain syncMain) {
		super(syncMain);
		
	}

	protected int getSyncPIMListType() {
		return PIM.CONTACT_LIST;
	}

	protected AbsSyncData newSyncData() {
		return new ContactSyncData();
	}

	protected void addPIMItemImpl(PIMList itemList, AbsSyncData d) throws Exception {
		Contact c = ((ContactList)itemList).createContact();
		d.exportData(c);
		
		c.commit();
		d.setBBID(getPIMItemUID(c));
	}

	protected void deletePIMItemImpl(PIMItem item) throws Exception {
		((ContactList)item.getPIMList()).removeContact((Contact)item);
	}

	protected String getPIMItemUID(PIMItem item){
		return AbsSyncData.getStringField(item, Contact.UID);
	}
}
