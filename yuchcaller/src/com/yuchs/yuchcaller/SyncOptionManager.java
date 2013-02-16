package com.yuchs.yuchcaller;

import java.util.Vector;

import local.yuchcallerlocalResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.EmailAddressEditField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchs.yuchcaller.sync.SyncMain;

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
	 * account manager field
	 */
	private VerticalFieldManager		mAccountMgr = null;
	private NullField					mAccountMgrNull = new NullField(Field.NON_FOCUSABLE);
	
	private EmailAddressEditField		mYuchAccount = null;
	private EditField					mYuchPass	= null;
	
	private ButtonField					mLoginBtn	= null;
	private ButtonField					mSigninBtn	= null;
	
	public SyncOptionManager(YuchCaller _app){
		mMainApp = _app;
		
		add(mAccountMgrNull);
		
		if(mMainApp.getProperties().getYuchAccount().length() == 0
		|| mMainApp.getProperties().getYuchAccessToken().length() == 0){
			prepareAccountMgr();
		}
	}
	
	private void prepareAccountMgr(){
		
		if(mAccountMgr == null){
			mAccountMgr = new VerticalFieldManager();
			
			mYuchAccount = new EmailAddressEditField(mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_ACCOUNT_PREFIX),
													mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_ACCOUNT_INIT));
			
			mYuchPass = new EmailAddressEditField(mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_PASS_PREFIX),
												mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_PASS_INIT));
			
			mAccountMgr.add(mYuchAccount);
			mAccountMgr.add(mYuchPass);
			
			HorizontalFieldManager btnMgr = new HorizontalFieldManager();
			
			mLoginBtn	= new ButtonField(mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_LOGIN_BTN),
										ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY | ButtonField.FIELD_HCENTER);
			
			mSigninBtn	= new ButtonField(mMainApp.m_local.getString(yuchcallerlocalResource.SYNC_SIGNIN_BTN),
										ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY | ButtonField.FIELD_HCENTER);
			
			btnMgr.add(mLoginBtn);
			btnMgr.add(mSigninBtn);
			
			mAccountMgr.add(btnMgr);
			
			mLoginBtn.setChangeListener(this);
			mSigninBtn.setChangeListener(this);
		}
		
		if(mAccountMgr.getManager() == null){
			replace(mAccountMgrNull, mAccountMgr);
		}		
	}

	public void fieldChanged(Field field, int context) {
		if(context != FieldChangeListener.PROGRAMMATIC){
			if(field == mLoginBtn){
				
			}else{
				
			}
		}
		
	}
	
	private void reportInfo(String _info){
		
	}
	
	private void reportError(String _error){
		
	}
	
	private void reportError(String _label,Exception e){
		
	}
	
	
	//! read yuch account and request the Refresh/Access token
	private boolean readYuchAccount(){
		
		YuchCallerProp tProp = mMainApp.getProperties();
		
		if(tProp.getYuchAccount().length() == 0 || tProp.getYuchPass().length() == 0){
			return false;
		}
		
		reportInfo("Reading Yuch Account...");
		
		if(tProp.getYuchAccessToken().length() == 0 || tProp.getYuchRefreshToken().length() == 0){
			
			// request the yuch server
			
			String url = "http://192.168.10.4:8888/f/login/";
			//String url = "http://www.yuchs.com/f/login/";
			
			url += YuchCaller.getHTTPAppendString();
			
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
						
						reportInfo("Read Yuch done!");
						
						return true;
						
					}else{
						reportError("Unkown:" + tResult);
					}					
				}
				
			}catch(Exception e){
				// network problem
				reportError("Can not get the YuchAccount",e);
			}			
		}
		
		return false;
	}
}
