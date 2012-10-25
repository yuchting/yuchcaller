package com.yuchs.yuchcaller;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.Persistable;

public class YuchCallerProp implements Persistable {
	
	//Hash
	private static long PERSISTENCE_ID = 5762556588347221845L;
	
    //Persistent object wrapping the effective properties instance
    private static PersistentObject sm_store;
    
    private static YuchCallerProp sm_thisProp;
    
    //Ensure that an effective properties set exists on startup.
	static {
		try{
			sm_store = PersistentStore.getPersistentObject(PERSISTENCE_ID);
		    synchronized (sm_store) {
		        if ((sm_thisProp = (YuchCallerProp)sm_store.getContents()) == null) {
		        	sm_thisProp = new YuchCallerProp();
		        	sm_store.setContents(sm_thisProp);
		        	sm_store.commit();
		        }
		    }
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		
	//! receive phone vibration time
	private int m_receivePhoneVibrationTime = 100;
	
	//! hangup phone vibration time
	private int m_hangupPhoneVibrationTime = 100;
	
	//! get the receive phone vibration time(milli-second)
	public int getRecvPhoneVibrationTime(){
		return m_receivePhoneVibrationTime;
	}
	
	//!get the receive phone vibration time(milli-second)
	public void setRecvPhoneVibrationTime(int _time){
		m_receivePhoneVibrationTime = _time;
	}
	
	//! get the hang up phone vibration time
	public int getHangupPhoneVibrationTime(){
		return m_hangupPhoneVibrationTime;
	}
	
	//! set the hang up phone vibration time(milli-second)
	public void setHangupPhoneVibrationTime(int _time){
		m_hangupPhoneVibrationTime = _time;
	}
	
	//Retrieves a copy of the effective properties set from storage.
    public static YuchCallerProp instance(){
        return sm_thisProp;
    }
    
    //Retrieves a copy of the effective properties set from storage.
    public void save(){
    	synchronized (sm_store){
        	sm_store.setContents(this);
        	sm_store.commit();	        
	    }
    }
}
