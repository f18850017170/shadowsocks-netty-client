package cn.vonfly;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

@ChannelHandler.Sharable
public final class SocksServerConnectHandler extends SimpleChannelInboundHandler<SocksCmdRequest>{
    private Bootstrap bootstrap = new Bootstrap();
    protected void channelRead0(final ChannelHandlerContext ctx, SocksCmdRequest msg) throws Exception {
        Promise<Channel> promise = ctx.executor().newPromise();

        promise.addListener(new GenericFutureListener<Future<Channel>>() {
            public void operationComplete(Future<Channel> future) throws Exception {
                final Channel outBoundChannel = future.getNow();//连接到远程代理服务器的channel
                if (future.isSuccess()){
                    //TODO
                    //todo
                    ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS,SocksAddressType.IPv4))//TODO
                            .addListener(new ChannelFutureListener() {
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    ctx.pipeline().remove(SocksServerConnectHandler.this);
                                    ctx.pipeline().addLast(new LocalOutReplayHandler((SocketChannel) outBoundChannel));
                                    outBoundChannel.pipeline().addLast(new RemoteInReplayHandler((SocketChannel) ctx.channel()));
                                }
                            });
                }
            }
        });
        final Channel inBoundChannel = ctx.channel();
        bootstrap.group(inBoundChannel.eventLoop()).channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS,10000)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .handler(new ClientChannelPromiseHandle(promise)); //
        //TODO 代理的地址和端口
        bootstrap.connect("45.63.104.54",27).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()){
                    //回写local ss 连接失败信息
                    ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, SocksAddressType.IPv4));//TODO
                }else {
                    System.out.println("连接到远程代理成功");
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}