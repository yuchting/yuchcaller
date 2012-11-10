package com.yuchs.yuchcaller;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.Font;
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
	
	//! location information position x
	private int m_locationInfoPosition_x	= 0;
	
	//! location information position 
	private int m_locationInfoPosition_y	= getLocationInfoInitPos_y();
	
	//! location information color
	private int m_locationInfoColor		= YuchCaller.fsm_OS_version.startsWith("4.5")?0:0xffffff;
	
	//! show system menu or only show phone/contact screen
	private boolean m_showSystemMenu		= true;
	
	//! default height
	private int m_locationInfoHeight		= Font.getDefault().getHeight();
	
	// static function to get the initialize y position of location information label
	private static int getLocationInfoInitPos_y(){
		if(YuchCaller.fsm_OS_version.startsWith("4.5")){
			return YuchCaller.fsm_display_height - YuchCaller.fsm_display_height / 3 + 12;
		}else if(YuchCaller.fsm_display_height > YuchCaller.fsm_display_width){
			return YuchCaller.fsm_display_height / 3;
		}else{
			return YuchCaller.fsm_display_height - YuchCaller.fsm_display_height / 3;
		}
	}
			
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
	
	public int getLocationPosition_x(){return m_locationInfoPosition_x;}
	public void setLocationPosition_x(int _x){m_locationInfoPosition_x = _x;}
	
	public int getLocationPosition_y(){return m_locationInfoPosition_y;}
	public void setLocationPosition_y(int _y){m_locationInfoPosition_y = _y;}
	
	public int getLocationColor(){return m_locationInfoColor;}
	public void setLocationColor(int _color){m_locationInfoColor = _color;}
	
	public int getLocationHeight(){return m_locationInfoHeight;}
	public void setLocationHeight(int _height){m_locationInfoHeight = _height;}
	
	public boolean showSystemMenu(){return m_showSystemMenu;}
	public void setShowSystemMenu(boolean _show){m_showSystemMenu = _show;}
	
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
