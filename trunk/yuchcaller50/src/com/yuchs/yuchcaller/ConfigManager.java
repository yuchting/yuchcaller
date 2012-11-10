package com.yuchs.yuchcaller;

import local.yuchcallerlocalResource;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
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
	
	//! is advance manager show
	private boolean				m_advanceManagerShow= false;
	
	private YuchCaller	m_mainApp = null;
	
	public ConfigManager(YuchCaller _mainApp){
		
		m_mainApp = _mainApp;
				
		// search the phone
		LabelField t_label = new LabelField(_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_SEARCH),Field.NON_FOCUSABLE);
		t_label.setFont(m_mainLabelBoldFont);
		add(t_label);
		
		// create the EditField to return dirty false
		m_searchNumberInput		= new EditField("","",11,EditField.NO_NEWLINE | EditField.FILTER_NUMERIC ){
			public boolean isDirty() {
				return false;
			}
		};
		
		add(m_searchNumberInput);
		m_searchNumberInput.setChangeListener(this);
		
		// initialize the sample
		m_locationTextColorSample = new ColorSampleField(YuchCallerProp.instance().getLocationColor());
		m_locationTextColorSample.setFont(m_mainApp.generateLocationTextFont());
		add(m_locationTextColorSample);
		
		// add SeparatorField
		add(new SeparatorField(Field.NON_FOCUSABLE));
		
		// add the switch
		m_advanceSwitchLabel		= new LabelField(getAdvanceSwitchLabel(),Field.FOCUSABLE){
			protected boolean keyChar( char character, int status, int time ){
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
		};
		
		m_advanceSwitchLabel.setFont(m_mainLabelBoldFont);
		m_advanceSwitchLabel.setChangeListener(this);
		add(m_advanceSwitchLabel);
		add(m_advanceManagerNull);		
	}
	
	private void showAdvanceSetting(boolean _show){
		
		if(m_advanceManager == null){
			
			// initialize the ObjectChoiceField select colorField and find the index of current color
			int t_choiceIdx = 0;
			Object[] t_choiceObj = new Object[fsm_locationCandColor.length];
			for(int i = 0;i < fsm_locationCandColor.length;i++){
				t_choiceObj[i] = new ColorChoiceField(fsm_locationCandColor[i]);
				
				if(fsm_locationCandColor[i] == YuchCallerProp.instance().getLocationColor()){
					t_choiceIdx = i;				
				}
			}
			
			m_advanceManager = new VerticalFieldManager();
			
			LabelField t_label = new LabelField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_VIBRATION),Field.NON_FOCUSABLE);
			t_label.setFont(m_mainLabelBoldFont);
			m_advanceManager.add(t_label);
			
			m_recvVibrationTime = new EditField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_RECV_VIBRATE_TIME),
												Integer.toString(YuchCallerProp.instance().getRecvPhoneVibrationTime()),
												// Vibration time in milliseconds, from 0 to 25500.
												//
												4,
												EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );
			
			m_hangupVibrationTime = new EditField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_HANGUP_VIBRATE_TIME),
												Integer.toString(YuchCallerProp.instance().getHangupPhoneVibrationTime()),
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
						Integer.toString(YuchCallerProp.instance().getLocationPosition_x()),
						3,
						EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );

				m_locationInfoPosition_y = new EditField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_POSITION_Y),
									Integer.toString(YuchCallerProp.instance().getLocationPosition_y()),
									3,
									EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );
				
				
				
				m_advanceManager.add(m_locationInfoPosition_x);
				m_advanceManager.add(m_locationInfoPosition_y);
			}
			
			m_locationInfoHeight = new EditField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_HEIGHT),
					Integer.toString(YuchCallerProp.instance().getLocationHeight()),
					2,
					EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );

			m_locationInfoHeight.setChangeListener(this);
			m_advanceManager.add(m_locationInfoHeight);		
			
			m_locationInfoColor	= new ObjectChoiceField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_COLOR),
														t_choiceObj,t_choiceIdx);
			
			m_advanceManager.add(m_locationInfoColor);
			m_locationInfoColor.setChangeListener(this);
			
			m_showSystemMenu	= new CheckboxField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_SHOW_SYSTEM_MENU), YuchCallerProp.instance().showSystemMenu());
			m_advanceManager.add(m_showSystemMenu);
			
			m_showDebugScreenBtn	= new ButtonField(m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_SHOW_DEBUG),
														ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
			m_advanceManager.add(m_showDebugScreenBtn);
			m_showDebugScreenBtn.setChangeListener(this);
			
		}
		
		if(_show){
			replace(m_advanceManagerNull, m_advanceManager);
		}else{
			replace(m_advanceManager, m_advanceManagerNull);
		}
		
		m_advanceSwitchLabel.setText(getAdvanceSwitchLabel());
	}
	
	private String getAdvanceSwitchLabel(){
		return m_advanceManagerShow?("(-)" + m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_ADVANCE)):
						"(+)" + m_mainApp.m_local.getString(yuchcallerlocalResource.PHONE_CONFIG_ADVANCE);
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
			m_advanceManagerShow = !m_advanceManagerShow;
			showAdvanceSetting(m_advanceManagerShow);
		}else if(field == m_showDebugScreenBtn){
			m_mainApp.popupDebugInfoScreen();			
		}
	}	
	
	// save the properties
	public void saveProp(){
		try{

			YuchCallerProp.instance().setRecvPhoneVibrationTime(getTextFieldNum(m_recvVibrationTime));
			YuchCallerProp.instance().setHangupPhoneVibrationTime(getTextFieldNum(m_hangupVibrationTime));
			
			if(m_locationInfoPosition_x != null){
				YuchCallerProp.instance().setLocationPosition_x(getTextFieldNum(m_locationInfoPosition_x));
				YuchCallerProp.instance().setLocationPosition_y(getTextFieldNum(m_locationInfoPosition_y));
			}
			
			YuchCallerProp.instance().setLocationColor(fsm_locationCandColor[m_locationInfoColor.getSelectedIndex()]);
			YuchCallerProp.instance().setLocationHeight(MathUtilities.clamp(20, getTextFieldNum(m_locationInfoHeight), 40));
			
			if(m_showSystemMenu.getChecked() != YuchCallerProp.instance().showSystemMenu()){
				YuchCallerProp.instance().setShowSystemMenu(m_showSystemMenu.getChecked());
				m_mainApp.initMenus(false);
			}

			YuchCallerProp.instance().save();
			
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

}
