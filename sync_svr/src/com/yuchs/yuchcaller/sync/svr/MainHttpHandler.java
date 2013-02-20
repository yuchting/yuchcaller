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


public class MainHttpHandler extends SimpleChannelUpstreamHandler {
	
	public static final String		HTTP_CONTENT_TYPE = "Content-type";
	public static final String		HTTP_CONTENT_LENGTH = "Content-Length";
	public static final String		HTTP_CONTENT_ENCODING = "Content-Encoding";
	
	
	
	
	// the logger
	private final Logger	mLogger;
	
	//! client version code
	private int	mClientVersionCode;
	
	//! receive buffer for adapt the thunk http body
	private ChannelBuffer mReceiveBuffer 	= new DynamicChannelBuffer(256);
	
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
			
			if(request.getMethod() != HttpMethod.POST){
				throw new Exception("Error GET/POST");
			}
			
			mContentEncoded = request.getHeader(HTTP_CONTENT_ENCODING) != null;
			mContentLength	= Integer.parseInt(request.getHeader(HTTP_CONTENT_LENGTH));
			
			mAuthCode		= request.getHeader("Auth-Code");
			
			tCb = request.getContent();
			
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
			
			byte[] tContentBytes;
			
			if(mContentEncoded){
				InputStream in = new ByteArrayInputStream(mReceiveBuffer.array());			
				
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
				tContentBytes = mReceiveBuffer.array();
			}
			
			triggerProcess(tContentBytes,e);
		}
	}
	
	/**
	 * trigger process
	 */
	private void triggerProcess(byte[] _bytes,MessageEvent e)throws Exception{
		
		// attempt to zip the data
		boolean zip = false;
		byte[] tResultBytes;
		
		if(mAuthCode != null){
			
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
		    
		}else{

			GoogleAPISync tSync;
			InputStream in = new ByteArrayInputStream(_bytes);
			try{
				mClientVersionCode = sendReceive.ReadShort(in);			
				String tType = sendReceive.ReadString(in);
				
				if(tType.equals("calendar")){
					tSync = new CalendarSync(in,mLogger);
				}else{
					throw new Exception("Error Type");
				}
			
			}finally{
				in.close();
			}
			
			tResultBytes = tSync.getResult();
			
			if(tResultBytes.length != 0){
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				try{
					GZIPOutputStream zos = new GZIPOutputStream(os,6);
					try{
						zos.write(tResultBytes);				
					}finally{
						zos.close();
					}
					
					byte[] zipBytes = os.toByteArray();
					if(zipBytes.length < tResultBytes.length){
						tResultBytes = zipBytes;
						zip = true;
					}
				}finally{
					os.close();
				}
			}
		}
		
		// write the response 
		ChannelBuffer buffer 	= new DynamicChannelBuffer(2048);
		buffer.writeBytes(tResultBytes);
		
		HttpResponse response	= new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
		response.setContent(buffer);
		response.setHeader(HTTP_CONTENT_LENGTH, response.getContent().writerIndex());
		if(zip){
			response.setHeader(HTTP_CONTENT_ENCODING, "gzip");
		}
		
		Channel ch = e.getChannel();
		
		// Write the initial line and the header.
		ch.write(response);
		ch.disconnect();
		ch.close();
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
		
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		
		response.setHeader(HTTP_CONTENT_TYPE, "text/plain; charset=UTF-8");		
		response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

		Channel ch = ctx.getChannel();
		if(ch.isWritable()){
			// Close the connection as soon as the error message is sent.
			ch.write(response);
			ch.close();
		}
	}
}
