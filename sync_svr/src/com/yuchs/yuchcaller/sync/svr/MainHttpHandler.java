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
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.json.JSONObject;


public class MainHttpHandler extends SimpleChannelUpstreamHandler {
	
	public static final String		HTTP_CONTENT_TYPE = "Content-type";
	
	public static final String		MIME_JSON_TYPE	= "application/json";
	
	// the logger
	private final Logger	mLogger;
	
	public MainHttpHandler(Logger _logger){
		mLogger = _logger;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();
				
		if(request.getMethod() != HttpMethod.POST){
			throw new Exception("Error POST");
		}
		
		JSONObject tJson = new JSONObject(readPostBodyText(request));
		
		if(!tJson.has("Type")){
			throw new Exception("Error no Type");
		}
		
		String tType = tJson.getString("Type");
		
		GoogleAPISync tSync;
		if(tType.equals("calender")){
			tSync = new CalenderSync(tJson,mLogger);
		}else{
			throw new Exception("Error Type");
		}	
		
		ChannelBuffer buffer 	= new DynamicChannelBuffer(2048);
		buffer.writeBytes(tSync.getResult().getBytes("UTF-8"));
		
		HttpResponse response	= new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
		response.setContent(buffer);
		response.setHeader("Content-Type", MIME_JSON_TYPE + "; charset=UTF-8");
		response.setHeader("Content-Length", response.getContent().writerIndex());
		
		Channel ch = e.getChannel();
		
		// Write the initial line and the header.
		ch.write(response);
		ch.disconnect();
		ch.close();
	}
	
	/**
	 * read the body string from the http request
	 * @param _request
	 * @return
	 * @throws Exception
	 */
	private String readPostBodyText(HttpRequest _request)throws Exception{
		
		boolean tGzip = _request.getHeader("Accept-Encoding") != null;
		
		final ChannelBuffer tCb = _request.getContent();
		
		int length = 0;
		int offset = 0;
		
		byte[] tContentBytes;
		
		if(tGzip){
			InputStream in = new ByteArrayInputStream(tCb.toByteBuffer().array(),tCb.arrayOffset(),tCb.readableBytes());			
			
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
						length			= tContentBytes.length; 
						
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
			byte[] tHttpBytes = tCb.array();
			
			length = tCb.capacity() - tCb.arrayOffset();
			offset = tCb.arrayOffset();
			
			tContentBytes = tHttpBytes;
		}
		
		String result = new String(tContentBytes,offset,length,"UTF-8");
		return result;
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

		// Close the connection as soon as the error message is sent.
		ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
		
		//mLogger.LogOut()
	}
}
