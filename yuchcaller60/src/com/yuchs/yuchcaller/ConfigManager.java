package com.yuchs.yuchcaller;

import java.util.Vector;

import local.yuchcallerlocalResource;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.PhoneArguments;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.ActiveRichTextField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.util.MathUtilities;

public class ConfigManager extends VerticalFieldManager implements FieldChangeListener {
	
	//! location candidate color
	private static final int[]		fsm_locationCandColor = 
	{
		0,
		0xffffff,
		0xf7d8a2,
		0x52aa80,
		0x0b93df,
		0xcb4ae2,
		0xfef171,
		0xfdbe55,
		0x8a8dfe,
		0xd20005,
		0x59d200,
		0x44c778,
		0xbc9543,
		0xaba8a8,
	};
	
		
	//! font to show main label 
	private Font		m_mainLabelBoldFont = getFont().derive(getFont().getStyle() | Font.BOLD , getFont().getHeight());
	
	//! the config editField of recv-phone vibration
	private EditField m_recvVibrationTime = null;
	
	//! the config editField of hangup-phone vibration
	private EditField m_hangupVibrationTime = null;
	private EditField m_locationInfoPosition_x = null;	
	private EditField m_locationInfoPosition_y = null;
	
	private ObjectChoiceField m_locationInfoColor = null;	
	private EditField m_locationInfoHeight			= null;
	
	private EditField m_searchNumberInput			= null;
	
	private CheckboxField	m_showSystemMenu		= null;
	
	private ColorSampleField m_locationTextColorSample = null;
	
	private ButtonField		m_showDebugScreenBtn	= null;
	
	//! advance switch label
	private LabelField	m_advanceSwitchLabel		= null;
	
	//! parent manager for advance attribute edit field
	private VerticalFieldManager	m_advanceManager	= null;
	private NullField				m_advanceManagerNull= new NullField(Field.NON_FOCUSABLE);
	
	
	//! the search result list
	private SearchResultListMgr		m_intelSearchListMgr = new SearchResultListMgr();
	
	//! intel search input edit field
	private EditField				m_intelSearchInput = null;
	
	//! matched special number
	private Vector					m_matchedList	= new Vector();
	
	//! a page number to display matchedList
	private final	int 			MatchedLabelPageNum	= 5;
	
	//! currrent matched special number index
	private int					m_addMatchedSNIdx		= 0;
	private ClickLabel				m_matchedNextPageLabel	= null;
	
	//! matched special number display allocate poor
	private Vector					m_allocList		= new Vector();
	
	//! about switch label
	private ClickLabel				m_aboutSwitchLabel	= null;
	
	//! the RichText field to show about text
	private ActiveRichTextField		m_aboutTextField	= null;
	private NullField				m_aboutTextFieldNull = new NullField(Field.NON_FOCUSABLE);
	
	
	private YuchCaller	m_mainApp = null;
	
