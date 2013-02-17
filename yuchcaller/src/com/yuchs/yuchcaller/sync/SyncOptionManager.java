package com.yuchs.yuchcaller.sync;

import java.util.Vector;

import local.yuchcallerlocalResource;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EmailAddressEditField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchs.yuchcaller.YuchCaller;
import com.yuchs.yuchcaller.YuchCallerProp;

/**
 * this class will display and manager sync data to the sync.yuchs.com server  
 * @author tzz
 *
 */
public class SyncOptionManager extends VerticalFieldManager implements FieldChangeListener{
	
	/**
	 * main app
	 */
	final private YuchCaller		mMainApp;
	
	/**
	 * login wait information dialog
	 */
	private Dialog					mWaitInfoDlg = null;
	
	/**
	 * the small font to sync
	 */
	private Font					mSmallFont = Font.getDefault().derive(Font.getDefault().getStyle(),Font.getDefault().getHeight() - 2);
	
	/**
	 * account manager field
	 */
	private VerticalFieldManager		mAccountMgr = null;
	private NullField					mAccountMgrNull = new NullField(Field.NON_FOCUSABLE);
	
	private EmailAddressEditField		mYuchAccount = null;
	private PasswordEditField			mYuchPass	= null;
	
	private ButtonField					mLoginBtn	= null;
	private ButtonField					mSigninBtn	= null;
	
	/**
	 * config manager
	 */
	private VerticalFieldManager		mConfigMgr	= null;
	private NullField					mConfigMgrNull = new NullField(Field.NON_FOCUSABLE);
	
	private ButtonField					mSyncBtn	= null;
	private ButtonField					mLogoutBtn	= null;
	
	public SyncOptionManager(YuchCaller _app){
		mMainApp = _app;
		
		add(mAccountMgrNull);
		add(mConfigMgrNull);
		
		if(mMainApp.getProperties().getYuchAccount().length() == 0
		|| mMainApp.getProperties().getYuchAccessToken().length() == 0){
			prepareAccountMgr();
		}else{
			prepareConfigMgr();
		}
	}
	
	private void prepareAccountMgr(){
		
		if(mAccountMgr == null){
			mAccountMgr = new VerticalFieldManager();
			
			mYuchAccount = new EmailAddressEditField(mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_ACCOUNT_PREFIX),
													mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_ACCOUNT_INIT));
			
