package com.yuchs.yuchcaller.sync.svr.contact;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Category;
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
		
		if(mSvrSyncDataList == null){
			mSvrSyncDataList = new Vector<Object>();
		}
		
		URL feedUrl = getContactURL("");
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
			for(ContactEntry e : resultFeed.getEntries()){
				if(e.hasName()){
					// must has name
					//
					mSvrSyncDataList.add(e);
					sb.append(e.getEdited().getValue());
				}
			}
		}		
		
		mAllSvrSyncDataMD5 = getMD5(sb.toString());
		
		mLogger.LogOut(mYuchAcc + " Load Contact Number:" + mSvrSyncDataList.size());
		
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
		
		ContactSyncData		cmp = new ContactSyncData();
		cmp.importGoogleData((ContactEntry)o);
		
		return cmp.equals(g);
	}	

	@Override
	protected GoogleAPISyncData newSyncData() {
		return new ContactSyncData();
	}

	@Override
	protected void deleteGoogleData(GoogleAPISyncData g) throws Exception {
		
		ContactEntry contact = myService.getEntry(getContactURL(g.getGID()), ContactEntry.class);
		if(contact != null){
			
			
			// delete contact
			try{
				contact.delete();
			}catch(NullPointerException e){
				if(e.getMessage().startsWith("No authentication header information")){
					
					mGoogleCredential.refreshToken();
					
					contact.delete();
					
				}else{
					throw e;
				}
			}
			
			
			// ouput debug info
			String tDebugInfo = mYuchAcc + " deleteContact:" + g.getBBID();
			if(g.getAPIData() != null){
				ContactData cd = (ContactData)g.getAPIData();
				tDebugInfo += " " + cd.names[ContactData.NAME_GIVEN] + cd.names[ContactData.NAME_GIVEN];
			}
			mLogger.LogOut(tDebugInfo);
		}		
	}

	@Override
	protected Object updateGoogleData(Object o,GoogleAPISyncData g) throws Exception {
		ContactEntry contact = (ContactEntry)o;
		if(contact != null){
			
			g.exportGoogleData(contact, mTimeZoneID);
			
			URL editUrl = new URL(contact.getEditLink().getHref());
			try{
				
				contact = myService.update(editUrl, contact);
				
			}catch(NullPointerException e){
				if(e.getMessage().startsWith("No authentication header information")){
					
					mGoogleCredential.refreshToken();
					
					contact = myService.update(editUrl, contact);
					
				}else{
					throw e;
				}
			}
			
			
			g.setGID(contact.getId());
			g.setLastMod(contact.getUpdated().getValue());
			
			// ouput debug info
			String tDebugInfo = mYuchAcc + " updateContact:" + g.getBBID();
			if(g.getAPIData() != null){
				ContactData cd = (ContactData)g.getAPIData();
				tDebugInfo += " " + cd.names[ContactData.NAME_GIVEN] + cd.names[ContactData.NAME_GIVEN];
			}
			mLogger.LogOut(tDebugInfo);
		}
		
		return contact;
	}

	@Override
	protected Object uploadGoogleData(GoogleAPISyncData g) throws Exception {
		
		ContactEntry contact = new ContactEntry();
		g.exportGoogleData(contact, mTimeZoneID);
		
		try{
			
			contact = myService.insert(getContactURL(""), contact);
			
		}catch(NullPointerException e){
			if(e.getMessage().startsWith("No authentication header information")){
				
				mGoogleCredential.refreshToken();
				
				contact = myService.insert(getContactURL(""), contact);
				
			}else{
				throw e;
			}
		}
		
		
		g.setGID(contact.getId());
		g.setLastMod(contact.getUpdated().getValue());
		
		// ouput debug info
		String tDebugInfo = mYuchAcc + " uploadContact:" + g.getBBID();
		if(g.getAPIData() != null){
			ContactData cd = (ContactData)g.getAPIData();
			tDebugInfo += " " + cd.names[ContactData.NAME_GIVEN] + cd.names[ContactData.NAME_GIVEN];
		}
		mLogger.LogOut(tDebugInfo);
		
		return contact;
	}

}
