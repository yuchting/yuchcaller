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
package com.yuchs.yuchcaller.sync.svr;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

public class CalendarSync extends GoogleAPISync{

	/**
	 * the service of this request
	 */
	private final Calendar mService;
	
	/**
	 * the list of received from client
	 */
	private Vector<CalendarSyncData>	mClientSyncDataList = null;
		
	/**
	 * event list
	 */
	private Vector<Event>				mSvrSyncDataList = null;
	
	/**
	 * different type
	 * 
	 * -1: sync type
	 * 0 : diff type
	 * 1 : need list type
	 */
	private int	mDiffType					= -1;
	
	/**
	 * buffered Hash map for reduce Google API refresh
	 */
	private static ConcurrentHashMap<String,BufferedEvents> smEventHashMap = new ConcurrentHashMap<String,BufferedEvents>();	
	
	public CalendarSync(InputStream in,Logger _logger) throws Exception {
		super(in,_logger);
		
		mService = new Calendar(mHttpTransport, mJsonFactory,mGoogleCredential);
		
		// read the client sync data and type
		readClientSyncData(in);
		
		// read the server calendar (or read from the BufferdEvents list)
		readSvrCalendar();		
		
		// compare the event
		compareEvent();
	}
	
	/**
	 * read the client sync data via the json object
	 * @param _json
	 * @throws Exception
	 */
	private void readClientSyncData(InputStream in)throws Exception{
		
		mDiffType = in.read();
		if(mDiffType != 0){
			
			if(mClientSyncDataList == null){
				mClientSyncDataList = new Vector<CalendarSyncData>();
			}
			
			int num = sendReceive.ReadInt(in);
			
			for(int i = 0;i < num;i++){
				CalendarSyncData data	= new CalendarSyncData();
				data.input(in);
				mClientSyncDataList.add(data);
			}
		}		
	}

	
	/**
	 * read the server event list
	 * @throws Exception
	 */
	private void readSvrCalendar()throws Exception{
		
		BufferedEvents tFormerEvent = smEventHashMap.get(mYuchAcc);
		
		if(tFormerEvent != null){
			if(tFormerEvent.mRefreshTime - System.currentTimeMillis() < 2 * 60 * 1000){
				mAllSvrSyncDataMD5	= tFormerEvent.mAllEventMd5;
				mSvrSyncDataList	= tFormerEvent.mEventList;
				
				return;
			}
		}
		
		if(mSvrSyncDataList == null){
			mSvrSyncDataList = new Vector<Event>();
		}
		
		com.google.api.services.calendar.Calendar.Events.List tList = mService.events().list("primary");

		tList.setTimeMin(new DateTime(new Date(mMinTimeToSync) , TimeZone.getTimeZone(mTimeZoneID)));

		Events events = tList.execute();
			   
		while (true) {
			
			// insert and sort the event list by the last modification time
			for (Event event : events.getItems()) {
				
				for(int i = 0;i < mSvrSyncDataList.size();i++){
					Event e = (Event)mSvrSyncDataList.get(i);
					if(getEventLastMod(event) > getEventLastMod(e)){
										
						mSvrSyncDataList.insertElementAt(event, i);
						
						event = null;
						break;
					}
				}
				
				if(event != null){
					mSvrSyncDataList.add(event);
				}
		    }
			
		    String pageToken = events.getNextPageToken();
		    if (pageToken != null && !pageToken.isEmpty()) {
		    	events = mService.events().list("primary").setPageToken(pageToken).execute();
		    }else{
		    	break;
		    }
		}
		
		StringBuffer sb = new StringBuffer();
		StringBuffer debug = new StringBuffer();
		
		for(Event e : mSvrSyncDataList){
			
			long tLastMod = getEventLastMod(e);
					sb.append(tLastMod);
					
					debug.append(tLastMod).append(":").append(e.getId()).append("-").append(e.getSummary()).append("\n");
		}
		
		mAllSvrSyncDataMD5 = getMD5(sb.toString());
		
		// buffered the former event;
		if(tFormerEvent == null){
			tFormerEvent = new BufferedEvents();
		}
		
		tFormerEvent.mAllEventMd5	= mAllSvrSyncDataMD5;
		tFormerEvent.mEventList		= mSvrSyncDataList;
		tFormerEvent.mRefreshTime	= System.currentTimeMillis();
		
		smEventHashMap.put(mYuchAcc,tFormerEvent);
		
		System.out.println(debug.toString());
	}
	
	
	/**
	 * compare the event
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
	 * 		process<-----------add update upload delete needlist
	 * 		|					|
	 * 		needList---------->updated google calendar  ( diff type 1)
	 * 		|					|
	 * 		succ<--------------mod time list
	 * 
	 */
	private void compareEvent()throws Exception{
					
		if(mDiffType == 0){

			String tResultStr;
			if(mAllClientSyncDataMD5.equals(mAllSvrSyncDataMD5)){
				// same events
				//
				tResultStr = "succ";
			}else{
				tResultStr = "diff";
			}
			
			mResult = tResultStr.getBytes("UTF-8");	
			
		}else if(mDiffType == 1){
			
			// export the 
			// ADD DEL UPDATE NEED list
			//
			mResult = exportDiffList();
			
		}else if(mDiffType == 2){
			
			// process the NeedList
			//
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try{
				sendReceive.WriteInt(os, mClientSyncDataList.size());
				
				// process the need list's client update 
				//
				for(int i = 0;i < mClientSyncDataList.size();i++){
					
					CalendarSyncData client = mClientSyncDataList.get(i);
					
					for(Event svr : mSvrSyncDataList){
						if(client.getGID().equals(svr.getId())){
							
							updateEvent(client);
							
							sendReceive.WriteString(os, client.getBBID());
							sendReceive.WriteLong(os,client.getLastMod());
							
							break;
						}
					}
				}
				
				mResult = os.toByteArray();
				
			}finally{
				os.close();
				os = null;
			}
		}else{
			throw new Exception("Error diff type:" + mDiffType);
		}		
	}
	
