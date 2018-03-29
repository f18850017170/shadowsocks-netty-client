package cn.vonfly;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

public class HttpRequestHandler extends SimpleChannelInboundHandler<HttpMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpMessage msg) throws Exception {
        DefaultHttpRequest request = (DefaultHttpRequest) msg;
        HttpMethod method = request.method();
        String uri = request.uri();
        String host = request.headers().get(HttpHeaderNames.HOST);
        HttpVersion httpVersion = request.protocolVersion();
        System.out.println("method= " + method + ",version=" + httpVersion + ",host=" + host + ",uri=" + uri);
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
