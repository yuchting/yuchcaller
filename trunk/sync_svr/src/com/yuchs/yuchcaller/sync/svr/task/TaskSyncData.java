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
package com.yuchs.yuchcaller.sync.svr.task;

import java.util.Date;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.tasks.model.Task;
import com.yuchs.yuchcaller.sync.svr.GoogleAPIData;
import com.yuchs.yuchcaller.sync.svr.GoogleAPISyncData;
import com.yuchs.yuchcaller.sync.svr.calendar.CalendarSync;
import com.yuchs.yuchcaller.sync.svr.contact.ContactSyncData;

public class TaskSyncData extends GoogleAPISyncData {

	public TaskData getData(){return (TaskData)m_APIData;}
	public void setData(TaskData data){m_APIData = data;}
	
	@Override
	public void exportGoogleData(Object g, String timeZoneID) throws Exception {
		Task task = (Task)g;
		
		task.setTitle(getData().summary);
		task.setNotes(getData().note);
		
		if(getData().deadline != 0){
			Date tDate = new Date(getData().deadline);
			task.setDue(new DateTime(tDate, TimeZone.getTimeZone(timeZoneID)));
		}
		
		task.setStatus(getData().status == TaskData.STATUS_COMPLETED ? "completed" : "needsAction");		
	}

	@Override
	public void importGoogleData(Object g, String timeZoneID) throws Exception {
		Task task = (Task)g;
		
		setGID(task.getId());
		setLastMod(task.getUpdated().getValue());
		
		if(getData() == null){
			setData(new TaskData());
		}else{
			getData().clear();
		}
		
		getData().summary	= task.getTitle();
		getData().note		= task.getNotes();
		
		if(task.getDue() != null){
			getData().deadline	= task.getDue().getValue();
		}
		 
		if(task.getStatus() != null && task.getStatus().equals("completed")){
			getData().status = TaskData.STATUS_COMPLETED;
		}
	}

	@Override
	protected GoogleAPIData newData() {
		return new TaskData();
	}

	@Override
	public boolean equals(Object o){
		if(o instanceof TaskSyncData){
			TaskData own = getData();
			TaskData cmp = ((TaskSyncData)o).getData();
			
			if(!ContactSyncData.cmpNameString(own.note, cmp.note)){
				return false;
			}
			
			if(!ContactSyncData.cmpNameString(own.summary, cmp.summary)){
				return false;
			}
			
			if(own.deadline != cmp.deadline){
				return false;
			}
			
			if(own.status == TaskData.STATUS_COMPLETED && cmp.status != TaskData.STATUS_COMPLETED){
				return false;
			}
			
			if(cmp.status == TaskData.STATUS_COMPLETED && own.status != TaskData.STATUS_COMPLETED){
				return false;
			}
			
			return true;
		}
		
		return false;
	}
}
