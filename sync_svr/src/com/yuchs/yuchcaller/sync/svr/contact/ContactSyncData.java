package com.yuchs.yuchcaller.sync.svr.contact;

import java.text.SimpleDateFormat;
import java.util.List;

import com.google.gdata.data.TextContent;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.AdditionalName;
import com.google.gdata.data.extensions.City;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.FamilyName;
import com.google.gdata.data.extensions.GivenName;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.NamePrefix;
import com.google.gdata.data.extensions.NameSuffix;
import com.google.gdata.data.extensions.OrgName;
import com.google.gdata.data.extensions.OrgTitle;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.data.extensions.PoBox;
import com.google.gdata.data.extensions.PostCode;
import com.google.gdata.data.extensions.Region;
import com.google.gdata.data.extensions.Street;
import com.google.gdata.data.extensions.StructuredPostalAddress;
import com.yuchs.yuchcaller.sync.svr.GoogleAPIData;
import com.yuchs.yuchcaller.sync.svr.GoogleAPISyncData;

public class ContactSyncData extends GoogleAPISyncData {
	
	private static final String GoogleSchemaPrefix = "http://schemas.google.com/g/2005#";

	private static final String[] GoogleAddressSchemasTypes = 
	{
		"work",
		"home",		
	};
	
	private static final String[] GooglePhoneNumberType =
	{
		"work",
		"work2",
		"home",
		"home2",
		
		"mobile",
		"mobile2",
		"pager",
		"work_fax",
		
		"other",
	};
	
	private static final String[] GoogleEmailType = 
	{
		"work",
		"home",
		"other",
	};	
	

	public ContactData getData(){return (ContactData)m_APIData;}
	public void setData(ContactData data){m_APIData = data;}
	
	@Override
	protected GoogleAPIData newData() {
		return new ContactData();
	}
	
	
	@Override
	public void exportGoogleData(Object g, String timeZoneID) throws Exception {
		ContactEntry contact = (ContactEntry)g;
			
		if(getData().names != null){
			
			Name tNames = null;
			if(contact.hasName()){
				tNames = contact.getName();
			}else{
				tNames = new Name();
			}

			for(int i = 0;i < getData().names.length;i++){
				String n = getData().names[i];
				switch(i){
				case ContactData.NAME_FAMILY:
					if(n != null && n.length() > 0){
						tNames.setFamilyName(new FamilyName(n, null));
					}
					break;
				case ContactData.NAME_GIVEN:
					if(n != null && n.length() > 0){
						tNames.setGivenName(new GivenName(n, null));
					}					
					break;
				case ContactData.NAME_OTHER:
					if(n != null && n.length() > 0){
						tNames.setAdditionalName(new AdditionalName(n, null));
					}
					break;
				case ContactData.NAME_PREFIX:
					if(n != null && n.length() > 0){
						tNames.setNamePrefix(new NamePrefix(n));
					}
					break;
				case ContactData.NAME_SUFFIX:
					if(n != null && n.length() > 0){
						tNames.setNameSuffix(new NameSuffix(n));
					}
					break;				
				}
			}
			
			contact.setName(tNames);
		}
			
		if(getData().addr_home != null || getData().addr_work != null){
			
			// the tList cannot be null (check the getStructuredPostalAddresses source code for detail)
			// 
			List<StructuredPostalAddress> tList = contact.getStructuredPostalAddresses();					
			StructuredPostalAddress[] addreeses = new StructuredPostalAddress[2];
			
			for(int i = 0;i < tList.size();i++){
				StructuredPostalAddress addr = tList.get(i);
				if(addr.getRel() != null){
					
					// search the type
					for(int j = 0;j < GoogleAddressSchemasTypes.length;i++){
						
						if(addr.getRel().endsWith(GoogleAddressSchemasTypes[j])){
							addreeses[i] = addr;
													
							// remove it and add again 
							tList.remove(i);
							i--;
							
							break;
						}
					}
				}
			}
			String[][] arrs = {
				getData().addr_work,
				getData().addr_home,				
			};
			
			for(int i = 0;i < addreeses.length;i++){
				addreeses[i] = genPostalAddress(addreeses[i],arrs[i]);

				if(addreeses[i] != null){
					
					addreeses[i].setRel(GoogleSchemaPrefix + GoogleAddressSchemasTypes[i]);
					tList.add(addreeses[i]);					
				}
			}
		}
		
		if(getData().tel != null){
			
			List<PhoneNumber> tList = contact.getPhoneNumbers();
						
			for(int i = 0;i < getData().tel.length;i++){
				String type 	= GooglePhoneNumberType[i];
				String number	= getData().tel[i]; 
				
				PhoneNumber addNumber = null;
				
				for(PhoneNumber p : tList){
					if( (p.getRel() != null && p.getRel().endsWith(type))
					|| (p.getLabel() != null && p.getLabel().endsWith(type))){
						addNumber = p;
					}
				}
				
				if(addNumber != null){
					if(number == null || number.length() <= 0){
						tList.remove(addNumber);
					}else{
						addNumber.setPhoneNumber(number);
					}
				}else{
					if(number != null && number.length() > 0){
						
						addNumber = new PhoneNumber();
						
						if(type.endsWith("2")){
							addNumber.setLabel(type);
						}else{
							addNumber.setRel(GoogleSchemaPrefix + type);
						}
						
						addNumber.setPhoneNumber(number);
						
						tList.add(addNumber);
					}
				}	
							
			}
		}
		
		if((getData().org != null && getData().org.length() > 0) || (getData().title != null && getData().title.length() >0)){
			
			Organization org = null;
			if(contact.hasOrganizations()){
				org = contact.getOrganizations().get(0);
			}else{
				org = new Organization();
				contact.addOrganization(org);
			}
						
			org.setOrgName(new OrgName(getData().org == null ? "" : getData().org));
			org.setOrgTitle(new OrgTitle(getData().title == null ? "" : getData().title));
			org.setLabel("Work");
			org.setRel("http://schemas.google.com/g/2005#work");
		}
		
		if(getData().email != null){
			
			List<Email> tList = contact.getEmailAddresses();
			
			for(int i = 0;i < getData().email.length;i++){
				for(Email email : tList){
					if((email.getRel() != null && email.getRel().endsWith(GoogleEmailType[i]))){
						
						if(getData().email[i] != null && getData().email[i].length() >0){
							email.setAddress(getData().email[i]);
						}
						
						break;
					}
				}
			}
			
			
		}
	}
		
