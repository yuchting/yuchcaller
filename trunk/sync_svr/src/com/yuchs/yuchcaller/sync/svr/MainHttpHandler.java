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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.yuchs.yuchcaller.sync.svr.calendar.CalendarSync;
import com.yuchs.yuchcaller.sync.svr.contact.ContactSync;
import com.yuchs.yuchcaller.sync.svr.task.TaskSync;


public class MainHttpHandler extends SimpleChannelUpstreamHandler {
	
	public static final String		HTTP_CONTENT_TYPE = "Content-type";
	public static final String		HTTP_CONTENT_LENGTH = "Content-Length";
	public static final String		HTTP_CONTENT_ENCODING = "Content-Encoding";
	
	// the logger
	private final Logger	mLogger;
	
	//! client version code
	private int	mClientVersionCode;
	
	//! receive buffer for adapt the thunk http body
	private ChannelBuffer mReceiveBuffer 	= new DynamicChannelBuffer(512);
	
	/**
	 * auth code from the www.yuchs.com for check the google auth code 
	 */
	private String			mAuthCode		= null;
	
	//! http content length
	private int	mContentLength		= 0;
	
	//! whether the content has been encoded
	private boolean mContentEncoded	= false;
	
	public MainHttpHandler(Logger _logger){
		mLogger = _logger;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)throws Exception {
		
		Object message = e.getMessage();
		ChannelBuffer tCb;
		
		if(message instanceof HttpRequest){
			
			HttpRequest request = (HttpRequest) message;
			
			if(request.getMethod() == HttpMethod.GET){
				
				// redirect the GET request to www.yuchs.com
				HttpResponse response	= new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.FOUND);
				response.setHeader("Location","http://www.yuchs.com");
				
				// write back
				Channel ch = e.getChannel();
				ch.write(response);
				ch.disconnect();
				ch.close();
				
				return;
				
			}else if(request.getMethod() == HttpMethod.OPTIONS){
				
				// allow the OPTIONS if XMLHttpRequest requested in http://api.yuchs.com/passgen.html
				//
				HttpResponse response	= new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
				response.setHeader("Access-Control-Allow-Origin", "*");
				response.setHeader("Access-Control-Allow-Methods", "POST");
				response.setHeader("Access-Control-Allow-Headers","auth-code, origin, content-length, content-type");
				
				// write back
				Channel ch = e.getChannel();
				ch.write(response);
				ch.disconnect();
				ch.close();
				
				return;
			}
			
			mContentLength	= Integer.parseInt(request.getHeader(HTTP_CONTENT_LENGTH));
			mAuthCode		= request.getHeader("Auth-Code");
						
			tCb				= request.getContent();
			
		}else if(message instanceof DefaultHttpChunk ){
			
			HttpChunk request = (HttpChunk) message;
			tCb = request.getContent();
			
		}else if(message instanceof HttpChunk){
			/**
		     * The 'end of content' marker in chunked encoding.
		     */
			e.getChannel().close();
			return;
			
		}else{
			throw new Exception("Error Message Type: " + message.getClass().getName());
		}
		
		while(tCb.readable()){
			mReceiveBuffer.writeByte(tCb.readByte());	
		}
		
