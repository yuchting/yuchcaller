package com.yuchs.yuchcaller.sync.svr.contact;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.TextContent;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.data.extensions.StructuredPostalAddress;
import com.yuchs.yuchcaller.sync.svr.GoogleAPISync;
import com.yuchs.yuchcaller.sync.svr.GoogleAPISyncData;
import com.yuchs.yuchcaller.sync.svr.Logger;

public class ContactSync extends GoogleAPISync {
	
	private ContactsService myService = new ContactsService("YuchCaller");
	
	
	
	public ContactSync(InputStream in, Logger logger) throws Exception {
		super(in, logger);

		// set the credentials
		myService.setOAuth2Credentials(mGoogleCredential);
		
		// read the svr google data
		readSvrGoogleData();
		
		// compare event
		compareEvent();
	}
	
	/**
	 * get the URL by google id
	 * @param gid
	 * @return
	 */
	private URL getContactURL(String gid)throws Exception{
		return new URL("https://www.google.com/m8/feeds/contacts/default/full/" + gid);
	}


	/**
	 * read the contact data
	 */
	@Override
	protected void readSvrGoogleData() throws Exception {
		
		if(fetchFormerEvent()){
			return ;
		}
		
		URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
		Query myQuery = new Query(feedUrl);
		myQuery.setMaxResults(999999);
		myQuery.setStringCustomParameter("orderby","lastmodified");
		ContactFeed resultFeed;
		
		try{
			resultFeed = myService.query(myQuery, ContactFeed.class);
		}catch(NullPointerException e){
			if(e.getMessage().startsWith("No authentication header information")){
				mGoogleCredential.refreshToken();
				resultFeed = myService.query(myQuery, ContactFeed.class);
			}else{
				throw e;
			}
		}
		
		StringBuffer sb = new StringBuffer();
		
		if(resultFeed != null && resultFeed.getEntries() != null){	
			for(ContactEntry entry : resultFeed.getEntries()){
				if(entry.hasName()){
					// must has name
					//
					mSvrSyncDataList.add(entry);
					sb.append(entry.getEdited().getValue());
				}
			}
		}		
		
		mAllSvrSyncDataMD5 = getMD5(sb.toString());
		
		storeFormerEvent();
	}
	
	
	@Override
	protected String getGoogleDataId(Object o) {
		ContactEntry contact = (ContactEntry)o;
		return contact.getId();
	}

	@Override
	protected long getGoogleDataLastMod(Object o) {
		ContactEntry contact = (ContactEntry)o;
		return contact.getUpdated().getValue();
	}

	@Override
	protected boolean isFristSyncSameData(Object o, GoogleAPISyncData g)throws Exception {
		ContactEntry 		contact = (ContactEntry)o;
		ContactSyncData		c		= (ContactSyncData)g;
		
		ContactData			cd		= c.getData();
				
		// names compare
		if(contact.hasName()){
			
			Name tName = contact.getName();
			
			String[] gdNames = new String[]{
				tName.getFamilyName() 		!= null ? tName.getFamilyName().getValue():null,
				tName.getGivenName() 		!= null ? tName.getGivenName().getValue():null,
				tName.getAdditionalName() 	!= null ? tName.getAdditionalName().getValue():null,
				tName.getNamePrefix() 		!= null ? tName.getNamePrefix().getValue():null,
				tName.getNameSuffix() 		!= null ? tName.getNameSuffix().getValue():null,
			};
			
			for(int i = 0 ;i < gdNames.length;i++){
				if(!cmpNameString(gdNames[i],cd.names[i])){
					return false;
				}
			}
			
		}else{
			return false;
		}
		
		// address compared
		//
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
			
			for(int i = 0;i < gdHomeAddr.length;i++){
				if(!cmpNameString(gdWorkAddr[i],cd.addr_work[i]) || !cmpNameString(gdHomeAddr[i],cd.addr_home[i])){
					return false;
				}
			}
			
		}else{
			if(!isNullArr(cd.addr_work) || !isNullArr(cd.addr_home)){
				return false;
			}
		}
		
		if(contact.hasPhoneNumbers()){
			
			String[] gdTel = new String[ContactData.TEL_SIZE];
			
			List<PhoneNumber> tList = contact.getPhoneNumbers();
			
			for(PhoneNumber p : tList){
				if(p.getRel() != null){
					
					if(p.getRel().endsWith("work")){
						
						gdTel[ContactData.TEL_WORK] = p.getPhoneNumber();
						
					}else if(p.getRel().endsWith("home")){
						
						gdTel[ContactData.TEL_HOME] = p.getPhoneNumber();
						
					}else if(p.getRel().endsWith("mobile")){
						
						gdTel[ContactData.TEL_MOBILE] = p.getPhoneNumber();
						
					}else if(p.getRel().endsWith("pager")){
						
						gdTel[ContactData.TEL_PAGER] = p.getPhoneNumber();
						
					}else if(p.getRel().endsWith("work_fax")){
						
						gdTel[ContactData.TEL_FAX] = p.getPhoneNumber();
					}
					
				}else if(p.getLabel() != null){
					
					if(p.getLabel().startsWith("work2")){
						
						gdTel[ContactData.TEL_WORK2] = p.getPhoneNumber();
						
					}else if(p.getLabel().startsWith("home2")){
						
						gdTel[ContactData.TEL_HOME2] = p.getPhoneNumber();
						
					}else if(p.getLabel().startsWith("mobile2")){
						
						gdTel[ContactData.TEL_MOBILE2] = p.getPhoneNumber();
						
					}else{
						
						gdTel[ContactData.TEL_OTHER] = p.getPhoneNumber();
					}
				}
			}
			
			for(int i = 0 ;i < gdTel.length;i++){
				if(!cmpNameString(gdTel[i],cd.tel[i])){
					return false;
				}
			}
		}else{
			if(!isNullArr(cd.tel)){
				return false;
			}
		}
		
