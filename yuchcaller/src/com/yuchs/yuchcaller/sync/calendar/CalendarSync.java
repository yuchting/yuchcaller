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

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMItem;

import net.rim.blackberry.api.pdap.BlackBerryPIMList;
import net.rim.blackberry.api.pdap.PIMListListener;

import com.yuchs.yuchcaller.sync.AbsSync;
import com.yuchs.yuchcaller.sync.AbsSyncData;
import com.yuchs.yuchcaller.sync.SyncMain;

public class CalendarSync extends AbsSync implements PIMListListener{
				
	public CalendarSync(SyncMain _syncMain){
		super(_syncMain);
				
		try{

			// add the calendar event listener
			BlackBerryPIMList tEventList = (BlackBerryPIMList)PIM.getInstance().openPIMList(PIM.EVENT_LIST,PIM.READ_WRITE);
			try{
				tEventList.addListener(this);
			}finally{
				tEventList.close();
				tEventList = null;
			}
		}catch(Exception e){
			mSyncMain.m_mainApp.SetErrorString("CSI", e);
		}

	}
	
	/**
	 * return the sync main type:
	 * 
	 * SyncMain.SYNC_CALENDAR
	 * SyncMain.SYNC_CONTACT
	 * SyncMain.SYNC_TASK
	 * 
	 * @return
	 */
	protected int getSyncMainType(){
		return SyncMain.SYNC_CALENDAR;
	}
	
	/**
	 * create a sync data
	 * @return
	 */
	protected AbsSyncData newSyncData(){
		return new CalendarSyncData();
	}
	
	/**
	 * read the calendar information from bb calendar
	 */
	protected void readBBSyncData(){
				
		try{
			
			EventList t_events = (EventList)PIM.getInstance().openPIMList(PIM.EVENT_LIST,PIM.READ_ONLY);
			try{

				Enumeration t_allEvents = t_events.items();
				
				Vector t_eventList = new Vector();
			    if(t_allEvents != null){
				    while(t_allEvents.hasMoreElements()) {			    	
				    	t_eventList.addElement(t_allEvents.nextElement());
				    }
			    }
			    			    
			    mSyncDataList.removeAllElements();
			    for(int i = 0;i < t_eventList.size();i++){
			    	
			    	Event event = (Event)t_eventList.elementAt(i);
			    	
			    	CalendarSyncData syncData = new CalendarSyncData();
			    	syncData.importData(event,t_events);
			    					    	
			    	mSyncDataList.addElement(syncData);
			    }			    
			    
			}finally{
				t_events.close();
				t_events = null;
			}
		    
		}catch(Exception e){
			reportError("Can not read calendar event list.",e);
		}
	}
	
	/**
	 * add a PIMItem to bb by the abs sync data (CalendarSyncData/ContactSyncData/TaskSyncData)
	 * @param _data
	 */
	protected void addPIMItem(Vector _addList){
		
		if(_addList == null || _addList.size() <= 0){
			return;
		}
		
		try{
			
			EventList tEvents = (EventList)PIM.getInstance().openPIMList(PIM.EVENT_LIST,PIM.READ_WRITE);
			try{
				
				Enumeration t_allEvents = tEvents.items();
				Vector t_eventList =  new Vector();
				
			    if(t_allEvents != null){
				    while(t_allEvents.hasMoreElements()) {			    	
				    	t_eventList.addElement(t_allEvents.nextElement());
				    }
			    }
			    
			    // add the event to bb system
				//
				for(int i = 0;i < _addList.size();i++){
					CalendarSyncData d = (CalendarSyncData)_addList.elementAt(i);
					
					Event e = tEvents.createEvent();
					d.exportData(e,tEvents);
					
					e.commit();
					d.setBBID(AbsSyncData.getStringField(e, Event.UID));
					
					// added to main list
					mSyncDataList.addElement(d);
				}
			    
			}finally{
				tEvents.close();
				tEvents = null;
			}
			
		}catch(Exception e){
			mSyncMain.m_mainApp.SetErrorString("API", e);
		}
		
	}
	
