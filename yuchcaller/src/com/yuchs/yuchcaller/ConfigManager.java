package com.yuchs.yuchcaller;

import local.yuchcallerlocalResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.VerticalFieldManager;

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
	
	private ColorSampleField m_locationTextColorSample = null;
	
	public ConfigManager(){
		
		LabelField t_label = new LabelField(YuchCaller.sm_local.getString(yuchcallerlocalResource.PHONE_VIBRATION),Field.NON_FOCUSABLE);
		t_label.setFont(m_mainLabelBoldFont);
		add(t_label);
		
		m_recvVibrationTime = new EditField(YuchCaller.sm_local.getString(yuchcallerlocalResource.PHONE_RECV_VIBRATE_TIME),
											Integer.toString(YuchCallerProp.instance().getRecvPhoneVibrationTime()),
											// Vibration time in milliseconds, from 0 to 25500.
											//
											4,
											EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );
		
		m_hangupVibrationTime = new EditField(YuchCaller.sm_local.getString(yuchcallerlocalResource.PHONE_HANGUP_VIBRATE_TIME),
											Integer.toString(YuchCallerProp.instance().getHangupPhoneVibrationTime()),
											// Vibration time in milliseconds, from 0 to 25500.
											//
											4,
											EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );
		add(m_recvVibrationTime);
		add(m_hangupVibrationTime);
		
		add(new SeparatorField());
		
		t_label = new LabelField(YuchCaller.sm_local.getString(yuchcallerlocalResource.PHONE_CALL),Field.NON_FOCUSABLE);
		t_label.setFont(m_mainLabelBoldFont);
		add(t_label);
		
		m_locationInfoPosition_x = new EditField(YuchCaller.sm_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_POSITION_X),
												Integer.toString(YuchCallerProp.instance().getLocationPosition_x()),
												3,
												EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );
		
		m_locationInfoPosition_y = new EditField(YuchCaller.sm_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_POSITION_Y),
												Integer.toString(YuchCallerProp.instance().getLocationPosition_y()),
												3,
												EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );
		
		m_locationInfoHeight = new EditField(YuchCaller.sm_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_HEIGHT),
												Integer.toString(YuchCallerProp.instance().getLocationHeight()),
												2,
												EditField.NO_NEWLINE | EditField.FILTER_NUMERIC );
		
		m_locationInfoHeight.setChangeListener(this);
		
		add(m_locationInfoPosition_x);
		add(m_locationInfoPosition_y);
		add(m_locationInfoHeight);
		
		
		
		// initialize the ObjectChoiceField select colorField and find the index of current color
		int t_choiceIdx = 0;
		Object[] t_choiceObj = new Object[fsm_locationCandColor.length];
		for(int i = 0;i < fsm_locationCandColor.length;i++){
			t_choiceObj[i] = new ColorChoiceField(fsm_locationCandColor[i]);
			
			if(fsm_locationCandColor[i] == YuchCallerProp.instance().getLocationColor()){
				t_choiceIdx = i;				
			}
		}
		
		m_locationInfoColor	= new ObjectChoiceField(YuchCaller.sm_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_COLOR),
													t_choiceObj,t_choiceIdx);
		
		add(m_locationInfoColor);
		
		// initialize the sample
		m_locationTextColorSample = new ColorSampleField(fsm_locationCandColor[t_choiceIdx]);
		add(m_locationTextColorSample);
		
		m_locationInfoColor.setChangeListener(this);
	}
	

	public void fieldChanged(Field field, int context) {
		if(field == m_locationInfoColor){
			
			// change the color
			m_locationTextColorSample.setColor(fsm_locationCandColor[m_locationInfoColor.getSelectedIndex()]);
			ConfigManager.this.invalidate();
			
		}else if(field == m_locationInfoHeight){
			
			// get the height of setting
			int t_height = getTextFieldNum(m_locationInfoHeight);
			
			t_height = Math.min(40, t_height);
			t_height = Math.max(20, t_height);
			
			// replace sample text
			ColorSampleField t_newField = new ColorSampleField(m_locationTextColorSample.m_color);
			t_newField.setFont(m_locationTextColorSample.getFont().derive(m_locationTextColorSample.getFont().getStyle(),t_height));
			
			replace(m_locationTextColorSample, t_newField);
			m_locationTextColorSample = t_newField;
		}
		
	}
	
	// save the properties
	public void saveProp(){
		try{

			YuchCallerProp.instance().setRecvPhoneVibrationTime(getTextFieldNum(m_recvVibrationTime));
			YuchCallerProp.instance().setHangupPhoneVibrationTime(getTextFieldNum(m_hangupVibrationTime));
			YuchCallerProp.instance().setLocationPosition_x(getTextFieldNum(m_locationInfoPosition_x));
			YuchCallerProp.instance().setLocationPosition_y(getTextFieldNum(m_locationInfoPosition_y));
			YuchCallerProp.instance().setLocationColor(fsm_locationCandColor[m_locationInfoColor.getSelectedIndex()]);
			
			YuchCallerProp.instance().save();
			
			// notify the yuchcaller to change style of text font
			YuchCaller.sm_instance.changeLocationTextFont();
			
		}catch(Exception ex){
			YuchCaller.sm_instance.SetErrorString("CMSP",ex);
			YuchCaller.sm_instance.DialogAlert("Error! " + ex.getMessage());
		}
	}
	
	// get the text field string and convert it to number 
	public static int getTextFieldNum(TextField _text){
		if(_text.getTextLength() == 0){
			return 0;
		}
		
		try{
			
			return Integer.parseInt(_text.getText());
			
		}catch(Exception ex){
			YuchCaller.sm_instance.SetErrorString("GTFN",ex);
		}
		
		return 0;
	}
	
	/**
	 * color sample field 
	 * @author tzz
	 *
	 */
	final class ColorSampleField extends LabelField{

		int m_color;
		
		public ColorSampleField(int _color){
			super(YuchCaller.sm_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_COLOR_SAMPLE),Field.NON_FOCUSABLE | Field.FIELD_RIGHT);
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
				return YuchCaller.sm_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_COLOR_BLACK);
			case 0xffffff:
				return YuchCaller.sm_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_COLOR_WHITE);
			default:
				return YuchCaller.sm_local.getString(yuchcallerlocalResource.PHONE_CALL_TEXT_COLOR_DUMMY) + " #" + Integer.toHexString(m_color);
			}
		}
	}

}
