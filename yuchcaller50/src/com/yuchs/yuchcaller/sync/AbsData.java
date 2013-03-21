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
package com.yuchs.yuchcaller.sync;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * data interface to operate
 * @author tzz
 *
 */
public interface AbsData {

	/**
	 * clear the data to initial state
	 */
	public void clear();
	
	/**
	 * import the data from stream
	 * @param _in
	 */
	public void inputData(InputStream _in)throws Exception;
		
	/**
	 * output data to a stream
	 * @param _os
	 * @throws Excetion
	 */
	public void outputData(OutputStream _os)throws Exception;
	
}
