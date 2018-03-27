package cn.vonfly.channelhandle;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.*;

@ChannelHandler.Sharable
public class SocksRequestChannelHandle extends SimpleChannelInboundHandler<SocksRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksRequest msg) throws Exception {
        switch (msg.requestType()){
            case INIT:
                System.out.println("init socks");
                ctx.write(new SocksInitResponse(SocksAuthScheme.AUTH_PASSWORD));
                break;
            case AUTH:
                System.out.println("auth with remote server");
                //TODO 需要访问远程服务器进行验证 此处暂不验证
                ctx.write(new SocksAuthResponse(SocksAuthStatus.SUCCESS));
                break;
            case CMD:
                ctx.pipeline().addFirst(new SocksCmdRequestDecoder());
                break;
            case UNKNOWN:
                ctx.write(new SocksInitResponse(SocksAuthScheme.UNKNOWN));
                ctx.close();
                break;

        }

    }
}
