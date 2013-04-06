package com.yuchs.yuchcaller.db;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

/**
 * 
 * @author tzz
 *
 */
public class DbGenerate {

	
	
	public static final String fsm_outputFilename = "yuchcaller.db";
	
	public static final int	fsm_version = 274209;
		
	private Vector<String>			m_province	= new Vector<String>();
	private Vector<CityNumber>		m_city		= new Vector<CityNumber>();
	private Vector<String>			m_carrierList		= new Vector<String>();	
	
	private Vector<CellPhoneData>	m_cellPhone		= new Vector<CellPhoneData>();
	private Vector<PhoneData>		m_phone			= new Vector<PhoneData>();
	private Vector<SpecialNumber>	m_specialNumber = new Vector<SpecialNumber>();
	
	private Logger					m_log		= new Logger("");
	
	class CityNumber{
		int areaId;
		String cityName;
		
		@Override
		public String toString(){
			return Integer.toString(areaId) + " " + cityName;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
				
		(new DbGenerate()).mainProcess(Integer.toString(fsm_version) + ".txt");
	}
	
	// main process
	private void mainProcess(String _file){
	
		m_log.EnabelSystemOut(true);

		try{
			prepareData(_file);
			
			generate();
		}catch(Exception ex){
			m_log.PrinterException(ex);
		}
	}
	
	private void prepareData(String _file) throws Exception{
		BufferedReader in = new BufferedReader(new InputStreamReader(
									new FileInputStream(_file),"UTF-8"));
		
		try{
			String line = null;
			
			CellPhoneData	t_preCellPhone = null;
			
			int				t_phoneNum;
			int				t_provinceId;
			int				t_cityId;
			int				t_carrierId;
			int				t_areaId;
			
			boolean			t_added = true;
			
			while((line = in.readLine()) != null){
				
				String[] t_param = line.split("\t");
				
				if(t_param.length == 6){
					
					t_phoneNum		= Integer.parseInt(t_param[0]);
					t_provinceId	= addList(m_province,t_param[1]);
					t_carrierId		= findCarrier(t_param[3],t_param[1]);
					t_areaId		= Integer.parseInt(t_param[4]);
					t_cityId		= addCity(t_param[2],t_areaId);
					
					t_added 		= true;
					
					if(t_preCellPhone != null){
						if(t_preCellPhone.m_province == t_provinceId 
						&& t_preCellPhone.m_city == t_cityId
						&& t_preCellPhone.m_carrier == t_carrierId){
							
							t_preCellPhone.m_phoneNumberEnd++;
														
							t_added = false;
						}
					}
					
					if(t_added){
						t_preCellPhone 					= new CellPhoneData();
						t_preCellPhone.m_phoneNumber	= t_phoneNum;
						t_preCellPhone.m_province		= (byte)t_provinceId;
						t_preCellPhone.m_city			= (short)t_cityId;
						t_preCellPhone.m_carrier		= (byte)t_carrierId;
						
						if(t_preCellPhone.m_carrier == -1){
							System.err.println("Fuck!");							
						}
						
						m_cellPhone.add(t_preCellPhone);
					}
					
					if(!hasAreaPhoneData(t_areaId)){
						PhoneData pd 		= new PhoneData();
						pd.m_phoneNumber	= t_areaId;
						pd.m_province		= (byte)t_provinceId;
						pd.m_city			= (short)t_cityId;
						
						m_phone.add(pd);
					}
				}else if(t_param.length == 2){
					SpecialNumber t_sn = new SpecialNumber();
					t_sn.m_presents		= t_param[0];
					if(t_param[1].length() == 10 && (t_param[1].startsWith("400") || t_param[1].startsWith("800"))){
						t_sn.m_number	= DbIndex.parse800or400Number(t_param[1]);
						if(-2143372537 == t_sn.m_number || t_param[1].equals("4008111111")){
							t_sn.m_number	= DbIndex.parse800or400Number(t_param[1]);
							System.out.println("find");
						}
					}else{
						t_sn.m_number	= Integer.parseInt(t_param[1]);
					}
					
					m_specialNumber.add(t_sn);
				}else{
					System.out.println(line);
				}
			}
		}finally{
			in.close();
		}
		
		
		// sort the phone data
		Collections.sort(m_phone);
		Collections.sort(m_specialNumber);
		
		m_log.LogOut("Prepara data:" +
				"\nProvince:" + m_province.size() + 
				"\nCity:" + m_city.size() + 
				"\nSpecial:" + m_specialNumber.size() +
				"\nCell:" + m_cellPhone.size() + 
				"\nPhone:"+m_phone.size());
	}
	
