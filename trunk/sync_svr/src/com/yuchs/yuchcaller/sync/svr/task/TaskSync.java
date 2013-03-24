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

import java.io.InputStream;
import java.util.Vector;

import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.Tasks.TasksOperations;
import com.google.api.services.tasks.model.Task;
import com.yuchs.yuchcaller.sync.svr.GoogleAPISync;
import com.yuchs.yuchcaller.sync.svr.GoogleAPISyncData;
import com.yuchs.yuchcaller.sync.svr.Logger;

public class TaskSync extends GoogleAPISync {
	

	private Tasks mService;
	
	public TaskSync(InputStream in, Logger logger) throws Exception {
		super(in, logger);
		
		mService = new Tasks(mHttpTransport, mJsonFactory, mGoogleCredential);
	}

	@Override
	public void readSvrGoogleData() throws Exception {
		if(fetchFormerEvent()){
			return ;
		}
		
		if(mSvrSyncDataList == null){
			mSvrSyncDataList = new Vector<Object>();
		}
		
		TasksOperations.List requestList = mService.tasks().list("@default");
		requestList.setMaxResults((long)TaskData.MAX_SYNC_TASK);
				
		com.google.api.services.tasks.model.Tasks tasks = requestList.execute();
		
		while (tasks != null && tasks.getItems() != null) {
			
			for(Task task : tasks.getItems()){
				if(task.getTitle() == null || task.getTitle().length() <= 0){
					continue;
				}
				long mod = getTaskLastMod(task);
				if(mod == 0){
					continue;
				}
				
				for(int i = 0;i < mSvrSyncDataList.size();i++){
					Task t = (Task)mSvrSyncDataList.get(i);
					if(mod > getTaskLastMod(t)){
										
						mSvrSyncDataList.insertElementAt(task, i);
						
						task = null;
						break;
					}
				}
				
				if(task != null){
					mSvrSyncDataList.add(task);
				}
			}			
			
			String pageToken = tasks.getNextPageToken();
			if (pageToken != null && !pageToken.isEmpty()) {
				tasks = mService.tasks().list("@default").setPageToken(pageToken).execute();
			}else{
				break;
			}			
		}
		
		StringBuffer sb = new StringBuffer();		
		for(int i = 0;i < mSvrSyncDataList.size();i++){
			Task t = (Task)mSvrSyncDataList.get(i);
			
			long tLastMod = getTaskLastMod(t);
			sb.append(tLastMod);
		}
		
		mAllSvrSyncDataMD5  = getMD5(sb.toString());
		storeFormerEvent();
		
		mLogger.LogOut(mYuchAcc + " Load Task Number:" + mSvrSyncDataList.size());
	}
	
	/**
	 * get the task last modified time
	 * @param t
	 * @return
	 */
	public static long getTaskLastMod(Task t){
		if(t.getUpdated() != null){
			return t.getUpdated().getValue();
		}
		
		return 0;
	}
	
	@Override
	protected void deleteGoogleData(GoogleAPISyncData g) throws Exception {
		mService.tasks().delete("@default", g.getGID()).execute();
	}

	@Override
	protected String getGoogleDataId(Object o) {
		Task t = (Task)o;
		return t.getId();
	}

	@Override
	protected long getGoogleDataLastMod(Object o) {
		Task t = (Task)o;
		return t.getUpdated().getValue();
	}

	@Override
	protected boolean isFristSyncSameData(Object o, GoogleAPISyncData g)throws Exception {
		TaskSyncData a = (TaskSyncData)g;
		
		TaskSyncData b = new TaskSyncData();
		b.importGoogleData(o, mTimeZoneID);
		
		return a.equals(b);
	}

	@Override
	protected GoogleAPISyncData newSyncData() {
		return new TaskSyncData();
	}

	@Override
	protected Object updateGoogleData(Object o, GoogleAPISyncData g)throws Exception {
		Task task = (Task)o;
		g.exportGoogleData(task, mTimeZoneID);
		
		task = mService.tasks().update("@default", task.getId(), task).execute();
		return task;
	}

	@Override
	protected Object uploadGoogleData(GoogleAPISyncData g) throws Exception {
		Task task = new Task();
		g.exportGoogleData(task, mTimeZoneID);
		
		// get the previous task
		String previous = null;
		if(!mSvrSyncDataList.isEmpty()){
			Task rearTask = (Task)mSvrSyncDataList.get(mSvrSyncDataList.size() - 1);
			previous = rearTask.getId();
		}
		
		TasksOperations.Insert insert = mService.tasks().insert("@default", task);
		if(previous != null){
			insert.setPrevious(previous);
		}
		
		return insert.execute();
	}
}
