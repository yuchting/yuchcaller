package com.yuchs.yuchcaller.sync;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.RepeatRule;

import net.rim.blackberry.api.pdap.BlackBerryEvent;

import com.yuchs.yuchcaller.sendReceive;

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
		
	private static final int[]	WeekConstant = 
	{
		RepeatRule.MONDAY,RepeatRule.TUESDAY,RepeatRule.WEDNESDAY,RepeatRule.THURSDAY,RepeatRule.FRIDAY,RepeatRule.SATURDAY,
	};
	
	private static final String[] WeekSign = 
	{
		"MO","TU","WE","TH","FR","SU"	
	};
	
	private static final String[] RepeatTypeStr = 
	{
		"DAILY","WEEKLY","MONTHLY","YEARLY"
	};
	
	private static final int[] RepeatType = 
	{
		RepeatRule.DAILY,RepeatRule.WEEKLY,RepeatRule.MONTHLY,RepeatRule.YEARLY,
	};
	
	/**
	 * import blackberry event
	 * @param event
	 */
	public void importData(Event _event)throws Exception{
				
		if(m_calendarData == null){
			m_calendarData = new CalendarData();
		}
				
		// set the repeat information
		RepeatRule repeat = _event.getRepeat();
		if(repeat != null){
						
			// http://www.ietf.org/rfc/rfc2445
			//
			String tRecurring = "RRULE:";
			
			int re = repeat.getInt(RepeatRule.FREQUENCY);
			
			for(int i = 0;i < RepeatType.length;i++){
				if(RepeatType[i] == re){
					tRecurring += "FREQ=" + RepeatTypeStr[i]; 
					break;
				}
			}
			
			try{
				tRecurring += ";BYDAY=" + repeat.getInt(RepeatRule.DAY_IN_MONTH);
			}catch(Exception e){}
			
			try{
				tRecurring += ";BYDAY=" + repeat.getInt(RepeatRule.DAY_IN_YEAR);
			}catch(Exception e){}
			
			try{
				tRecurring += ";BYWEEKNO=" + repeat.getInt(RepeatRule.WEEK_IN_MONTH);
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
				
				String tOut = "";
				
				for(int i = 0;i < WeekConstant.length;i++){
						
					if((WeekConstant[i] & tDayInWeek) != 0){
						
						if(tOut.length() != 0){
							tOut += ",";
						}
						
						tOut += WeekSign[i];
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
			case Event.SUMMARY:
				m_calendarData.summary = getStringField(_event, id);
				break;
			case Event.START:
				m_calendarData.start = getDateField(_event, id);
				break;
			case Event.END:
				m_calendarData.end = getDateField(_event, id);
				break;
			case Event.LOCATION:
				m_calendarData.location = getStringField(_event,id);
				break;
			case Event.NOTE:
				m_calendarData.note = getStringField(_event,id);
				break;
			case Event.ALARM:
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
			case Event.CLASS:
				int cls = getIntField(_event, id);
				switch(cls){
				case Event.CLASS_CONFIDENTIAL:
					cls = CalendarData.CLASS_CONFIDENTIAL;
					break;
				case Event.CLASS_PRIVATE:
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
	
	/**
	 * export the calendar to the blackberry event
	 * @param event
	 * @throws Exception
	 */
	public void exportData(Event _event,EventList _eventList)throws Exception{
		
		if(m_calendarData.repeat_type.length() > 0){
			// add the repeat rule
			//
			String repeat = m_calendarData.repeat_type;
			RepeatRule repeatRule = new RepeatRule();
			
			while(true){
				int idx = repeat.indexOf('\n');
				if(idx == -1){
					parseRULE(repeat,repeatRule);
					break;
				}else{
					parseRULE(repeat.substring(0,idx),repeatRule);
					repeat = repeat.substring(idx + 1);
				}
			} 
			
			_event.setRepeat(repeatRule);
		}
		
		setStringField(_eventList,_event, Event.SUMMARY,m_calendarData.summary);
		setDateField(_eventList,_event, Event.START,m_calendarData.start);
		setDateField(_eventList,_event, Event.END,m_calendarData.end);
		
		setStringField(_eventList,_event, Event.LOCATION,m_calendarData.location);
		setStringField(_eventList,_event, Event.NOTE,m_calendarData.note);
		
		if(m_calendarData.alarm > 0){
			setIntField(_eventList,_event, Event.ALARM,m_calendarData.alarm);
		}
		
		if(m_calendarData.allDay){
			setBooleanField(_eventList,_event, BlackBerryEvent.ALLDAY,m_calendarData.allDay);
		}
		
		setStringArrayField(_eventList,_event, BlackBerryEvent.ATTENDEES,m_calendarData.attendees);
		setIntField(_eventList,_event,BlackBerryEvent.FREE_BUSY,m_calendarData.free_busy);
		
		int id = BlackBerryEvent.CLASS_PRIVATE;
		switch(m_calendarData.event_class){
		case CalendarData.CLASS_PUBLIC:
			id = BlackBerryEvent.CLASS_PUBLIC;
			break;
		case CalendarData.CLASS_CONFIDENTIAL:
			id = BlackBerryEvent.CLASS_CONFIDENTIAL;
			break;
		}
		
		setIntField(_eventList, _event, BlackBerryEvent.CLASS, id);		
	}
	
	private void parseRULE(String ruleStr,RepeatRule rule){
		
		if(ruleStr.startsWith("RRULE:")){
			
			ruleStr = ruleStr.substring(6);
			
			Hashtable tables = parseRULEDetail(ruleStr);
			
			Enumeration enum = tables.keys();
			int repeat = RepeatRule.DAILY;
			
			while(enum.hasMoreElements()){
				
				String key = (String)enum.nextElement();
				String value = (String)tables.get(key);
				
				if(key.equalsIgnoreCase("FREQ")){
					
					for(int i = 0;i < RepeatTypeStr.length;i++){
						if(RepeatTypeStr[i].equalsIgnoreCase(value)){
							rule.setInt(RepeatRule.FREQUENCY, RepeatType[i]);
							repeat = RepeatType[i];							
							break;
						}
					}
					
				}else if(key.equalsIgnoreCase("BYDAY")){
					
					Vector days = splitStr(value,',');
					
					if(!days.isEmpty()){
						
						int tDayInWeek = 0;
						
						for(int i = 0;i < days.size();i++ ){
						
							String day = (String)days.elementAt(i);
							
							try{
								int dayno = Integer.parseInt(day);
								
								int id;
								switch(repeat){
								case RepeatRule.MONTHLY:
									id = RepeatRule.DAY_IN_MONTH;
									break;
								default:
									id = RepeatRule.DAY_IN_YEAR;
									break;
								}
								
								setRepeatRuleIntField(rule, id, dayno);
								
							}catch(Exception e){
								
								for(int idx = 0;idx < WeekSign.length ;idx ++){
									if(WeekSign[i].equalsIgnoreCase(day)){
										tDayInWeek |= WeekConstant[idx];
									}
								}
							}
						}
						
						if(tDayInWeek != 0){
							setRepeatRuleIntField(rule, RepeatRule.DAY_IN_WEEK,tDayInWeek);
						}						
					}
					
				}else if(key.equalsIgnoreCase("BYWEEKNO")){
					
					setRepeatRuleIntField(rule,RepeatRule.WEEK_IN_MONTH, value);	
					
				}else if(key.equalsIgnoreCase("BYMONTH")){
					
					setRepeatRuleIntField(rule,RepeatRule.MONTH_IN_YEAR, value);
					
				}else if(key.equalsIgnoreCase("INTERVAL")){
					
					setRepeatRuleIntField(rule,RepeatRule.INTERVAL, value);
				}else if(key.equalsIgnoreCase("UNTIL")){
					
					long time = parseR2445FormatData(value);
					setRepeatRuleDateField(rule, RepeatRule.END, time);
					
				}else if(key.equalsIgnoreCase("COUNT")){
					setRepeatRuleIntField(rule, RepeatRule.COUNT, value);
				}
				
			}
			
		}else if(ruleStr.startsWith("EXDATE:")){
			ruleStr = ruleStr.substring(7);
			
			try{
				Vector date = splitStr(ruleStr, ',');
				for(int i = 0;i < date.size();i++){
					rule.addExceptDate(parseR2445FormatData(date.elementAt(i).toString()));
				}
			}catch(Exception e){}
		}
	}
	
	/**
	 * set the repeat rull class int field
	 * @param _rule
	 * @param _id
	 * @param _value
	 */
	private static void setRepeatRuleIntField(RepeatRule _rule,int _id,String _value){
		try{
			int v = Integer.parseInt(_value);
			_rule.setInt(_id, v);
		}catch(Exception e){}
	}
	
	/**
	 * set the repeat rule class date field
	 * @param _rule
	 * @param _id
	 * @param _value
	 */
	private static void setRepeatRuleDateField(RepeatRule _rule,int _id,String _value){
		try{
			long v = Long.parseLong(_value);
			_rule.setDate(_id, v);
		}catch(Exception e){}
	}
	
	/**
	 * set the repeat rull class int field
	 * @param _rule
	 * @param _id
	 * @param _value
	 */
	private static void setRepeatRuleIntField(RepeatRule _rule,int _id,int _value){
		try{
			_rule.setInt(_id, _value);
		}catch(Exception e){}
	}
	
	/**
	 * set the repeat rule class date field
	 * @param _rule
	 * @param _id
	 * @param _value
	 */
	private static void setRepeatRuleDateField(RepeatRule _rule,int _id,long _value){
		try{
			_rule.setDate(_id, _value);
		}catch(Exception e){}
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
	
	/**
	 * parse rule for 
	 * @param ruleStr
	 * @return
	 */
	private static Hashtable parseRULEDetail(String ruleStr){
		Hashtable table = new Hashtable();
		
		while(ruleStr.length() > 0){
			
			int idx = ruleStr.indexOf('=');
			
			if(idx != -1){
				
				String key		= ruleStr.substring(0,idx);
				ruleStr			= ruleStr.substring(idx + 1);
				
				String value;
				idx = ruleStr.indexOf(';');
				
				if(idx != -1){
					value = ruleStr.substring(0,idx);
					ruleStr = ruleStr.substring(idx + 1);
				}else{
					value = ruleStr;
				}
				
				table.put(key, value);
				
				if(idx == -1){
					break;
				}
				
			}else{
				break;
			}
		}
		
		return table;
	}
	
	/**
	 * spliter string
	 * @param s
	 * @param split
	 * @return
	 */
	public static Vector splitStr(String s,char split){
		
		Vector tResult = new Vector();
		
		StringBuffer sb = new StringBuffer();
		
		for(int i = 0;i < s.length();i++){
			char c = s.charAt(i);		
			if(c == split){
				if(sb.length() == 0){
					continue;
				}
				
				tResult.addElement(sb.toString());
				
				sb = new StringBuffer();
			}else{
				sb.append(c);
			}
		}
		
		if(sb.length() > 0){
			tResult.addElement(sb.toString());
		}
		
		return tResult;
	}
	
	static Calendar sm_calendar = Calendar.getInstance();
	static Date		sm_timeDate = new Date();
	
	/**
	 * get the format string by date
	 * @param date
	 * @return
	 */
	public static String getR2445FormatDate(long date){
		
		synchronized(sm_calendar){

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
	}
	
	private static String parseTime(int time){
		
		if(time < 10){
			return "0" + Integer.toString(time);
		}else{
			return Integer.toString(time);
		}
	}
	
	/**
	 * parse the rfc 2445 date format 
	 * @param _value
	 * @return
	 */
	public static long parseR2445FormatData(String _value){
		
		if(_value.length() < 8){ // "19901001".length()
			return 0;
		}
		
		String year		= _value.substring(0,4);
		String month	= _value.substring(4,6);
		String day		= _value.substring(6,8);
		
		String hour		= "0";
		String minutes	= "0";
		String second	= "0";
		
		if(_value.length() > 15){ // "19901001T201010".length()
			hour	= _value.substring(9,11);
			minutes	= _value.substring(11,14);
			minutes	= _value.substring(14,16);
		}
		
		int y = 1970;
		int mon = 0;
		int d = 0;
		
		int h = 0;
		int mm = 0;
		int ss = 0;
		
		try{y = Integer.parseInt(year);}catch(Exception e){}
		try{mon = Integer.parseInt(month);}catch(Exception e){}
		try{d = Integer.parseInt(day);}catch(Exception e){}
		
		try{h = Integer.parseInt(hour);}catch(Exception e){}
		try{mm = Integer.parseInt(minutes);}catch(Exception e){}
		try{ss = Integer.parseInt(second);}catch(Exception e){}
		
		long time;
		synchronized(sm_calendar){
			sm_calendar.setTime(sm_timeDate);
			
			sm_calendar.set(Calendar.YEAR, y);
			sm_calendar.set(Calendar.MONTH, mon);
			sm_calendar.set(Calendar.DAY_OF_MONTH, d);
			
			sm_calendar.set(Calendar.HOUR, h);
			sm_calendar.set(Calendar.MINUTE, mm);
			sm_calendar.set(Calendar.SECOND, ss);
			
			time = sm_calendar.getTime().getTime();
		}
		
		if(_value.charAt(_value.length() - 1) != 'Z'){
			time += TimeZone.getDefault().getRawOffset() * 3600 * 1000;
		}
		
		return time;
	}
	
	/**
	 * get the blackberry event string 
	 * @param _event
	 * @param _id
	 * @return
	 */
	public static String getStringField(Event _event,int _id){
		int tCount = _event.countValues(_id);
		if(tCount > 0){
			return _event.getString(_id, 0);
		}
		
		return "";
	}
	
	/**
	 * set the event id by string
	 * @param _list 
	 * @param _event
	 * @param _id
	 * @param _value
	 */
	public static void setStringField(EventList _list,Event _event,int _id,String _value){
		if(_list.isSupportedField(_id)){
			
			if(_event.countValues(_id) > 0){
				if(_value != null && _value.length() > 0){
					_event.setString(_id,0,Event.ATTR_NONE,_value);
				}else{
					_event.removeValue(_id,0);
				}
			}else{
				if(_value != null && _value.length() > 0){
					_event.addString(_id,Event.ATTR_NONE,_value);
				}				
			}
		}
	}
	
	/**
	 * get the the long(for date) field
	 * @param _event
	 * @param _id
	 * @return
	 */
	public static long getDateField(Event _event,int _id){
		int tCount = _event.countValues(_id);
		if(tCount > 0){
			return _event.getDate(_id, 0);
		}
		
		return 0;
	}
	
	/**
	 * set the date field for event
	 * @param _list
	 * @param _event
	 * @param _id
	 * @param _value
	 */
	public static void setDateField(EventList _list,Event _event,int _id,long _value){
		
		if(_list.isSupportedField(_id)){
			if(_event.countValues(_id) > 0){
				_event.setDate(_id,0,Event.ATTR_NONE,_value);
			}else{
				_event.addDate(_id,Event.ATTR_NONE,_value);
			}
			
		}
	}
	
	/**
	 * set the date field for event by data value
	 * @param _list
	 * @param _event
	 * @param _id
	 * @param _value
	 */
	public static void setDateField(EventList _list,Event _event,int _id,String _value){
		
		try{
			long v = Long.parseLong(_value);
			setDateField(_list,_event,_id,v);
		}catch(Exception e){}
	}
	
	
	/**
	 * get the integer field for the event
	 * @param _event
	 * @param _id
	 * @return
	 */
	public static int getIntField(Event _event,int _id){
		int tCount = _event.countValues(_id);
		if(tCount > 0){
			return _event.getInt(_id, 0);
		}
		
		return 0;
	}
	
	/**
	 * set the int value of this event
	 * @param _list
	 * @param _event
	 * @param _id
	 * @param _value
	 */
	public static void setIntField(EventList _list,Event _event,int _id,int _value){
		
		if(_list.isSupportedField(_id)){
			if(_event.countValues(_id) > 0){
				_event.setInt(_id,0,Event.ATTR_NONE,_value);
			}else{
				_event.addInt(_id,Event.ATTR_NONE,_value);
			}
		}
	}
	
	/**
	 * set the int field by int string
	 * @param _list
	 * @param _event
	 * @param _id
	 * @param _value
	 */
	public static void setIntField(EventList _list,Event _event,int _id,String _value){
		
		try{
			int v = Integer.parseInt(_value);
			setIntField(_list,_event,_id,v);
		}catch(Exception e){}
	}
	
	/**
	 * get the boolean field for the event
	 * @param _event
	 * @param _id
	 * @return
	 */
	public static boolean getBooleanField(Event _event,int _id){
		int tCount = _event.countValues(_id);
		if(tCount > 0){
			return _event.getBoolean(_id, 0);
		}
		
		return false;
	}
	
	/**
	 * set the boolean field
	 * @param _list
	 * @param _event
	 * @param _id
	 * @param _value
	 */
	public static void setBooleanField(EventList _list,Event _event,int _id,boolean _value){
		if(_list.isSupportedField(_id)){
			if(_event.countValues(_id) > 0){
				_event.setBoolean(_id, 0, Event.ATTR_NONE, _value);
			}else{
				_event.addBoolean(_id, Event.ATTR_NONE, _value);
			}
		}
	}
	
	/**
	 * get the string array field 
	 * @param _event
	 * @param _id
	 * @return
	 */
	public static String[] getStringArrayField(Event _event,int _id){
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
	
	/**
	 * set the String array field
	 * @param _list
	 * @param _event
	 * @param _id
	 * @param _value
	 */
	public static void setStringArrayField(EventList _list,Event _event,int _id,String[] _value){
		if(_list.isSupportedField(_id)){
			if(_event.countValues(_id) > 0){
				if(_value != null && _value.length > 0){
					_event.setStringArray(_id, 0, Event.ATTR_NONE, _value);
				}else{
					_event.removeValue(_id,0);
				}				
			}else{
				if(_value != null && _value.length > 0){
					_event.addStringArray(_id, Event.ATTR_NONE, _value);
				}				
			}
		}
	}
}