	public ConfigManager(YuchCaller _mainApp){
		
		m_mainApp = _mainApp;
				
		// search the phone
		LabelField t_label = new LabelField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_SEARCH),Field.NON_FOCUSABLE);
		t_label.setFont(m_mainLabelBoldFont);
		add(t_label);
		
		// create the EditField to return dirty false
		m_searchNumberInput		= new InputEditField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_SEARCH_PROMPT),
														EditField.FILTER_NUMERIC);
		
		add(m_searchNumberInput);
		m_searchNumberInput.setChangeListener(this);
		
		// initialize the sample
		m_locationTextColorSample = new ColorSampleField(m_mainApp.getProperties().getLocationColor());
		m_locationTextColorSample.setFont(m_mainApp.generateLocationTextFont());
		add(m_locationTextColorSample);
		
		// add SeparatorField
		add(new SeparatorField(Field.NON_FOCUSABLE));
		
		t_label	= new LabelField(_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_INTEL_SEARCH),Field.NON_FOCUSABLE);
		t_label.setFont(m_mainLabelBoldFont);
		add(t_label);
		
		m_intelSearchInput	= new InputEditField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_INTEL_SEARCH_PROMPT),0);
		add(m_intelSearchInput);
		m_intelSearchInput.setChangeListener(this);
		
		add(m_intelSearchListMgr);
		
		// add SeparatorField
		add(new SeparatorField(Field.NON_FOCUSABLE));
		
		// add the switch
		m_advanceSwitchLabel		= new ClickLabel(getAdvanceSwitchLabelText());		
		m_advanceSwitchLabel.setFont(m_mainLabelBoldFont);
		m_advanceSwitchLabel.setChangeListener(this);
		add(m_advanceSwitchLabel);
		add(m_advanceManagerNull);
		
		// add the about label
		m_aboutSwitchLabel			= new ClickLabel(getAboutSwitchLabelText());
		m_aboutSwitchLabel.setFont(m_mainLabelBoldFont);
		m_aboutSwitchLabel.setChangeListener(this);
		add(m_aboutSwitchLabel);
		add(m_aboutTextFieldNull);
	}
	
	private void showOrHideAdvanceSetting(){
		
		if(m_advanceManager == null){
			
			// initialize the ObjectChoiceField select colorField and find the index of current color
			int t_choiceIdx = 0;
			Object[] t_choiceObj = new Object[fsm_locationCandColor.length];
			for(int i = 0;i < fsm_locationCandColor.length;i++){
				t_choiceObj[i] = new ColorChoiceField(fsm_locationCandColor[i]);
				
				if(fsm_locationCandColor[i] == m_mainApp.getProperties().getLocationColor()){
					t_choiceIdx = i;				
				}
			}
			
			m_advanceManager = new VerticalFieldManager();
			
			LabelField t_label = new LabelField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_VIBRATION),Field.NON_FOCUSABLE);
			t_label.setFont(m_mainLabelBoldFont);
			m_advanceManager.add(t_label);
			
			m_recvVibrationTime = new EditField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_RECV_VIBRATE_TIME),
												Integer.toString(m_mainApp.getProperties().getRecvPhoneVibrationTime()),
												// Vibration time in milliseconds, from 0 to 25500.
												//
												4,
												EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );
			
			m_hangupVibrationTime = new EditField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_HANGUP_VIBRATE_TIME),
												Integer.toString(m_mainApp.getProperties().getHangupPhoneVibrationTime()),
												// Vibration time in milliseconds, from 0 to 25500.
												//
												4,
												EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );
			m_advanceManager.add(m_recvVibrationTime);
			m_advanceManager.add(m_hangupVibrationTime);
			
			
			// separator
			m_advanceManager.add(new SeparatorField());
			
			t_label = new LabelField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL),Field.NON_FOCUSABLE);
			t_label.setFont(m_mainLabelBoldFont);
			m_advanceManager.add(t_label);
			
			if(YuchCaller.fsm_OS_version.startsWith("4.5")){
				m_locationInfoPosition_x = new EditField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_POSITION_X),
						Integer.toString(m_mainApp.getProperties().getLocationPosition_x()),
						3,
						EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );

				m_locationInfoPosition_y = new EditField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_POSITION_Y),
									Integer.toString(m_mainApp.getProperties().getLocationPosition_y()),
									3,
									EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );
				
				
				
				m_advanceManager.add(m_locationInfoPosition_x);
				m_advanceManager.add(m_locationInfoPosition_y);
			}
			
			m_locationInfoHeight = new EditField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_HEIGHT),
					Integer.toString(m_mainApp.getProperties().getLocationHeight()),
					2,
					EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );

			m_locationInfoHeight.setChangeListener(this);
			m_advanceManager.add(m_locationInfoHeight);		
			
			m_locationInfoColor	= new ObjectChoiceField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_COLOR),
														t_choiceObj,t_choiceIdx);
			
			m_advanceManager.add(m_locationInfoColor);
			m_locationInfoColor.setChangeListener(this);
			
			m_showSystemMenu	= new CheckboxField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_SHOW_SYSTEM_MENU), m_mainApp.getProperties().showSystemMenu());
			m_advanceManager.add(m_showSystemMenu);
			
			m_showDebugScreenBtn	= new ButtonField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_SHOW_DEBUG),
														ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
			m_advanceManager.add(m_showDebugScreenBtn);
			m_showDebugScreenBtn.setChangeListener(this);
			
		}
		
		if(m_advanceManagerNull.getManager() != null){
			replace(m_advanceManagerNull, m_advanceManager);
		}else{
			replace(m_advanceManager, m_advanceManagerNull);
		}
		
		m_advanceSwitchLabel.setText(getAdvanceSwitchLabelText());
	}
	
	private String getAdvanceSwitchLabelText(){
		return (m_advanceManagerNull.getManager() != null || m_advanceManager == null)?
						("(+)" + m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_ADVANCE)):
						"(-)" + m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_ADVANCE);
	}
	
	private String getAboutSwitchLabelText(){
		return (m_aboutTextFieldNull.getManager() != null || m_aboutTextField == null)?
					("(+)" + m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_ABOUT)):
					"(-)" + m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_ABOUT);
	}
	
	//! field change event processing function
	public void fieldChanged(Field field, int context) {
		
		if(FieldChangeListener.PROGRAMMATIC == context){
			return;
		}
		
		if(field == m_locationInfoColor){
			
			// change the color
			m_locationTextColorSample.setColor(fsm_locationCandColor[m_locationInfoColor.getSelectedIndex()]);
			ConfigManager.this.invalidate();
			
		}else if(field == m_locationInfoHeight){
			
			// get the height of setting
			int t_height = MathUtilities.clamp(20, getTextFieldNum(m_locationInfoHeight), 40);
			
			// replace sample text
			ColorSampleField t_newField = new ColorSampleField(m_locationTextColorSample.m_color);
			t_newField.setFont(m_locationTextColorSample.getFont().derive(m_locationTextColorSample.getFont().getStyle(),t_height));
			
			replace(m_locationTextColorSample, t_newField);
			m_locationTextColorSample = t_newField;
		}else if(field == m_searchNumberInput){
			if(m_searchNumberInput.getTextLength() > 0){
				String t_number = m_searchNumberInput.getText();
				m_locationTextColorSample.setText(m_mainApp.searchLocation(t_number));
			}			 
		}else if(field == m_advanceSwitchLabel){
			
			showOrHideAdvanceSetting();
			
		}else if(field == m_showDebugScreenBtn){
			
			m_mainApp.popupDebugInfoScreen();
			
		}else if(field == m_intelSearchInput){
			if(m_intelSearchInput.getTextLength() > 0){
				// search the matched result
				m_mainApp.getDbIndex().fillMatchResult(m_intelSearchInput.getText(), m_matchedList, 50);
				m_addMatchedSNIdx = 0;
				
				refreshMatchedList();
			}else{
				clearMatchedList();
			}
		}else if(m_matchedNextPageLabel == field){
			
			m_addMatchedSNIdx += MatchedLabelPageNum;
			Field t_first = addIntelSearchList(m_addMatchedSNIdx);
			t_first.setFocus();
			
		}else if(m_aboutSwitchLabel == field){
			if(m_aboutTextField == null){
				m_aboutTextField	= new ActiveRichTextField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_ABOUT_DETAIL),EditField.READONLY);
			}
			
			if(m_aboutTextFieldNull.getManager() != null){
				replace(m_aboutTextFieldNull, m_aboutTextField);
			}else{
				replace(m_aboutTextField, m_aboutTextFieldNull);
			}
			
			m_aboutSwitchLabel.setText(getAboutSwitchLabelText());
			
		}else if(field instanceof ClickLabel){
			
			// is intel-search result label
			//
			ClickLabel t_cl = (ClickLabel)field;
			if(t_cl.m_speNumber != null){
				if(Dialog.ask(Dialog.D_YES_NO, m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_ASK_DIAL_NUMBER) + "\n" + t_cl.toString(), 
				Dialog.NO) == Dialog.YES){
					try{
						PhoneArguments call = new PhoneArguments(PhoneArguments.ARG_CALL, t_cl.m_speNumber.getDialNumber());
						Invoke.invokeApplication(Invoke.APP_TYPE_PHONE, call);
					}catch(Exception ex){
						m_mainApp.DialogAlert("Dial Error:" + ex.getMessage());
					}					
				}
			}
		}
	}	
	
	
	// save the properties
	public void saveProp(){
		try{

			m_mainApp.getProperties().setRecvPhoneVibrationTime(getTextFieldNum(m_recvVibrationTime));
			m_mainApp.getProperties().setHangupPhoneVibrationTime(getTextFieldNum(m_hangupVibrationTime));
			
			if(m_locationInfoPosition_x != null){
				m_mainApp.getProperties().setLocationPosition_x(getTextFieldNum(m_locationInfoPosition_x));
				m_mainApp.getProperties().setLocationPosition_y(getTextFieldNum(m_locationInfoPosition_y));
			}
			
			m_mainApp.getProperties().setLocationColor(fsm_locationCandColor[m_locationInfoColor.getSelectedIndex()]);
			m_mainApp.getProperties().setLocationHeight(MathUtilities.clamp(20, getTextFieldNum(m_locationInfoHeight), 40));
			
			if(m_showSystemMenu.getChecked() != m_mainApp.getProperties().showSystemMenu()){
				m_mainApp.getProperties().setShowSystemMenu(m_showSystemMenu.getChecked());
				m_mainApp.initMenus(false);
			}

			m_mainApp.getProperties().save();
			
			// notify the yuchcaller to change style of text font
			m_mainApp.changeLocationTextFont();
			
		}catch(Exception ex){
			m_mainApp.SetErrorString("CMSP",ex);
			m_mainApp.DialogAlert("Error! " + ex.getMessage());
		}
	}
	
	// get the text field string and convert it to number 
	public int getTextFieldNum(TextField _text){
		if(_text.getTextLength() == 0){
			return 0;
		}
		
		try{
			return Integer.parseInt(_text.getText());
		}catch(Exception ex){
			m_mainApp.SetErrorString("GTFN",ex);
		}
		
		return 0;
	}
		
	//! main screen is discarded
	protected void onUndisplay(){
		super.onUndisplay();
		
		if(this == m_mainApp.m_configManager){
			m_mainApp.m_configManager = null;
		}		
	}
	
	//! refresh the Matched List 
	private void refreshMatchedList(){
		
		clearMatchedList();
		
		// add the label
		addIntelSearchList(m_addMatchedSNIdx);
	}
	
	//! add a part of list
	private Field addIntelSearchList(int _begin){
		
		// delete former m_matchedNextPageLabel first
		if(m_matchedNextPageLabel != null 
		&& m_matchedNextPageLabel.getManager() != null){
			m_intelSearchListMgr.delete(m_matchedNextPageLabel);
		}	
		
		// get the min page size end number
		int t_end = Math.min(_begin + MatchedLabelPageNum, m_matchedList.size());
		Field t_first = null;
		for(int i = _begin;i < t_end;i++){
			SpecialNumber t_sn = (SpecialNumber)m_matchedList.elementAt(i);
			
			Field t_clickedField = allocLabelField(t_sn);
			
			if(t_first == null){
				t_first = t_clickedField;
			}
			m_intelSearchListMgr.add(t_clickedField);
		}
		
		if(t_end != m_matchedList.size()){
			if(m_matchedNextPageLabel == null){
				m_matchedNextPageLabel = new ClickLabel(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_INTEL_SEARCH_MORE));
				m_matchedNextPageLabel.setChangeListener(this);
			}
			
			m_intelSearchListMgr.add(m_matchedNextPageLabel);
		}
		
		return t_first;
	}
		
	//! clear the matched list
	private void clearMatchedList(){
		
		// release all field
		int t_fieldCount = m_intelSearchListMgr.getFieldCount();
		for(int i = 0;i < t_fieldCount;i++){
			
			Field t_field = m_intelSearchListMgr.getField(i);
			
			if(t_field != m_matchedNextPageLabel){
				// except matched next page label to restore
				//
				m_allocList.addElement(m_intelSearchListMgr.getField(i));
			}
		}
		
		m_intelSearchListMgr.deleteAll();
	}
	
	//! allocate label field
	private ClickLabel allocLabelField(SpecialNumber _sn){
		ClickLabel t_label;
		if(!m_allocList.isEmpty()){
			int t_idx = m_allocList.size() - 1;
			
			t_label = (ClickLabel)m_allocList.elementAt(t_idx);
			t_label.setText(_sn.toString());
			
			m_allocList.removeElementAt(t_idx);
		}else{
			t_label = new ClickLabel(_sn.toString());
			t_label.setChangeListener(this);
		}
		
		t_label.m_speNumber = _sn;
		return t_label;
	}
	
	
	/**
	 * color sample field 
	 * @author tzz
	 *
	 */
	final class ColorSampleField extends LabelField{

		int m_color;
		
		public ColorSampleField(int _color){
			super(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_COLOR_SAMPLE),Field.NON_FOCUSABLE);
			m_color = _color;
		}
		
		public void setColor(int _color){
			m_color = _color;
		}
				
		protected void paint(Graphics graphics) {
			int t_color = graphics.getColor();
			try{
				graphics.setColor(m_color);
				super.paint(graphics);
			}finally{
				graphics.setColor(t_color);
			}
		}
	}
	
	/**
	 * color field to show the choice field
	 * @author tzz
	 *
	 */
	final class ColorChoiceField{

		int m_color;
		
		public ColorChoiceField(int _color){
			m_color = _color;
		}
				
		public String toString(){
			switch(m_color){
			case 0:
				return m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_COLOR_BLACK);
			case 0xffffff:
				return m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_COLOR_WHITE);
			default:
				return m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_COLOR_DUMMY) + " #" + Integer.toHexString(m_color);
			}
		}
	}
	
	/**
	 * Input edit field with prompt text when unfocus and never dirty
	 * @author tzz
	 *
	 */
	final class InputEditField extends EditField{
		final String m_unfocusPrompt;
		
		// unfocus
		public InputEditField(String _prompt,long _style){
			super("","",11,EditField.NO_NEWLINE | _style);
			m_unfocusPrompt		= _prompt;
		}
		
		// never dirty to prompt save
		public boolean isDirty() {
			return false;
		}
		
		protected void onFocus(int direction){
			super.onFocus(direction);
			invalidate();
		}
		
		protected void onUnfocus(){
			super.onUnfocus();
			invalidate();
		}
		
		// paint the prompt text when unfocus
		public void paint(Graphics _g){
			
			if(!isFocus() && getTextLength() == 0	// is not focus and text is zero 
			&& m_unfocusPrompt != null && m_unfocusPrompt.length() > 0){	// unfocusPrompt is valuable
				
				int t_color = _g.getColor();
				try{
					_g.setColor(0xb0b2b0);
					_g.drawText(m_unfocusPrompt, 3, 0);
				}finally{
					_g.setColor(t_color);
				}
			}
			
			super.paint(_g);
		}
	}
	
	//! the intelSearchListMgr
	final class SearchResultListMgr extends VerticalFieldManager{
		
		public SearchResultListMgr(){
			super(Manager.NO_HORIZONTAL_SCROLL);
		}
	}
	
	/**
	 * ClickLabel for some label
	 * 
	 * @author tzz
	 *
	 */
	final class ClickLabel extends LabelField{
	
		public SpecialNumber m_speNumber = null;
		
		public ClickLabel(String _label){
			super(_label,Field.FOCUSABLE);
		}
		
		public boolean isDirty(){
			return false;
		}
		
		protected boolean keyChar( char character, int status, int time){
	        if( character == Characters.ENTER || character == Characters.SPACE) {
	            fieldChangeNotify( 0 );
	            return true;
	        }
	        return super.keyChar( character, status, time );
	    }

	    protected boolean navigationClick( int status, int time ){ 
	        keyChar(Characters.ENTER, status, time );            
	        return true;
	    }		
	}

}
