package com.yuchs.yuchcaller;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.phonegui.PhoneScreen;
import net.rim.blackberry.api.phone.phonegui.ScreenModel;
import net.rim.device.api.ui.component.LabelField;

public class CallScreenPlugin {
	
	// buffer the former phone number avoid search phone data many times
	private static String sm_formerNumber = "";
	private static String sm_formerLocation = "";
	
	public static final int	INCOMING =  1;
	public static final int	WAITING =  1;
	public static final int	OUTGOING =  2;
	public static final int	ACTIVECALL =  2;

	// applay the 
	public static void apply(YuchCaller _mainApp,int callId,int _screenType){
		
		ScreenModel screenModel = new ScreenModel(callId);
		if(!screenModel.isSupported()){
			_mainApp.SetErrorString("screenModel.isSupported() is false , device is locked!");
			return ;
		}
		
		PhoneCall t_pc			= Phone.getCall(callId);
		String t_number 		= YuchCaller.parsePhoneNumber(t_pc.getPhoneNumber());
		String t_locationInfo	= sm_formerNumber.equals(t_number)?sm_formerLocation:_mainApp.m_dbIndex.findPhoneData(t_number);
		
		if(t_locationInfo.length() != 0){
			PhoneScreen ps = screenModel.getPhoneScreen(PhoneScreen.PORTRAIT, _screenType);
			
			LabelField t_label = new LabelField(t_locationInfo);
			//t_label.setFont(_mainApp.generateLocationTextFont());
			
			ps.add(t_label);
			screenModel.sendAllDataToScreen();
		}
	}
}

