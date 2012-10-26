package com.yuchs.yuchcaller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.flurry.blackberry.FlurryAgent;

import local.yuchcallerlocalResource;
import net.rim.blackberry.api.options.OptionsManager;
import net.rim.blackberry.api.options.OptionsProvider;
import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneListener;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.container.MainScreen;

public class YuchCaller extends Application implements OptionsProvider,PhoneListener{

	public final static int 		fsm_display_width		= Display.getWidth();
	public final static int 		fsm_display_height		= Display.getHeight();
	public final static String	fsm_OS_version			= CodeModuleManager.getModuleVersion((CodeModuleManager.getModuleHandleForObject("")));
	public final static String	fsm_client_version		= ApplicationDescriptor.currentApplicationDescriptor().getVersion();
	public final static long		fsm_PIN					= DeviceInfo.getDeviceId();
	public final static String	fsm_IMEI				= "bb";
	
	public final static ResourceBundle sm_local = ResourceBundle.getBundle(yuchcallerlocalResource.BUNDLE_ID, yuchcallerlocalResource.BUNDLE_NAME);
	
	public static YuchCaller		sm_instance;
	
	//! data base index manager class
	private DbIndex			m_dbIndex	= new DbIndex();
	
	
	//! the config editField of recv-phone vibration
	private EditField m_recvVibrationTime = null;
	
	//! the config editField of hangup-phone vibration
	private EditField m_hangupVibrationTime = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (ApplicationManager.getApplicationManager().inStartup()){
			
			initFlurry();
			
			//Enter the auto start portion of the application.
			//Register an options provider and exit.
			sm_instance = new YuchCaller();
			sm_instance.init();
			sm_instance.enterEventDispatcher();
		}
	}
	
	/**
	 * intialize the flurry
	 */
	private static void initFlurry(){
		
		try{
			InputStream t_file = Class.forName("com.yuchs.yuchcaller.YuchCaller").getResourceAsStream("/FlurryKey.txt");
			try{

				ByteArrayOutputStream os = new ByteArrayOutputStream();
				try{
					int t_readByte;
					while((t_readByte = t_file.read()) != -1){
						os.write(t_readByte);
					}
					
					FlurryAgent.onStartApp(new String(os.toByteArray()));
					
				}finally{
					os.close();
				}				
			}finally{
				t_file.close();
			}			
		}catch(Exception e){
			System.out.println("Flurry init failed!"+e.getMessage());
		}
	}
	
	/**
	 * initialize the application state:
	 * 
	 * 1, register the options Provider
	 */
	private void init(){
		
		// register the system option menu item
		OptionsManager.registerOptionsProvider(this);
		
		// register the Phone lister
		Phone.addPhoneListener(this);
	}
	
	private Dialog m_alartDlg = null;
	public void DialogAlert(final String _msg){
		
		if(m_alartDlg != null){
			return;
		}
		
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run(){
				synchronized(getEventLock()){
					
					synchronized (YuchCaller.this) {
						m_alartDlg = new Dialog(Dialog.D_OK,_msg,
								Dialog.OK,Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION),Manager.VERTICAL_SCROLL){
							public void close(){
								super.close();
								synchronized (YuchCaller.this) {
									m_alartDlg = null;
								}
							}
						};
						
						m_alartDlg.setEscapeEnabled(true);			
						UiApplication.getUiApplication().pushGlobalScreen(m_alartDlg,1, UiEngine.GLOBAL_QUEUE);
					}
				};
			}
		});				
    }
	

	//@{ OptionsProvider
	public String getTitle() {
		return sm_local.getString(yuchcallerlocalResource.App_Title);
	}
	
	

	// fill the main screen
	public void populateMainScreen(MainScreen mainScreen) {
		 
		m_recvVibrationTime = new EditField(sm_local.getString(yuchcallerlocalResource.PHONE_RECV_VIBRATE_TIME),
											Integer.toString(YuchCallerProp.instance().getRecvPhoneVibrationTime()),
											// Vibration time in milliseconds, from 0 to 25500.
											//
											4,
											EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );
		
		m_hangupVibrationTime = new EditField(sm_local.getString(yuchcallerlocalResource.PHONE_HANGUP_VIBRATE_TIME),
											Integer.toString(YuchCallerProp.instance().getHangupPhoneVibrationTime()),
											// Vibration time in milliseconds, from 0 to 25500.
											//
											4,
											EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );
		
		mainScreen.add(m_recvVibrationTime);
		mainScreen.add(m_hangupVibrationTime);
		
	}

	public void save() {
		try{

			String t_recvStr	= m_recvVibrationTime.getText();
			String t_hangupStr	= m_hangupVibrationTime.getText();
			
			int t_recv		= t_recvStr.length() == 0 ? 0 : Integer.parseInt(m_recvVibrationTime.getText());
			int t_hangup	= t_hangupStr.length() == 0 ? 0 : Integer.parseInt(m_hangupVibrationTime.getText());
			
			YuchCallerProp.instance().setRecvPhoneVibrationTime(t_recv);
			YuchCallerProp.instance().setHangupPhoneVibrationTime(t_hangup);
			
			YuchCallerProp.instance().save();
			
		}catch(Exception ex){
			ex.printStackTrace();
			DialogAlert("Error! " + ex.getMessage());
		}
	}
	//@}

	
	//@{ PhoneListener
	public void callAdded(int callId) {}
	public void callAnswered(int callId) {}
	public void callConferenceCallEstablished(int callId) {}

	public void callConnected(int callId) {
		if(YuchCallerProp.instance().getRecvPhoneVibrationTime() != 0){
			Alert.startVibrate(YuchCallerProp.instance().getRecvPhoneVibrationTime());
		}				
	}

	public void callDisconnected(int callId) {
		if(YuchCallerProp.instance().getHangupPhoneVibrationTime() != 0){
			Alert.startVibrate(YuchCallerProp.instance().getHangupPhoneVibrationTime());
		}		
	}	

	public void callDirectConnectConnected(int callId) {}
	public void callDirectConnectDisconnected(int callId) {}
	public void callEndedByUser(int callId) {}
	public void callFailed(int callId, int reason) {}
	public void callHeld(int callId) {}
	public void callIncoming(int callId) {}
	public void callInitiated(int callid) {}
	public void callRemoved(int callId) {}
	public void callResumed(int callId) {}
	public void callWaiting(int callid) {}
	public void conferenceCallDisconnected(int callId) {}
	//@}

}

