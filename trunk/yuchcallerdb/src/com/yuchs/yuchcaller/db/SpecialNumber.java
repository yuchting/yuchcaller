package com.yuchs.yuchcaller.db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;

public class SpecialNumber extends BinSearchNumber implements Comparable<SpecialNumber>{
	
	int 		m_number;
	String		m_presents;
	
	//! client search using weight;
	public int			m_searchWeight;
		
	public void Read(InputStream in)throws Exception{
		m_number	= sendReceive.ReadInt(in);
		m_presents	= sendReceive.ReadString(in); 
	}
	
	public void Write(OutputStream os)throws Exception{		
		sendReceive.WriteInt(os,m_number);
		sendReceive.WriteString(os, m_presents);
	}
	
	//! get the string of number to dial 
	public String getDialNumber(){
		if((m_number & 0x80000000) != 0){
			return DbIndex.export800or400Number(m_number);
		}else{
			return Integer.toString(m_number);
		}
	}
	
	public String toString(){
		return m_presents + " (" + getDialNumber() + ")";
	}

	//! compare with a number for bineary search
	public int Compare(int _number){
		
		if(m_number < _number){
			return -1;
		}else if(m_number > _number){
			return 1;
		}else{
			return 0;
		}		
	}

	@Override
	public int compareTo(SpecialNumber o) {
		if(m_number < o.m_number){
			return -1;
		}else if(m_number > o.m_number){
			return 1;
		}
		
		return 0;
	}
	
	
	// get rid of the same number
	public static void main(String[] _arg)throws Exception{
		if(_arg.length < 2){
			System.out.println("please input the file in order to get rid of same number");
			return;
		}
		
		Vector<SpecialNumber> t_list = new Vector<SpecialNumber>();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
											new FileInputStream(_arg[1]),"UTF-8"));
		try{
			String line;
			
			topWhile:
			while((line = in.readLine()) != null){
				
				String[] t_param = line.split("\t");
				
				if(t_param.length != 2){
					System.err.println(line + " is error to split!");
					continue;
				}
				
				int t_number;
				try{
					if(t_param[1].length() == 10 && (t_param[1].startsWith("400") || t_param[1].startsWith("800"))){
						t_number	= DbIndex.parse800or400Number(t_param[1]);
					}else{
						t_number		= Integer.parseInt(t_param[1]);
					}
				}catch(Exception e){
					System.err.println(line + " number parse error:" + e.getMessage());
					continue;
				}				
				
				// search list
				for(SpecialNumber sn : t_list){
					if(sn.Compare(t_number) == 0){
						continue topWhile;
					}
				}
				
				SpecialNumber t_sn = new SpecialNumber();
				t_sn.m_presents = t_param[0];
				t_sn.m_number	= t_number;
				
				t_list.add(t_sn);
			
			}
		}finally{
			in.close();
		}
		
		final String t_outputFile = "spcialNumber_unique.txt";
		
		BufferedWriter os = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(t_outputFile)));
		try{
			for(SpecialNumber sn : t_list){
				os.write(sn.m_presents + "\t" + DbIndex.export800or400Number(sn.m_number));
				os.write('\n');
			}
		}finally{
			os.close();
		}
		
		System.out.println("Write " + t_list.size() + " unique line to file " + t_outputFile);
	}
}
