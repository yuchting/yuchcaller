package com.yuchs.yuchcaller.sync;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.microedition.pim.Event;
import javax.microedition.pim.RepeatRule;

import com.yuchs.yuchcaller.sendReceive;

import net.rim.blackberry.api.pdap.BlackBerryEvent;

public class CalendarSyncData {
	
	// the bb system calendar UID
	private String bID = null;
	
	// the google calendar UID
	private String gID = null;
	
	// last modified time
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
	 * import blackberry event
	 * @param event
	 */
	public void importData(BlackBerryEvent _event)throws Exception{
		
		if(m_calendarData == null){
			m_calendarData = new CalendarData();
		}
				
		// set the repeat information
		RepeatRule repeat = _event.getRepeat();
		if(repeat != null){
						
			// http://www.ietf.org/rfc/rfc2445
			//
			String tRecurring;
			
			int re = repeat.getInt(RepeatRule.FREQUENCY);
			
			switch(re){
			case RepeatRule.DAILY:
				tRecurring = "RRULE:FREQ=DAILY";
				break;
			case RepeatRule.WEEKLY:
				tRecurring = "RRULE:FREQ=WEEKLY";
				break;
			case RepeatRule.MONTHLY:
				tRecurring = "RRULE:FREQ=MONTHLY";
				break;
			case RepeatRule.YEARLY:
				tRecurring = "RRULE:FREQ=YEARLY";				
				break;
			default:
				tRecurring = "";
				break;
			}
			
			try{
				tRecurring += ";BYDAY=" + repeat.getInt(RepeatRule.DAY_IN_MONTH);
			}catch(Exception e){}
			
			try{
				tRecurring += ";BYWEEK=" + repeat.getInt(RepeatRule.WEEK_IN_MONTH);
			}catch(Exception e){}
			
			try{
				tRecurring += ";BYMONTH=" + repeat.getInt(RepeatRule.MONTH_IN_YEAR);
			}catch(Exception e){}
			
			try{
				tRecurring += ";INTERVAL=" + repeat.getInt(RepeatRule.INTERVAL);
			}catch(Exception e){}
			
			try{
				tRecurring += ";UNTIL=" + getR2445FormatDate(repeat.getDate(RepeatRule.END));
			}catch(Exception e){}
			
			try{
				tRecurring += ";COUNT=" + repeat.getInt(RepeatRule.COUNT);
			}catch(Exception e){}
				
			try{
				int tDayInWeek = repeat.getInt(RepeatRule.DAY_IN_WEEK);
								
				int[]	tWeekConstant = 
				{
					RepeatRule.MONDAY,RepeatRule.TUESDAY,RepeatRule.WEDNESDAY,RepeatRule.THURSDAY,RepeatRule.FRIDAY,RepeatRule.SATURDAY,
				};
				
				String[] tWeekSign = 
				{
					"MO","TU","WE","TH","FR","SU"	
				};
				
				String tOut = "";
				
				for(int i = 0;i < tWeekConstant.length;i++){
						
					if((tWeekConstant[i] & tDayInWeek) != 0){
						
						if(tOut.length() != 0){
							tOut += ",";
						}
						
						tOut += tWeekSign[i];
					}
				}
				
				if(tOut.length() != 0){
					tRecurring += ";BYDAY=" + tOut;
				}
				
			}catch(Exception e){}
			
			try{
				
				Enumeration enum = repeat.getExceptDates();
				
				String exRule = "";
				while(enum != null && enum.hasMoreElements()){
					Date date = (Date)enum.nextElement();
					
					if(exRule.length() == 0){
						exRule = "\nEXDATE:";
					}else{
						exRule += ",";
					}
					
					exRule += getR2445FormatDate(date.getTime());
				}
				
				tRecurring += exRule;
				
			}catch(Exception e){}
			
			m_calendarData.repeat_type = tRecurring;
		}
		
		// set the fields information
		int[] fieldIds = _event.getFields();
		int id;
		
		for(int index = 0; index < fieldIds.length; ++index){
			
			id = fieldIds[index];
			
			switch(id){
			case Event.UID:
				bID = getStringField(_event,id);
				break;
			case BlackBerryEvent.SUMMARY:
				m_calendarData.summary = getStringField(_event, id);
				break;
			case BlackBerryEvent.START:
				m_calendarData.start = getDateField(_event, id);
				break;
			case BlackBerryEvent.END:
				m_calendarData.end = getDateField(_event, id);
				break;
			case BlackBerryEvent.LOCATION:
				m_calendarData.location = getStringField(_event,id);
				break;
			case BlackBerryEvent.NOTE:
				m_calendarData.note = getStringField(_event,id);
				break;
			case BlackBerryEvent.ALARM:
				m_calendarData.alarm = getIntField(_event,id);
				break;
			case BlackBerryEvent.ALLDAY:
				m_calendarData.allDay = getBooleanField(_event, id);
				break;
			case BlackBerryEvent.ATTENDEES:
				m_calendarData.attendees = getStringArrayField(_event, id);
				break;
			case BlackBerryEvent.FREE_BUSY:
				m_calendarData.free_busy = getIntField(_event, id);
				break;
			case BlackBerryEvent.CLASS:
				int cls = getIntField(_event, id);
				switch(cls){
				case BlackBerryEvent.CLASS_CONFIDENTIAL:
					cls = CalendarData.CLASS_CONFIDENTIAL;
					break;
				case BlackBerryEvent.CLASS_PRIVATE:
					cls = CalendarData.CLASS_PRIVATE;
					break;
				default:
					cls = CalendarData.CLASS_PUBLIC;
					break;
				}
				m_calendarData.event_class = cls;
				break;
			}
			
//			if(_test){
//
//				if(id == Event.UID){
//					event.setString(id, 0,Event.STRING, "GoogleCalendarID");
//					event.commit();
//				}
//			}
			
//			if(t_event.getFieldDataType(id) == Event.STRING){
//				
//				for(int j=0; j < event.countValues(id); ++j){
//					String value = event.getString(id, j);
//					
//					System.out.println(Integer.toString(id) + "=" + value);
//				}
//			}
		}
		
//		ByteArrayOutputStream os = new ByteArrayOutputStream();
//		try{
//			m_calendarData.outputData(os);
//			md5 = SyncMain.md5(os.toByteArray());
//		}finally{
//			os.close();
//		}
	}
	
