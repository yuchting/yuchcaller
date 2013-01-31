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

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONObject;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.Event;

public class CalenderSync extends GoogleAPISync{

	/**
	 * the service of this request
	 */
	private final Calendar mService;
	
	public CalenderSync(JSONObject clientJson,Logger _logger) throws Exception {
		super(clientJson,_logger);
		
		mService = new Calendar(mHttpTransport, mJsonFactory,mGoogleCredential);
		
		com.google.api.services.calendar.Calendar.Events.List tList = mService.events().list("primary");
	    	    
		tList.setTimeMin(new DateTime(new Date(System.currentTimeMillis() - 365 * 24 * 3600 * 1000L) , TimeZone.getTimeZone(mTimeZoneID)));

	    Events events = tList.execute();
	    
	    while(true){
	    	for(Event event : events.getItems()){
	    		
	    	}
	    }
	    while (true) {
	      for (Event event : events.getItems()) {
	    	  
	        System.out.println(event.getId() + ":" + event.getSummary());
	        List<String> t_recurrenceList = event.getRecurrence();
	        if(t_recurrenceList != null){
	        	for(String s : t_recurrenceList){
	        		System.out.println("   r:"+s);
	        	}
	        }
	        
	        System.out.println("   "+event.getStart().getDateTime().getValue()+":" + event.getDescription());
	      }
	      
	      String pageToken = events.getNextPageToken();
	      if (pageToken != null && !pageToken.isEmpty()) {
	        events = mService.events().list("primary").setPageToken(pageToken).execute();
	      } else {
	        break;
	      }
	}
	
	@Override
	public String getResult(){
		return "";
	}

}