	/**
	 * genPostalAddress
	 * @param addr
	 * @param arr
	 * @return
	 */
	private StructuredPostalAddress genPostalAddress(StructuredPostalAddress addr,String[] arr){
		
		if(arr != null){
			
			if(addr == null){
				addr = new StructuredPostalAddress();
			}
			
			for(int i = 0;i < arr.length;i++){
				String d = arr[i];
				
				switch(i){
				case ContactData.ADDR_POBOX:
					if(d != null && d.length() > 0){
						addr.setPobox(new PoBox(d));
					}
					
					break;
				case ContactData.ADDR_EXTRA:
					break;
				case ContactData.ADDR_STREET:
					if(d != null && d.length() > 0){
						addr.setStreet(new Street(d));
					}
					break;
				case ContactData.ADDR_LOCALITY:
					if(d != null && d.length() > 0){
						addr.setCity(new City(d));
					}
					break;
				case ContactData.ADDR_REGION:
					if(d != null && d.length() > 0){
						addr.setRegion(new Region(d));
					}
					break;
				case ContactData.ADDR_POSTALCODE:
					if(d != null && d.length() > 0){
						addr.setPostcode(new PostCode(d));
					}
					break;
				case ContactData.ADDR_COUNTRY:
					if(d != null && d.length() > 0){
						addr.setPobox(new PoBox(d));
					}
					break;
				}
			}
		}
				
		return addr;
	}

