package com.yuchs.yuchcaller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.pim.Contact;

import local.yuchcallerlocalResource;
import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.blackberry.api.options.OptionsManager;
import net.rim.blackberry.api.options.OptionsProvider;
import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.PhoneListener;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.device.api.compress.GZIPInputStream;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;
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
	public final static long		fsm_PIN					= DeviceInfo.getDeviceId();
	
	// current Client version
	public final String			ClientVersion			= ApplicationDescriptor.currentApplicationDescriptor().getVersion();
	
	public Bitmap	m_backgroundBitmap		= null;
	public Bitmap	m_answerBitmap			= null;
	public Bitmap	m_ignoreBitmap			= null;
	
	public final ResourceBundle 	m_local = ResourceBundle.getBundle(yuchcallerlocalResource.BUNDLE_ID, yuchcallerlocalResource.BUNDLE_NAME);
	
	//! data base index manager class
	private DbIndex	m_dbIndex	= new DbIndex();	
	
	//! user answer the call id to avoid vibrate
	private int	m_userAnswerCall = -1;
	
	//! user end this call id to avoid vibrate
	private int	m_userEndCall = -1;
	
	//! the active phone call manager
	private ReplaceVerticalFieldManager m_activePhoneCallManager = null;
	
	//! error string list
	private Vector			m_errorString		= new Vector();
	
	//! the alert dialog
	private Dialog m_alartDlg = null;
	
	//! config manager to show the yuchCaller config
	private ConfigManager	m_configManager		= null;
	
	//! call screen plugin for display text to phone screen
	private CallScreenPlugin	m_callScreenPlugin = new CallScreenPlugin(this);
	
	//! replace incoming call screen to close it when phone call is disconnect
	public ReplaceIncomingCallScreen m_replaceIncomingCallScreen = null;
	
	/**
	 * replace vertical field manager for acvtive phone call screen's manager
	 * @author tzz
	 *
	 */
	private class ReplaceVerticalFieldManager extends VerticalFieldManager{
		
		public String	m_locationInfo	= "";
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
				
				String t_displayLoc;
				if(m_locationInfo != null && m_locationInfo.length() > 0){
					t_displayLoc = m_locationInfo;
					
				}else{
					t_displayLoc = m_local.getString(yuchcallerlocalResource.PHONE_UNKNOWN_NUMBER);
				}
				
				_g.drawText(t_displayLoc,
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
		
		initFlurry();
					
		//Enter the auto start portion of the application.
		//Register an options provider and exit.
		YuchCaller t_caller = new YuchCaller();
		t_caller.init();
		t_caller.enterEventDispatcher();
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
		
		// register the Phone lister
		Phone.addPhoneListener(this);
		
		// initialize the database
		initDbIndex();
		
		// add menu to device application
		initMenus();
		
		// initialize the bitmap of replace incoming call screen
		(new Thread(){
			public void run(){
				try{

					if(m_backgroundBitmap == null && fsm_OS_version.startsWith("4.5")){
							
						byte[] bytes = IOUtilities.streamToBytes(YuchCaller.this.getClass().getResourceAsStream("/background.png"));		
						m_backgroundBitmap =  EncodedImage.createEncodedImage(bytes , 0, bytes .length).getBitmap();
						 
						bytes = IOUtilities.streamToBytes(YuchCaller.this.getClass().getResourceAsStream("/answer.png"));		
						m_answerBitmap =  EncodedImage.createEncodedImage(bytes , 0, bytes .length).getBitmap();
						 
						bytes = IOUtilities.streamToBytes(YuchCaller.this.getClass().getResourceAsStream("/ignore.png"));		
						m_ignoreBitmap =  EncodedImage.createEncodedImage(bytes , 0, bytes .length).getBitmap();
					}
										
				}catch(Exception ex){
					SetErrorString("RCS", ex);
				}
			}
		}).start();
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
				
				// register the system option menu item
				// after read the version
				OptionsManager.registerOptionsProvider(YuchCaller.this);
			}
		}).start();
	}
	
	/**
	 * add the menus to device application
	 */
	private void initMenus(){
		invokeLater(new Runnable() {
			
			public void run() {
				CheckLocationMenu t_addrMenu = new CheckLocationMenu();
				ApplicationMenuItemRepository t_repository = ApplicationMenuItemRepository.getInstance();
				
				t_repository.addMenuItem(ApplicationMenuItemRepository.MENUITEM_ADDRESSBOOK_LIST, t_addrMenu);
				t_repository.addMenuItem(ApplicationMenuItemRepository.MENUITEM_ADDRESSCARD_EDIT, t_addrMenu);
				t_repository.addMenuItem(ApplicationMenuItemRepository.MENUITEM_ADDRESSCARD_VIEW, t_addrMenu);
				t_repository.addMenuItem(ApplicationMenuItemRepository.MENUITEM_PHONE, t_addrMenu);
				t_repository.addMenuItem(ApplicationMenuItemRepository.MENUITEM_PHONELOG_VIEW, t_addrMenu);
				 
			}
		});
	}
	
	/**
	 * popup a dialog to show user some message
	 * @param _msg
	 */
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
	public Font generateLocationTextFont(){
		return Font.getDefault().derive(Font.getDefault().getStyle(),YuchCallerProp.instance().getLocationHeight());
	}
	
	//@{ OptionsProvider
	public String getTitle() {
		return m_local.getString(yuchcallerlocalResource.App_Title) + " ("+ClientVersion+") DataBase (" + m_dbIndex.getVersion() + ")";
	}
	
	// fill the main screen
	public void populateMainScreen(MainScreen mainScreen) {
		m_configManager = new ConfigManager(this);
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
		
		closeReplaceIncomingCallScreen();
	}

	public void callDirectConnectConnected(int callId) {}
	public void callDirectConnectDisconnected(int callId) {
		closeReplaceIncomingCallScreen();
	}
	public void callEndedByUser(int callId) {
		m_userEndCall = callId;
	}
	public void callFailed(int callId, int reason) {}
	public void callHeld(int callId) {}
	public void callIncoming(int callId) {
			
		if(fsm_OS_version.startsWith("4.5") && m_replaceIncomingCallScreen == null){
			PhoneCall t_call = Phone.getCall(callId);			
			m_replaceIncomingCallScreen = new ReplaceIncomingCallScreen(parsePhoneNumber(t_call.getDisplayPhoneNumber()),this);
			
			try{
				UiApplication.getUiApplication().pushGlobalScreen(m_replaceIncomingCallScreen,0,UiEngine.GLOBAL_MODAL);
			}catch(Exception ex){
				SetErrorString("CI",ex);
				m_replaceIncomingCallScreen = null;
			}
		}else if(!fsm_OS_version.startsWith("4")){
			m_callScreenPlugin.apply(callId,CallScreenPlugin.INCOMING);
		}
		
	}
	public void callInitiated(int callId) {
		replaceActivePhoneCallManager(callId);
	}	
	public void callRemoved(int callId) {}
	public void callResumed(int callId) {}
	public void callWaiting(int callid) {}
	public void conferenceCallDisconnected(int callId) {
		closeReplaceIncomingCallScreen();
	}
	//@}
	
	private void closeReplaceIncomingCallScreen(){
		if(m_replaceIncomingCallScreen != null){
			m_replaceIncomingCallScreen.close();
			m_replaceIncomingCallScreen = null;
		}
	}
	
	/**
	 * replace the active phone call manager to display own Phone
	 */
	private void replaceActivePhoneCallManager(int callId){
		
		if(fsm_OS_version.startsWith("4.")){
			// 5.0 os has native method to display
			//
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
			m_activePhoneCallManager.m_locationInfo = m_dbIndex.findPhoneData(parsePhoneNumber(t_phone.getDisplayPhoneNumber()));
		}else{
			// 5.0 OS will add in calling screen 
			//
			m_callScreenPlugin.apply(callId,CallScreenPlugin.ACTIVECALL);
		}
	}
	

	//! parse the phone number as number
	public static String parsePhoneNumber(String _origPhoneNumber){
		
		StringBuffer t_sb = new StringBuffer();
		for(int i = _origPhoneNumber.length() - 1; i >= 0 ;i--){
			char c = _origPhoneNumber.charAt(i);
			if(Character.isDigit(c) || c == '+'){
				t_sb.insert(0, c);
			}			
		}
		
		return t_sb.toString();
	}
	
	//! start
	private Thread m_checkVersionThread = null;
	
	// download the version to get know the version
	public synchronized void checkVersion(){
		
		if(m_checkVersionThread == null && !canNotConnectNetwork()){
			
			m_checkVersionThread = new Thread(){
				public void run(){
					try{
						HttpConnection conn = (HttpConnection)Connector.open("http://yuchcaller.googlecode.com/files/latest_version?a=" + (new Random()).nextInt());
						try{
							
						}finally{
							conn.close();
							conn = null;
						}
						
						
					}catch(Exception ex){
						SetErrorString("CV", ex);
					}
					
					synchronized (YuchCaller.this) {
						m_checkVersionThread = null;
					}					
				}
			};
			
			m_checkVersionThread.start();
		}
	}
	
	//! cannot visit data network
	public static boolean canNotConnectNetwork(){
		boolean t_radioNotAvail = (RadioInfo.getSignalLevel() <= -110 || !RadioInfo.isDataServiceOperational());
		
		return t_radioNotAvail && (WLANInfo.getAPInfo() == null);
	}
	
	//! get the prefix of tele attribute
	public String getPrefixByTelAttr(int _attr){
		switch(_attr){
		case Contact.ATTR_HOME:
			return m_local.getString(yuchcallerlocalResource.PHONE_CALL_HOME);
		case Contact.ATTR_MOBILE:
			return m_local.getString(yuchcallerlocalResource.PHONE_CALL_MOBILE);
		case Contact.ATTR_WORK:
			return m_local.getString(yuchcallerlocalResource.PHONE_CALL_WORK);
		default:
			return m_local.getString(yuchcallerlocalResource.PHONE_CALL_OTHER);
		}
	}
	
	//! search number
	public String searchLocation(String _number){
		String t_info = m_dbIndex.findPhoneData(_number);
		if(t_info.length() == 0){
			t_info = m_local.getString(yuchcallerlocalResource.PHONE_UNKNOWN_NUMBER);
		}
	
		return t_info;
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
	
	/**
	 * check location menu to add to AddressBook Phone application to show location 
	 * @author tzz
	 *
	 */
	public class CheckLocationMenu extends ApplicationMenuItem{
		public CheckLocationMenu(){
			super(0);
		}

		public Object run(Object context) {
			if(context != null){
				if(context instanceof BlackBerryContact){
					// MENUITEM_ADDRESSBOOK_LIST
					// MENUITEM_ADDRESSCARD_EDIT
					// MENUITEM_ADDRESSCARD_VIEW
					DialogAlert(getLocationInfo((BlackBerryContact)context));
				}else if(context instanceof PhoneCallLog){
					// MENUITEM_PHONELOG_VIEW
					//
					PhoneCallLog t_log = (PhoneCallLog)context;
					DialogAlert(searchLocation(t_log.getParticipant().getNumber()));					
				}
				
			}
			return null;
		}

		public String toString() {
			return m_local.getString(yuchcallerlocalResource.PHONE_CHECK_LOCATION);
		}
		
		// get the blackberry contact phone number location info 
		private String getLocationInfo(BlackBerryContact bbContact){
			
			int t_valueNum = bbContact.countValues(Contact.TEL);
			if(t_valueNum == 0){
				return m_local.getString(yuchcallerlocalResource.PHONE_NON_PHONE_NUMBER);
			}

			StringBuffer t_sb = new StringBuffer();
			
			for(int j = 0;j < t_valueNum;j++){
				
				String t_number = bbContact.getString(Contact.TEL, j);
				
				if(t_number != null){
					t_sb.append(searchLocation(t_number)).append(' ')
						.append(getPrefixByTelAttr(bbContact.getAttributes(Contact.TEL, j)));
						
				}
				
				if(j + 1 < t_valueNum){
					t_sb.append('\n');
				}
			}
			
			return t_sb.toString();
		}
	}

}

