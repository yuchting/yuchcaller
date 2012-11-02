package com.yuchs.yuchcaller;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.pim.Contact;
import javax.microedition.pim.PIM;

import local.yuchcallerlocalResource;
import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.blackberry.api.pdap.BlackBerryContactList;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.FullScreen;

public class ReplaceIncomingCallScreen extends FullScreen {
	
	private static Font sm_displayFont 			= Font.getDefault().derive(Font.getDefault().getStyle() | Font.BOLD,Font.getDefault().getHeight() + 8);
	private static int	sm_textInterval = 5;
	
	//! contact list 
	private Vector	m_contactList 	= new Vector();
		
	//! current phone number
	private String	m_currIncomingNumber = null;
	
	//!	contact name 
	private String m_currContactName	= "";
	
	//! phone prefix
	private String m_phoneSuffix		= "";
	
	//! location information of searched from the dbIndex
	private String m_locationInfo		= "";
	
	//! main app
	private YuchCaller m_mainApp = null;
	
	//! the phone App
	private UiApplication m_phoneApp = null;	
	
	public ReplaceIncomingCallScreen(String _phoneNumber,YuchCaller _mainApp){
		
		m_mainApp				= _mainApp;
		m_currIncomingNumber	= _phoneNumber;	
		m_phoneApp				= UiApplication.getUiApplication();
		
		loadRightContactList();
		fillRightContactField();					
		
		m_locationInfo = m_mainApp.m_dbIndex.findPhoneData(m_currIncomingNumber);
	}
	
