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
package com.yuchs.yuchcaller.sync.svr.calendar;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.yuchs.yuchcaller.sync.svr.GoogleAPISync;
import com.yuchs.yuchcaller.sync.svr.GoogleAPISyncData;
import com.yuchs.yuchcaller.sync.svr.Logger;

public class CalendarSync extends GoogleAPISync{

	/**
	 * the service of this request
	 */
	private final Calendar mService;
	
	
	public CalendarSync(InputStream in,Logger _logger) throws Exception {
		super(in,_logger);
		
		mService = new Calendar(mHttpTransport, mJsonFactory,mGoogleCredential);
		
		// read the server calendar (or read from the BufferdEvents list)
		readSvrGoogleData();		
		
		// compare the event
		compareEvent();
	}

	/**
	 * read the server event list
	 * @throws Exception
	 */
	@Override
	protected void readSvrGoogleData()throws Exception{
		
		if(fetchFormerEvent()){
			return;
		}
		
		if(mSvrSyncDataList == null){
			mSvrSyncDataList = new Vector<Object>();
		}
		
		com.google.api.services.calendar.Calendar.Events.List tList = mService.events().list("primary");

		tList.setTimeMin(new DateTime(new Date(mMinTimeToSync) , TimeZone.getTimeZone(mTimeZoneID)));
		tList.setMaxResults(999999);

		Events events = tList.execute();
			   
		while (events != null && events.getItems() != null) {
			
			// insert and sort the event list by the last modification time
			for (Event event : events.getItems()) {
				
				long mod = getEventLastMod(event);
				if(mod == 0){
					continue;
				}
				
				for(int i = 0;i < mSvrSyncDataList.size();i++){
					Event e = (Event)mSvrSyncDataList.get(i);
					if(mod > getEventLastMod(e)){
										
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
		
		for(int i = 0;i < mSvrSyncDataList.size();i++){
			Event e = (Event)mSvrSyncDataList.get(i);
			
			long tLastMod = getEventLastMod(e);
			sb.append(tLastMod);
			
			debug.append(tLastMod).append(":").append(e.getId()).append("-").append(e.getSummary()).append("\n");
		}
		
		mAllSvrSyncDataMD5 = getMD5(sb.toString());
		
		mLogger.LogOut(debug.toString());
		
		storeFormerEvent();
	}
	
	@Override
	protected GoogleAPISyncData newSyncData(){
		return new CalendarSyncData();
	}
	
	@Override
	protected String getGoogleDataId(Object o){
		Event e = (Event)o;
		return e.getId();
	}
	
	/**
	 * is first sync same event ?
	 * @param e
	 * @param d
	 * @return
	 */
	@Override
	protected boolean isFristSyncSameData(Object o,GoogleAPISyncData g){
		
		Event e				= (Event)o;
		CalendarSyncData d	= (CalendarSyncData)g;
			
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
		
		
		return false;
	}
		
	
	/**
	 * upload a event to google calendar
	 * 
	 * @param data
	 * @throws Exception
	 */
	@Override
	protected Object uploadGoogleData(GoogleAPISyncData data)throws Exception{
		
//		Event event = new Event();
//
//		event.setSummary("Appointment");
//		event.setLocation("Somewhere");
//
//
//		Date startDate = new Date();
//		Date endDate = new Date(startDate.getTime() + 3600000);
//		DateTime start = new DateTime(startDate, TimeZone.getTimeZone("UTC"));
//		event.setStart(new EventDateTime().setDateTime(start));
//		DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
//		event.setEnd(new EventDateTime().setDateTime(end));

		
		Event tEvent = new Event();
		data.exportGoogleData(tEvent,mTimeZoneID);
						
		tEvent = mService.events().insert("primary", tEvent).execute();
		data.setGID(tEvent.getId());
		data.setLastMod(getEventLastMod(tEvent));
		
		// ouput debug info
		String tDebugInfo = mYuchAcc + " uploadEvent:" + data.getBBID();
		if(data.getAPIData() != null){
			CalendarData cd = (CalendarData)data.getAPIData();
			tDebugInfo += " " + cd.summary;
		}
		mLogger.LogOut(tDebugInfo);
		
		return tEvent;
	}
	
	/**
	 * update the google calendar event
	 * @param o 
	 * @param data
	 * @throws Exception
	 */
	@Override
	protected Object updateGoogleData(Object o,GoogleAPISyncData data)throws Exception{
			
		Event tEvent = (Event)o;
		data.exportGoogleData(tEvent,mTimeZoneID);
		
		tEvent = mService.events().update("primary", data.getGID(),tEvent).execute();
		data.setGID(tEvent.getId());
		data.setLastMod(getEventLastMod(tEvent));		
		
		// ouput debug info
		String tDebugInfo = mYuchAcc + " updateEvent:" + data.getBBID();
		if(data.getAPIData() != null){
			CalendarData cd = (CalendarData)data.getAPIData();
			tDebugInfo += " " + cd.summary;
		}
		mLogger.LogOut(tDebugInfo);
		
		return tEvent;
	}
	
	/**
	 * delete the event 
	 * @param data
	 * @throws Exception
	 */
	@Override
	protected void deleteGoogleData(GoogleAPISyncData data)throws Exception{
		try{ 
			
			mService.events().delete("primary", data.getGID()).execute();
			
			// ouput debug info
			String tDebugInfo = mYuchAcc + " deleteEvent:" + data.getBBID();
			if(data.getAPIData() != null){
				CalendarData cd = (CalendarData)data.getAPIData();
				tDebugInfo += " " + cd.summary;
			}
			
			mLogger.LogOut(tDebugInfo);
			
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
		
		long tLastMod = 0;
		
		if(event.getUpdated() != null){
			tLastMod = event.getUpdated().getValue();
			
		}else if(event.getCreated() != null){
			
			tLastMod = event.getCreated().getValue();
		}
		
		return tLastMod;
	}


	@Override
	protected long getGoogleDataLastMod(Object o) {		
		return getEventLastMod((Event)o);
	}
}
