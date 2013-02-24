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

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import javax.microedition.pim.Event;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;
import javax.microedition.pim.RepeatRule;

import net.rim.blackberry.api.pdap.BlackBerryEvent;

import com.yuchs.yuchcaller.sync.AbsData;
import com.yuchs.yuchcaller.sync.AbsSyncData;
import com.yuchs.yuchcaller.sync.SyncMain;

public class CalendarSyncData extends AbsSyncData{
	
	/**
	 * ste the data to Calendar data
	 * @param data
	 */
	public void setData(CalendarData data){m_data = data;}
	
	/**
	 * get CalendarData
	 * @return
	 */
	public CalendarData getData(){return (CalendarData)m_data;}
	
	/**
	 * need calculate md5 by minTime
	 * @param minTime
	 * @return
	 */
	protected boolean needCalculateMD5(long minTime){
		if(getData() == null){
			return false;
		}
		
		return getData().start > minTime || getData().repeat_type.length() != 0;
	}
	
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
	 * @param list		EventList
	 */
	public void importData(PIMItem _item,PIMList _list)throws Exception{
		
		Event event = (Event)_item;
		
		if(getData() == null){
			setData(new CalendarData());
		}else{
			getData().clear();
		}
				
		// set the repeat information
		RepeatRule repeat = event.getRepeat();
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
				tRecurring += ";INTERVAL=" + repeat.getInt(RepeatRule.INTERVAL);
			}catch(Exception e){}
			
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
			
			getData().repeat_type = tRecurring;
		}
		
		// set the fields information
		int[] fieldIds = event.getFields();
		int id;
		
		for(int index = 0; index < fieldIds.length; ++index){
			
			id = fieldIds[index];
			
			switch(id){
			case Event.UID:
				bID = getStringField(event,id);
				break;
			case Event.SUMMARY:
				getData().summary = getStringField(event, id);
				break;
			case Event.START:
				getData().start = getDateField(event, id);
				break;
			case Event.END:
				getData().end = getDateField(event, id);
				break;
			case Event.LOCATION:
				getData().location = getStringField(event,id);
				break;
			case Event.NOTE:
				getData().note = getStringField(event,id);
				break;
			case Event.ALARM:
				getData().alarm = getIntField(event,id);
				break;
			case BlackBerryEvent.ALLDAY:
				getData().allDay = getBooleanField(event, id);
				break;
			case BlackBerryEvent.ATTENDEES:
				getData().attendees = getStringArrayField(_list,event,id);
				break;
			case BlackBerryEvent.FREE_BUSY:
				getData().free_busy = getIntField(event, id);
				break;
			case Event.CLASS:
				int cls = getIntField(event, id);
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
				getData().event_class = cls;
				break;
			}
		}
	}
	
	/**
	 * export the calendar to the blackberry event
	 * @param event
	 * @throws Exception
	 */
	public void exportData(PIMItem _item,PIMList _list)throws Exception{
		Event event = (Event)_item;
		
		if(getData().repeat_type.length() > 0){
			// add the repeat rule
			//
			String repeat = getData().repeat_type;
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
			
			event.setRepeat(repeatRule);
		}
		
		setStringField(_list,event, Event.SUMMARY,getData().summary);
		setDateField(_list,event, Event.START,getData().start);
		setDateField(_list,event, Event.END,getData().end);
		
		setStringField(_list,event, Event.LOCATION,getData().location);
		setStringField(_list,event, Event.NOTE,getData().note);
		
		if(getData().alarm > 0){
			setIntField(_list,event, Event.ALARM,getData().alarm);
		}
		
		if(getData().allDay){
			setBooleanField(_list,event, BlackBerryEvent.ALLDAY,getData().allDay);
		}
		
		setStringArrayField(_list,event, BlackBerryEvent.ATTENDEES,getData().attendees);
		setIntField(_list,event,BlackBerryEvent.FREE_BUSY,getData().free_busy);
		
		int id = BlackBerryEvent.CLASS_PRIVATE;
		switch(getData().event_class){
		case CalendarData.CLASS_PUBLIC:
			id = BlackBerryEvent.CLASS_PUBLIC;
			break;
		case CalendarData.CLASS_CONFIDENTIAL:
			id = BlackBerryEvent.CLASS_CONFIDENTIAL;
			break;
		}
		
		setIntField(_list, event, BlackBerryEvent.CLASS, id);		
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
					
					Vector days = SyncMain.splitStr(value,',');
					
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
				Vector date = SyncMain.splitStr(ruleStr, ',');
				for(int i = 0;i < date.size();i++){
					rule.addExceptDate(parseR2445FormatData(date.elementAt(i).toString()));
				}
			}catch(Exception e){}
		}
	}
	
	/**
	 * new a data for calendar
	 * @return
	 */
	protected AbsData newData(){
		return new CalendarData();
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
	
	
}
