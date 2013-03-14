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
package com.yuchs.yuchcaller.sync.svr.contact;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.gdata.data.Content;
import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.TextContent;
import com.google.gdata.data.contacts.Birthday;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.AdditionalName;
import com.google.gdata.data.extensions.City;
import com.google.gdata.data.extensions.Country;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.FamilyName;
import com.google.gdata.data.extensions.GivenName;
import com.google.gdata.data.extensions.HouseName;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.NamePrefix;
import com.google.gdata.data.extensions.NameSuffix;
import com.google.gdata.data.extensions.Neighborhood;
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
				
				if(isNullString(n)){
					continue;
				}
				
				switch(i){
				case ContactData.NAME_FAMILY:
					tNames.setFamilyName(new FamilyName(n, null));					
					break;
				case ContactData.NAME_GIVEN:
					tNames.setGivenName(new GivenName(n, null));				
					break;
				case ContactData.NAME_OTHER:
					tNames.setAdditionalName(new AdditionalName(n, null));
					break;
				case ContactData.NAME_PREFIX:
					tNames.setNamePrefix(new NamePrefix(n));
					break;
				case ContactData.NAME_SUFFIX:
					tNames.setNameSuffix(new NameSuffix(n));
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
					addreeses[i].setPrimary(i == 0);
					
					tList.add(addreeses[i]);					
				}
			}
		}
		
		if(getData().tel != null){
			
			List<PhoneNumber> tList = contact.getPhoneNumbers();
			List<PhoneNumber> tAddList = new ArrayList<PhoneNumber>();
			
			for(int i = 0;i < getData().tel.length;i++){
				String type 	= GooglePhoneNumberType[i];
				String number	= getData().tel[i]; 
				
				boolean modified = false;
				
				for(PhoneNumber p : tList){
					if( (p.getRel() != null && p.getRel().endsWith(type))
					|| (p.getLabel() != null && p.getLabel().endsWith(type))){
						
						if(!isNullString(number)){
							p.setPhoneNumber(number);
						}else{
							tList.remove(p);
						}
						
						modified = true;

						break;
					}
				}
				
				if(!modified){
					if(!isNullString(number)){
						
						PhoneNumber addNumber = new PhoneNumber();
						
						// can NOT set Rel or Label both
						//
						if(type.endsWith("2")){
							addNumber.setLabel(type);
						}else{
							addNumber.setRel(GoogleSchemaPrefix + type);
						}
						addNumber.setPrimary(i == 0);
						addNumber.setPhoneNumber(number);
						
						tAddList.add(addNumber);
					}
				}			
			}
			
			// add the phone to already list
			for(PhoneNumber p : tAddList){
				tList.add(p);
			}
		}
		
		if(!isNullString(getData().org) || !isNullString(getData().title)){
			
			Organization org = null;
			if(contact.hasOrganizations()){
				org = contact.getOrganizations().get(0);
			}else{
				org = new Organization();
				contact.addOrganization(org);
			}
			
			if(!isNullString(getData().org)){
				org.setOrgName(new OrgName(getData().org));
			}
			
			if(!isNullString(getData().title)){
				org.setOrgTitle(new OrgTitle(getData().title));
			}
			
			org.setRel(GoogleSchemaPrefix + "work");
		}
		
		if(getData().email != null){
			
			List<Email> tList = contact.getEmailAddresses();
			List<Email> tAddList = new ArrayList<Email>();
			
			for(int i = 0;i < getData().email.length;i++){
				
				String type		= GoogleEmailType[i];
				String emailStr = getData().email[i];
				
				boolean modified = false;
				
				for(Email email : tList){
					if(email.getRel() != null && email.getRel().endsWith(type)){
						
						if(!isNullString(emailStr)){
							email.setAddress(emailStr);
						}else{
							tList.remove(email);
						}
						
						modified = true;
						
						break;
					}
				}
				
				if(!modified){
					
					if(!isNullString(emailStr)){
						// haven't added to email
						Email email = new Email();
						email.setRel(GoogleSchemaPrefix + type);
						email.setAddress(emailStr);
						
						if(i == 0){
							email.setPrimary(true);
						}
						
						tAddList.add(email);
					}					
				}
			}
			
			// add the addList to email address
			for(Email e : tAddList){
				tList.add(e);
			}
		}
		
		if(getData().birthday > 0){
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			contact.setBirthday(new Birthday(format.format(new Date(getData().birthday - TimeZone.getTimeZone(timeZoneID).getRawOffset()))));
		}

		if(!isNullString(getData().note)){
			contact.setContent(TextConstruct.plainText(getData().note));
		}else{
			if(contact.getContent() instanceof TextContent){
				contact.setContent((Content)null);
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
				if(isNullString(d)){
					continue;
				}
				
				switch(i){
				case ContactData.ADDR_POBOX:
					addr.setPobox(new PoBox(d));
					break;
				case ContactData.ADDR_EXTRA:
					addr.setNeighborhood(new Neighborhood(d));
					break;
				case ContactData.ADDR_STREET:
					addr.setStreet(new Street(d));
					break;
				case ContactData.ADDR_LOCALITY:
					addr.setCity(new City(d));
					break;
				case ContactData.ADDR_REGION:
					addr.setRegion(new Region(d));
					break;
				case ContactData.ADDR_POSTALCODE:
					addr.setPostcode(new PostCode(d));
					break;
				case ContactData.ADDR_COUNTRY:
					addr.setCountry(new Country("CN",d));
					break;
				}
			}
		}
				
		return addr;
	}

	@Override
	public void importGoogleData(Object g,String timeZoneID) throws Exception {
		
		ContactEntry contact = (ContactEntry)g;
		
		setGID(getContactEntryId(contact));
		setLastMod(contact.getUpdated().getValue());
		
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
						gdAddr[ContactData.ADDR_EXTRA] 		= addr.getNeighborhood() != null ? addr.getNeighborhood().getValue() : null;
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
					
					String type = GooglePhoneNumberType[i];
										
					if(p.getRel() != null && p.getRel().endsWith(type)){
						gdTel[i] = p.getPhoneNumber();
					}else if(p.getLabel() != null && p.getLabel().equals(type)){
						gdTel[i] = p.getPhoneNumber();
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
			getData().birthday = format.parse(dateStr).getTime() + TimeZone.getTimeZone(timeZoneID).getRawOffset();
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

			// current BB system can't be store OTHER SUFFIX name
			if(!cmpNameStringArr(cmpData.names,ownData.names,(1 << ContactData.NAME_OTHER) | (1 << ContactData.NAME_SUFFIX))){
				return false;
			}
			
			if(!cmpNameString(cmpData.nickname,ownData.nickname)){
				return false;
			}
			
			// current BB system can't be store POBOX address attribute
			if(!cmpNameStringArr(cmpData.addr_work,ownData.addr_work,(1 << ContactData.ADDR_POBOX))
			|| !cmpNameStringArr(cmpData.addr_home,ownData.addr_home,(1 << ContactData.ADDR_POBOX))){
				return false;
			}
						
			if(!cmpNameStringArr(cmpData.tel,ownData.tel,0)){
				return false;
			}
			
			// compare the email
			// because the BB system can't store the attribute of email so the difference index of array has the same attribute(Contact.ATTR_NONE)
			//
			if(cmpData.email != null){
				if(ownData.email != null){
					for(String a : cmpData.email){
						
						boolean found = false;
						
						for(String b : ownData.email){
							if(cmpNameString(a,b)){
								found = true;
								break;
							}
						}
						
						if(!found){
							return false;
						}
					}
				}else{
					if(!cmpNameStringArr(cmpData.email, ownData.email, 0)){
						return false;
					}
				}				
			}else{
				if(!cmpNameStringArr(cmpData.email, ownData.email, 0)){
					return false;
				}
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
	 * @param exceptMask
	 * @return
	 */
	public static boolean cmpNameStringArr(String[] a,String[] b,int exceptMask){
		if(a == null && b == null){
			return true;
		}
		
		if(a == null || b == null){
			
			if(a != null){
				return isNullArr(a);
			}
			
			if(b != null){
				return isNullArr(b);
			}
			
			return false;
		}
		
		if(a.length != b.length){
			return false;
		}
		
		for(int i = 0;i < a.length;i++){
			
			if((exceptMask & (1 << i)) != 0){
				continue;
			}
			
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
	public static boolean cmpNameString(String a,String b){

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
	public static boolean isNullArr(String[] arr){
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
	public static boolean isNullString(String s){
		return s == null || s.length() == 0;
	}
	
	/**
	 * get google data ContactEntry id
	 * @return
	 */
	public static String getContactEntryId(Object o){
		ContactEntry contact = (ContactEntry)o;
		
		int backSlashIdx = contact.getId().lastIndexOf('/');
		if(backSlashIdx != -1){
			return contact.getId().substring(backSlashIdx + 1);
		}
		
		return contact.getId();		
	}
}