	/**
	 * is first sync same event ?
	 * @param e
	 * @param d
	 * @return
	 */
	private boolean isFirstSyncSameEvent(Event e,CalendarSyncData d){
				
		if(d.getGID().isEmpty() && d.getData() != null){
			
			if((d.getData().summary.equals(e.getSummary()) || (d.getData().summary.length() == 0 && e.getSummary() == null))
			&& (d.getData().note.equals(e.getDescription()) || (d.getData().note.length() == 0 && e.getDescription() == null) )){ // same text attribute
				
				if(e.getRecurrence() != null){
					// recurrence event
					//
					List<String> recurList = e.getRecurrence();
					StringBuffer sb = new StringBuffer();
					for(String s : recurList){
						if(sb.length() > 0){
							sb.append("\n");
						}
						sb.append(s);
					}
					
					return sb.toString().equalsIgnoreCase(d.getData().repeat_type);
					
				}else{
					
					
					if(e.getStart() != null){
					
						DateTime ed = e.getStart().getDate();
						if(ed == null){
							ed = e.getStart().getDateTime();
						}
					
						if( ed != null && Math.abs(d.getData().start - ed.getValue()) <= 1000){
							
							// same date
							//
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	private byte[] exportDiffList()throws Exception{
		
		// different list
		//
		Vector<CalendarSyncData> tAddList		= null;
		Vector<CalendarSyncData> tDelList	 	= null;
		Vector<CalendarSyncData> tUploadList	= null;
		Vector<CalendarSyncData> tUpdateList	= null;
		Vector<CalendarSyncData> tNeedList		= null;
		
		add_total:
		for(Event e : mSvrSyncDataList){
			
			for(CalendarSyncData d : mClientSyncDataList){
				
				if(isFirstSyncSameEvent(e,d)){
						
					if(tUploadList == null){
						tUploadList = new Vector<CalendarSyncData>();
					}
					
					d.setGID(e.getId());
					d.setLastMod(getEventLastMod(e));
					
					tUploadList.add(d);
					
					// remove this avoid next compare
					mClientSyncDataList.remove(d);
					
					continue add_total;
					
				}else{

					if(d.getGID().equals(e.getId())){
						
						if(d.getLastMod() == -1){
							deleteEvent(d);
						}
						
						continue add_total;
					}
				}
			}
			
			if(tAddList == null){
				tAddList = new Vector<CalendarSyncData>();
			}
			
			CalendarSyncData data = new CalendarSyncData();
			data.importEvent(e);

			tAddList.add(data);
		}
		
		del_total:
		for(CalendarSyncData d : mClientSyncDataList){
			
			if(d.getLastMod() != -1){
				
				for(Event e : mSvrSyncDataList){
					
					if(d.getGID().equals(e.getId())){
						
						long tEventLastMod = getEventLastMod(e);
						
						if(d.getLastMod() > tEventLastMod){
							// client is updated
							// need list
							//
							if(tNeedList == null){
								tNeedList = new Vector<CalendarSyncData>();
							}
							tNeedList.add(d);
							
						}else if(d.getLastMod() < tEventLastMod){
							
							// server's updated
							// update list
							//
							if(tUpdateList == null){
								tUpdateList = new Vector<CalendarSyncData>();
							}
							
							d.importEvent(e);
							tUpdateList.add(d);
						}
						
						continue del_total;
					}
				}
			}
			
			
			if(!d.getGID().isEmpty()){

				// delete the client calender
				if(tDelList == null){
					tDelList = new Vector<CalendarSyncData>();
				}
				tDelList.add(d);
				
			}else{
				
				// upload a new event
				if(tUploadList == null){
					tUploadList = new Vector<CalendarSyncData>();
				}
				
				// upload event to google calendar
				uploadEvent(d);
				
				tUploadList.add(d);
			}
		}
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try{
			
			if(tAddList != null){
				sendReceive.WriteInt(os, tAddList.size());
				for(CalendarSyncData d : tAddList){
					d.output(os, true);
				}
			}else{
				sendReceive.WriteInt(os, 0);
			}
			
			if(tDelList != null){
				sendReceive.WriteInt(os, tDelList.size());
				
				for(CalendarSyncData d : tDelList){
					sendReceive.WriteString(os,d.getBBID());
				}
			}else{
				sendReceive.WriteInt(os,0);
			}
			
			if(tUpdateList != null){
				sendReceive.WriteInt(os, tUpdateList.size());
				
				for(CalendarSyncData d : tUpdateList){
					d.output(os, true);
				}
			}else{
				sendReceive.WriteInt(os,0);
			}
			
			if(tUploadList != null){
				sendReceive.WriteInt(os, tUploadList.size());
				
				for(CalendarSyncData d : tUploadList){
					sendReceive.WriteString(os,d.getBBID());
					sendReceive.WriteString(os,d.getGID());
					sendReceive.WriteLong(os,d.getLastMod());
				}
				
			}else{
				sendReceive.WriteInt(os,0);
			}
			
			if(tNeedList != null){
				sendReceive.WriteInt(os,tNeedList.size());
				for(CalendarSyncData d : tNeedList){
					sendReceive.WriteString(os,d.getBBID());
				}
			}else{
				sendReceive.WriteInt(os,0);
			}
		
			return os.toByteArray();
			
		}finally{
			os.close();
		}
	}
	
	
	/**
	 * upload a event to google calendar
	 * 
	 * @param data
	 * @throws Exception
	 */
	private Event uploadEvent(CalendarSyncData data)throws Exception{
		
		Event tEvent = new Event();
		data.exportEvent(tEvent,mTimeZoneID);
		
		// TODO: delete follow code
		tEvent.setId("" + new Random().nextInt());
		tEvent.setUpdated(new DateTime(new Date()));
		mSvrSyncDataList.add(tEvent);
				
		//tEvent = mService.events().insert("primary", tEvent).execute();
		data.setGID(tEvent.getId());
		data.setLastMod(getEventLastMod(tEvent));
		
		mLogger.LogOut(mYuchAcc + " uploadEvent:" + data.getBBID());
		
		return tEvent;
	}
	
	/**
	 * update the google calendar event 
	 * @param data
	 * @throws Exception
	 */
	private Event updateEvent(CalendarSyncData data)throws Exception{
		
		Event tEvent = new Event();
		data.exportEvent(tEvent,mTimeZoneID);
		
		// TODO: delete follow code
		tEvent.setId("" + new Random().nextInt());
		tEvent.setUpdated(new DateTime(new Date()));
		
		//tEvent = mService.events().update("primary", data.getGID(),tEvent).execute();
		data.setGID(tEvent.getId());
		data.setLastMod(getEventLastMod(tEvent));		
		
		mLogger.LogOut(mYuchAcc + " updateEvent:" + data.getBBID());
		
		return tEvent;
	}
	
	/**
	 * delete the event 
	 * @param data
	 * @throws Exception
	 */
	private void deleteEvent(CalendarSyncData data)throws Exception{
		try{
			// TODO delete follow code 
			//mService.events().delete("primary", data.getGID()).execute();
			
			mLogger.LogOut(mYuchAcc + " deleteEvent:" + data.getBBID());
		}catch(Exception e){
			mLogger.PrinterException(mYuchAcc,e);
		}
	}
		
	/**
	 * get last modification time 
	 * @param event
	 * @return
	 */
	public static long getEventLastMod(Event event){
		
		long tLastMod;
		if(event.getUpdated() != null){
			tLastMod = event.getUpdated().getValue();
		}else{
			tLastMod = event.getCreated().getValue();
		}
		
		return tLastMod;
	}
		
	/**
	 * buffered events list
	 * @author tzz
	 *
	 */
	private class BufferedEvents{

		//! former refresh time from the Google API
		public long mRefreshTime;
		
		//! all event md5
		public String mAllEventMd5;
		
		//! the event of List
		public Vector<Event>	mEventList = null;
		
	}

}
