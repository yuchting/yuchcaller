package com.yuchs.yuchcaller.sync.svr.task;

import java.io.InputStream;

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
		TasksOperations.List requestList = mService.tasks().list("@default");
		//requestList.setMaxResults(arg0)
		com.google.api.services.tasks.model.Tasks tasks = mService.tasks().list("@default").execute();

		for(Task task : tasks.getItems()){
			
		}

	}
	
	@Override
	protected void deleteGoogleData(GoogleAPISyncData g) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getGoogleDataId(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected long getGoogleDataLastMod(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected boolean isFristSyncSameData(Object o, GoogleAPISyncData g)throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected GoogleAPISyncData newSyncData() {
		return new TaskSyncData();
	}

	

	@Override
	protected Object updateGoogleData(Object o, GoogleAPISyncData g)throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object uploadGoogleData(GoogleAPISyncData g) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
