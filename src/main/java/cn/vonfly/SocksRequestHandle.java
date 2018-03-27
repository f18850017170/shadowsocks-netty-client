package cn.vonfly;

import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.*;
@ChannelHandler.Sharable
public final class SocksRequestHandle extends SimpleChannelInboundHandler<SocksRequest> {
    private ChannelConfig channelConfig;
    public SocksRequestHandle(ChannelConfig channelConfig){
        this.channelConfig = channelConfig;
    }
    protected void channelRead0(ChannelHandlerContext ctx, SocksRequest msg) throws Exception {
        switch (msg.requestType()){
            case INIT:
                System.out.println("local server init");
                ctx.write(new SocksInitResponse(SocksAuthScheme.NO_AUTH));
                break;
            case AUTH:
                System.out.println("def auth success");
                ctx.write(new SocksAuthResponse(SocksAuthStatus.SUCCESS));
                break;
            case CMD:
                SocksCmdRequest request = (SocksCmdRequest) msg;
                if (request.cmdType() == SocksCmdType.CONNECT){
                    System.out.println("local server connected");
                    ctx.pipeline().addFirst(new SocksCmdRequestDecoder());
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