		if(mReceiveBuffer.writerIndex() >= mContentLength){
			
			mLogger.LogOut("\tRecv Content length:" + mContentLength);
			
			byte[] tContentBytes;			
			if(mAuthCode == null){
				
				// sync process
				//
				InputStream in = new ByteArrayInputStream(mReceiveBuffer.array());
				mContentEncoded = in.read() == 1;
				
				if(mContentEncoded){
					
					try{
						GZIPInputStream zin = new GZIPInputStream(in);
						try{
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							try{

								int c;
								while((c = zin.read()) != -1){
									os.write(c);
								}
								
								tContentBytes	= os.toByteArray();
														
							}finally{
								os.close();
							}
							
						}finally{
							zin.close();
						}
					}finally{
						in.close();
						in = null;
					}
					
				}else{
					tContentBytes = new byte[mContentLength - 1];
					sendReceive.ForceReadByte(in, tContentBytes, tContentBytes.length);
				}
			}else{
				// www.yuchs.com retrieve the AuthCode Token
				//
				tContentBytes = mReceiveBuffer.array();
			}
						
			triggerProcess(tContentBytes,e);
		}
	}
	
	/**
	 * trigger process
	 */
	private void triggerProcess(byte[] _bytes,MessageEvent e)throws Exception{
		
		mLogger.LogOut("\tTriggerProcess length:" + _bytes.length);
		
		// attempt to zip the data
		boolean zip = false;
		byte[] tResultBytes;
		
		if(mAuthCode != null){
			
			if(mAuthCode.equals("Request")){
				
				String[] params = (new String(_bytes,"UTF-8")).split("&");
				
				String result = null;
				
				for(String p : params){
					if(p.startsWith("pass")){
						String[] pass = p.split("=");
						if(pass.length >= 2 && pass[1].equals(Main.YuchUserPass)){
							result = Main.PrivateSvrRefreshToken + "|" + Main.PrivateSvrAccessToken + "|0|0|0";
						}
					}
				}
				
				if(result == null){
					result = new String("<Error>Error for User Pass");
				}
				
				tResultBytes = result.getBytes("UTF-8");
				
			}else{

				// service for the www.yuchs.com appengine request
				// check com.yuchting.yuchberry.yuchsign.server.servlet.YuchcallerSyncAuth for detail
				//
				HttpTransport httpTransport = new NetHttpTransport();
				JacksonFactory jsonFactory = new JacksonFactory();
				
			    GoogleAuthorizationCodeTokenRequest request = new GoogleAuthorizationCodeTokenRequest(httpTransport, jsonFactory,
																		GoogleAPISync.getGoogleAPIClientId(), GoogleAPISync.getGoogleAPIClientSecret(), 
																		mAuthCode, "urn:ietf:wg:oauth:2.0:oob");
			    
			    GoogleTokenResponse token = request.execute();
			    
			    tResultBytes = (token.getRefreshToken() + "&" + token.getAccessToken()).getBytes("UTF-8");
			}
		    
		}else{

			GoogleAPISync tSync;
			InputStream in = new ByteArrayInputStream(_bytes);
			
			try{
				mClientVersionCode = sendReceive.ReadShort(in);			
				String tType = sendReceive.ReadString(in);
				
				if(tType.equals("calendar")){
					tSync = new CalendarSync(in,mLogger);
				}else if(tType.equals("contact")){
					tSync = new ContactSync(in,mLogger);
				}else if(tType.equals("task")){
					tSync = new TaskSync(in,mLogger);
				}else{
					throw new Exception("Error Type : " + tType);
				}
				
				tSync.readSvrGoogleData();
				tSync.compareEvent();				
			
			}finally{
				in.close();
			}
			
			tResultBytes = tSync.getResult();
				
			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			try{
				
				byte[] zipBytes = null;
				
				if(tResultBytes.length > 128){
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					try{
						// try to zip it  
						GZIPOutputStream zos = new GZIPOutputStream(os,6);
						try{
							zos.write(tResultBytes);				
						}finally{
							zos.close();
						}
						byte[] testZip = os.toByteArray();
						if(testZip.length < tResultBytes.length){
							zipBytes = testZip;
						}
					}finally{
						os.close();
					}
				}
				
				if(zipBytes != null ){
					
					fos.write(1);
					fos.write(zipBytes);
					
					zip = true;
					
				}else{
					fos.write(0);
					fos.write(tResultBytes);
				}
				
				tResultBytes = fos.toByteArray();
				
			}finally{
				fos.close();
			}
		}
		
		// write the response 
		ChannelBuffer buffer 	= new DynamicChannelBuffer(2048);
		buffer.writeBytes(tResultBytes);
		
		HttpResponse response	= new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
		response.setContent(buffer);
		response.setHeader(HTTP_CONTENT_LENGTH, response.getContent().writerIndex());
		
		if(mAuthCode != null){
			response.setHeader("Access-Control-Allow-Origin", "*");
		}
		
		mLogger.LogOut("WriteBack Result Length:" + response.getContent().writerIndex() + " with zip " + zip);
		
		// write back
		try{
			Channel ch = e.getChannel();
			if(ch.isWritable()){
				try{
					ch.write(response);
					ch.disconnect();
				}finally{
					ch.close();
				}
			}
			
		}catch(Exception ex){
			mLogger.PrinterException(ex);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)throws Exception {
		
		Channel ch = e.getChannel();
		Throwable cause = e.getCause();
		if (cause instanceof TooLongFrameException) {
			sendError(ctx, HttpResponseStatus.BAD_REQUEST,e);
			return;
		}

		cause.printStackTrace();
		if (ch.isConnected()) {
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR,e);
		}else{
			sendError(ctx, HttpResponseStatus.FORBIDDEN,e);
		}		
	}

	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status,ExceptionEvent e) {
		
		Channel ch = ctx.getChannel();
		if(ch.isWritable() && ch.isConnected()){
			
			HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
			
			response.setHeader(HTTP_CONTENT_TYPE, "text/plain; charset=UTF-8");		
			response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n" + e.getCause().getMessage(), CharsetUtil.UTF_8));

			
			// Close the connection as soon as the error message is sent.
			ch.write(response);
			ch.close();
		}
		
		mLogger.PrinterException(e.getCause());
	}
}