	// add the string to list to return idx
	private static int addList(Vector<String> _list,String _province){
		
		for(int i = 0;i < _list.size();i++){
			if(_list.get(i).indexOf(_province) != -1){
				return i;
			}
		}
		
		_list.add(_province);
		return _list.size() -1;
	}
	
	// add the string to list to return idx
	private int addCity(String _city,int _areaId){
		CityNumber cn;
		for(int i = 0;i < m_city.size();i++){
			cn = m_city.get(i);
			if(cn.areaId == _areaId){
				if(cn.cityName.indexOf(_city) == -1){
					cn.cityName = cn.cityName + " " + _city; 
				}
				return i;
			}
		}
		
		cn = new CityNumber();
		cn.areaId = _areaId;
		cn.cityName = _city;
		m_city.add(cn);
		
		return m_city.size() - 1;
	}
	
	// find the carrier idx
	private int findCarrier(String _carrier,String _province){
		for(int i = 0;i< m_carrierList.size();i++){
			if(_carrier.indexOf(m_carrierList.elementAt(i)) != -1){
				return i;
			}	
		}
		
		if(_carrier.indexOf(_province) != -1){
			_carrier = _carrier.substring(_province.length());
		}
		
		m_carrierList.addElement(_carrier);
		
		return m_carrierList.size() - 1;
	}
	
	//! has area phone
	private boolean hasAreaPhoneData(int _area){
		for(PhoneData pd : m_phone){
			if(pd.m_phoneNumber == _area){
				return true;
			}
		}
		
		return false;
	}
	
	private void generate()throws Exception{
		
		ByteArrayOutputStream t_orig = new ByteArrayOutputStream();
		
		// magic number string
		t_orig.write('y');
		t_orig.write('u');
		t_orig.write('c');
		t_orig.write('h');
				
		// write number
		sendReceive.WriteInt(t_orig, fsm_version);
		
		// write the table
		sendReceive.WriteStringVector(t_orig, m_carrierList);
		sendReceive.WriteStringVector(t_orig, m_province);
		
		final int t_size = m_city.size();
		sendReceive.WriteInt(t_orig,t_size);
		for(int i = 0;i < t_size;i++){
			sendReceive.WriteString(t_orig,m_city.get(i).cityName);
		}
				
		// special number
		sendReceive.WriteInt(t_orig,m_specialNumber.size());
		for(SpecialNumber sn : m_specialNumber){
			sn.Write(t_orig);
		}
		
		// fixed phone number
		sendReceive.WriteInt(t_orig, m_phone.size());
		for(PhoneData pd : m_phone){
			pd.Write(t_orig);			
		}
		
		// cell phone number
		sendReceive.WriteInt(t_orig, m_cellPhone.size());
		for(CellPhoneData cpd : m_cellPhone){
			cpd.Write(t_orig);			
		}
		
		// compress
		ByteArrayOutputStream zos = new ByteArrayOutputStream();
		GZIPOutputStream zo = new GZIPOutputStream(zos,6);
		zo.write(t_orig.toByteArray());
		zo.close();
		
		byte[] t_zipData = zos.toByteArray();		
		FileOutputStream t_file = new FileOutputStream(fsm_outputFilename);
		try{
			t_file.write(t_zipData);
		}finally{
			t_file.close();
		}
	}
}
