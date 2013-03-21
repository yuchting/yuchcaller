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

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;



public class Main {

	private Logger	mMainLogger;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {		
		
		(new Main()).startNetty(6029);
		
		//System.getProperties().put("socksProxySet","true");
		//System.getProperties().put("socksProxyHost","127.0.0.1");
		//System.getProperties().put("socksProxyPort","7070");
				
	    // The clientId and clientSecret are copied from the API Access tab on
	    // the Google APIs Console
//	    String clientId = GoogleAPISync.getGoogleAPIClientId();
//	    String clientSecret = GoogleAPISync.getGoogleAPIClientSecret();
//	    
//	    NetHttpTransport httpTransport = new NetHttpTransport();
//	    JacksonFactory jsonFactory = new JacksonFactory();
//
//	    // Or your redirect URL for web based applications.
//	    String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
//	    
//	    Iterable<String> scope = Arrays.asList(CalendarScopes.CALENDAR,TasksScopes.TASKS,"https://www.google.com/m8/feeds");
//	    
//	    
//	    // Step 1: Authorize -->
//	    String authorizationUrl = new GoogleAuthorizationCodeRequestUrl(GoogleAPISync.getGoogleAPIClientId(), redirectUrl,scope).setAccessType("offline").build();
//
//	    // Point or redirect your user to the authorizationUrl.
//	    System.out.println("Go to the following link in your browser:");
//	    System.out.println(authorizationUrl);
//
//	    // Read the authorization code from the standard input stream.
//	    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//	    System.out.println("What is the authorization code?");
//	    String code = in.readLine();
//	    // End of Step 1 <--
//		
//	    // Step 2: Exchange -->
//	    GoogleAuthorizationCodeTokenRequest request = new GoogleAuthorizationCodeTokenRequest(httpTransport, jsonFactory,GoogleAPISync.getGoogleAPIClientId(), 
//	    																						GoogleAPISync.getGoogleAPIClientSecret(), code, redirectUrl);
//	    GoogleTokenResponse token = request.execute();
//	    //token.setExpiresInSeconds(3600l*24l*365l*100L);
//	    System.out.println("Expire time:" + token.getExpiresInSeconds());
//	    System.out.println("Token Type:" + token.getTokenType());
//	    System.out.println("refresh_token:" + token.getRefreshToken());
//	    System.out.println("access_token:" +token.getAccessToken());
//	    // End of Step 2 <--
	    
		
//		GoogleCredential cd = new GoogleCredential.Builder()
//								    .setClientSecrets(GoogleAPISync.getGoogleAPIClientId(), GoogleAPISync.getGoogleAPIClientSecret())
//								    .setJsonFactory(jsonFactory).setTransport(httpTransport).build()
//								    .setRefreshToken("1/0COUtW0S_cagTrdEYNexehMpx2TLrfQfAhGZv8rcZ6g")
//								    .setAccessToken("ya29.AHES6ZSAtKhBhABFO7kA4Bx8fTPCTnJzPqgHBJMe4UL3smY");
		
//		ContactsService service = new ContactsService("YuchCaller");
//		service.setOAuth2Credentials(cd);
//		
//		
//		
//		URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
//		Query myQuery = new Query(feedUrl);
//		myQuery.setMaxResults(999999);
//		myQuery.setStringCustomParameter("orderby","lastmodified");
//		ContactFeed resultFeed;
//		
//		try{
//			//resultFeed = service.query(myQuery, ContactFeed.class);
//			
//			ContactEntry cc = service.getEntry(new URL("https://www.google.com/m8/feeds/contacts/default/full/52656a38a8499d2"), ContactEntry.class);
//			System.out.println("" + cc.getName().getFullName());
//			System.out.println("" + cc.getName().getNamePrefix());
//			
//		}catch(NullPointerException e){
//			if(e.getMessage().startsWith("No authentication header information")){
//				cd.refreshToken();
//				//resultFeed = service.query(myQuery, ContactFeed.class);
//				
//				ContactEntry cc = service.getEntry(new URL("https://www.google.com/m8/feeds/contacts/default/full/52656a38a8499d2"), ContactEntry.class);
//				System.out.println("" + cc.getName().getFullName());
//				System.out.println("" + cc.getName().getNamePrefix());
//				
//				List<PhoneNumber> tList = cc.getPhoneNumbers();
//				List<StructuredPostalAddress> tList1 = cc.getStructuredPostalAddresses();
//				//Occupation op = cc.getOccupation();
//				List<Organization> tList2 = cc.getOrganizations();
//				Organization org = tList2.get(0);
//				System.out.println(org.getOrgName());
//				System.out.println(org.getOrgTitle());
//				
//				Birthday b = cc.getBirthday();
//				System.out.println(b.getValue());
//				System.out.println(b.getWhen());
//				
//				//System.out.println(op.getValue());
//				System.out.println(tList);
//				
//			}else{
//				throw e;
//			}
//		}
		
//		System.out.println("Contacts size:" + resultFeed.getEntries().size());
//		
//		//resultFeed.get
//		  // Print the results
//		  System.out.println(resultFeed.getTitle().getPlainText());
//		  for (ContactEntry entry : resultFeed.getEntries()) {
//			 
//			  System.out.println("last modified time: " + entry.getUpdated().getValue());
//			  
//			  System.out.println("id:" + entry.getId());
//			 
//		    if (entry.hasName()) {
//		    	
//		      Name name = entry.getName();
//		      if (name.hasFullName()) {
//		        String fullNameToDisplay = name.getFullName().getValue();
//		        if (name.getFullName().hasYomi()) {
//		          fullNameToDisplay += " (" + name.getFullName().getYomi() + ")";
//		        }
//		      System.out.println("\\\t\\\t" + fullNameToDisplay);
//		      
//			     
//		      } else {
//		        System.out.println("\\\t\\\t (no full name found)");
//		      }
//		      if (name.hasNamePrefix()) {
//		        System.out.println("\\\t\\\t" + name.getNamePrefix().getValue());
//		      } else {
//		        System.out.println("\\\t\\\t (no name prefix found)");
//		      }
//		      if (name.hasGivenName()) {
//		        String givenNameToDisplay = name.getGivenName().getValue();
//		        if (name.getGivenName().hasYomi()) {
//		          givenNameToDisplay += " (" + name.getGivenName().getYomi() + ")";
//		        }
//		        System.out.println("\\\t\\\t" + givenNameToDisplay);
//		      } else {
//		        System.out.println("\\\t\\\t (no given name found)");
//		      }
//		      if (name.hasAdditionalName()) {
//		        String additionalNameToDisplay = name.getAdditionalName().getValue();
//		        if (name.getAdditionalName().hasYomi()) {
//		          additionalNameToDisplay += " (" + name.getAdditionalName().getYomi() + ")";
//		        }
//		        System.out.println("\\\t\\\t" + additionalNameToDisplay);
//		      } else {
//		        System.out.println("\\\t\\\t (no additional name found)");
//		      }
//		      if (name.hasFamilyName()) {
//		        String familyNameToDisplay = name.getFamilyName().getValue();
//		        if (name.getFamilyName().hasYomi()) {
//		          familyNameToDisplay += " (" + name.getFamilyName().getYomi() + ")";
//		        }
//		        System.out.println("\\\t\\\t" + familyNameToDisplay);
//		      } else {
//		        System.out.println("\\\t\\\t (no family name found)");
//		      }
//		      if (name.hasNameSuffix()) {
//		        System.out.println("\\\t\\\t" + name.getNameSuffix().getValue());
//		      } else {
//		        System.out.println("\\\t\\\t (no name suffix found)");
//		      }
//		    } else {
//		      System.out.println("\t (no name found)");
//		    }
//		    System.out.println("Email addresses:");
//		    for (Email email : entry.getEmailAddresses()) {
//		      System.out.print(" " + email.getAddress());
//		      if (email.getRel() != null) {
//		        System.out.print(" rel:" + email.getRel());
//		      }
//		      if (email.getLabel() != null) {
//		        System.out.print(" label:" + email.getLabel());
//		      }
//		      if (email.getPrimary()) {
//		        System.out.print(" (primary) ");
//		      }
//		      System.out.print("\n");
//		    }
//		    System.out.println("IM addresses:");
//		    for (Im im : entry.getImAddresses()) {
//		      System.out.print(" " + im.getAddress());
//		      if (im.getLabel() != null) {
//		        System.out.print(" label:" + im.getLabel());
//		      }
//		      if (im.getRel() != null) {
//		        System.out.print(" rel:" + im.getRel());
//		      }
//		      if (im.getProtocol() != null) {
//		        System.out.print(" protocol:" + im.getProtocol());
//		      }
//		      if (im.getPrimary()) {
//		        System.out.print(" (primary) ");
//		      }
//		      System.out.print("\n");
//		    }
//		    System.out.println("Groups:");
//		    for (GroupMembershipInfo group : entry.getGroupMembershipInfos()) {
//		      String groupHref = group.getHref();
//		      System.out.println("  Id: " + groupHref);
//		    }
//		    System.out.println("Extended Properties:");
//		    for (ExtendedProperty property : entry.getExtendedProperties()) {
//		      if (property.getValue() != null) {
//		        System.out.println("  " + property.getName() + "(value) = " +
//		            property.getValue());
//		      } else if (property.getXmlBlob() != null) {
//		        System.out.println("  " + property.getName() + "(xmlBlob)= " +
//		            property.getXmlBlob().getBlob());
//		      }
//		    }
//		    Link photoLink = entry.getContactPhotoLink();
//		    String photoLinkHref = photoLink.getHref();
//		    System.out.println("Photo Link: " + photoLinkHref);
//		    if (photoLink.getEtag() != null) {
//		      System.out.println("Contact Photo's ETag: " + photoLink.getEtag());
//		    }
//		    System.out.println("Contact's ETag: " + entry.getEtag());
//		    
//		    Content cc =  entry.getContent();
//		  if(cc instanceof TextContent){
//			  System.out.println("Plain Text content " + entry.getPlainTextContent());
//		  }
//		    
//		    System.out.println();
//		    System.out.println();
//		  }

		
//		
//	    Calendar service = new Calendar(httpTransport, jsonFactory,cd);
//    
//	    	    
//	    com.google.api.services.calendar.Calendar.Events.List cList = service.events().list("primary");
//	    
//	    //cList.setTimeZone("Asia/Shanghai");
//	    
//	    cList.setTimeMin(new DateTime(new Date(System.currentTimeMillis()), TimeZone.getTimeZone("Asia/Shanhai")));
//
//	    Events events = cList.execute();  
//	    while (true) {
//	      for (Event event : events.getItems()) {
//	    	  
//	        System.out.println(event.getId() + ":" + event.getSummary());
//	        List<String> t_recurrenceList = event.getRecurrence();
//	        if(t_recurrenceList != null){
//	        	for(String s : t_recurrenceList){
//	        		System.out.println("   r:"+s);
//	        	}
//	        }
//	        
//	        System.out.println("   "+event.getStart().getDateTime().getValue()+":" + event.getDescription());
//	      }
//	      String pageToken = events.getNextPageToken();
//	      if (pageToken != null && !pageToken.isEmpty()) {
//	        events = service.events().list("primary").setPageToken(pageToken).execute();
//	      } else {
//	        break;
//	      }
//	    }
	    
	    
	    //com.google.api.services.calendar.model.Calendar calendar = service.calendars().get("primary").execute();
	    //System.out.println(calendar.getSummary());

//	    CalendarList calendarList = service.calendarList().list().execute();
//	    
//	    while (true) {
//    	  for (CalendarListEntry calendarListEntry : calendarList.getItems()) {
//    	    System.out.println(calendarListEntry.getSummary());
//    	  }
//    	  String pageToken = calendarList.getNextPageToken();
//    	  if (pageToken != null && !pageToken.isEmpty()) {
//    	    calendarList = service.calendarList().list().setPageToken(pageToken).execute();
//    	  } else {
//    	    break;
//    	  }
//    	}

//	    com.google.api.services.calendar.Calendar.Events.List clist = service.events().list("primary");
//	    
//	    clist.setTimeMax(new DateTime(System.currentTimeMillis()));
//	    clist.setTimeMin(new DateTime(System.currentTimeMillis() - 3600000L * 24L * 365L));
//	    
//	    clist.
	    	    
//	    Event event = new Event();
//
//	    event.setDescription("this is no description");
//	    event.setSummary("testtest test");
//	    event.setLocation("Home");
//
//	    ArrayList<EventAttendee> attendees = new ArrayList<EventAttendee>();
//	    attendees.add(new EventAttendee().setEmail("yuchting@gmail.com"));
//	    // ...
//	    event.setAttendees(attendees);
//
//	    DateTime start = DateTime.parseRfc3339("2012-06-03T10:00:00.000-07:00");
//	    DateTime end = DateTime.parseRfc3339("2013-06-03T10:25:00.000-07:00");
//	    event.setStart(new EventDateTime().setDateTime(start).setTimeZone("Asia/Shanghai"));
//	    event.setEnd(new EventDateTime().setDateTime(end).setTimeZone("Asia/Shanghai"));
//	    event.setRecurrence(Arrays.asList("RRULE:FREQ=WEEKLY;UNTIL=20130701T100000-07:00"));
//
//	    Event recurringEvent = service.events().insert("primary", event).execute();
	    
	    
//	    System.out.println(recurringEvent.getHtmlLink()); 
		
	}
	
	// timer of read data for channel 
	private Timer mReadTimeOutTimer = new HashedWheelTimer();
	
	private void startNetty(int _port){
		
		mMainLogger = new Logger("");
		mMainLogger.EnabelSystemOut(false);
				
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory(){
			
			@Override
			public ChannelPipeline getPipeline() throws Exception {
								 
				// Create a default pipeline implementation.
				ChannelPipeline pipeline = Channels.pipeline(new ReadTimeoutHandler(mReadTimeOutTimer, 180));
				
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("encoder", new HttpResponseEncoder());
								
				pipeline.addLast("handler", new MainHttpHandler(mMainLogger));
				return pipeline;
			}
		});
		
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.connectTimeoutMillis", 5000);
		
		bootstrap.bind(new InetSocketAddress(_port));
		
		mMainLogger.LogOut("Server start on " + _port);
	}
}
