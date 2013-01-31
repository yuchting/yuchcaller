package com.yuchs.yuchcaller.sync;

import java.io.ByteArrayOutputStream;

import javax.microedition.pim.Event;
import javax.microedition.pim.RepeatRule;

import net.rim.blackberry.api.pdap.BlackBerryEvent;

public class CalenderSyncData {
	
	// the bb system calender UID
	private String bID = null;
	
	// the google calender UID
	private String gID = null;
	
	// this sync solt data md5
	private String md5 = null;
	
	// calender data
	private CalenderData	m_calenderData = null;
	
	public String getBBID(){
		return bID;
	}
	
	public void setGID(String _id){gID = _id;}
	public String getGID(){	return gID;}
	
	public String getMD5(){return md5;}
	
	public CalenderData getData(){
		return m_calenderData;
	}
	
	/**
	 * import blackberry event
	 * @param event
	 */
	public void importData(BlackBerryEvent _event)throws Exception{
		
		if(m_calenderData == null){
			m_calenderData = new CalenderData();
		}		
		
		// set the repeat information
		RepeatRule repeat = _event.getRepeat();
		if(repeat != null){
			
//			int re = repeat.getInt(RepeatRule.FREQUENCY);
//			switch(re){
//			case RepeatRule.DAILY:
//				re = CalenderData.REPEAT_DAILY;
//				break;
//			case RepeatRule.WEEKLY:
//				re = CalenderData.REPEAT_WEEKLY;
//				break;
//			case RepeatRule.MONTHLY:
//				re = CalenderData.REPEAT_MONTHLY;
//				break;
//			case RepeatRule.YEARLY:
//				re = CalenderData.REPEAT_YEARLY;
//				break;
//			default:
//				re = CalenderData.REPEAT_NO;
//				break;
//			}
			
			m_calenderData.repeat_type = repeat.toString();
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
				m_calenderData.summary = getStringField(_event, id);
				break;
			case BlackBerryEvent.START:
				m_calenderData.start = getDateField(_event, id);
				break;
			case BlackBerryEvent.END:
				m_calenderData.end = getDateField(_event, id);
				break;
			case BlackBerryEvent.LOCATION:
				m_calenderData.location = getStringField(_event,id);
				break;
			case BlackBerryEvent.NOTE:
				m_calenderData.note = getStringField(_event,id);
				break;
			case BlackBerryEvent.ALARM:
				m_calenderData.alarm = getIntField(_event,id);
				break;
			case BlackBerryEvent.ALLDAY:
				m_calenderData.allDay = getBooleanField(_event, id);
				break;
			case BlackBerryEvent.ATTENDEES:
				m_calenderData.attendees = getStringArrayField(_event, id);
				break;
			case BlackBerryEvent.FREE_BUSY:
				m_calenderData.free_busy = getIntField(_event, id);
				break;
			case BlackBerryEvent.CLASS:
				int cls = getIntField(_event, id);
				switch(cls){
				case BlackBerryEvent.CLASS_CONFIDENTIAL:
					cls = CalenderData.CLASS_CONFIDENTIAL;
					break;
				case BlackBerryEvent.CLASS_PRIVATE:
					cls = CalenderData.CLASS_PRIVATE;
					break;
				default:
					cls = CalenderData.CLASS_PUBLIC;
					break;
				}
				m_calenderData.event_class = cls;
				break;
			}
			
//			if(_test){
//
//				if(id == Event.UID){
//					event.setString(id, 0,Event.STRING, "GoogleCalenderID");
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
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try{
			m_calenderData.outputData(os);
			md5 = SyncMain.md5(os.toByteArray());
		}finally{
			os.close();
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