	@Override
	public void importGoogleData(Object g) throws Exception {
		
		ContactEntry contact = (ContactEntry)g;
		
		if(getData() == null){
			setData(new ContactData());			
		}else{
			getData().clear();
		}
		
		// names
		if(contact.hasName()){
			
			Name tName = contact.getName();
			
			getData().names = new String[]{
				tName.hasFamilyName() 	? tName.getFamilyName().getValue():null,
				tName.hasGivenName() 	? tName.getGivenName().getValue():null,
				tName.hasAdditionalName()? tName.getAdditionalName().getValue():null,
				tName.hasNamePrefix() 	? tName.getNamePrefix().getValue():null,
				tName.hasNameSuffix() 	? tName.getNameSuffix().getValue():null,
			};
			
		}else{
			throw new Exception("[YuchCaller] Contact Entry without names!");
		}
		
		if(contact.hasStructuredPostalAddresses()){

			List<StructuredPostalAddress> tList = contact.getStructuredPostalAddresses();
			
			String[] gdHomeAddr = new String[ContactData.ADDR_SiZE];
			String[] gdWorkAddr = new String[ContactData.ADDR_SiZE];
			
			for(int i = 0;i < tList.size();i++){
				StructuredPostalAddress addr = tList.get(i);
				if(addr.getRel() != null){
					
					String[] gdAddr = null;
					if(addr.getRel().endsWith("home")){
						gdAddr = gdHomeAddr;
					}else if(addr.getRel().endsWith("work")){
						gdAddr = gdWorkAddr;
					}
					
					if(gdAddr != null){
						gdAddr[ContactData.ADDR_POBOX] 		= addr.getPobox()	!= null ? addr.getPobox().getValue() : null;
						gdAddr[ContactData.ADDR_EXTRA] 		= null;
						gdAddr[ContactData.ADDR_STREET] 	= addr.getStreet()	!= null ? addr.getStreet().getValue() : null;
						gdAddr[ContactData.ADDR_LOCALITY]	= addr.getCity()	!= null ? addr.getCity().getValue() : null;
						gdAddr[ContactData.ADDR_REGION]		= addr.getRegion()	!= null ? addr.getRegion().getValue() : null;
						gdAddr[ContactData.ADDR_POSTALCODE]	= addr.getPostcode()!= null ? addr.getPostcode().getValue() : null;
						gdAddr[ContactData.ADDR_COUNTRY]	= addr.getCountry()!= null ? addr.getCountry().getValue() : null;
					}					
				}
			}
			
			getData().addr_home = gdHomeAddr;
			getData().addr_work = gdWorkAddr;
		}
		
		if(contact.hasPhoneNumbers()){
			
			String[] gdTel = new String[ContactData.TEL_SIZE];
			
			List<PhoneNumber> tList = contact.getPhoneNumbers();
			
			for(int i = 0;i < gdTel.length;i++){

				for(PhoneNumber p : tList){
					if((p.getRel() != null && p.getRel().endsWith(GooglePhoneNumberType[i]))
					|| (p.getLabel() != null && p.getLabel().endsWith(GooglePhoneNumberType[i])) ){
						
						gdTel[i] = p.getPhoneNumber();
						
						break;						
					}
				}
			}
						
			getData().tel = gdTel;	
		}
		
		if(contact.hasOrganizations()){
			
			List<Organization> tList = contact.getOrganizations();
			
			Organization organization = tList.get(0);
			getData().org	= organization.hasOrgName() ? organization.getOrgName().getValue() : null;
			getData().title	= organization.hasOrgTitle() ? organization.getOrgTitle().getValue() : null;
			
		}
		
		if(contact.hasEmailAddresses()){
			
			String[] gdemails = new String[ContactData.EMAIL_SIZE];
			
			List<Email> tList = contact.getEmailAddresses();
			
			for(int i = 0;i < gdemails.length;i++){
				
				for(Email email : tList){
					if((email.getRel() != null && email.getRel().endsWith(GoogleEmailType[i]))){
						gdemails[i] = email.getAddress();
					}else{
						gdemails[ContactData.EMAIL_OTHER] = email.getAddress();
					}
				}
			}
			
			getData().email = gdemails;		
		}
		
		if(contact.hasBirthday()){
			String dateStr = contact.getBirthday().getWhen();
			if(dateStr.startsWith("00")){
				dateStr = dateStr.replace("00", "19");
			}
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			getData().birthday = format.parse(dateStr).getTime();
		}
		
		if(contact.getContent() instanceof TextContent){
			TextContent text = (TextContent)contact.getContent();
			getData().note = text.getContent().getPlainText();
		}
	}
	

	/**
	 * compare to ContactSyncData
	 */
	@Override
	public boolean equals(Object o){
		
		if(o instanceof ContactSyncData){
			
			ContactSyncData cmp = (ContactSyncData)o;
			if(cmp.getAPIData() == null || getAPIData() == null){
				return false;
			}
			
			ContactData ownData 	= getData();
			ContactData cmpData 	= cmp.getData();
			
			if(!cmpNameString(cmpData.title,ownData.title)){
				return false;
			}

			if(!cmpNameStringArr(cmpData.names,ownData.names)){
				return false;
			}
			
			if(!cmpNameString(cmpData.nickname,ownData.nickname)){
				return false;
			}
			
			if(!cmpNameStringArr(cmpData.addr_work,ownData.addr_work)){
				return false;
			}
			
			if(!cmpNameStringArr(cmpData.addr_home,ownData.addr_home)){
				return false;
			}
			
			if(!cmpNameStringArr(cmpData.tel,ownData.tel)){
				return false;
			}
			
			if(!cmpNameStringArr(cmpData.email,ownData.email)){
				return false;
			}
			
			if(!cmpNameString(cmpData.org,ownData.org)){
				return false;
			}
			
			if(!cmpNameString(cmpData.note,ownData.note)){
				return false;
			}
			
			if(cmpData.birthday != ownData.birthday){
				return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * compare the string array 
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean cmpNameStringArr(String[] a,String[] b){
		if(a == null && b == null){
			return true;
		}
		
		if(a == null || b == null){
			return false;
		}
		
		if(a.length != b.length){
			return false;
		}
		
		for(int i = 0;i < a.length;i++){
			if(!cmpNameString(a[i], b[i])){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * compare the string whether equal
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean cmpNameString(String a,String b){

		if(a != null){
			
			if(a.length() == 0 && b == null){
				return true;
			}

			return a.equals(b);
			
		}else{
			
			return (b == null || b.length() == 0);
		}
	}
	
	/**
	 * is null array
	 * @param arr
	 * @return
	 */
	private static boolean isNullArr(String[] arr){
		for(String s : arr){
			if(s != null && s.length() > 0){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	private static boolean isNullString(String s){
		return s == null || s.length() == 0;
	}
}
