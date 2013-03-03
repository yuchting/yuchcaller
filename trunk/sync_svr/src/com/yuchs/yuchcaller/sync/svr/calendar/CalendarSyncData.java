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
			
			DateTime startTime = DateTime.parseRfc3339(format.format(new Date(getData().start)));
			event.setStart(new EventDateTime().setDateTime(startTime).setTimeZone(_timeZoneID));
			
			startTime = DateTime.parseRfc3339(format.format(new Date(getData().start + 24 * 3600 * 1000)));
			event.setEnd(new EventDateTime().setDateTime(startTime).setTimeZone(_timeZoneID));
			
		}else{

			Date tDate = new Date(getData().start);
			event.setStart(new EventDateTime().setDate(new DateTime(tDate,TimeZone.getTimeZone(_timeZoneID))));
			
			tDate	= new Date(getData().end);
			event.setEnd(new EventDateTime().setDate(new DateTime(tDate,TimeZone.getTimeZone(_timeZoneID))));
			
			event.setLocation(getData().location);
		}
		
		if(getData().alarm > 0){

			Reminders tReminders = new Reminders();
			List<EventReminder> tRemindersList = new ArrayList<EventReminder>();
			
			EventReminder re = new EventReminder();
			
			re.setMethod("popup");
			re.setMinutes(getData().alarm / 60);
			
			tRemindersList.add(re);
			
			tReminders.setOverrides(tRemindersList);
			event.setReminders(tReminders);
		}
		
		event.setDescription(getData().note);
		
		// the attendees
		//
		if(getData().attendees != null){
			
			List<EventAttendee> tAttendeesList = new ArrayList<EventAttendee>();
			for(String email : getData().attendees){
				EventAttendee eventAtt = new EventAttendee();
				eventAtt.setEmail(email);
				eventAtt.setDisplayName(email);
				
				tAttendeesList.add(eventAtt);
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
				tRecurrence.add(re);
			}
			
			event.setRecurrence(tRecurrence);
		}
	}
	
	/**
	 * import the data from the google calendar Event
	 * @param g
	 */
	@Override
	public void importGoogleData(Object g)throws Exception{
		
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
		
		getData().start	= getEventDateTime(event.getStart());
		getData().end		= getEventDateTime(event.getEnd());
		
		getData().location	= event.getLocation();
		
		getData().alarm = 0;
		
		// set the alarm 
		Reminders tReminders = event.getReminders();
		if(tReminders != null){
			List<EventReminder> _eventReminderList = tReminders.getOverrides();
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
		
		DateTime ed = date.getDateTime();
		if(ed == null){
			ed = date.getDate();
		}
		
		if(date == null || ed == null){
			return 0;
		}
		
		if(ed.toStringRfc3339().length() <= 11){
			// yyyy-mm-dd 
			//
			getData().allDay = true;
		}
		
		return ed.getValue();
	}
		
	
	
}
