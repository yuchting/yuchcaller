package com.yuchs.yuchcaller;

import java.io.InputStream;
import java.util.Vector;

public class DbIndex {

	//! phone data index list
	private Vector		m_phoneDataList = new Vector();
	
	//! cell phone data index list
	private Vector		m_cellPhoneDataList = new Vector();
	
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
				
				if(_number.length() == 13){
					// country code
					t_countryCode = getCountry(_number.substring(0, 3));
					
					_number = _number.substring(3);
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
				
				return t_countryCode + t_province + t_city + t_carrier;
			}
			
		}catch(Exception e){
			System.out.println("DbIndex Error:" + e.getMessage());
		}
		
		return  "";
	}
	
	//! search the phone data
	private PhoneData searchPhoneData(String _cityNumber){
		
		int t_num4 = Integer.parseInt(_cityNumber.substring(0, 4));
		int t_num3 = Integer.parseInt(_cityNumber.substring(0, 3));
		
		int t_index = binSearch(m_phoneDataList,t_num4);
		if(t_index != -1){
			return (PhoneData)m_phoneDataList.elementAt(t_index);
		}
		
		t_index = binSearch(m_phoneDataList,t_num3);
		if(t_index != -1){
			return (PhoneData)m_phoneDataList.elementAt(t_index);
		}
		
		return null;
	}
	
	//! search the cell phone data
	private CellPhoneData searchCellPhoneData(String _cellPhone){
		int t_num7 = Integer.parseInt(_cellPhone.substring(0,7));
		
		int t_index = binSearch(m_cellPhoneDataList,t_num7);
		if(t_index != -1){
			return (CellPhoneData)m_cellPhoneDataList.elementAt(t_index);
		}
		
		return null;
	}
	
	//! bineary search 
	private int binSearch(Vector _list,int _number){
		int t_begin 	= 0;
		int t_end 		= _list.size() - 1;
		int t_index;
		
		while(t_begin < t_end){
			t_index = (t_begin + t_end) / 2;
			
			PhoneData t_pd = (PhoneData)_list.elementAt(t_index);
			
			int t_cmp = t_pd.Compare(_number);
			
			if(t_cmp < 0){
				t_begin = t_index;
			}else if(t_cmp > 0){
				t_end = t_index;
			}else{
				return t_index;
			}
		}
		
		return -1;
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
		
		int t_num = sendReceive.ReadInt(in);
		for(int i = 0;i< t_num;i++){
			PhoneData t_data = new PhoneData();
			t_data.Read(in);
			
			m_phoneDataList.addElement(t_data);
		}
		
		t_num = sendReceive.ReadInt(in);
		for(int i = 0;i< t_num;i++){
			CellPhoneData t_data = new CellPhoneData();
			t_data.Read(in);
			
			m_cellPhoneDataList.addElement(t_data);
		}
		
		System.out.println("DbIndex read successfully!");
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
