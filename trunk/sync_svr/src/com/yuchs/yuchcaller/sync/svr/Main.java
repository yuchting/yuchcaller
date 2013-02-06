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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.tasks.TasksScopes;


public class Main {

	private Logger	mMainLogger;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {		
		
		(new Main()).startNetty(8888);
		
		//System.getProperties().put("socksProxySet","true");
		//System.getProperties().put("socksProxyHost","127.0.0.1");
		//System.getProperties().put("socksProxyPort","7070");
				
	    // The clientId and clientSecret are copied from the API Access tab on
	    // the Google APIs Console
//	    String clientId = GoogleAPISync.getGoogleAPIClientId();
//	    String clientSecret = GoogleAPISync.getGoogleAPIClientSecret();
//
//	    // Or your redirect URL for web based applications.
//	    String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
//	    
//	    Iterable<String> scope = Arrays.asList(CalendarScopes.CALENDAR,TasksScopes.TASKS,"https://www.google.com/m8/feeds");
//	    
//	    
//	    // Step 1: Authorize -->
//	    String authorizationUrl = new GoogleAuthorizationCodeRequestUrl(clientId, redirectUrl,scope).setAccessType("offline").build();
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
//	    HttpTransport httpTransport = new NetHttpTransport();
//		JacksonFactory jsonFactory = new JacksonFactory();
//	    GoogleAuthorizationCodeTokenRequest request = new GoogleAuthorizationCodeTokenRequest(httpTransport, jsonFactory,clientId, clientSecret, code, redirectUrl);
//	    GoogleTokenResponse token = request.execute();
//	    //token.setExpiresInSeconds(3600l*24l*365l*100L);
//	    System.out.println("Expire time:" + token.getExpiresInSeconds());
//	    System.out.println("Token Type:" + token.getTokenType());
//	    System.out.println("refresh_token:" + token.getRefreshToken());
//	    System.out.println("access_token:" +token.getAccessToken());
//	    // End of Step 2 <--
	    
		
//		GoogleCredential cd = new GoogleCredential.Builder()
//								    .setClientSecrets(clientId, clientSecret)
//								    .setJsonFactory(jsonFactory).setTransport(httpTransport).build()
//								    .setRefreshToken("1/5IYu1JNlGdBMoIMo5SrOEVFt1wIzk-GWw-EHbwFwGz8")
//								    .setAccessToken("ya29.AHES6ZSssslGQywppEfLsx9CW8u2cwbaNoqqdQKmuT4Eo9fwgtnvbw");
//		
//	    Calendar service = new Calendar(httpTransport, jsonFactory,cd);
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
	
	private void startNetty(int _port){
		
		mMainLogger = new Logger("");
		mMainLogger.EnabelSystemOut(true);
		
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory(){
			
			@Override
			public ChannelPipeline getPipeline() throws Exception {
								 
				// Create a default pipeline implementation.
				ChannelPipeline pipeline = Channels.pipeline();
				
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("encoder", new HttpResponseEncoder());
								
				pipeline.addLast("handler", new MainHttpHandler(mMainLogger));			
				return pipeline;
			}
		});
		
		bootstrap.bind(new InetSocketAddress(_port));
		System.out.println("admin start on "+_port);
	}
}
