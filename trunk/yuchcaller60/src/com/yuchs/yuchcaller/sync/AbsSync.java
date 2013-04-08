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
package com.yuchs.yuchcaller.sync;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;

import local.yuchcallerlocalResource;

import net.rim.blackberry.api.pdap.BlackBerryPIMList;
import net.rim.blackberry.api.pdap.PIMListListener;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.util.Arrays;

import com.yuchs.yuchcaller.YuchCaller;
import com.yuchs.yuchcaller.YuchCallerProp;
import com.yuchs.yuchcaller.sendReceive;

public abstract class AbsSync implements PIMListListener{
	
	public static final String[] fsm_syncTypeString =
	{
		"contact",
		"calendar",
		"task",
	};
	
	/**
	 * the version fo sync file
	 */
	private static final int SyncFileVersion = 0;
	
	/**
	 * sync data list
	 */
	protected Vector mSyncDataList = new Vector();
		
	//! sync main class
	final protected SyncMain	mSyncMain;
	
	/**
	 * whether is modified by programmely PIMListListener callback 
	 */
	private boolean mModifiedProgrammely = false;
	
	/**
	 * syncing state 
	 */
	private boolean mSyncing		= false;
	
	/**
	 * the sync modified number array
	 */
	private int[] mSyncModNumber = {0,0,0,0};
	
	/**
	 * add data when sync
	 */
	private Vector mAddDataWhenSync = new Vector();
	
	/**
	 * remove data when sync
	 */
	private Vector mDelDataWhenSync = new Vector();
	
	/**
	 * modified data when sync
	 */
	private Vector mModDataWhenSync = new Vector();
	
	/**
	 * sync modified number name
	 */
	private static String[] SyncModNumberName = null;
	
	/**
	 * sync requesting prompt label string
	 */
	private static String	SyncRequesting = null;
	
	/**
	 * sync successfully
	 */
	private static String SyncSuccessfully = null;
	
	/**
	 * sync successfully without any changed
	 */
	private static String SyncSuccessfullyWithoutChanged = null;
			
	/**
	 * constructor of this AbsSync class
	 * @param _syncMain
	 */
	public AbsSync(SyncMain _syncMain){
		mSyncMain = _syncMain;
		
		if(SyncModNumberName == null){
			
			SyncModNumberName = new String[4];
			
			SyncModNumberName[0] = mSyncMain.m_mainApp.m_local.getString(yuchcallerlocalResource.SYNC_MOD_ADDED);
			SyncModNumberName[1] = mSyncMain.m_mainApp.m_local.getString(yuchcallerlocalResource.SYNC_MOD_DELETED);
			SyncModNumberName[2] = mSyncMain.m_mainApp.m_local.getString(yuchcallerlocalResource.SYNC_MOD_UPDATED);
			SyncModNumberName[3] = mSyncMain.m_mainApp.m_local.getString(yuchcallerlocalResource.SYNC_MOD_UPLOADED);
			
			SyncRequesting					= mSyncMain.m_mainApp.m_local.getString(yuchcallerlocalResource.SYNC_REQUESTING);
			SyncSuccessfully				= mSyncMain.m_mainApp.m_local.getString(yuchcallerlocalResource.SYNC_SUCC);
			SyncSuccessfullyWithoutChanged	= mSyncMain.m_mainApp.m_local.getString(yuchcallerlocalResource.SYNC_SUCC_WITHOUT_ANY_CHANGED);
		}		 
		
		try{

			// add the PIMList listener
			BlackBerryPIMList tEventList = (BlackBerryPIMList)PIM.getInstance().openPIMList(getSyncPIMListType(),PIM.READ_WRITE);
			
			try{
				tEventList.addListener(this);
			}finally{
				tEventList.close();
				tEventList = null;
			}
		}catch(Exception e){
			mSyncMain.m_mainApp.SetErrorString("CSC", e);
		}
		
		// read the calendar/contact/task information in bb database
		readBBSyncData();
		
		// read sync file (gID for bID)
		readWriteSyncFile(true);
	}
	
