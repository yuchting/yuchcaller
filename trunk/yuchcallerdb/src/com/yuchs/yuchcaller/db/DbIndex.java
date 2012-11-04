package com.yuchs.yuchcaller.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

public class DbIndex {
	
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
	
	//! the version of db index
	private int		m_dbIndexVersion	= 0;
	
	//! index the phone number
	public synchronized String findPhoneData(String _number){
		
		try{

			if(_number.length() < 11){
				
				return "";
				
			}else if(_number.length() >= 11){
				
				String t_countryCode = "";
				
				if(_number.charAt(0) == '+'){
					
					// country code number
					int t_countryCodeNum = _number.length() - 11;
					t_countryCode = getCountry(_number.substring(0, t_countryCodeNum));
					
					_number = _number.substring(t_countryCodeNum);
				}
				
				String t_province	= "";
				String t_city		= "";
				String t_carrier	= "";
				
				if(_number.charAt(0) == '0'){
					
					// phone
					PhoneData t_pd = searchPhoneData(_number);
					if(t_pd != null){
						t_province	= (String)m_provinceList.elementAt(t_pd.m_province);
						t_city		= (String)m_cityList.elementAt(t_pd.m_city);
					}
					
				}else{
					
					// cell phone
					CellPhoneData t_cpd = searchCellPhoneData(_number);
					if(t_cpd != null){
						t_province	= (String)m_provinceList.elementAt(t_cpd.m_province);
						t_city		= (String)m_cityList.elementAt(t_cpd.m_city);
						t_carrier	= (String)m_carrierList.elementAt(t_cpd.m_carrier);
					}
				}
				
				if(t_city.equals(t_province)){
					// Beijing / ShangHai...
					return t_countryCode + t_province + t_carrier;
				}
				
				return t_countryCode + t_province + t_city + t_carrier;
			}
			
		}catch(Exception e){
			System.out.println("DbIndex Error:" + e.getMessage());
		}
		
		return  "";
	}
	
	//! get the data base version
	public int getVersion(){
		return m_dbIndexVersion;
	}
	
	//! search the phone data
	private PhoneData searchPhoneData(String _cityNumber){
		
		try{

			int t_num4 = Integer.parseInt(_cityNumber.substring(0, 4));
			int t_num3 = Integer.parseInt(_cityNumber.substring(0, 3));
			
			PhoneData t_pd = binSearch(t_num4,true);
			if(t_pd != null){
				return t_pd;
			}
			
			return binSearch(t_num3,true);
		}catch(Exception ex){
			System.out.println(ex.getMessage());
			
			return null;
		}
		
	}
	
	//! search the cell phone data
	private CellPhoneData searchCellPhoneData(String _cellPhone){
		try{

			int t_num7 = Integer.parseInt(_cellPhone.substring(0,7));
			return (CellPhoneData)binSearch(t_num7,false);
		}catch(Exception ex){
			
			System.out.println(ex.getMessage());
			
			return null;
		}
	}
	
	//! bineary search 
	private PhoneData binSearch(int _number,boolean _phoneOrCell)throws Exception{
		int t_begin 	= 0;
		int t_end 		= _phoneOrCell?(m_phoneDataSize - 1) : (m_cellPhoneDataSize - 1);
		int t_index;
		
		while(t_begin <= t_end){
			t_index = (t_begin + t_end) / 2;
			
			PhoneData t_pd = (PhoneData)readPhoneData(t_index,_phoneOrCell);
			
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
	
	private PhoneData readPhoneData(int _index,boolean _phoneOrCell)throws Exception{
		m_mainInputStream.reset();
		if(_phoneOrCell){
			m_mainInputStream.skip(_index * 7 + 4); // 4 bytes size of PhoneData
			m_tmpPhoneData.Read(m_mainInputStream);
			return m_tmpPhoneData;
		}else{
			m_mainInputStream.skip(_index * 10 + m_phoneDataSize * 7 + 8);// 8 bytes size of PhoneData & CellPhoneData
			m_tmpCellPhoneData.Read(m_mainInputStream);
			return m_tmpCellPhoneData;
		}
	}
	
	/**
	 * get the country name
	 * @return
	 */
	private String getCountry(String _code){
		if(_code == "+86" || _code == "086"){
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
	
	public static void main(String[] _args)throws Exception{
		DbIndex t_dbIdx = new DbIndex();
		
		FileInputStream t_file = new FileInputStream("yuchcaller.db");
		try{
			GZIPInputStream zip = new GZIPInputStream(t_file);
			t_dbIdx.ReadIdxFile(zip);
		}finally{
			t_file.close();
		}
		
		System.out.println(t_dbIdx.findPhoneData("13000009715"));
	}
	
}
