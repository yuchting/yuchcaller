package com.yuchs.yuchcaller;


public class CallScreenPlugin {
	
	// buffer the former phone number avoid search phone data many times
	private String m_formerNumber = "";
	private String m_formerLocation = "";
	
	public static final int	INCOMING =  1;
	public static final int	WAITING =  1;
	public static final int	OUTGOING =  2;
	public static final int	ACTIVECALL =  2;
	
	final private YuchCaller	m_mainApp;
	
	public CallScreenPlugin(YuchCaller _mainApp){
		m_mainApp = _mainApp;
	}

	// applay the 
	public void apply(int callId,int _screenType){
	}
	

	/**
	 * clear the buffered number 
	 */
	public void clearBufferedNumber(){
		m_formerNumber 	= "";
		m_formerLocation= "";
	}
}