	/**
	 * find the sync data in list
	 * @param _bbID		bid of event
	 * @return
	 */
	protected AbsSyncData getSyncData(String _bbid){
		
		for(int i = 0;i < mSyncDataList.size();i++){
			AbsSyncData d = (AbsSyncData)mSyncDataList.elementAt(i); 
			if(d.getBBID().equals(_bbid)){
				return d;
			}
		}
		
		return null;
	}
	
	/**
	 * remove the sync data in main list
	 * @param _bbid
	 */
	protected void removeSyncData(String _bbid){
		
		for(int i = 0;i < mSyncDataList.size();i++){
			AbsSyncData d = (AbsSyncData)mSyncDataList.elementAt(i); 
			if(d.getBBID().equals(_bbid)){
				mSyncDataList.removeElementAt(i);
				break;
			}
		}
	}
	
	/**
	 * read the calendar information from bb calendar
	 */
	public void readBBSyncData(){
				
		try{
			
			reportInfo(mSyncMain.m_mainApp.m_local.getString(yuchcallerlocalResource.SYNC_READING_BB_DATA));
			
			PIMList tPIMList = (PIMList)PIM.getInstance().openPIMList(getSyncPIMListType(),PIM.READ_ONLY);
			try{

				Enumeration t_allItems = tPIMList.items();
				
				Vector t_eventList = new Vector();
			    if(t_allItems != null){
				    while(t_allItems.hasMoreElements()) {			    	
				    	t_eventList.addElement(t_allItems.nextElement());
				    }
			    }
			    
			    synchronized(mSyncDataList){
			    	mSyncDataList.removeAllElements();
				    for(int i = 0;i < t_eventList.size();i++){
				    	
				    	PIMItem item = (PIMItem)t_eventList.elementAt(i);
				    	
				    	AbsSyncData syncData = newSyncData();
				    	syncData.importData(item);
				    	
//				    	ContactSyncData csd = (ContactSyncData)syncData;
//				    	if("ZHOU".equals(csd.getData().names[ContactData.NAME_GIVEN])){
//				    					    		
//				    		csd.getData().email = new String[ContactData.EMAIL_SIZE];
//				    		
//				    		for(int a = 0; a < csd.getData().email.length;a++){
//				    			csd.getData().email[a] = Integer.toString(a) + "@test.com";
//				    		}
//				    		
//				    		csd.exportData(item);
//				    		
//				    		item.commit();
//				    		
//				    		//syncData.importData(item);
//				    	}
				    					    					    	
				    	mSyncDataList.addElement(syncData);
				    }	
			    }
			   		    
			    
			}finally{
				tPIMList.close();
				tPIMList = null;
			}

		}catch(Exception e){
			reportError("Can not read " + fsm_syncTypeString[getReportLabel()] + " PIMList:",e);
		}
		
		reportInfo(null);
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
	protected abstract int getSyncPIMListType();
	
	/**
	 * create a sync data
	 * @return
	 */
	protected abstract AbsSyncData newSyncData();
	
	/**
	 * get the PIMItem UID
	 * @param item
	 * @return
	 */
	protected abstract String getPIMItemUID(PIMItem item);
	
	/**
	 * add PIMItem to bb system
	 * @param item
	 * @param d
	 * @throws Exception
	 */
	protected abstract void addPIMItemImpl(PIMList itemList,AbsSyncData d)throws Exception;
	
	/**
	 * delete the PIM item from BB system
	 * @param item
	 * @throws Exception
	 */
	protected abstract void deletePIMItemImpl(PIMItem item)throws Exception;
		
	/**
	 * add a PIMItem to bb by the abs sync data (CalendarSyncData/ContactSyncData/TaskSyncData)
	 * @param _data
	 */
	protected void addPIMItem(Vector _addList){
		
		if(_addList == null || _addList.size() <= 0){
			return;
		}
		
		try{
						
			PIMList tPIMList = (PIMList)PIM.getInstance().openPIMList(getSyncPIMListType(),PIM.READ_WRITE);
			try{
				
				Enumeration t_allEvents = tPIMList.items();
				Vector t_eventList =  new Vector();
				
			    if(t_allEvents != null){
				    while(t_allEvents.hasMoreElements()) {			    	
				    	t_eventList.addElement(t_allEvents.nextElement());
				    }
			    }
			    
			    // add the event to bb system
				//
				for(int i = 0;i < _addList.size();i++){
					AbsSyncData d = (AbsSyncData)_addList.elementAt(i);
										
					mModifiedProgrammely = true;
					try{
						addPIMItemImpl(tPIMList, d);
					}finally{
						mModifiedProgrammely = false;
					}
					
					// added to main list
					mSyncDataList.addElement(d);
				}
			    
			}finally{
				tPIMList.close();
				tPIMList = null;
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
			PIMList tPIMList = (PIMList)PIM.getInstance().openPIMList(getSyncPIMListType(),PIM.READ_WRITE);
			try{
				
				Enumeration t_allEvents = tPIMList.items();
				Vector t_eventList =  new Vector();
				
			    if(t_allEvents != null){
				    while(t_allEvents.hasMoreElements()) {			    	
				    	t_eventList.addElement(t_allEvents.nextElement());
				    }
			    }
			    
				for(int i = 0;i < _delList.size();i++){
					String bid = (String)_delList.elementAt(i);
					
					AbsSyncData d = (AbsSyncData)getSyncData(bid);
					if(d != null){
						
						if(d.getLastMod() != -1){
							
							// this event is NOT been deleted by client
							//
							for(int idx = 0;idx < t_eventList.size();idx++){
								
								PIMItem item = (PIMItem)t_eventList.elementAt(idx);
																
								if(d.getBBID().equals(getPIMItemUID(item))){
									
									mModifiedProgrammely = true;
									try{
										deletePIMItemImpl(item);
									}finally{
										mModifiedProgrammely = false;
									}
									
									t_eventList.removeElementAt(idx);									
									break;
								}
							}
						}
						
						removeSyncData(d.getBBID());
					}
				}
			    
			}finally{
				tPIMList.close();
				tPIMList = null;
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
			PIMList tPIMList = (PIMList)PIM.getInstance().openPIMList(getSyncPIMListType(),PIM.READ_WRITE);
			try{
				
				Enumeration t_allEvents = tPIMList.items();
				Vector t_eventList =  new Vector();
				
			    if(t_allEvents != null){
				    while(t_allEvents.hasMoreElements()) {			    	
				    	t_eventList.addElement(t_allEvents.nextElement());
				    }
			    }
			    
			    for(int i = 0;i < _updateList.size();i++){
			    	AbsSyncData update = (AbsSyncData)_updateList.elementAt(i);
					
					// remove sync data first
					removeSyncData(update.getBBID());
					
					// add data again
					mSyncDataList.addElement(update);
					
					for(int idx = 0;idx < t_eventList.size();idx++){
						PIMItem item = (PIMItem)t_eventList.elementAt(idx);
						
						if(update.getBBID().equals(getPIMItemUID(item))){
							update.exportData(item);
							
							mModifiedProgrammely = true;
							try{
								item.commit();
							}finally{
								mModifiedProgrammely = false;
							}
							
						}
					}
				}
			    
			}finally{
				tPIMList.close();
				tPIMList = null;
			}
			
		}catch(Exception e){
			mSyncMain.m_mainApp.SetErrorString("UPI", e);
		}
	}

	
	
	/**
	 * start sync, main proccess function
	 * 
	 * @return change sync entry number
	 */
	public int startSync(){
		
		mSyncing = true;
		
		// just call sync request
		int changeNumber = syncRequest();
		
		mSyncing = false;
		
		
		// process the buffer data when syncing
		//
		
		// get the status whether write sync file 
		boolean writeSyncFile = !mDelDataWhenSync.isEmpty() || !mModDataWhenSync.isEmpty();

		
		// add data
		synchronized(mAddDataWhenSync){
			for(int i = 0;i < mAddDataWhenSync.size();i++){
				AbsSyncData syncData = (AbsSyncData)mAddDataWhenSync.elementAt(i);
				mSyncDataList.addElement(syncData);
			}
			
			mAddDataWhenSync.removeAllElements();
		}		
		
		// delete
		synchronized(mDelDataWhenSync){
			for(int i = 0;i < mDelDataWhenSync.size();i++){
				String bid = (String)mDelDataWhenSync.elementAt(i);
				itemRemovedImpl(bid,false);
			}
						
			mDelDataWhenSync.removeAllElements();
		}
		
		
		// modified
		synchronized(mModDataWhenSync){
			for(int i = 0;i < mModDataWhenSync.size();i++){
				ModSyncData mod = (ModSyncData)mModDataWhenSync.elementAt(i);
				
				synchronized(mSyncDataList){
					for(int idx = 0;idx < mSyncDataList.size();i++){
						AbsSyncData d = (AbsSyncData)mSyncDataList.elementAt(i);
						if(d.getBBID().equals(mod.oldBid)){
							
							mSyncDataList.removeElementAt(i);
							mSyncDataList.addElement(mod.newData);
							
							break;
						}
					}
				}
			}
			
			mModDataWhenSync.removeAllElements();
		}
	
		if(writeSyncFile){
			readWriteSyncFile(false);
		}
		
		return changeNumber;
	}
	
	/**
	 * write the account information to a OutputStream
	 * @param os
	 * @param type
	 * @param md5		sync data md5
	 * @param diffType 	of sync
	 * @throws Exception
	 */
	private void writeAccountInfo(OutputStream os,String md5,long minSyncTime,int diffType)throws Exception{
		
		YuchCallerProp tProp = mSyncMain.m_mainApp.getProperties();
		
		// write the version
		sendReceive.WriteShort(os,(short)0);
		
		// write the type (calendar/contact/task)
		sendReceive.WriteString(os, fsm_syncTypeString[getReportLabel()]);
		
		// send the min time for sync
		sendReceive.WriteLong(os, minSyncTime);
		
		sendReceive.WriteString(os,tProp.getYuchRefreshToken());
		sendReceive.WriteString(os,tProp.getYuchAccessToken());
		
		sendReceive.WriteString(os,tProp.getYuchAccount());
		
		sendReceive.WriteString(os,TimeZone.getDefault().getID());
		sendReceive.WriteString(os,md5);
		
		// write the diff type
		os.write(diffType);
	}
	
	/**
	 * sync main URL
	 */
	private static String SyncMainURL = DeviceInfo.isSimulator() ? "http://127.0.0.1" : "http://sync.yuchs.com";
	
	/**
	 * sync request
	 * 
	 * work following
	 * 
	 * 		client			server
	 * 		|					|
	 * 		Mod md5------------>md5 compare
	 * 		|					|
	 * 		succ(no change)<----nochange or diff 
	 * 		|					|
	 * 		|					|
	 * 		|					|
	 * 		diff list---------->diff list process ( diff type 0)
	 * 		|					|
	 * 		process<-----------add/updat/upload/delete/needlist
	 * 		|					|
	 * 		needList---------->updated google calendar  ( diff type 1)
	 * 		|					|
	 * 		succ<--------------mod time list
	 * 
	 * @return changed number
	 */
	protected int syncRequest(){

		Arrays.fill(mSyncModNumber, 0);
		
		try{

			reportInfo(SyncRequesting);
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try{
				
				long tSyncMinTime = System.currentTimeMillis() - mSyncMain.m_mainApp.getProperties().getSyncFormerDays() * 24 * 3600000L;
				
				reportInfo("repare MD5");
				
				String md5 = prepareSyncMD5(tSyncMinTime);
				
				writeAccountInfo(os,md5,tSyncMinTime,0);
				
				String url = SyncMainURL + YuchCaller.getHTTPAppendString();
				
				reportInfo("requesting...");
				
				String tResultStr = new String(SyncMain.requestPOSTHTTP(url,os.toByteArray(),true),"UTF-8");
										
				if(tResultStr.equals("succ")){
				
					// successfully!
					reportInfo(SyncSuccessfullyWithoutChanged);
					
				}else if(tResultStr.equals("diff")){
				
					reportInfo("requesting diff");
					
					// write the diff sign
					InputStream diffIn = new ByteArrayInputStream(SyncMain.requestPOSTHTTP(url, outputDiffList(md5,tSyncMinTime) ,true));
					try{
						
						reportInfo("requesting need");
						
						Vector tNeedList = processDiffList(diffIn);
						if(tNeedList != null){
							// send the need list to update server's event
							//
							sendNeedList(tNeedList,url,md5,tSyncMinTime);
						}
						
						reportSuccInfo();
						
					}finally{
						diffIn.close();
						diffIn = null;
					}
				}					
				
				reportError(null);
				
			}finally{
				os.close();
				os = null;
			}
			
		}catch(Exception e){
			// sync request failed
			reportError("Sync Error",e);
			reportInfo(null);
		}
		
		int changedNum = 0;
		for(int i = 0;i < mSyncModNumber.length;i++){
			changedNum += mSyncModNumber[i];
		}
		
		return changedNum;
	}
	
	/**
	 * report successfully information
	 */
	private void reportSuccInfo(){
		StringBuffer info = new StringBuffer(SyncSuccessfully);
		
		for(int i = 0;i < mSyncModNumber.length;i++){
			if(mSyncModNumber[i] > 0){
				info.append(SyncModNumberName[i]).append(mSyncModNumber[i]).append(" ");
			}
		}
		
		reportInfo(info.toString());
	}
	
	/**
	 * get the different list 
	 */
	private byte[] outputDiffList(String md5,long minSyncTime)throws Exception{

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try{
			
			writeAccountInfo(os,md5,minSyncTime,1);
			
			sendReceive.WriteInt(os, mSyncDataList.size());
			
			for(int idx = 0;idx < mSyncDataList.size();idx++){

				AbsSyncData syncData = (AbsSyncData)mSyncDataList.elementAt(idx);
				syncData.output(os,syncData.getGID() == null || syncData.getGID().length()  == 0);
			}			
			
			return os.toByteArray();
		}finally{
			os.close();
			os = null;
		}
	}

	/**
	 * read or write sync data file
	 * @param _readOrWrite
	 */
	protected void readWriteSyncFile(boolean _readOrWrite){
		
		String tFilename = YuchCallerProp.fsm_rootPath_back + "YuchCaller/" + fsm_syncTypeString[getReportLabel()] + ".data"; 
		
		mSyncMain.m_mainApp.getProperties().preWriteReadIni(_readOrWrite, tFilename);
		
		try{
			FileConnection fc = (FileConnection) Connector.open(tFilename,Connector.READ_WRITE);
			try{
				if(_readOrWrite){
					
					if(!fc.exists()){
						return;
					}
					
					InputStream in = fc.openInputStream();
					try{
						int tVersion = in.read();
						
						int num = sendReceive.ReadInt(in);
						for(int i= 0;i < num;i++){
							String	bid = sendReceive.ReadString(in);
							String	gid = sendReceive.ReadString(in);
							long	mod = sendReceive.ReadLong(in);
													
							AbsSyncData syncData = getSyncData(bid);
							if(syncData != null){
								syncData.setGID(gid);
								syncData.setLastMod(mod);
							}
						}
					}finally{
						in.close();
						in = null;
					}
					
					
				}else{
					
					if(!fc.exists()){
						fc.create();					
					}
										
					OutputStream os = fc.openOutputStream();
					try{
						os.write(SyncFileVersion);
						sendReceive.WriteInt(os, mSyncDataList.size());
						
						for(int idx = 0;idx < mSyncDataList.size();idx++){

							AbsSyncData syncData = (AbsSyncData)mSyncDataList.elementAt(idx);
							
							sendReceive.WriteString(os, syncData.getBBID());
							sendReceive.WriteString(os, syncData.getGID());
							sendReceive.WriteLong(os, syncData.getLastMod());
						}
						
					}finally{
						os.close();
						os = null;
					}				
				}			
				
			}finally{
				fc.close();
				fc = null;
			}
		}catch(Exception e){
			reportError("Can not read " + tFilename,e);
		}
		
		mSyncMain.m_mainApp.getProperties().postWriteReadIni(tFilename);
	}
		
	/**
	 * prepare the calendar/contact/task and calculate the MD5
	 * @return
	 */
	protected String prepareSyncMD5(long _minTime){
				
		Vector tRemoveList = null;
		Vector tSortList = new Vector();
		
		for(int idx = mSyncDataList.size() - 1;idx >= 0;idx--){

			AbsSyncData syncData = (AbsSyncData)mSyncDataList.elementAt(idx);
			
			if(!syncData.needCalculateMD5(_minTime,idx)){

				if(tRemoveList == null){
					tRemoveList = new Vector();
				}
				
				tRemoveList.addElement(syncData);
	    		continue;
	    	}
			
			// sort insert the calendar by the start time
			for(int i = 0;i < tSortList.size();i++){
				AbsSyncData d = (AbsSyncData)tSortList.elementAt(i);
				if(syncData.getLastMod() > d.getLastMod()){
					tSortList.insertElementAt(syncData, i);
					
					syncData = null;
					break;
				}
			}
			
			if(syncData != null){
				tSortList.addElement(syncData);				
			}
		}
				
		// remove the former time event
		if(tRemoveList != null){
			for(int i = 0;i < tRemoveList.size();i++){
				AbsSyncData syncData = (AbsSyncData)tRemoveList.elementAt(i);
				removeSyncData(syncData.getBBID());
			}			
		}
		
		StringBuffer sb = new StringBuffer();
		
		// calculate the md5
		for(int i = 0;i < tSortList.size();i++){
			AbsSyncData d = (AbsSyncData)tSortList.elementAt(i);
			sb.append(d.getLastMod());
		}
		
		return SyncMain.md5(sb.toString());
	}
	
	/**
	 * process the different list
	 * @param in
	 * @return need list
	 */
	protected Vector processDiffList(InputStream in)throws Exception{
		
		Vector tAddList		= null;
		Vector tDelList	 	= null;
		Vector tUploadList	 = null;
		Vector tUpdateList	= null;
		Vector tNeedList	= null;
		
		// get the add list
		int num = sendReceive.ReadInt(in);
		
		if(getReportLabel() == PIM.CONTACT_LIST){
			mSyncMain.m_mainApp.SetErrorString("processDiffList 0 " + num);
		}
		
		for(int i = 0;i < num;i++){
			AbsSyncData e = newSyncData();
			e.input(in);
			
			if(tAddList == null){
				tAddList = new Vector();
			}
			tAddList.addElement(e);
			
			mSyncModNumber[0] = num;
		}

		// get the delete list
		num = sendReceive.ReadInt(in);
		
		if(getReportLabel() == PIM.CONTACT_LIST){
			mSyncMain.m_mainApp.SetErrorString("processDiffList 1 " + num);
		}
		
		for(int i = 0;i < num;i++){
			
			if(tDelList == null){
				tDelList = new Vector();
			}
			
			// add the BID to delete
			tDelList.addElement(sendReceive.ReadString(in));
			
			mSyncModNumber[1] = num;
		}
		
		// get the update list
		num = sendReceive.ReadInt(in);
		
		if(getReportLabel() == PIM.CONTACT_LIST){
			mSyncMain.m_mainApp.SetErrorString("processDiffList 2 " + num);
		}
		
		for(int i = 0;i < num;i++){
			AbsSyncData e = newSyncData();
			e.input(in);
			
			if(tUpdateList == null){
				tUpdateList = new Vector();
			}
			tUpdateList.addElement(e);
			
			mSyncModNumber[2] = num;
		}
		
		// get the upload list
		num = sendReceive.ReadInt(in);
		
		if(getReportLabel() == PIM.CONTACT_LIST){
			mSyncMain.m_mainApp.SetErrorString("processDiffList 3 " + num);
		}
		
		for(int i = 0;i < num;i++){
			AbsSyncData e = newSyncData();
			
			e.setBBID(sendReceive.ReadString(in));
			e.setGID(sendReceive.ReadString(in));
			e.setLastMod(sendReceive.ReadLong(in));
			
			if(tUploadList == null){
				tUploadList = new Vector();
			}
			tUploadList.addElement(e);
			
			mSyncModNumber[3] = num;
		}
		
		// get the need list
		num = sendReceive.ReadInt(in);
		
		if(getReportLabel() == PIM.CONTACT_LIST){
			mSyncMain.m_mainApp.SetErrorString("processDiffList 3 " + num);
		}
		
		for(int i = 0;i < num;i++){
			if(tNeedList == null){
				tNeedList = new Vector();
			}
			
			// add the BID to update
			tNeedList.addElement(sendReceive.ReadString(in));
		}
		
		// add PIMItem to BB system
		addPIMItem(tAddList);
		
		// delete PIMItem from BB system
		deletePIMItem(tDelList);
		
		// update PIMItem in BB system
		updatePIMItem(tUpdateList);
		
		if(tUploadList != null){
			// upload list (refresh current bb calendar's GID)
			//
			for(int i = 0;i < tUploadList.size();i++){
				AbsSyncData uploaded = (AbsSyncData)tUploadList.elementAt(i);
				
				AbsSyncData d = (AbsSyncData)getSyncData(uploaded.getBBID());
				if(d != null){
					d.setGID(uploaded.getGID());
					d.setLastMod(uploaded.getLastMod());
				}
			}
		}
		
		if((tAddList != null || tDelList != null || tUpdateList != null || tUploadList != null) && tNeedList == null){
			readWriteSyncFile(false);
		}
		
		return tNeedList;
	}
	
	/**
	 * send need list and process result
	 * @param needList
	 * @param _url
	 * @throws Exception
	 */
	protected void sendNeedList(Vector needList,String _url,String _md5,long minSyncTime)throws Exception{
		
		if(getReportLabel() == PIM.CONTACT_LIST){
			mSyncMain.m_mainApp.SetErrorString("sendNeedList 0 ");
		}
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try{
			
			writeAccountInfo(os,_md5,minSyncTime,2);
						
			// write the sync data
			sendReceive.WriteInt(os,needList.size());
			
			for(int i = 0;i < needList.size();i++){
				AbsSyncData d = (AbsSyncData)getSyncData(needList.elementAt(i).toString());
				d.output(os, true);				
			}
			
			if(getReportLabel() == PIM.CONTACT_LIST){
				mSyncMain.m_mainApp.SetErrorString("sendNeedList 1 ");
			}
			
			
			InputStream in = new ByteArrayInputStream(SyncMain.requestPOSTHTTP(_url, os.toByteArray(), true));
			try{
				
				int num = sendReceive.ReadInt(in);
				for(int i = 0;i < num;i++){
					String bid		= sendReceive.ReadString(in);
					long modTime	= sendReceive.ReadLong(in);
					
					AbsSyncData d = (AbsSyncData)getSyncData(bid);
					d.setLastMod(modTime);
				}
				
				readWriteSyncFile(false);
				
			}finally{
				in.close();
				in = null;
			}
			
			if(getReportLabel() == PIM.CONTACT_LIST){
				mSyncMain.m_mainApp.SetErrorString("sendNeedList 2 ");
			}
			
		}finally{
			os.close();
			os = null;
		}
		
	}
	
	//{{ PIMListListener
	public void itemAdded(PIMItem item) {
		
		if(mModifiedProgrammely){
			mModifiedProgrammely = false;
			return;
		}
		
		
		try{
			
			AbsSyncData syncData = newSyncData();
	    	syncData.importData(item);
	    				    	
	    	if(mSyncing){
	    		// buffered it when syncing
				mAddDataWhenSync.addElement(syncData);
				return;
			}
				    	
	    	mSyncDataList.addElement(syncData);
		
		}catch(Exception e){
			mSyncMain.m_mainApp.SetErrorString("CSIA", e);
		}
	}

	public void itemRemoved(PIMItem item) {
		
		if(mModifiedProgrammely){
			mModifiedProgrammely = false;
			return;
		}
				
		if(item != null){
			
			String bid = getPIMItemUID(item);
			
			if(mSyncing){
				mDelDataWhenSync.addElement(bid);
				return;
			}
						
			itemRemovedImpl(bid,true);
		}
	}
	
	/**
	 * item removed impl for sync data list
	 * @param bid
	 */
	public void itemRemovedImpl(String bid,boolean writeFile){
		
		synchronized(mSyncDataList){
			for(int i = 0;i < mSyncDataList.size();i++){
				AbsSyncData d = (AbsSyncData)mSyncDataList.elementAt(i);
				if(d.getBBID().equals(bid)){
					
					if(d.getGID() == null){
						// remove directly
						mSyncDataList.removeElementAt(i);
					}else{
						// mark delete to wait sync to delete server's event
						d.setLastMod(-1);
					
						if(writeFile){
							// write the sync file to avoid lost data when BB system is down
							readWriteSyncFile(false);
						}
					}
					
					break;
				}
			}
		}
	}

	public void itemUpdated(PIMItem oldItem, PIMItem newItem) {
		
		if(mModifiedProgrammely){
			mModifiedProgrammely = false;
			return;
		}	
		
		if(oldItem != null && newItem != null){
			
			try{
				String bid = getPIMItemUID(oldItem);
				
				if(mSyncing){
					ModSyncData d = new ModSyncData();
					d.oldBid	= bid;
					d.newData	= newSyncData();
					
					d.newData.importData(newItem);
					d.newData.setLastMod(System.currentTimeMillis());
					
					mModDataWhenSync.addElement(d);
					return;
				}
				
				synchronized(mSyncDataList){
					for(int i = 0;i < mSyncDataList.size();i++){
						AbsSyncData d = (AbsSyncData)mSyncDataList.elementAt(i);
						if(d.getBBID().equals(bid)){
							
							d.importData(newItem);
							d.setLastMod(System.currentTimeMillis());
															
							readWriteSyncFile(false);
							
							break;
						}
					}
				}
				
			}catch(Exception e){
				mSyncMain.m_mainApp.SetErrorString("CSIA", e);
			}
		}
	} 
	//}}
		
	/**
	 * get the 0-base index of report by SyncPIMListType
	 * @return
	 */
	private final int getReportLabel(){
		switch(getSyncPIMListType()){
		case PIM.CONTACT_LIST:
			return 0;
		case PIM.EVENT_LIST:
			return 1;		
		default:
			return 2;
		}
	}
	/**
	 * report error to sync main
	 * @param error
	 */
	protected final void reportError(String error){
		mSyncMain.reportError(error, getReportLabel());
	}
	
	protected final void reportError(String label,Exception e){
		mSyncMain.reportError(label,e,getReportLabel());
	}
	
	/**
	 * report the information
	 * @param info
	 */
	protected final void reportInfo(String info){
		mSyncMain.reportInfo(info,getReportLabel());
	}
	
	
	/**
	 * data class for itemUpdate when syncing
	 * @author tzz
	 *
	 */
	class ModSyncData{
		public String 		oldBid;
		public AbsSyncData newData;
	}
}
