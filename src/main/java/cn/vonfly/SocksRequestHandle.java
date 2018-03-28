package cn.vonfly;

import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.*;
@ChannelHandler.Sharable
public final class SocksRequestHandle extends SimpleChannelInboundHandler<SocksRequest> {

    protected void channelRead0(ChannelHandlerContext ctx, SocksRequest msg) throws Exception {
        switch (msg.requestType()){
            case INIT:
                System.out.println("local server init");
                //SocksCmdRequestDecoder 执行一次后会自己移除自己
                ctx.pipeline().addFirst(new SocksCmdRequestDecoder());//将下一个请求 从字节码解码为SocksCmdRequest才能获得CMD 此时SocksInitRequestDecoder已被移除
                ctx.write(new SocksInitResponse(SocksAuthScheme.NO_AUTH));
                break;
            case AUTH:
                //若在init时 直接返回NO_AUTH则此处不会被访问到
                System.out.println("def auth success");
                //SocksCmdRequestDecoder  执行一次后会自己移除自己
                ctx.pipeline().addFirst(new SocksCmdRequestDecoder());//将下一个请求 从字节码解码为SocksCmdRequest才能获得CMD  此时SocksInitRequestDecoder已被移除
                ctx.write(new SocksAuthResponse(SocksAuthStatus.SUCCESS));
                break;
            case CMD:
                SocksCmdRequest request = (SocksCmdRequest) msg;
                if (request.cmdType() == SocksCmdType.CONNECT){
                    System.out.println("local server connected");
//                    ctx.pipeline().addLast(new SocksCmdRequestDecoder());
                    ctx.pipeline().addLast(new SocksServerConnectHandler()); //TODO
                    ctx.pipeline().remove(this);
                    ctx.fireChannelRead(request);
                }
                break;
            case UNKNOWN:
                ctx.close();
                System.out.println("un know SocksRequest type");
                break;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //TODO
    }
}
