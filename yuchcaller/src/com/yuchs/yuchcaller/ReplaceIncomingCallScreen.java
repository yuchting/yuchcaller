package com.yuchs.yuchcaller;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;

import local.yuchcallerlocalResource;
import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.FullScreen;

public class ReplaceIncomingCallScreen extends FullScreen {

	private static Bitmap sm_background	= null;
	private static Bitmap sm_answer		= null;
	private static Bitmap sm_ignore		= null;
	
	private static boolean sm_loadBitmap = false;	
	private static Font	sm_displayFont = Font.getDefault().derive(Font.getDefault().getStyle() | Font.BOLD,Font.getDefault().getHeight() + 8);
	private static int	sm_textInterval = 5;
	
	//! contact list 
	private static Vector	sm_contactList 	= null;
	
	//! the phone app screen to restore suspend paint
	private Screen m_phoneAppScreen = null;
	
	//! current phone number
	private String	m_currIncomingNumber = null;
	
	//!	contact name 
	private String m_currContactName	= "";
	
	//! phone prefix
	private String m_phonePrefix		= "";
	
	//! main app
	private YuchCaller m_mainApp = null;
	
	
	
	public ReplaceIncomingCallScreen(final String _phoneNumber,YuchCaller _mainApp){
		
		m_mainApp				= _mainApp;
		m_currIncomingNumber	= _phoneNumber;	
					
		(new Thread(){
			public void run(){
				try{

					if(sm_background == null && !sm_loadBitmap){
						sm_loadBitmap = true;
							
						byte[] bytes = IOUtilities.streamToBytes(m_mainApp.getClass().getResourceAsStream("/background.png"));		
						sm_background =  EncodedImage.createEncodedImage(bytes , 0, bytes .length).getBitmap();
						 
						bytes = IOUtilities.streamToBytes(m_mainApp.getClass().getResourceAsStream("/answer.png"));		
						sm_answer =  EncodedImage.createEncodedImage(bytes , 0, bytes .length).getBitmap();
						 
						bytes = IOUtilities.streamToBytes(m_mainApp.getClass().getResourceAsStream("/ignore.png"));		
						sm_ignore =  EncodedImage.createEncodedImage(bytes , 0, bytes .length).getBitmap();
					}
					
					if(sm_contactList == null){
						loadContactList();
					}
					
					fillRightContact(_phoneNumber);					
					
					ReplaceIncomingCallScreen.this.invalidate();
					
				}catch(Exception ex){
					m_mainApp.SetErrorString("RCS", ex);
				}
			}
		}).start();		
	}
	
	//! load the contact list 
	private void loadContactList(){
		if(sm_contactList == null){
			sm_contactList		= new Vector();
		}
		
		sm_contactList.removeAllElements();
		
		try{

			ContactList t_contactList = (ContactList)PIM.getInstance().openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY);
			
			Enumeration allContacts = t_contactList.items();
			
		    if(allContacts != null){
			    while(allContacts.hasMoreElements()) {
			    	sm_contactList.addElement(allContacts.nextElement());
			    }
		    }
		}catch(Exception ex){
			m_mainApp.SetErrorString("LCL", ex);
		}
	}
	
	private void fillRightContact(final String _phoneNumber){
		for(int i = 0;i < sm_contactList.size();i++){
			BlackBerryContact bbContact = (BlackBerryContact)sm_contactList.elementAt(i);
			
			int t_valueNum = bbContact.countValues(Contact.TEL);
			boolean t_isRightContact = false;
			
			for(int j = 0;j < t_valueNum;j++){
				
				String t_number = bbContact.getString(Contact.TEL, j);
				if(t_number != null){
					if(t_number.equals("13260009715")){
						System.out.print(false);
					}
					
					if(_phoneNumber.length() == 11){
						t_isRightContact = t_number.indexOf(_phoneNumber) != -1;
					}else if(_phoneNumber.length() == 13){
						t_isRightContact = t_number.indexOf(_phoneNumber) != -1 && t_number.length() >= 11;						
					}else if(_phoneNumber.length() == 8){
						t_isRightContact = t_number.indexOf(_phoneNumber) != -1;
					}
					
					if(t_isRightContact){
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
			_g.setColor(0);
			_g.fillRect(0, 0, YuchCaller.fsm_display_width, YuchCaller.fsm_display_height);
		}finally{
			_g.setColor(t_color);
		}
	}
	
	protected void paint(Graphics _g){
		int t_operation_text_y = YuchCaller.fsm_display_height - sm_displayFont.getHeight();
		
		if(sm_background != null){
			
			int bg_x = (YuchCaller.fsm_display_width - sm_background.getWidth()) / 2;
			int bg_y = YuchCaller.fsm_display_height - sm_background.getHeight();
			
			_g.drawBitmap(bg_x,bg_y,sm_background.getWidth(),sm_background.getHeight(),sm_background,0,0);
			
			int btn_y = YuchCaller.fsm_display_height - sm_answer.getHeight();
			_g.drawBitmap(0,btn_y,sm_answer.getWidth(),sm_answer.getHeight(),sm_answer,0,0);
			
			int ignore_x = YuchCaller.fsm_display_width - sm_ignore.getWidth();
			_g.drawBitmap(ignore_x,btn_y,sm_ignore.getWidth(),sm_ignore.getHeight(),sm_ignore,0,0);
			
			t_operation_text_y -= sm_answer.getHeight() + sm_textInterval;
		}
		
		int t_color = _g.getColor();
		try{
			_g.setColor(0xffffff);
			
			int t_ignoreTextWidth = sm_displayFont.getAdvance(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_IGNORE));
			
			_g.drawText(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_ANSWER), 0, t_operation_text_y);
			_g.drawText(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_IGNORE), 
						YuchCaller.fsm_display_width - t_ignoreTextWidth, t_operation_text_y);
			
			int t_phoneNumberTextWidth = sm_displayFont.getAdvance(m_currIncomingNumber);
			
			_g.drawText(m_currIncomingNumber, (YuchCaller.fsm_display_width - t_phoneNumberTextWidth) / 2,
											(YuchCaller.fsm_display_height - sm_displayFont.getHeight()) / 2);
			
			if(m_currContactName.length() != 0){
				int t_contactNameWidth = sm_displayFont.getAdvance(m_currContactName);
				
				_g.drawText(m_currContactName,(YuchCaller.fsm_display_width - t_contactNameWidth) / 2,
											(YuchCaller.fsm_display_height - sm_displayFont.getHeight()) / 2 - sm_displayFont.getHeight() - sm_textInterval);

			}
			
		}finally{
			_g.setColor(t_color);
		}
	}
	
	//! ignore the return key
	public boolean onClose(){
		return false;
	}
	
	// close the replace incoming call screen
    public void close(){
    	m_phoneAppScreen.getUiEngine().suspendPainting(false);
    	m_mainApp.m_replaceIncomingCallScreen = null;
    	super.close();
    }
    
    public boolean keyDown(int keycode,int time){
    	int t_key = Keypad.key(keycode);
    	if(t_key == 18){ // hangup key
    		close();
    	}
    	
    	return super.keyDown(keycode, time);
    }
		
	protected void onObscured(){
		m_phoneAppScreen = UiApplication.getUiApplication().getActiveScreen();
		m_phoneAppScreen.getUiEngine().suspendPainting(true);
    }

    protected void onExposed(){
    	close();
    }
   
}
