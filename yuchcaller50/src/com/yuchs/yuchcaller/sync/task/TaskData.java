package com.yuchs.yuchcaller.sync.task;

import java.io.InputStream;
import java.io.OutputStream;

import com.yuchs.yuchcaller.sendReceive;
import com.yuchs.yuchcaller.sync.AbsData;

public class TaskData implements AbsData {
	
	/**
	 * max number task to sync
	 */
	public static final int	MAX_SYNC_TASK = 500;

	public static final int STATUS_NOT_STARTED = 0;
	public static final int STATUS_IN_PROGRESS = 1;
	public static final int STATUS_COMPLETED = 2;
	public static final int STATUS_WAITING = 3;
	public static final int STATUS_DEFERRED = 4;
	
	/**
	 * status of this task
	 */
	public int status = STATUS_NOT_STARTED;
	
	/**
	 * summary of this task
	 */
	public String summary = null;
	
	/**
	 * note of this task
	 */
	public String note		= null;
	
	/**
	 * deadline date
	 */
	public long deadline	= 0;	
	
	public void clear() {
		status	= STATUS_NOT_STARTED;
		summary = null;
		note	= null;
		deadline= 0;
	}

	public void inputData(InputStream in) throws Exception {
		status	= in.read();
		summary	= sendReceive.ReadString(in);
		note	= sendReceive.ReadString(in);
		deadline= sendReceive.ReadLong(in);
	}

	public void outputData(OutputStream os) throws Exception {
		os.write(status);
		sendReceive.WriteString(os,summary);
		sendReceive.WriteString(os, note);
		sendReceive.WriteLong(os, deadline);
	}

}
