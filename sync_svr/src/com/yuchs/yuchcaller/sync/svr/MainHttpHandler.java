package com.yuchs.yuchcaller.sync.svr;

import java.net.URLDecoder;

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
		
		String tContentType = request.getHeader("Content-Type");
		if(tContentType == null || !tContentType.startsWith(MIME_JSON_TYPE)){
			throw new Exception("Error Request Content-Type");
		}
		
		if(request.getMethod() != HttpMethod.POST){
			throw new Exception("Error POST");
		}
		
		String tDecodeJson = URLDecoder.decode(request.getContent().toString(),"UTF-8");
		JSONObject tJson = new JSONObject(tDecodeJson);
		
		String uri = request.getUri();
		System.out.println("uri:" + uri);
		
		HttpResponse response	= new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
        ChannelBuffer buffer 	= new DynamicChannelBuffer(2048);       
        
		buffer.writeBytes("hello!! 你好".getBytes("UTF-8"));
		
		response.setContent(buffer);
		response.setHeader("Content-Type", MIME_JSON_TYPE + "; charset=UTF-8");
		response.setHeader("Content-Length", response.getContent().writerIndex());
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
			sendError(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}

		cause.printStackTrace();
		if (ch.isConnected()) {
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}else{
			sendError(ctx, HttpResponseStatus.FORBIDDEN);
		}		
	}

	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		
		response.setHeader(HTTP_CONTENT_TYPE, "text/plain; charset=UTF-8");		
		response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

		// Close the connection as soon as the error message is sent.
		ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
		
		//mLogger.LogOut()
	}
}
