package com.yuchs.yuchcaller.sync.svr;

import java.io.InputStream;
import java.io.OutputStream;
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

public class CalendarSyncData {
	
	// the bb system calendar UID
	private String bID = null;
	
	// the google calendar UID
	private String gID = null;
	
	// this sync solt data md5
	private long lastMod = 0;
	
	// calendar data
	private CalendarData	m_calendarData = null;
	
	public void setBBID(String _bID){bID = _bID;}
	public String getBBID(){return bID;}
	
	public void setGID(String _id){gID = _id;}
	public String getGID(){	return gID;}
	
	public void setLastMod(long _mod){lastMod = _mod;}
	public long getLastMod(){return lastMod;}
	
	public void setData(CalendarData data){m_calendarData = data;}
	public CalendarData getData(){return m_calendarData;}
	
	/**
	 * import the data from the google calendar Event
	 * @param _event
	 */
	public void importEvent(Event _event)throws Exception{
		setGID(_event.getId());
		setLastMod(_event.getUpdated().getValue());
		
		if(m_calendarData == null){
			m_calendarData = new CalendarData();
		}
		
		m_calendarData.repeat_type = "";		
		List<String> tList = _event.getRecurrence();
		if(tList != null){
			for(String re : tList){
				if(m_calendarData.repeat_type.length() != 0){
					m_calendarData.repeat_type += "\n"; 
				}
				
				m_calendarData.repeat_type += re;
			}			
		}
		
		m_calendarData.summary = _event.getSummary();
		m_calendarData.note		= _event.getDescription();
		
		m_calendarData.start	= ge_eventDateTime(_event.getStart());
		m_calendarData.end	= ge_eventDateTime(_event.getEnd());
		
		m_calendarData.location	= _event.getLocation();
		
		m_calendarData.alarm = 0;
		
		// set the alarm 
		Reminders tReminders = _event.getReminders();
		if(tReminders != null){
			List<EventReminder> _eventReminderList = tReminders.getOverrides();
			if(_eventReminderList != null){
				for(EventReminder ev : _eventReminderList){
					if(ev.getMinutes() != null){
						int second = ev.getMinutes() * 60;
						
						if(m_calendarData.alarm == 0 || m_calendarData.alarm > second){
							m_calendarData.alarm = second;
						}
					}
				}
			}
		}
		
		m_calendarData.attendees = null;
		
		// set the attendess
		List<EventAttendee> tAttendeesList = _event.getAttendees();
		if(tAttendeesList != null){
			
			Vector<String>	tTmpList = new Vector<String>();
			for(EventAttendee ea : tAttendeesList){
				if(ea.getEmail() != null){
					tTmpList.add(ea.getEmail());
				}				
			}
			
			m_calendarData.attendees = new String[tTmpList.size()];
			
			for(int i = 0;i < tTmpList.size();i++){
				m_calendarData.attendees[i] = tTmpList.get(i);
			}
		}
		
		if(_event.getVisibility().equals("default")
		|| _event.getVisibility().equals("private")){
			m_calendarData.event_class = CalendarData.CLASS_PRIVATE; 
		}else if(_event.getVisibility().equals("public")){
			m_calendarData.event_class = CalendarData.CLASS_PUBLIC;
		}else{
			m_calendarData.event_class = CalendarData.CLASS_CONFIDENTIAL;
		}
	}
	
	/**
	 * export the data to the google's calendar Event
	 * @param _event
	 * @param _timeZoneID
	 */
	public void exportEvent(Event _event,String _timeZoneID)throws Exception{
		
		_event.setSummary(getData().summary);
		
		if(getData().allDay){
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			
			DateTime startTime = DateTime.parseRfc3339(format.format(new Date(getData().start)));
			_event.setStart(new EventDateTime().setDateTime(startTime).setTimeZone(_timeZoneID));
			
			startTime = DateTime.parseRfc3339(format.format(new Date(getData().start + 24 * 3600 * 1000)));
			_event.setEnd(new EventDateTime().setDateTime(startTime).setTimeZone(_timeZoneID));
			
		}else{

			Date tDate = new Date(getData().start);
			_event.setStart(new EventDateTime().setDate(new DateTime(tDate,TimeZone.getTimeZone(_timeZoneID))));
			
			tDate	= new Date(getData().end);
			_event.setEnd(new EventDateTime().setDate(new DateTime(tDate,TimeZone.getTimeZone(_timeZoneID))));
			
			_event.setLocation(getData().location);
		}
		
		if(getData().alarm > 0){

			Reminders tReminders = new Reminders();
			List<EventReminder> tRemindersList = new ArrayList<EventReminder>();
			
			EventReminder re = new EventReminder();
			
			re.setMethod("popup");
			re.setMinutes(getData().alarm / 60);
			
			tRemindersList.add(re);
			
			tReminders.setOverrides(tRemindersList);
			_event.setReminders(tReminders);
		}
		
		_event.setDescription(getData().note);
		
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
			
			_event.setAttendees(tAttendeesList);
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
		
		_event.setVisibility(tVisibility);
		
		
		// repeat type
		//
		if(!getData().repeat_type.isEmpty()){
			List<String> tRecurrence = new ArrayList<String>();
			
			String[] arr = getData().repeat_type.split("\n");
			for(String re : arr){
				tRecurrence.add(re);
			}
			
			_event.setRecurrence(tRecurrence);
		}
	}
	
	private long ge_eventDateTime(EventDateTime date){ 
		if(date == null || date.getDate() == null){
			return 0;
		}
		
		if(date.getDate().toStringRfc3339().length() <= 11){
			// yyyy-mm-dd 
			//
			m_calendarData.allDay = true;
		}
		
		return date.getDate().getValue();
	}
	
	
	
	/**
	 * input the data from the byte stream
	 * @param in
	 * @throws Exception
	 */
	public void input(InputStream in,boolean _inputData)throws Exception{
		setBBID(sendReceive.ReadString(in));
		setGID(sendReceive.ReadString(in));
		setLastMod(sendReceive.ReadLong(in));
		
		if(_inputData){

			if(m_calendarData == null){
				m_calendarData = new CalendarData();
			}
			
			m_calendarData.inputData(in);
		}
	}
	
	/**
	 * output the data to the byte stream
	 * @param os
	 * @throws Exception
	 */
	public void output(OutputStream os,boolean _outputData)throws Exception{
		sendReceive.WriteString(os,getBBID());
		sendReceive.WriteString(os,getGID());
		sendReceive.WriteLong(os,getLastMod());
		
		if(m_calendarData != null && _outputData){
			m_calendarData.outputData(os);
		}
	}
}
