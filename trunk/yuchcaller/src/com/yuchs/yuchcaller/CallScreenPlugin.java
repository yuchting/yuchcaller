package com.yuchs.yuchcaller;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.container.MainScreen;


public class CallScreenPlugin {
	
	// buffer the former phone number avoid search phone data many times
	private String m_formerNumber = "";
	private String m_formerLocation = "";
	
	public static final int	INCOMING =  1;
	public static final int	WAITING =  1;
	public static final int	OUTGOING =  2;
	public static final int	ACTIVECALL =  2;
	
	final private YuchCaller	m_mainApp;
	

	// whether the phone screen plugin supported
	public static boolean isPhoneScreenPluginSupported(){
		return false;
	}
	
	public CallScreenPlugin(YuchCaller _mainApp){
		m_mainApp = _mainApp;
	}

	// applay the 
	public boolean apply(int callId,int _screenType){
		return false;
	}
	

	/**
	 * clear the buffered number 
	 */
	public void clearBufferedNumber(){
		m_formerNumber 	= "";
		m_formerLocation= "";
	}
	
	/**
	 * return the different MainScreen between 4.6+ and 4.5 os
	 * to set the background of MainScreen check detail in follow URL
	 * 
	 * http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/348583/800332/800505/800256/How_to_-_Change_the_background_color_of_a_screen.html?nodeid=800335&vernum=0
	 * 
	 * @return
	 */
	public static MainScreen getConfigMainScreen(YuchCaller _mainApp){
		
		final ConfigManager t_manager = new ConfigManager(_mainApp);
		MainScreen t_mainScreen = new MainScreen(Screen.DEFAULT_MENU | Screen.DEFAULT_CLOSE){
			protected boolean onSave(){
				t_manager.saveProp();
				return true;
			}
			
			public boolean onClose(){
				if(t_manager.escapeKeyPress()){
					super.onClose();
					return true;
				}
				
				return false;
			}
		};
	
		t_mainScreen.add(t_manager);
		t_mainScreen.setTitle(_mainApp.getTitle());
		
		return t_mainScreen;
	}
}
