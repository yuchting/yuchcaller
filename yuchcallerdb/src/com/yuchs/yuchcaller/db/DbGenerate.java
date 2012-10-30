package com.yuchs.yuchcaller.db;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

/**
 * 
 * @author tzz
 *
 */
public class DbGenerate {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 2){
			System.out.println("please append a parse file...");
			return ;
		}
		
		(new DbGenerate()).mainProcess(args[1]);
	}
	
	public static final String fsm_outputFilename = "yuchcaller.db";
	
	public static final int	fsm_version = 267235;
		
	private Vector<String>			m_province	= new Vector<String>();
	private Vector<String>			m_city		= new Vector<String>();
	private Vector<String>			m_carrierList		= new Vector<String>();	
	
	private Vector<CellPhoneData>	m_cellPhone = new Vector<CellPhoneData>();
	private Vector<PhoneData>		m_phone = new Vector<PhoneData>();
	
	private Logger					m_log		= new Logger("");
	
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
					t_cityId		= addList(m_city,t_param[2]);
					t_carrierId		= findCarrier(t_param[3],t_param[2]);
					t_areaId		= Integer.parseInt(t_param[4]);
					
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
				}
			}
		}finally{
			in.close();
		}
		
		m_log.LogOut("Prepara data:\nProvince:" + m_province.size() + "\nCity:" + m_city.size() + "\nCell:" + m_cellPhone.size() + "\nPhone:"+m_phone.size());
	}
	
	// add the string to list to return idx
	private static int addList(Vector<String> _list,String _province){
		int t_idx;
		if((t_idx = _list.indexOf(_province)) == -1){
			_list.add(_province);
			
			t_idx = _list.size() - 1;
		}
		
		return t_idx;
	}
	
	// find the carrier idx
	private int findCarrier(String _carrier,String _city){
		for(int i = 0;i< m_carrierList.size();i++){
			if(_carrier.indexOf(m_carrierList.elementAt(i)) != -1){
				return i;
			}	
		}
		
		if(_carrier.indexOf(_city) != -1){
			_carrier = _carrier.substring(_city.length());
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
		sendReceive.WriteStringVector(t_orig, m_city);

		sendReceive.WriteInt(t_orig, m_phone.size());
		for(PhoneData pd : m_phone){
			pd.Write(t_orig);			
		}
		
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
