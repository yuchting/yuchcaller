package com.yuchs.yuchcaller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

public class DbIndex {
	
	/**
	 * debug index error debug inforamtion output interface
	 * @author tzz
	 *
	 */
	public interface DbIndexDebugOut{
		public void debug(String _tag,Exception e);
		public void debug(String _info);
	}
	
	//! main inputStream to read data
	private ByteArrayInputStream	m_mainInputStream = null;
		
	//! the size of data
	private int		m_phoneDataSize = 0;
	private int		m_cellPhoneDataSize = 0;
	
	//! temprary data for read form the main stream
	private PhoneData	m_tmpPhoneData = new PhoneData();	
	private CellPhoneData m_tmpCellPhoneData = new CellPhoneData();
	
	//! carrier list
	private Vector		m_carrierList = new Vector();
	
	//! province list
	private Vector		m_provinceList = new Vector();
	
	//! city list
	private Vector		m_cityList		= new Vector();
	
	//! special number list
	private Vector		m_specialList = new Vector();
	
	//! the version of db index
	private int		m_dbIndexVersion	= 0;
	
	//! interface to debug infor output
	private DbIndexDebugOut		m_debugOutput	= null;
	
	/**
	 * initailize the data base index with debugouput interface
	 * @param _debugOutput
	 */
	public DbIndex(DbIndexDebugOut _debugOutput){
		m_debugOutput		= _debugOutput;
	}
	
	//! output debug information
	private void debugInfo(String _tag,Exception e){
		if(m_debugOutput != null){
			m_debugOutput.debug(_tag,e);
		}
	}

	//! output debug information
	private void debugInfo(String _info){
		if(m_debugOutput != null){
			m_debugOutput.debug(_info);
		}
	}
	
	//! index the phone number
	public synchronized String findPhoneData(String _number){
		
		try{

			if(_number.length() < 11 && _number.charAt(0) != '+'){
				
				String t_special = specialNumber(_number);
				
				if(t_special.length() == 0){
					// prefix search
					//
					if(_number.charAt(0) == '0' && _number.length() >= 3){
						PhoneData t_pd = searchPhoneData(_number);
						if(t_pd != null){
							t_special = composeLocationInfo(t_pd);
						}
					}else{
						CellPhoneData t_cpd = searchCellPhoneData(_number);
						if(t_cpd != null){
							t_special = composeLocationInfo(t_cpd);
						}
					}
				}
				
				return t_special;
				
			}else if(_number.length() >= 11){
				
				String t_countryCode = "";
				
				if(_number.charAt(0) == '+'){
					
					// country code number
					int t_countryCodeNum = _number.length() - 11;
					t_countryCode = getCountry(_number.substring(0, t_countryCodeNum));
					
					_number = _number.substring(t_countryCodeNum);
				}
								
				if(_number.charAt(0) == '0'){
					
					// fixed phone
					PhoneData t_pd = searchPhoneData(_number);
					if(t_pd != null){
						return t_countryCode + composeLocationInfo(t_pd);
					}
					
				}else{
					
					// cell phone
					CellPhoneData t_cpd = searchCellPhoneData(_number);
					if(t_cpd != null){
						return t_countryCode + composeLocationInfo(t_cpd);
					}
				}
			}
			
		}catch(Exception e){
			debugInfo("FPD", e);
		}
		
		return  "";
	}
	
	// compose location information by PhoneData
	private String composeLocationInfo(PhoneData _pd){
		
		String t_province	= "";
		String t_city		= "";
		String t_carrier	= "";
		
		if(_pd instanceof CellPhoneData){
			CellPhoneData t_cpd = (CellPhoneData)_pd;
			
			t_province	= (String)m_provinceList.elementAt(t_cpd.m_province);
			t_city		= (String)m_cityList.elementAt(t_cpd.m_city);
			t_carrier	= (String)m_carrierList.elementAt(t_cpd.m_carrier);
		}else{
			t_province	= (String)m_provinceList.elementAt(_pd.m_province);
			t_city		= (String)m_cityList.elementAt(_pd.m_city);
		}
		
		if(t_city.equals(t_province)){
			// Beijing / ShangHai...
			return t_province + t_carrier;
		}
		
		return t_province + t_city + t_carrier;
	}
	
	private String specialNumber(String _number){
		try{
			SpecialNumber t_sn = (SpecialNumber)binSearch(Integer.parseInt(_number), 2);
			if(t_sn != null){
				return t_sn.m_presents;
			}
		}catch(Exception ex){
			debugInfo("SN", ex);
		}
		
		return "";		
	}
	
	//! get the data base version
	public int getVersion(){
		return m_dbIndexVersion;
	}
	
