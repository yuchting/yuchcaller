package com.yuchs.yuchcaller.sync.svr;

import java.io.InputStream;
import java.io.OutputStream;

public interface GoogleAPIData {
	
	public void inputData(InputStream _in)throws Exception;
	
	public void outputData(OutputStream _os)throws Exception;
}
