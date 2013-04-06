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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Event.Reminders;
import com.yuchs.yuchcaller.sync.svr.GoogleAPIData;
import com.yuchs.yuchcaller.sync.svr.GoogleAPISync;
import com.yuchs.yuchcaller.sync.svr.GoogleAPISyncData;

public class CalendarSyncData extends GoogleAPISyncData{
	
	public void setData(CalendarData data){m_APIData = data;}
	public CalendarData getData(){return (CalendarData)m_APIData;}
	
	@Override
	protected GoogleAPIData newData() {
		return new CalendarData();
	}
		
	/**
	 * export the data to the google's calendar Event
	 * @param g
	 * @param _timeZoneID
	 */
	public void exportGoogleData(Object g,String _timeZoneID)throws Exception{
		
		Event event = (Event)g;
		
		event.setSummary(getData().summary);
		
		if(getData().allDay){
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			
			Date tDate = new Date(getData().start - (getData().start % (24 * 3600000) ));
			DateTime startTime = new DateTime(format.format(tDate)); 
			
			event.setStart(new EventDateTime().setDate(startTime).setTimeZone(_timeZoneID));
			
			tDate = new Date(tDate.getTime() + 24 * 3600000);
			DateTime endTime = new DateTime(format.format(tDate)); 
			
			event.setEnd(new EventDateTime().setDate(endTime).setTimeZone(_timeZoneID));
			
		}else{

			Date tDate = new Date(getData().start);
			
			DateTime start = new DateTime(tDate, TimeZone.getTimeZone(_timeZoneID));
			event.setStart(new EventDateTime().setDateTime(start).setTimeZone(_timeZoneID));
			
			tDate = new Date(getData().end);
			DateTime end = new DateTime(tDate, TimeZone.getTimeZone(_timeZoneID));
			event.setEnd(new EventDateTime().setDateTime(end).setTimeZone(_timeZoneID));
		}
		
		event.setLocation(getData().location);
		
		if(getData().alarm > 0){
			
			List<EventReminder> tRemindersList = new ArrayList<EventReminder>();
			
			EventReminder re = new EventReminder();
			
			re.setMethod("popup");
			re.setMinutes(getData().alarm / 60);
			
			tRemindersList.add(re);
			
			Reminders tReminders = new Reminders();
			
			// don't use the calendar default reminders
			// otherwise bad request response
			//
			tReminders.setUseDefault(false);
			tReminders.setOverrides(tRemindersList);
			
			event.setReminders(tReminders);
		}
		
		event.setDescription(getData().note);				
		
		// the attendees
		//
		if(getData().attendees != null){			
			
			List<EventAttendee> tAttendeesList = new ArrayList<EventAttendee>();
			for(String email : getData().attendees){
				
				if(GoogleAPISync.isValidEmail(email)){
				
					EventAttendee eventAtt = null;
					
					if(event.getAttendees() != null){
						// search the attendee in already in attendees
						//
						List<EventAttendee> tSearchList = event.getAttendees();
						for(EventAttendee dee : tSearchList){
							if(dee.getEmail().equalsIgnoreCase(email)){
								eventAtt = dee;
								break;
							}
						}
					}
					
					if(eventAtt == null){
						eventAtt = new EventAttendee();
						eventAtt.setEmail(email);
					}
					
					tAttendeesList.add(eventAtt);
				}				
			}
			
			event.setAttendees(tAttendeesList);
		}

		// visibility
		String tVisibility;
		
		switch(getData().event_class){
		case CalendarData.CLASS_PUBLIC:
			tVisibility = "public";
			break;
		case CalendarData.CLASS_CONFIDENTIAL:
			tVisibility = "confidential";
			break;
		default:
			tVisibility = "private";
			break;
		}
		
		event.setVisibility(tVisibility);		
		
		// repeat type
		//
		if(!getData().repeat_type.isEmpty()){
			
			List<String> tRecurrence = new ArrayList<String>();
			
			String[] arr = getData().repeat_type.split("\n");
			for(String re : arr){
				
				if(re.indexOf("FREQ=YEARLY") != -1){
					
					String util = "";
					int idx = re.indexOf(";UNTIL="); 
					if(idx != -1){
						int endIdx = re.indexOf(";",idx + 1);
						if(endIdx == -1){
							util = re.substring(idx);
						}else{
							util = re.substring(idx,endIdx);
						}					
					}
					// Google calendar is NOT support relative date by year
					//
					re = "RRULE:FREQ=YEARLY;INTERVAL=1" + util;
				}
				
				tRecurrence.add(re);
			}
			
			event.setRecurrence(tRecurrence);
		}
	}
	
	/**
	 * import the data from the google calendar Event
	 * @param timeZoneID
	 * @param g
	 */
	@Override
	public void importGoogleData(Object g,String timeZoneID)throws Exception{
		
		Event event = (Event)g; 
		
		setGID(event.getId());
		setLastMod(CalendarSync.getEventLastMod(event));
		
		if(getData() == null){
			m_APIData = newData();
		}
		
		getData().repeat_type = "";		
		List<String> tList = event.getRecurrence();
		if(tList != null){
			for(String re : tList){
				if(getData().repeat_type.length() != 0){
					getData().repeat_type += "\n"; 
				}
				
				getData().repeat_type += re;
			}			
		}
		
		getData().summary	= event.getSummary();
		getData().note		= event.getDescription();
		
		getData().start		= getEventDateTime(event.getStart());
		getData().end		= getEventDateTime(event.getEnd());
		
		getData().location	= event.getLocation();
		
		getData().alarm = 0;
		
		// set the alarm 
		Reminders tReminders = event.getReminders();
		if(tReminders != null){
			List<EventReminder> _eventReminderList = tReminders.getOverrides();
			
			if(tReminders.getUseDefault() != null 
			&& tReminders.getUseDefault() == true){
				
				getData().alarm = 15 * 60;
				
			}else{

				if(_eventReminderList != null){
					for(EventReminder ev : _eventReminderList){
						if(ev.getMinutes() != null){
							int second = ev.getMinutes() * 60;
							
							if(getData().alarm == 0 || getData().alarm > second){
								getData().alarm = second;
							}
						}
					}
				}
			}
		}
		
		getData().attendees = null;
		
		// set the attendess
		List<EventAttendee> tAttendeesList = event.getAttendees();
		if(tAttendeesList != null){
			
			Vector<String>	tTmpList = new Vector<String>();
			for(EventAttendee ea : tAttendeesList){
				if(ea.getEmail() != null){
					tTmpList.add(ea.getEmail());
				}				
			}
			
			getData().attendees = new String[tTmpList.size()];
			
			for(int i = 0;i < tTmpList.size();i++){
				getData().attendees[i] = tTmpList.get(i);
			}
		}
		
		String visibility = event.getVisibility();
		
		if(visibility != null){			
			if(visibility.equalsIgnoreCase("default")
			|| visibility.equalsIgnoreCase("private")){
				
				getData().event_class = CalendarData.CLASS_PRIVATE; 
				
			}else if(visibility.equalsIgnoreCase("public")){
				
				getData().event_class = CalendarData.CLASS_PUBLIC;
			}else{
				getData().event_class = CalendarData.CLASS_CONFIDENTIAL;
			}
		}
		
	}
	
	private long getEventDateTime(EventDateTime date){
		
		if(date == null){
			return 0;
		}
		
		DateTime ed = date.getDateTime();
		if(ed == null){
			ed = date.getDate();
			
			if(ed != null){
				// yyyy-mm-dd 
				//
				getData().allDay = true;
			}else{
				return 0;
			}
		}
		
		return ed.getValue();
	}
		
	
	
}