	//! load the contact list 
	private void loadRightContactList(){
			
		m_contactList.removeAllElements();
		
		try{

			BlackBerryContactList t_contactList = (BlackBerryContactList)PIM.getInstance().openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY);
			try{
				Enumeration allContacts = t_contactList.items(m_currIncomingNumber,BlackBerryContactList.SEARCH_CONTACTS);
				
			    if(allContacts != null){
				    while(allContacts.hasMoreElements()) {
				    	m_contactList.addElement(allContacts.nextElement());
				    }
			    }
			    
			}finally{
				t_contactList.close();
			}
			
		}catch(Exception ex){
			m_mainApp.SetErrorString("LCL", ex);
		}
	}
	
	//! prepare the current contact name and prefix by ths phone number
	private void fillRightContactField(){
		
		for(int i = 0;i < m_contactList.size();i++){
			BlackBerryContact bbContact = (BlackBerryContact)m_contactList.elementAt(i);
			
			int t_valueNum = bbContact.countValues(Contact.TEL);
			boolean t_isRightContact = false;
			
			for(int j = 0;j < t_valueNum;j++){
				
				String t_number = bbContact.getString(Contact.TEL, j);
				
				if(t_number != null){
										
					if(m_currIncomingNumber.length() == 11){
						t_isRightContact = t_number.indexOf(m_currIncomingNumber) != -1;
					}else if(m_currIncomingNumber.length() >= 14){
						t_isRightContact = t_number.indexOf(m_currIncomingNumber) != -1 && t_number.length() >= 11;						
					}else if(m_currIncomingNumber.length() >= 8){
						t_isRightContact = t_number.indexOf(m_currIncomingNumber) != -1;
					}
					
					if(t_isRightContact){
						
						switch(bbContact.getAttributes(Contact.TEL, j)){
						case Contact.ATTR_HOME:
							m_phoneSuffix = m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_HOME);
							break;
						case Contact.ATTR_MOBILE:
							m_phoneSuffix = m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_MOBILE);
							break;
						case Contact.ATTR_WORK:
							m_phoneSuffix = m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_WORK);
							break;
						default:
							m_phoneSuffix = m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_OTHER);
							break;
						}
						break;
					}
				}
			}
			
			if(t_isRightContact){
				String[] t_names = bbContact.getStringArray(Contact.NAME, 0);
				
				if(t_names[BlackBerryContact.NAME_PREFIX] != null){
					m_currContactName += t_names[BlackBerryContact.NAME_PREFIX];
				}

				if(t_names[BlackBerryContact.NAME_FAMILY] != null){
					m_currContactName += t_names[BlackBerryContact.NAME_FAMILY]; 
				}
				
				if(t_names[BlackBerryContact.NAME_GIVEN] != null){
					m_currContactName += t_names[BlackBerryContact.NAME_GIVEN]; 
				}
				
				if(t_names[BlackBerryContact.NAME_SUFFIX] != null){
					m_currContactName += t_names[BlackBerryContact.NAME_SUFFIX]; 
				}
				
				if(t_names[BlackBerryContact.NAME_OTHER] != null){
					m_currContactName += t_names[BlackBerryContact.NAME_OTHER]; 
				}
				
				break;
			}
									
		}
	}

	protected void paintBackground(Graphics _g){
		// draw black background
		int t_color = _g.getColor();
		try{
			_g.setColor(0x02102f);
			_g.fillRect(0, 0, YuchCaller.fsm_display_width, YuchCaller.fsm_display_height);
		}finally{
			_g.setColor(t_color);
		}
	}
	
	protected void paint(Graphics _g){
		int t_operation_text_y = YuchCaller.fsm_display_height - sm_displayFont.getHeight();
					
		int bg_x = (YuchCaller.fsm_display_width - m_mainApp.m_backgroundBitmap.getWidth()) / 2;
		int bg_y = YuchCaller.fsm_display_height - m_mainApp.m_backgroundBitmap.getHeight();
		
		_g.drawBitmap(bg_x,bg_y,m_mainApp.m_backgroundBitmap.getWidth(),m_mainApp.m_backgroundBitmap.getHeight(),m_mainApp.m_backgroundBitmap,0,0);
		
		int btn_y = YuchCaller.fsm_display_height - m_mainApp.m_answerBitmap.getHeight();
		_g.drawBitmap(0,btn_y,m_mainApp.m_answerBitmap.getWidth(),m_mainApp.m_answerBitmap.getHeight(),m_mainApp.m_answerBitmap,0,0);
		
		int ignore_x = YuchCaller.fsm_display_width - m_mainApp.m_ignoreBitmap.getWidth();
		_g.drawBitmap(ignore_x,btn_y,m_mainApp.m_ignoreBitmap.getWidth(),m_mainApp.m_ignoreBitmap.getHeight(),m_mainApp.m_ignoreBitmap,0,0);
		
		t_operation_text_y -= m_mainApp.m_answerBitmap.getHeight() + sm_textInterval;		
		
		int t_color = _g.getColor();
		Font t_font = _g.getFont();
		try{
			_g.setColor(0xffffff);
			
			// display the application text
			_g.drawText(m_mainApp.getTitle(),0,0);
			
			// set the big text font
			_g.setFont(sm_displayFont);			
			
			// display the Answer and Ignore button text
			int t_ignoreTextWidth = sm_displayFont.getAdvance(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_IGNORE));
			
			_g.drawText(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_ANSWER), 0, t_operation_text_y);
			_g.drawText(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_IGNORE), 
						YuchCaller.fsm_display_width - t_ignoreTextWidth, t_operation_text_y);
						
			// compose the incoming phone number
			String t_number = m_currIncomingNumber;
			if(m_phoneSuffix.length() != 0){
				t_number += m_phoneSuffix; 
			}
			
			int t_phoneNumberTextWidth	= sm_displayFont.getAdvance(t_number);
			int t_phoneNumberText_y		= (YuchCaller.fsm_display_height - sm_displayFont.getHeight()) / 2;
			
			_g.drawText(t_number, (YuchCaller.fsm_display_width - t_phoneNumberTextWidth) / 2,t_phoneNumberText_y);
			
			// the contact name display
			if(m_currContactName.length() != 0){
				int t_contactNameWidth = sm_displayFont.getAdvance(m_currContactName);				
				_g.drawText(m_currContactName,(YuchCaller.fsm_display_width - t_contactNameWidth) / 2,
												t_phoneNumberText_y - sm_displayFont.getHeight() - sm_textInterval);
			}
			
			// the location information display
			if(m_locationInfo.length() != 0){
				Font t_locationInfoFont = m_mainApp.generateLocationTextFont();
				_g.setFont(t_locationInfoFont);
				
				int t_infoWidth = t_locationInfoFont.getAdvance(m_locationInfo);
				int t_x		= (YuchCaller.fsm_display_width - t_infoWidth) / 2;
				int t_y		= t_phoneNumberText_y + (sm_displayFont.getHeight() + t_locationInfoFont.getHeight()) / 2 + sm_textInterval * 2;
				
				_g.drawText(m_locationInfo,t_x,t_y);
			}
		}finally{
			_g.setColor(t_color);
			_g.setFont(t_font);
		}
	}
	
	//! ignore the return key
	public boolean onClose(){
		return false;
	}
	
	// close the replace incoming call screen
    public void close(){
    	m_mainApp.m_replaceIncomingCallScreen = null;
    	super.close();
    	
    	try{
    		if(m_phoneApp.isPaintingSuspended()){
    			m_phoneApp.suspendPainting(false);
    		}    		
    	}catch(Exception e){
    		m_mainApp.SetErrorString("RICSC", e);
    	}
    	
    }
    
    public boolean keyDown(int keycode,int time){
    	int t_key = Keypad.key(keycode);
    	if(t_key == 18){ // hangup key
    		close();
    	}
    	
    	return super.keyDown(keycode, time);
    }
    public void removeFocus(){
    	super.removeFocus();
    	
    	System.out.print(false);
    }
	protected void onObscured(){
		try{
			m_phoneApp.suspendPainting(true);
		}catch(Exception e){
			m_mainApp.SetErrorString("RICSOO", e);
		}		
    }

    protected void onExposed(){
    	close();
    }
   
}
