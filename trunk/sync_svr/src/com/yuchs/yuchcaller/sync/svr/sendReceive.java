/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchs.yuchcaller.sync.svr;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

public class sendReceive {
	
	// static function to input and output integer
	//
	static public void WriteStringVector(OutputStream _stream,Vector _vect)throws Exception{
		
		final int t_size = _vect.size();
		WriteInt(_stream,t_size);
		
		for(int i = 0;i < t_size;i++){
			WriteString(_stream,(String)_vect.elementAt(i));
		}
	}
	
	static public void WriteStringVector(OutputStream _stream,String[] _vect)throws Exception{
		
		final int t_size = _vect.length;
		WriteInt(_stream,t_size);
		
		for(int i = 0;i < t_size;i++){
			WriteString(_stream,(String)_vect[i]);
		}
	}
	
	static public void WriteString(OutputStream _stream,String _string)throws Exception{
		
		if(_string == null || _string.length() == 0){
			WriteInt(_stream,0);
		}else{
			byte[] t_strByte = _string.getBytes("UTF-8");
			
			WriteInt(_stream,t_strByte.length);
			_stream.write(t_strByte);			
		}
	}
		
	static public void ReadStringVector(InputStream _stream,Vector _vect)throws Exception{
		
		_vect.removeAllElements();
		
		final int t_size = ReadInt(_stream);
				
		for(int i = 0;i < t_size;i++){
			_vect.addElement(ReadString(_stream));
		}
	}
	
	static public String ReadString(InputStream _stream)throws Exception{
		
		final int len = ReadInt(_stream);
		
		if(len != 0){
			byte[] t_buffer = new byte[len];
			
			ForceReadByte(_stream,t_buffer,len);

			return new String(t_buffer,"UTF-8");
		}
		
		return "";
		
	}
	
	static public int ReadInt(InputStream _stream)throws Exception{
		
		int[] t_byte = {0,0,0,0};
	
		int t_counter = 0;
		
		for(int i = 0;i < t_byte.length;i++){
			
			while(true){
				
				t_byte[i] = _stream.read();				
				
				if(t_byte[i] == -1){
					
					if(t_counter++ >= 20){
						return -1;
					}
					// first sleep 
					//
					Thread.sleep(20);	
					continue;
					
				}else{
					break;
				}
			}			
						
		}
		
		return t_byte[0] | (t_byte[1] << 8) | (t_byte[2]  << 16) | (t_byte[3] << 24);	
	}
	
	static public short ReadShort(InputStream _stream)throws Exception{
		
		int[] t_byte = {0,0};
	
		int t_counter = 0;
		
		for(int i = 0;i < t_byte.length;i++){
			
			while(true){
				
				t_byte[i] = _stream.read();				
				
				if(t_byte[i] == -1){
					
					if(t_counter++ >= 20){
						return -1;
					}
					// first sleep 
					//
					Thread.sleep(20);	
					continue;
					
				}else{
					break;
				}
			}			
						
		}
		
		return (short)(t_byte[0] | (t_byte[1] << 8));
	}
	
	static public long ReadLong(InputStream _stream)throws Exception{
		final int t_timeLow = sendReceive.ReadInt(_stream);
		final long t_timeHigh = sendReceive.ReadInt(_stream);
				
		if(t_timeLow >= 0){
			return ((t_timeHigh << 32) | (long)(t_timeLow));
		}else{
			return ((t_timeHigh << 32) | (((long)(t_timeLow & 0x7fffffff)) | 0x80000000L));
		}
	}
		
	static public void WriteLong(OutputStream _stream,long _val)throws Exception{		
		sendReceive.WriteInt(_stream,(int)_val);
		sendReceive.WriteInt(_stream,(int)(_val >>> 32));
	}
	
	static public void WriteShort(OutputStream _stream,short _val)throws Exception{
		_stream.write(_val);
		_stream.write(_val >>> 8 );
	}

	static public void WriteInt(OutputStream _stream,int _val)throws Exception{
		_stream.write(_val);
		_stream.write(_val >>> 8 );
		_stream.write(_val >>> 16);
		_stream.write(_val >>> 24);
	}
	
	static public void WriteDouble(OutputStream _stream,double _val)throws Exception{
		if(_val == 0){
			WriteInt(_stream,0);
		}else{
			String t_valString = Double.toString(_val);
			WriteString(_stream,t_valString);
		}		
	}
	
	static public void WriteFloat(OutputStream _stream,float _val)throws Exception{
		if(_val == 0){
			WriteInt(_stream,0);
		}else{
			String t_valString = Float.toString(_val);
			WriteString(_stream,t_valString);
		}
	}
	
	static public double ReadDouble(InputStream _stream)throws Exception{
		String t_valString = ReadString(_stream);
		if(t_valString.length() == 0){
			return 0;
		}else{
			return Double.valueOf(t_valString).doubleValue();			
		}
		
	}
	
	static public float ReadFloat(InputStream _stream)throws Exception{
		String t_valString = ReadString(_stream);
		if(t_valString.length() == 0){
			return 0;
		}else{
			return Float.valueOf(t_valString).floatValue();
		}
	}
	
	static public void WriteBoolean(OutputStream _stream,boolean _val)throws Exception{
		_stream.write(_val?1:0);
	}
	static public boolean ReadBoolean(InputStream _stream)throws Exception{
		
		int t_counter = 0;
		int t_val = 0;
		while(true){
			
			t_val = _stream.read();				
			
			if(t_val == -1){
				
				if(t_counter++ >= 20){
					return false;
				}
				// first sleep 
				//
				Thread.sleep(20);	
				continue;
				
			}else{
				break;
			}
		}			

		return t_val == 1;		
	}
	
	static public void ForceReadByte(InputStream _stream,byte[] _buffer,int _readLen)throws Exception{
		int t_readIndex = 0;
		int t_counter = 0;
		
		while(_readLen > t_readIndex){
			
			final int t_c = _stream.read(_buffer,t_readIndex,_readLen - t_readIndex);
			
			if(t_c > 0){
				t_readIndex += t_c;
			}else{
								
				if(++t_counter > 20){
					throw new Exception("FroceReadByte failed to read " + _readLen );
				}
				
				// first sleep 
				//
				Thread.sleep(20);
			}
		}
	}
}