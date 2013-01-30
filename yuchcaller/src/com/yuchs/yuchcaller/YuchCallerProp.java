package com.yuchs.yuchcaller;

import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.ui.Font;

public class YuchCallerProp {
	    
    // directory of blackberry store 
	public final static String 	fsm_rootPath_back		= "file:///store/home/user/";
	
	//  the max font height
	public final static int		fsm_maxFontHeight		= 50;
    		
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
	
	//! location bold font
	private boolean m_locationBoldFont		= true;
	
	//! IP number dial prefix
	private String m_IPDialPrefix			= "";
	
	//! show system menu or only show phone/contact screen
	private boolean m_showSystemMenu		= true;
	
	//! default height
	private int m_locationInfoHeight		= Font.getDefault().getHeight();
	
	final private YuchCaller	m_mainApp;
	
	public YuchCallerProp(YuchCaller _mainApp){
		m_mainApp		= _mainApp;
		writeReadIni(true);
	}
	
	// static function to get the initialize y position of location information label
	private static int getLocationInfoInitPos_y(){
		if(YuchCaller.fsm_OS_version.startsWith("4.") 
		|| !CallScreenPlugin.isPhoneScreenPluginSupported()){
			return YuchCaller.fsm_display_height - YuchCaller.fsm_display_height / 3 + 12;
		}else{
			return 0;
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
	
	public boolean isBoldFont(){return m_locationBoldFont;}
	public void setBoldFont(boolean _bold){m_locationBoldFont = _bold;}
	
	public String getIPDialNumber(){return m_IPDialPrefix;}
	public void setIPDialNumber(String _prefix){m_IPDialPrefix = _prefix;}
	
	
    //Retrieves a copy of the effective properties set from storage.
    public void save(){
    	writeReadIni(false);
    }
    
	/**
	 * pre process write or read a file
	 * 
	 * Write: change original name to back file name when write
	 * 
	 * Read: change back filename to original filename if back file is existed
	 */
	private void preWriteReadIni(boolean _read,
			String _backPathFilename,String _orgPathFilename,
			String _backFilename,String _orgFilename){
		
		try{

			if(_read){
							
				FileConnection t_back = (FileConnection) Connector.open(_backPathFilename,Connector.READ_WRITE);
				try{
					if(t_back.exists()){
						FileConnection t_ini	= (FileConnection) Connector.open(_orgPathFilename,Connector.READ_WRITE);
						try{
							if(t_ini.exists()){
								t_ini.delete();
							}	
						}finally{
							t_ini.close();
							t_ini = null;
						}
						
						t_back.rename(_orgFilename);
					}
				}finally{
					t_back.close();
					t_back = null;
				}				
				
			}else{
				
				FileConnection t_ini	= (FileConnection) Connector.open(_orgPathFilename,Connector.READ_WRITE);
				try{
					if(t_ini.exists()){
						t_ini.rename(_backFilename);
					}
				}finally{
					t_ini.close();
					t_ini = null;
				}
				
				// needn't copy ,the normal WriteReadIni method will re-create the init.data file
				//
				//Copyfile(fsm_backInitFilename,fsm_initFilename);
			}
			
		}catch(Exception e){
			m_mainApp.SetErrorString("write/read PreWriteReadIni file from "+fsm_rootPath_back+" error :",e);
		}
	}
	
	/**
	 * delete the back file ~xxx.xxx
	 * @param _backfile
	 */
	private void postWriteReadIni(String _backfile){
		
		try{
			// delete the back file ~xxx.data
			//
			FileConnection t_backFile = (FileConnection) Connector.open(_backfile,Connector.READ_WRITE);
			try{
				if(t_backFile.exists()){
					t_backFile.delete();
				}
			}finally{
				t_backFile.close();
				t_backFile = null;
			}
		}catch(Exception e){
			m_mainApp.SetErrorString("PWRI", e);
		}
	}
	
	final static int		fsm_clientVersion = 2;
	
	static final String fsm_initFilename_init_data = "Init.data";
	static final String fsm_initFilename_back_init_data = "~Init.data";
	
	static final String fsm_directory			= fsm_rootPath_back + "YuchCaller/";
	
	static final String fsm_initFilename 		= fsm_directory + fsm_initFilename_init_data;
	static final String fsm_backInitFilename	= fsm_directory + fsm_initFilename_back_init_data;
	
	private synchronized void writeReadIni(boolean _read){
		
		
		// process the ~Init.data file to restore the destroy original file
		// that writing when device is down  
		//
		// check the issue 85 
		// http://code.google.com/p/yuchberry/issues/detail?id=85&colspec=ID%20Type%20Status%20Priority%20Stars%20Summary
		//
		preWriteReadIni(_read,fsm_backInitFilename,fsm_initFilename,
				fsm_initFilename_back_init_data,fsm_initFilename_init_data);
		
		try{
			
			if(!_read){
				// make sure the directory is exist
				FileConnection t_dir = (FileConnection) Connector.open(fsm_directory,Connector.READ_WRITE);
				try{
					if(!t_dir.exists()){
						t_dir.mkdir();
					}
				}finally{
					t_dir.close();
					t_dir = null;
				}
			}
			
			FileConnection fc = (FileConnection) Connector.open(fsm_initFilename,Connector.READ_WRITE);
			try{
				if(_read){
					
			    	if(fc.exists()){
			    		InputStream in = fc.openInputStream();
			    		try{
			    			final int t_currVer = sendReceive.ReadInt(in);
			    						    			
				    		m_receivePhoneVibrationTime = sendReceive.ReadInt(in);
				    		m_hangupPhoneVibrationTime	= sendReceive.ReadInt(in);
				    		
				    		m_locationInfoPosition_x	= sendReceive.ReadInt(in);
				    		m_locationInfoPosition_y	= sendReceive.ReadInt(in);
				    		
				    		m_locationInfoColor			= sendReceive.ReadInt(in);
				    		m_showSystemMenu			= sendReceive.ReadBoolean(in);
				    		m_locationInfoHeight		= sendReceive.ReadInt(in);
				    		m_locationBoldFont			= sendReceive.ReadBoolean(in);
				    		
				    		if(t_currVer >= 2){
				    			m_IPDialPrefix			= sendReceive.ReadString(in);
				    		}				    						
				    
			    			if(t_currVer == 0 && !YuchCaller.fsm_OS_version.startsWith("4.")){
				    			// some data variables function is changed
				    			//
				    			m_locationInfoPosition_y = 0;
				    		}
			    			
			    		}finally{
			    							    		
			    			in.close();
			    			in = null;
			    		}
			    	}
				}else{
										
					if(!fc.exists()){
						fc.create();
					}				
					
					OutputStream os = fc.openOutputStream();
					try{
						sendReceive.WriteInt(os,fsm_clientVersion);
						
						sendReceive.WriteInt(os,m_receivePhoneVibrationTime);
						sendReceive.WriteInt(os,m_hangupPhoneVibrationTime);
						
						sendReceive.WriteInt(os,m_locationInfoPosition_x);
						sendReceive.WriteInt(os,m_locationInfoPosition_y);
						
						sendReceive.WriteInt(os,m_locationInfoColor);
						sendReceive.WriteBoolean(os,m_showSystemMenu);
						sendReceive.WriteInt(os,m_locationInfoHeight);
						sendReceive.WriteBoolean(os, m_locationBoldFont);
						sendReceive.WriteString(os, m_IPDialPrefix);
						
					}finally{
						os.close();
						os = null;
					}
					
					postWriteReadIni(fsm_backInitFilename);
				}
			}finally{
				fc.close();
				fc = null;
			}
						
		}catch(Exception _e){
			m_mainApp.SetErrorString("write/read config file error :",_e);
		}		
	}
}