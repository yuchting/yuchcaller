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
		
		if(in.available() > 0){
			
			mDiffType = in.read();
			if(mDiffType == -1){
				throw new Exception("Error mDiffType type!");
			}
			
			if(mClientSyncDataList != null){
				mClientSyncDataList = new Vector<CalendarSyncData>();
			}
			
			int num = sendReceive.ReadInt(in);
			
			for(int i = 0;i < num;i++){
				CalendarSyncData data	= new CalendarSyncData();
				data.input(in, mDiffType == 1);
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
		
		com.google.api.services.calendar.Calendar.Events.List tList = mService.events().list("primary");
	    	    
		tList.setTimeMin(new DateTime(new Date(System.currentTimeMillis() - 365 * 24 * 3600 * 1000L) , TimeZone.getTimeZone(mTimeZoneID)));

	    Events events = tList.execute();
	   
	    StringBuffer sb = new StringBuffer();
	    
	    while (true) {
	    	for (Event event : events.getItems()) {
	    		mSvrSyncDataList.add(event);
	    		sb.append(getEventLastMod(event));
	        }
	        
	        String pageToken = events.getNextPageToken();
	        if (pageToken != null && !pageToken.isEmpty()) {
	        	events = mService.events().list("primary").setPageToken(pageToken).execute();
	        }else{
	        	break;
	        }
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
	}
	
	
	/**
	 * compare the event
	 */
	private void compareEvent()throws Exception{
		
		if(mAllClientSyncDataMD5.equals(mAllSvrSyncDataMD5)){
			
			// same events
			//
			mResult = (new String("succ")).getBytes("UTF-8");
			
		}else{
			
			// different events
			//				
			if(mDiffType == -1){
				mResult = (new String("diff")).getBytes("UTF-8");
				return;
			}
			
			if(mDiffType == 0){
				// export the 
				// ADD DEL UPDATE NEED list
				//
				mResult = exportDiffList();
				
			}else{
				
				// process the need list's client update 
				//
				while(mClientSyncDataList.isEmpty()){
					
					CalendarSyncData client = mClientSyncDataList.get(0);
					
					for(Event svr : mSvrSyncDataList){
						if(client.getGID().equals(svr.getId())){
							
							updateEvent(client);		
							break;
						}
					}
					
					mClientSyncDataList.remove(0);
				}
			}
		}
	}
	
	private byte[] exportDiffList()throws Exception{
		
		// different list
		//
		Vector<CalendarSyncData> tAddList		= null;
		Vector<String>			tDelList	 	= null;
		Vector<CalendarSyncData> tUploadList	= null;
		Vector<CalendarSyncData> tUpdateList	= null;
		Vector<String> 			tNeedList		= null;
		
		add_total:
		for(Event e : mSvrSyncDataList){
			
			for(CalendarSyncData d : mClientSyncDataList){
				
				if(d.getGID().isEmpty()){
					// maybe first sync
					// the GID is empty
					//						
					if(d.getData().summary.equals(e.getSummary()) 
					&& d.getData().note.equals(e.getDescription())
					// same date
					//
					&& e.getStart() != null
					&& e.getStart().getDate() != null
					&& Math.abs(d.getData().start - e.getStart().getDate().getValue()) <= 1000){
						
						if(tUploadList == null){
							tUploadList = new Vector<CalendarSyncData>();
						}
						
						d.setGID(e.getId());
						tUploadList.add(d);							
						
						continue;  
					}
					
				}else{

					if(d.getGID().equals(e.getId())){
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
				
			for(Event e : mSvrSyncDataList){
				if(d.getGID().equals(e.getId())){
					
					long tEventLastMod = getEventLastMod(e);
					
					if(d.getLastMod() > tEventLastMod){
						// client is updated
						// need list
						//
						if(tNeedList == null){
							tNeedList = new Vector<String>();
						}
						tNeedList.add(d.getBBID());
						
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
			
			if(d.getGID().isEmpty()){

				// delete the client calender
				if(tDelList == null){
					tDelList = new Vector<String>();
				}
				tDelList.add(d.getBBID());
				
			}else{
				
				// upload a new event
				if(tUploadList == null){
					tUploadList = new Vector<CalendarSyncData>();
				}
				
				// upload event
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
				
				for(String b : tDelList){
					sendReceive.WriteString(os,b);
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
				}
				
			}else{
				sendReceive.WriteInt(os,0);
			}
			
			if(tNeedList != null){
				sendReceive.WriteInt(os,tNeedList.size());
				for(String b : tNeedList){
					sendReceive.WriteString(os,b);
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
	private void uploadEvent(CalendarSyncData data)throws Exception{
		
		Event tEvent = new Event();
		data.exportEvent(tEvent,mTimeZoneID);
		
		Event createdEvent = mService.events().insert("primary", tEvent).execute();
		data.setGID(createdEvent.getId());
	}
	
	/**
	 * update the google calendar event 
	 * @param data
	 * @throws Exception
	 */
	private void updateEvent(CalendarSyncData data)throws Exception{
		
		Event tEvent = new Event();
		data.exportEvent(tEvent,mTimeZoneID);
		
		mService.events().update("primary", data.getGID(),tEvent).execute();
	}
		
	/**
	 * get last modification time 
	 * @param event
	 * @return
	 */
	private long getEventLastMod(Event event){
		
		long tLastMod;
		if(event.getUpdated() != null){
			tLastMod = event.getUpdated().getValue();
		}else{
			tLastMod = event.getCreated().getValue();
		}
		
		return tLastMod / 1000;
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