	public void exportData(Event event)throws Exception{
		
		
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
	
	
	static Calendar sm_calendar = Calendar.getInstance();
	static Date		sm_timeDate = new Date();
	
	/**
	 * get the format string by date
	 * @param date
	 * @return
	 */
	private static String getR2445FormatDate(long date){
		
		sm_timeDate.setTime(date);
		sm_calendar.setTime(sm_timeDate);
				
		StringBuffer sb = new StringBuffer();
		sb.append(sm_calendar.get(Calendar.YEAR)).append(parseTime(sm_calendar.get(Calendar.MONTH) + 1))
			.append(parseTime(sm_calendar.get(Calendar.DAY_OF_MONTH))).append("T")
			.append(parseTime(sm_calendar.get(Calendar.HOUR_OF_DAY)))
			.append(parseTime(sm_calendar.get(Calendar.MINUTE)))
			.append(parseTime(sm_calendar.get(Calendar.SECOND))).append("Z");
		
		return sb.toString();
	}
	
	private static String parseTime(int time){
		
		if(time < 10){
			return "0" + Integer.toString(time);
		}else{
			return Integer.toString(time);
		}
	}
	
	/**
	 * get the blackberry event string 
	 * @param _event
	 * @param _id
	 * @return
	 */
	private String getStringField(BlackBerryEvent _event,int _id){
		int tCount = _event.countValues(_id);
		if(tCount > 0){
			return _event.getString(_id, 0);
		}
		
		return "";
	}
	
	/**
	 * get the the long(for date) field
	 * @param _event
	 * @param _id
	 * @return
	 */
	private long getDateField(BlackBerryEvent _event,int _id){
		int tCount = _event.countValues(_id);
		if(tCount > 0){
			return _event.getDate(_id, 0);
		}
		
		return 0;
	}
	
	/**
	 * get the integer field for the event
	 * @param _event
	 * @param _id
	 * @return
	 */
	private int getIntField(BlackBerryEvent _event,int _id){
		int tCount = _event.countValues(_id);
		if(tCount > 0){
			return _event.getInt(_id, 0);
		}
		
		return 0;
	}
	
	/**
	 * get the boolean field for the event
	 * @param _event
	 * @param _id
	 * @return
	 */
	public boolean getBooleanField(BlackBerryEvent _event,int _id){
		int tCount = _event.countValues(_id);
		if(tCount > 0){
			return _event.getBoolean(_id, 0);
		}
		
		return false;
	}
	
	/**
	 * get the string array field 
	 * @param _event
	 * @param _id
	 * @return
	 */
	public String[] getStringArrayField(BlackBerryEvent _event,int _id){
		int tCount = _event.countValues(_id);
		if(tCount > 0){
			String[] tResult = new String[tCount];
			for(int i = 0 ;i < tCount;i++){
				tResult[i] = _event.getString(_id, i);
			}
			
			return tResult;
		}
		
		return null;
	}
}
