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
import java.io.OutputStream;

import com.yuchs.yuchcaller.sync.svr.GoogleAPIData;
import com.yuchs.yuchcaller.sync.svr.sendReceive;

public class TaskData implements GoogleAPIData {

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