	//! search the phone data
	private PhoneData searchPhoneData(String _cityNumber){
		
		try{
			int t_num4;
			if(_cityNumber.length() >= 4){
				t_num4 = Integer.parseInt(_cityNumber.substring(0, 4));

				PhoneData t_pd = (PhoneData)binSearch(t_num4,0);
				if(t_pd != null){
					return t_pd;
				}
			}
			
			int t_num3 = Integer.parseInt(_cityNumber.substring(0, 3));			
			return (PhoneData)binSearch(t_num3,0);
			
		}catch(Exception ex){
			debugInfo("SPD", ex);
		}
		
		return null;
	}
	
	//! search the cell phone data
	private CellPhoneData searchCellPhoneData(String _cellPhone){
		try{
			
			int t_num7 = Integer.parseInt(_cellPhone.substring(0,7));
			return (CellPhoneData)binSearch(t_num7,1);
			
		}catch(Exception ex){
			debugInfo("SCPD",ex);	
		}
		return null;
	}
	
	//! bineary search 
	private BinSearchNumber binSearch(int _number,int _numerType)throws Exception{
		int t_begin 	= 0;
		int t_end;
		
		switch(_numerType){
		case 0:
			t_end = (m_phoneDataSize - 1);
			break;
		case 1:
			t_end = (m_cellPhoneDataSize - 1);
			break;
		default:
			t_end = m_specialList.size() - 1;
			break;
		}
		
		int t_index;
		
		while(t_begin <= t_end){
			t_index = (t_begin + t_end) / 2;
			
			BinSearchNumber t_pd = (BinSearchNumber)readPhoneData(t_index,_numerType);
			
			int t_cmp = t_pd.Compare(_number);
			
			if(t_cmp < 0){
				t_begin = t_index + 1;
			}else if(t_cmp > 0){
				t_end = t_index - 1;
			}else{
				return t_pd;
			}
		}
		
		return null;
	}
	
	private BinSearchNumber readPhoneData(int _index,int _numerType)throws Exception{
		m_mainInputStream.reset();
		switch(_numerType){
		case 0:
			m_mainInputStream.skip(_index * 7 + 4); // 4 bytes size of PhoneData
			m_tmpPhoneData.Read(m_mainInputStream);
			return (BinSearchNumber)m_tmpPhoneData;
		case 1:
			m_mainInputStream.skip(_index * 10 + m_phoneDataSize * 7 + 8);// 8 bytes size of PhoneData & CellPhoneData
			m_tmpCellPhoneData.Read(m_mainInputStream);
			return (BinSearchNumber)m_tmpCellPhoneData;
		default:
			return (BinSearchNumber)m_specialList.elementAt(_index);		// special number index
		}
	}
	
	/**
	 * get the country name
	 * @return
	 */
	private String getCountry(String _code){
		if(_code == "+86"){
			return "";
		}else{
			return "";
		}
	}
	
	/**
	 * read the idx file by InputStream
	 * @param in
	 */
	public synchronized void ReadIdxFile(InputStream in)throws Exception{
		if(in.read() != 'y' || in.read() != 'u' || in.read() != 'c' || in.read() != 'h'){
			throw new Exception("Error YuchCaller Database File Format!");
		}
		
		m_dbIndexVersion = sendReceive.ReadInt(in);
		
		sendReceive.ReadStringVector(in, m_carrierList);
		sendReceive.ReadStringVector(in, m_provinceList);
		sendReceive.ReadStringVector(in, m_cityList);
		
		m_specialList.removeAllElements();
		
		int t_specialNum = sendReceive.ReadInt(in);
		for(int i = 0;i < t_specialNum;i++){
			SpecialNumber t_sn = new SpecialNumber();
			t_sn.Read(in);
			
			m_specialList.addElement(t_sn);
		}
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		try{

			int t_char;
			while((t_char = in.read()) != -1){
				t_os.write(t_char);			
			}
			
			m_mainInputStream = new ByteArrayInputStream(t_os.toByteArray());
			
		}finally{
			t_os.close();
		}
		
		m_phoneDataSize	= sendReceive.ReadInt(m_mainInputStream);
		
		m_mainInputStream.skip(m_phoneDataSize * 7); // size of PhoneData
		
		m_cellPhoneDataSize = sendReceive.ReadInt(m_mainInputStream);
	}
	
//	public static void main(String[] _args)throws Exception{
//		DbIndex t_dbIdx = new DbIndex();
//		
//		FileInputStream t_file = new FileInputStream("yuchcaller.db");
//		try{
//			GZIPInputStream zip = new GZIPInputStream(t_file);
//			t_dbIdx.ReadIdxFile(zip);
//		}finally{
//			t_file.close();
//		}
//		
//		System.out.println(t_dbIdx.findPhoneData("13260009715"));
//	}
	
}
