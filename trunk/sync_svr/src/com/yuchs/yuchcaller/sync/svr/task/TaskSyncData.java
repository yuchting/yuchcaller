package com.yuchs.yuchcaller.sync.svr.task;

import com.yuchs.yuchcaller.sync.svr.GoogleAPIData;
import com.yuchs.yuchcaller.sync.svr.GoogleAPISyncData;

public class TaskSyncData extends GoogleAPISyncData {

	@Override
	public void exportGoogleData(Object g, String timeZoneID) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void importGoogleData(Object g, String timeZoneID) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected GoogleAPIData newData() {
		return new TaskData();
	}

}