			mYuchPass = new PasswordEditField(mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_PASS_PREFIX),
												mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_PASS_INIT));
						
			mYuchAccount.setFont(mSmallFont);
			mYuchPass.setFont(mSmallFont);
			
			mAccountMgr.add(mYuchAccount);
			mAccountMgr.add(mYuchPass);
			
			HorizontalFieldManager btnMgr = new HorizontalFieldManager(Field.FIELD_HCENTER);
			
			mLoginBtn	= new ButtonField(mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_LOGIN_BTN),
										ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY | ButtonField.FIELD_HCENTER);
			
			mSigninBtn	= new ButtonField(mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_SIGNIN_BTN),
										ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY | ButtonField.FIELD_HCENTER);
			
			mLoginBtn.setFont(mSmallFont);
			mSigninBtn.setFont(mSmallFont);
			
			btnMgr.add(mLoginBtn);
			btnMgr.add(mSigninBtn);
			
			mAccountMgr.add(btnMgr);
			
			// set the stored account/pass
			YuchCallerProp tProp = mMainApp.getProperties();
			mYuchAccount.setText(tProp.getYuchAccount());
			mYuchPass.setText(tProp.getYuchPass());
			
			mLoginBtn.setChangeListener(this);
			mSigninBtn.setChangeListener(this);
		}
		
		if(mAccountMgr.getManager() == null){
			replace(mAccountMgrNull, mAccountMgr);
		}		
	}
	
	private void prepareConfigMgr(){
		if(mConfigMgr == null){
			mConfigMgr = new VerticalFieldManager();
			
			HorizontalFieldManager btnMgr = new HorizontalFieldManager(Field.FIELD_HCENTER);
			
			mSyncBtn	= new ButtonField(mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_SYNC_BTN),
											ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY | ButtonField.FIELD_HCENTER);
			
			mLogoutBtn	= new ButtonField(mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_LOGOUT_BTN),
											ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY | ButtonField.FIELD_HCENTER);

			btnMgr.add(mSyncBtn);
			btnMgr.add(mLogoutBtn);
			
			mConfigMgr.add(btnMgr);
		}
		
		if(mConfigMgr.getManager() == null){
			replace(mConfigMgrNull, mConfigMgr);
		}
	}

	private Thread mReadYuchAccThread = null;
	
	public void fieldChanged(Field field, int context) {
		if(context != FieldChangeListener.PROGRAMMATIC){
			if(field == mLoginBtn){
				if(isValidateEmail(mYuchAccount.getText())
				&& isValidateUserPass(mYuchPass.getText())){

					synchronized(this){
						
						if(mReadYuchAccThread == null){

							YuchCallerProp tProp = mMainApp.getProperties();
							tProp.setYuchAccount(mYuchAccount.getText());
							tProp.setYuchPass(mYuchPass.getText());
							
							tProp.save();
														
							mReadYuchAccThread = new Thread(){
								public void run(){
									readYuchAccount();
								}
							};
							
							mReadYuchAccThread.start();
						}
					}
					
					
				}else{
					
					mMainApp.DialogAlert("Please Enter right yuch account and pass!");
				}
				
			}else if(field == mSigninBtn){
				
			}
		}
		
	}
	
	
	/**
	 * report the information about the reading yuch account
	 * @param _info
	 */
	private void reportInfo(final String _info){
		
		Application.getApplication().invokeLater(new Runnable() {
			
			public void run() {
				if(mWaitInfoDlg == null){
					mWaitInfoDlg = new Dialog(_info,new Object[0],new int[0],0,null){
						public boolean onClose(){
							return false;
						}
					};
					mWaitInfoDlg.doModal();
				}else{
					mWaitInfoDlg.getLabel().setText(_info);
				}				
			}
		});
		
	}
	
	/**
	 * report the error
	 * @param _error
	 */
	private void reportError(final String _error){
		
		Application.getApplication().invokeLater(new Runnable() {
			public void run() {
				if(mWaitInfoDlg != null){
					mWaitInfoDlg.close();
					mWaitInfoDlg = null;
				}
				
				if(_error != null && _error.length() != 0){
					mMainApp.DialogAlert(_error);
				}
				
				synchronized(SyncOptionManager.this){
					mReadYuchAccThread = null;
				}
			}
		});
			
	}
	
	private void reportError(String _label,Exception e){		
		reportError(_label + ":" + e.getMessage() + " " + e.getClass().getName());
	}	
	
	//! read yuch account and request the Refresh/Access token
	private void readYuchAccount(){
		
		YuchCallerProp tProp = mMainApp.getProperties();
		
		if(tProp.getYuchAccount().length() == 0 
		|| tProp.getYuchPass().length() == 0){
			return;
		}

		reportInfo("Reading Yuch Account...");
		
		if(tProp.getYuchAccessToken().length() == 0 || tProp.getYuchRefreshToken().length() == 0){
			
			// request the yuch server
			
			String url = "http://192.168.100.116:8888/f/login/" + YuchCaller.getHTTPAppendString();
			//String url = "http://192.168.10.4:8888/f/login/" + YuchCaller.getHTTPAppendString();
			//String url = "http://www.yuchs.com/f/login/" + YuchCaller.getHTTPAppendString();
						
			String[] tParamName = {
				"acc",	"pass",	"type",
			};
			
			String[] tParamValue = {
				tProp.getYuchAccount(),
				tProp.getYuchPass(),				
				"yuchcaller",
			};
		
			try{
				String tResult = SyncMain.requestPOSTHTTP(url,tParamName,tParamValue);
				
				if(tResult.startsWith("<Error>")){
					reportError(tResult.substring(7));
				}else{
										
					Vector data = SyncMain.splitStr(tResult, '|');
					
					if(data.size() >= 5){						
						
						tProp.setYuchRefreshToken(data.elementAt(0).toString());
						tProp.setYuchAccessToken(data.elementAt(1).toString());
						
						tProp.save();
						
						mMainApp.invokeLater(new Runnable() {
							
							public void run() {
								prepareConfigMgr();
							}
						});						
						
					}else{
						reportError("Unkown:" + tResult);
					}					
				}
				
			}catch(Exception e){
				// network problem
				reportError("Can not get the YuchAccount",e);
			}			
		}
		
		// close the report info dialog
		reportError(null);
	}
	
	/**
	 * whether this string is valid Email  
	 * @param _str
	 * @return true if this string is email otherwise false
	 */
	public static boolean isValidateEmail(String _str){
		int t_at = _str.indexOf("@");
		if(t_at == -1){
			return false;
		}
		
		if(t_at == 0 || (_str.length() - 1) - t_at < 3){
			return false;
		}
		
		String t_addr = _str.substring(t_at + 1);
		
		int t_otherAt = t_addr.indexOf("@");
		if(t_otherAt != -1){
			return false;
		}
		
		int t_dot = t_addr.indexOf(".");
		if(t_dot == -1){
			return false;
		}
		
		if(t_dot == t_addr.length() - 1 ){
			return false;
		}
		
		return true;
	}
	
	/**
	 * is validate user pass 
	 * @param _str
	 * @return
	 */
	public static boolean isValidateUserPass(String _str){
		if(_str.length() < 6){
			return false;
		}
		
		
		for(int i = 0 ;i < _str.length();i++){
			char a = _str.charAt(i);
			if(!Character.isDigit(a) && !Character.isLowerCase(a) && !Character.isUpperCase(a)){
				return false;
			}
		}
		
		return true;
	}
}