		// compare the organization (company and job title)
		if(contact.hasOrganizations()){
			
			List<Organization> tList = contact.getOrganizations();
			
			Organization organization = tList.get(0);
			String org 		= organization.hasOrgName() ? organization.getOrgName().getValue() : null;
			String title 	= organization.hasOrgTitle() ? organization.getOrgTitle().getValue() : null;
			
			if(!cmpNameString(org,cd.org) || !cmpNameString(title,cd.title)){
				return false;
			}
			
		}else{
			if(!isNullString(cd.org) || !isNullString(cd.title)){
				return false;
			}
		}
		
		// compare the email address
		if(contact.hasEmailAddresses()){
			
			String[] gdemails = new String[ContactData.EMAIL_SIZE];
			
			List<Email> tList = contact.getEmailAddresses();
			for(Email email : tList){
				String e = email.getAddress();
				
				if(email.getRel() != null){
					if(email.getRel().endsWith("work")){
						gdemails[ContactData.EMAIL_WORK] = e;
					}else if(email.getRel().endsWith("home")){
						gdemails[ContactData.EMAIL_HOME] = e;
					}
				}else{
					if(email.getLabel() != null){
						gdemails[ContactData.EMAIL_OTHER] = e;
					}
				}
			}
			
			for(int i = 0 ;i < gdemails.length;i++){
				if(!cmpNameString(gdemails[i],cd.email[i])){
					return false;
				}
			}			
		}else{
			if(!isNullArr(cd.email)){
				return false;
			}
		}
		
		// compare the birthday
		//
		if(contact.hasBirthday()){
			String dateStr = contact.getBirthday().getWhen();
			if(dateStr.startsWith("00")){
				dateStr = dateStr.replace("00", "19");
			}
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			if(format.parse(dateStr).getTime() != cd.birthday){
				return false;
			}
		}else{
			if(cd.birthday != 0){
				return false;
			}
		}
		
		// compare the note
		if(contact.getContent() instanceof TextContent){
			TextContent text = (TextContent)contact.getContent();
			if(cmpNameString(text.getContent().getPlainText(), cd.note)){
				return false;
			}
		}else{
			if(!isNullString(cd.note)){
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
	

	@Override
	protected GoogleAPISyncData newSyncData() {
		return new ContactSyncData();
	}

	@Override
	protected void deleteGoogleData(GoogleAPISyncData g) throws Exception {
		
		ContactEntry contact = myService.getEntry(getContactURL(g.getGID()), ContactEntry.class);
		if(contact != null){
			// TODO
			//contact.delete();
			
			mLogger.LogOut(mYuchAcc + " deleteContact:" + g.getBBID());
		}		
	}

	@Override
	protected Object updateGoogleData(GoogleAPISyncData g) throws Exception {
		ContactEntry contact = myService.getEntry(getContactURL(g.getGID()), ContactEntry.class);
		if(contact != null){
			
			g.exportGoogleData(contact, mTimeZoneID);
			
			// TODO delete follow test code
			contact.setUpdated(new DateTime(System.currentTimeMillis()));
			
			//URL editUrl = new URL(contact.getEditLink().getHref());
			//contact = myService.update(editUrl, contact);
			
			g.setGID(contact.getId());
			g.setLastMod(contact.getUpdated().getValue());
			
			mLogger.LogOut(mYuchAcc + " updateContact:" + g.getBBID());
		}
		
		return contact;
	}

	@Override
	protected Object uploadGoogleData(GoogleAPISyncData g) throws Exception {
		ContactEntry contact = new ContactEntry();
		g.exportGoogleData(contact, mTimeZoneID);
		
		// TODO delete follow test code
		contact.setId(Integer.toString((new Random().nextInt(9999999))));
		contact.setUpdated(new DateTime(System.currentTimeMillis()));
		
		//contact = myService.insert(getContactURL(""), contact);
		
		g.setGID(contact.getId());
		g.setLastMod(contact.getUpdated().getValue());
		
		mLogger.LogOut(mYuchAcc + " uploadContact:" + g.getBBID());
		
		return contact;
	}

}
