package com.yuchs.yuchcaller;

import local.yuchcallerlocalResource;
import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.phonegui.PhoneScreen;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class CallScreenPlugin {
	
	// buffer the former phone number avoid search phone data many times
	private String m_formerNumber = "";
	private String m_formerLocation = "";
	
	public static final int	INCOMING =  1;
	public static final int	WAITING =  1;
	public static final int	OUTGOING =  2;
	public static final int	ACTIVECALL =  2;
	
	final private YuchCaller	m_mainApp;
	
	public CallScreenPlugin(YuchCaller _mainApp){
		m_mainApp = _mainApp;
	}
	
	// whether the phone screen plugin supported
	public static boolean isPhoneScreenPluginSupported(){
		return PhoneScreen.isSupported();
	}

	// applay the 
	public boolean apply(int callId,int _screenType){

		try{
			if(!isPhoneScreenPluginSupported()){
				m_mainApp.SetErrorString("PhoneScreen.isSupported() is false , device " + DeviceInfo.getDeviceName() + " is locked!");
				return false;
			}
			
			PhoneCall t_pc				= Phone.getCall(callId);
			String t_number 			= YuchCaller.parsePhoneNumber(t_pc.getPhoneNumber());
			
			m_formerLocation			= m_formerNumber.equals(t_number)?m_formerLocation:m_mainApp.searchLocation(t_number);
			m_formerNumber				= t_number;

			PhoneScreen ps = new PhoneScreen(callId, m_mainApp);
			
			final Font t_textFont = m_mainApp.generateLocationTextFont();
			final int t_width		= t_textFont.getAdvance(m_formerLocation);
			final int t_height	= t_textFont.getHeight();
			
			LabelField t_label = new LabelField(m_formerLocation,Field.NON_FOCUSABLE){
				public void paint(Graphics g){
					int t_color = g.getColor();
					try{
						g.setColor(YuchCallerProp.instance().getLocationColor());
						g.setFont(t_textFont);
						g.drawText(m_formerLocation, 0, 0);
					}finally{
						g.setColor(t_color);
					}
				}
				
				public int getPreferredWidth(){
					return t_width;
				}
				
				public int getPreferredHeight(){
					return t_height;
				}
				
				protected void layout(int _width,int _height){
					setExtent(getPreferredWidth(), getPreferredHeight());
				}
			};
			
			ps.add(t_label);
			ps.sendDataToScreen();			
			
		}catch(Exception e){
			m_mainApp.SetErrorString("CSPA", e);
		}
		
		return true;
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
		};
		
		Background bg = BackgroundFactory.createSolidBackground(0xdedfde);
		t_mainScreen.getMainManager().setBackground(bg);
		
		t_mainScreen.add(t_manager);
		t_mainScreen.setTitle(_mainApp.getTitle());
		
		return t_mainScreen;
	}
}
