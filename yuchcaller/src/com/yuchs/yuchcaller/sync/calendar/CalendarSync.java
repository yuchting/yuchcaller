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
package com.yuchs.yuchcaller.sync.calendar;

import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;

import net.rim.blackberry.api.pdap.PIMListListener;

import com.yuchs.yuchcaller.sync.AbsSync;
import com.yuchs.yuchcaller.sync.AbsSyncData;
import com.yuchs.yuchcaller.sync.SyncMain;

public class CalendarSync extends AbsSync implements PIMListListener{
				
	public CalendarSync(SyncMain _syncMain){
		super(_syncMain);
	}
	
	/**
	 * return the sync main type:
	 * 
	 * PIM.EVENT_LIST
	 * PIM.CONTACT_LIST
	 * PIM.TODO_LIST
	 * 
	 * @return
	 */
	protected int getSyncPIMListType(){
		return PIM.EVENT_LIST;
	}
	
	/**
	 * create a sync data
	 * @return
	 */
	protected AbsSyncData newSyncData(){
		return new CalendarSyncData();
	}
		
	/**
	 * get the PIMItem UID
	 * @param item
	 * @return
	 */
	protected String getPIMItemUID(PIMItem item){
		return AbsSyncData.getStringField((PIMItem)item, Event.UID);
	}
	
	/**
	 * add PIMItem to bb system
	 * @param itemList
	 * @param d
	 * @throws Exception
	 */
	protected void addPIMItemImpl(PIMList itemList,AbsSyncData d)throws Exception{
		
		Event e = ((EventList)itemList).createEvent();
		d.exportData(e);
		
		e.commit();				
		d.setBBID(getPIMItemUID(e));
	}

	
	/**
	 * delete the PIM item from BB system
	 * @param item
	 * @throws Exception
	 */
	protected void deletePIMItemImpl(PIMItem item)throws Exception{
		((EventList)item.getPIMList()).removeEvent((Event)item);
	}
	
	

	
}
