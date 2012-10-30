package com.yuchs.yuchcaller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Vector;

import local.yuchcallerlocalResource;
import net.rim.blackberry.api.options.OptionsManager;
import net.rim.blackberry.api.options.OptionsProvider;
import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.PhoneListener;
import net.rim.device.api.compress.GZIPInputStream;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.flurry.blackberry.FlurryAgent;

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
	
	//! user answer the call id to avoid vibrate
	private int	m_userAnswerCall = -1;
	
	//! user end this call id to avoid vibrate
	private int	m_userEndCall = -1;
	
	//! the active phone call manager
	private ReplaceVerticalFieldManager	m_activePhoneCallManager = null;
	
	//! error string list
	private Vector			m_errorString		= new Vector();
	
	//! the alert dialog
	private Dialog m_alartDlg = null;
	
	//! config manager to show the yuchCaller config
	private ConfigManager	m_configManager		= null;
	
	/**
	 * replace vertical field manager for acvtive phone call screen's manager
	 * @author tzz
	 *
	 */
	private class ReplaceVerticalFieldManager extends VerticalFieldManager{
		
		public String	m_locationInfo	= "Hello YuchCaller";
		public Font		m_font			= null;
		
		public ReplaceVerticalFieldManager(){
			m_font = generateLocationTextFont();			
		}
		
		protected void subpaint(Graphics _g){
			super.subpaint(_g);
			
			int t_color = _g.getColor();
			Font t_font = _g.getFont();
			
			try{
				_g.setColor(YuchCallerProp.instance().getLocationColor());
				_g.setFont(m_font);
				
				_g.drawText(m_locationInfo,
							YuchCallerProp.instance().getLocationPosition_x(),
							YuchCallerProp.instance().getLocationPosition_y());
				
			}finally{
				_g.setColor(t_color);
				_g.setFont(t_font);
			}
		}
	}
	
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
		
		// initialize the database
		initDbIndex();
	}
	
	/**
	 * initialize the dbindex
	 */
	private void initDbIndex(){
		(new Thread(){
			public void run(){
				try{
					InputStream t_file = Class.forName("com.yuchs.yuchcaller.YuchCaller").getResourceAsStream("/yuchcaller.db");
					try{
						GZIPInputStream t_in = new GZIPInputStream(t_file);
						try{
							YuchCaller.this.m_dbIndex.ReadIdxFile(t_in);
						}finally{
							t_in.close();
						}						
					}finally{
						t_file.close();
					}			
				}catch(Exception e){
					System.out.println("DbIndex init failed!"+e.getMessage());
				}				
			}
		}).start();
	}
	
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
	
	public synchronized void SetErrorString(final String _error){
		ErrorInfo ei = new ErrorInfo(_error);
		m_errorString.addElement(ei);
		if(m_errorString.size() > 100){
			m_errorString.removeElementAt(0);
		}
		
		if(DeviceInfo.isSimulator()){
			System.err.println("[YuchCaller]"+ei.toString());
		}
	}
	
	public void SetErrorString(String _label,Exception e){
		SetErrorString(_label + " " + e.getMessage() + " " + e.getClass().getName());
	}
	
	//! change location TextFont
	public void changeLocationTextFont(){
		if(m_activePhoneCallManager != null){
			m_activePhoneCallManager.m_font = generateLocationTextFont();
		}
	}
	
	//! generate the location text font
	private Font generateLocationTextFont(){
		return Font.getDefault().derive(Font.getDefault().getStyle(),YuchCallerProp.instance().getLocationHeight());
	}
	

	//@{ OptionsProvider
	public String getTitle() {
		return sm_local.getString(yuchcallerlocalResource.App_Title);
	}
	

	// fill the main screen
	public void populateMainScreen(MainScreen mainScreen) {
		m_configManager = new ConfigManager();
		mainScreen.add(m_configManager);		
	}

	// save the property
	public void save() {
		m_configManager.saveProp();
		m_configManager = null;
	}
	//@}

	
	//@{ PhoneListener
	public void callAdded(int callId) {}
	public void callAnswered(int callId) {
		m_userAnswerCall = callId;
	}
	public void callConferenceCallEstablished(int callId) {}

	public void callConnected(int callId) {
		if(m_userAnswerCall != callId && YuchCallerProp.instance().getRecvPhoneVibrationTime() != 0){
			Alert.startVibrate(YuchCallerProp.instance().getRecvPhoneVibrationTime());
		}
		
		replaceActivePhoneCallManager(callId);		
	}

	public void callDisconnected(int callId) {
		if(m_userEndCall != callId && YuchCallerProp.instance().getHangupPhoneVibrationTime() != 0){
			Alert.startVibrate(YuchCallerProp.instance().getHangupPhoneVibrationTime());
		}		
	}

	public void callDirectConnectConnected(int callId) {}
	public void callDirectConnectDisconnected(int callId) {}
	public void callEndedByUser(int callId) {
		m_userEndCall = callId;
	}
	public void callFailed(int callId, int reason) {}
	public void callHeld(int callId) {}
	public void callIncoming(int callId) {
		UiApplication.getUiApplication().requestForeground();
	}
	public void callInitiated(int callId) {
		replaceActivePhoneCallManager(callId);
	}	
	public void callRemoved(int callId) {}
	public void callResumed(int callId) {}
	public void callWaiting(int callid) {}
	public void conferenceCallDisconnected(int callId) {}
	//@}
	
	/**
	 * replace the active phone call manager to display own Phone
	 */
	private void replaceActivePhoneCallManager(int callId){
		
		if(m_activePhoneCallManager == null){
			try{
				m_activePhoneCallManager = new ReplaceVerticalFieldManager();
				
				Screen t_screen = UiApplication.getUiApplication().getActiveScreen();
				
				Field t_orig = t_screen.getField(0);
				t_screen.deleteAll();				
				
				m_activePhoneCallManager.add(t_orig);
				t_screen.add(m_activePhoneCallManager);
				
			}catch(Exception ex){
				SetErrorString("RAPCM", ex);
			}
		}
		
		PhoneCall t_phone = Phone.getCall(callId);
		m_activePhoneCallManager.m_locationInfo = m_dbIndex.findPhoneData(t_phone.getDisplayPhoneNumber());
	}
	
	private final static SimpleDateFormat fsm_errorInfoTimeFormat = new SimpleDateFormat("HH:mm:ss");

	// error information class to manager error
	final class ErrorInfo{
		Date		m_time;
		String		m_info;
		
		ErrorInfo(String _info){
			m_info	= _info;
			m_time	= new Date();
		}
		
		public String toString(){
			return fsm_errorInfoTimeFormat.format(m_time) + ":" + m_info;
		}
	}

}