	/**
	 * remove PIMItem from a del list
	 * @param _delList
	 */
	protected void deletePIMItem(Vector _delList){
		
		if(_delList == null || _delList.size() <= 0){
			return;
		}
		
		try{
			EventList tEvents = (EventList)PIM.getInstance().openPIMList(PIM.EVENT_LIST,PIM.READ_WRITE);
			try{
				
				Enumeration t_allEvents = tEvents.items();
				Vector t_eventList =  new Vector();
				
			    if(t_allEvents != null){
				    while(t_allEvents.hasMoreElements()) {			    	
				    	t_eventList.addElement(t_allEvents.nextElement());
				    }
			    }
			    
				for(int i = 0;i < _delList.size();i++){
					String bid = (String)_delList.elementAt(i);
					
					CalendarSyncData d = (CalendarSyncData)getSyncData(bid);
					if(d != null){
						
						if(d.getLastMod() != -1){
							
							// this event is NOT been deleted by client
							//
							for(int idx = 0;idx < t_eventList.size();idx++){
								
								Event e = (Event)t_eventList.elementAt(idx);
																
								if(d.getBBID().equals(AbsSyncData.getStringField(e, Event.UID))){
									
									tEvents.removeEvent(e);
									t_eventList.removeElement(e);
									
									break;
								}
							}
						}
						
						removeSyncData(d.getBBID());
					}
				}
			    
			}finally{
				tEvents.close();
				tEvents = null;
			}
			
		}catch(Exception e){
			mSyncMain.m_mainApp.SetErrorString("DPI", e);
		}
		
	}
	
	/**
	 * update the PIMItem from a update list(AbsSyncData list)
	 * @param _updateList
	 */
	protected void updatePIMItem(Vector _updateList){
		
		if(_updateList == null || _updateList.size() <= 0){
			return;
		}
		
		try{
			EventList tEvents = (EventList)PIM.getInstance().openPIMList(PIM.EVENT_LIST,PIM.READ_WRITE);
			try{
				
				Enumeration t_allEvents = tEvents.items();
				Vector t_eventList =  new Vector();
				
			    if(t_allEvents != null){
				    while(t_allEvents.hasMoreElements()) {			    	
				    	t_eventList.addElement(t_allEvents.nextElement());
				    }
			    }
			    
			    for(int i = 0;i < _updateList.size();i++){
					CalendarSyncData update = (CalendarSyncData)_updateList.elementAt(i);
					
					// remove sync data first
					removeSyncData(update.getBBID());
					
					// add data  
					mSyncDataList.addElement(update);
					
					for(int idx = 0;idx < t_eventList.size();idx++){
						Event e = (Event)t_eventList.elementAt(idx);
						
						if(update.getBBID().equals(AbsSyncData.getStringField(e, Event.UID))){
							update.exportData(e,tEvents);
							e.commit();
						}
					}
				}
			    
			}finally{
				tEvents.close();
				tEvents = null;
			}
			
		}catch(Exception e){
			mSyncMain.m_mainApp.SetErrorString("UPI", e);
		}
	}

	//{{ PIMListListener
	public void itemAdded(PIMItem item) {
		
		try{
			EventList tEventList = (EventList)PIM.getInstance().openPIMList(PIM.EVENT_LIST,PIM.READ_ONLY);
			try{
				CalendarSyncData syncData = new CalendarSyncData();
		    	syncData.importData((Event)item,tEventList);
		    				    	
		    	mSyncDataList.addElement(syncData);
			}finally{
				tEventList.close();
				tEventList = null;
			}
		}catch(Exception e){
			mSyncMain.m_mainApp.SetErrorString("CSIA", e);
		}
		
	}

	public void itemRemoved(PIMItem item) {
		
		if(item != null){
			
			String bid = CalendarSyncData.getStringField((Event)item, Event.UID);
			
			synchronized(mSyncDataList){
				for(int i = 0;i < mSyncDataList.size();i++){
					CalendarSyncData d = (CalendarSyncData)mSyncDataList.elementAt(i);
					if(d.getBBID().equals(bid)){
						
						if(d.getGID() == null){
							// remove directly
							mSyncDataList.removeElementAt(i);
						}else{
							// mark delete to wait sync to delete server's event
							d.setLastMod(-1);
						}
						break;
					}
				}
			}
		}
	}

	public void itemUpdated(PIMItem oldItem, PIMItem newItem) {
		
		if(oldItem != null && newItem != null){
			
			try{
				String bid = CalendarSyncData.getStringField((Event)oldItem, Event.UID);
				
				EventList tEventList = (EventList)PIM.getInstance().openPIMList(PIM.EVENT_LIST,PIM.READ_ONLY);
				try{
					synchronized(mSyncDataList){
						for(int i = 0;i < mSyncDataList.size();i++){
							CalendarSyncData d = (CalendarSyncData)mSyncDataList.elementAt(i);
							if(d.getBBID().equals(bid)){
								
								d.importData((Event)newItem, tEventList);
								d.setLastMod(System.currentTimeMillis());
																
								readWriteSyncFile(false);
								
								break;
							}
						}
					}
				}finally{
					tEventList.close();
					tEventList = null;
				}
			}catch(Exception e){
				mSyncMain.m_mainApp.SetErrorString("CSIA", e);
			}
		}
	}
	//}}
}
