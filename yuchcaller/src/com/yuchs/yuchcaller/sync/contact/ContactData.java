package com.yuchs.yuchcaller.sync.contact;

import java.io.InputStream;
import java.io.OutputStream;

import com.yuchs.yuchcaller.sendReceive;
import com.yuchs.yuchcaller.sync.AbsData;

public class ContactData implements AbsData {

	/**
	 * NAME string array index
	 */
	public static final int NAME_FAMILY = 0;
	public static final int NAME_GIVEN = 1;
	public static final int NAME_OTHER = 2;
	public static final int NAME_PREFIX = 3;
	public static final int NAME_SUFFIX = 4;
	
	/**
	 * ADDR string array index
	 */
	public static final int ADDR_POBOX = 0;
	public static final int ADDR_EXTRA = 1;
	public static final int ADDR_STREET = 2;
	public static final int ADDR_LOCALITY = 3;
	public static final int ADDR_REGION = 4;
	public static final int ADDR_POSTALCODE = 5;
	public static final int ADDR_COUNTRY = 6;
	
	public static final int ADDR_SiZE = ADDR_COUNTRY + 1;
	
	/**
	 * telephone number attribute
	 */
	public static final int TEL_WORK	= 0;
	public static final int TEL_WORK2	= 1;
	public static final int TEL_HOME	= 2;
	public static final int TEL_HOME2	= 3;
	public static final int TEL_MOBILE= 4;
	public static final int TEL_MOBILE2= 5;
	public static final int TEL_PAGER	= 6;
	public static final int TEL_FAX	= 7;
	public static final int TEL_OTHER	= 8;
	
	public static final int TEL_SIZE = TEL_OTHER + 1;
	
	/**
	 * email
	 */
	public static final int EMAIL_WORK = 0;
	public static final int EMAIL_HOME = 1;
	public static final int EMAIL_OTHER = 2;
	
	public static final int EMAIL_SIZE = EMAIL_OTHER + 1;
	
	
	/**
	 * job title
	 */
	public String title		= null;
	
	/**
	 * name string array
	 */
	public String[] names	= null;
	
	/**
	 * nickname
	 */
	public String nickname	= null;
	
	/**
	 * address string array
	 */
	public String[] addr_work	= null;
	public String[] addr_home	= null;
	
	/**
	 * telephone number
	 */
	public String[] tel		= null;
	
	/**
	 * email address string
	 */
	public String[] email	= null;
	
	/**
	 * organization(company)
	 */
	public String org		= null;
	
	/**
	 * note 
	 */
	public String note		= null;
	
	/**
	 * birthday
	 */
	public long	birthday	= 0;
	
	/**
	 * clear the all attribute  
	 */
	public void clear() {
		title = null;
		names = null;
		nickname = null;
		addr_work = null;
		addr_home = null;
		tel = null;
		email = null;
		org = null;		
		note = null;
		birthday = 0;
	}

	public void inputData(InputStream in) throws Exception {
		title	= sendReceive.ReadString(in);
		names	= sendReceive.ReadStringArr(in);
		nickname	= sendReceive.ReadString(in);
		addr_work	= sendReceive.ReadStringArr(in);
		addr_home	= sendReceive.ReadStringArr(in);
		tel		= sendReceive.ReadStringArr(in);
		email	= sendReceive.ReadStringArr(in);
		org		= sendReceive.ReadString(in);
		birthday= sendReceive.ReadLong(in);
	}

	public void outputData(OutputStream os) throws Exception {
		sendReceive.WriteString(os,title);
		sendReceive.WriteStringArr(os,names);
		sendReceive.WriteString(os,nickname);
		sendReceive.WriteStringArr(os,addr_work);
		sendReceive.WriteStringArr(os,addr_home);
		sendReceive.WriteStringArr(os,tel);
		sendReceive.WriteStringArr(os,email);
		sendReceive.WriteString(os,org);
		sendReceive.WriteLong(os,birthday);
	}

}

