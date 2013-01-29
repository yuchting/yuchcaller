package com.yuchs.yuchcaller.sync;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.pim.PIM;

import net.rim.blackberry.api.pdap.BlackBerryEvent;
import net.rim.blackberry.api.pdap.BlackBerryEventList;
import net.rim.device.api.crypto.MD5Digest;

import com.yuchs.yuchcaller.YuchCaller;
import com.yuchs.yuchcaller.YuchCallerProp;

public class SyncMain {
	
	public YuchCaller	m_mainApp;
	
	private boolean	m_isSyncing = false;
	
	// calender sync list
	private Vector		m_calenderSyncList = new Vector();
	
	public SyncMain(YuchCaller _mainApp){
		m_mainApp = _mainApp;
	}
	
	
	
	/**
	 * get the md5 string
	 * @param _org
	 * @return
	 */
	public static String md5(String _org){
		
		byte[] bytes = null;
		try{
			bytes = _org.getBytes("UTF-8");
		}catch(Exception e){
			bytes = _org.getBytes();
		}
		
		return md5(bytes);
		
	}
	
	/**
	 * get the md5
	 * @param _data
	 * @return
	 */
	public static String md5(byte[] _data){
		MD5Digest digest = new MD5Digest();
		
		digest.update(_data, 0, _data.length);

		byte[] md5 = new byte[digest.getDigestLength()];
		digest.getDigest(md5, 0, true);
		
		return convertToHex(md5);
	}

	/**
	 * convert bytes to hex string
	 * @param data
	 * @return
	 */
	public static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }
	
	public void startSync(final boolean _test){
		if(m_isSyncing){
			return ;
		}
		
		(new Thread(){
			public void run(){
				startSyncImpl(_test);
			}
		}).start();
	}
	
	private void startSyncImpl(final boolean _test){
		
		
		
		m_isSyncing = false;
	}
	
	/**
	 * read the calender information from bb calender
	 */
	private void readBBCalender(){
		
		try{
			
			BlackBerryEventList t_events = (BlackBerryEventList)PIM.getInstance().openPIMList(PIM.EVENT_LIST,PIM.READ_ONLY);

			Enumeration t_allEvents = t_events.items();
			
			Vector t_eventList = new Vector();
		    if(t_allEvents != null){
			    while(t_allEvents.hasMoreElements()) {
			    	t_eventList.addElement(t_allEvents.nextElement());
			    }
		    }
		    
		    m_calenderSyncList.removeAllElements();
		    
		    for(int i = 0;i < t_eventList.size();i++){
		    	
		    	BlackBerryEvent event = (BlackBerryEvent)t_eventList.elementAt(i);
		    	
		    	CalenderSyncData syncData = new CalenderSyncData();
		    	syncData.importData(event);
		    	
		    	m_calenderSyncList.addElement(syncData);
		    }
		    
		}catch(Exception e){
			m_mainApp.SetErrorString("RBBC", e);
		}
	}
	
	static final String fsm_calender_init_data 		= "calender.data";
	static final String fsm_calender__back_init_data 	= "~calender.data";
	
	static final String fsm_directory			= YuchCallerProp.fsm_rootPath_back + "YuchCaller/";
	
	static final String fsm_calenderFilename 		= fsm_directory + fsm_calender_init_data;
	static final String fsm_backcalenderFilename	= fsm_directory + fsm_calender__back_init_data;
	
	/**
	 * check the sync file
	 */
	private void readWriteSyncFile(boolean _read){
		
		try{
			YuchCallerProp.preWriteReadIni(_read, fsm_backcalenderFilename, fsm_calenderFilename, fsm_calender__back_init_data, fsm_calender_init_data);
		}catch(Exception e){
			m_mainApp.SetErrorString("SRWSF", e);
		}
		
		try{
			FileConnection fc = (FileConnection) Connector.open(fsm_calenderFilename,Connector.READ_WRITE);
			try{
				if(!fc.exists()){
					// construct the calender sync name
					//
					readBBCalender();
				}
			}finally{
				fc.close();
			}
		}catch(Exception e){
			
		}
	}
}
