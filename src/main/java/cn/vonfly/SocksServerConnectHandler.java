package cn.vonfly;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.socks.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

@ChannelHandler.Sharable
public final class SocksServerConnectHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {
    private Bootstrap bootstrap = new Bootstrap();

    protected void channelRead0(final ChannelHandlerContext ctx, final SocksCmdRequest msg) throws Exception {
        Promise<Channel> promise = ctx.executor().newPromise();

        promise.addListener(new GenericFutureListener<Future<Channel>>() {
            public void operationComplete(Future<Channel> future) throws Exception {
                final Channel remoteInLocalBoundChannel = future.getNow();//连接到远程代理服务器的channel
                if (future.isSuccess()) {
                    ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, SocksAddressType.IPv4))//返回成功 标志着socks4次认证完成 所以移除掉相应的channleHandle
                            .addListener(new ChannelFutureListener() {
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    ctx.pipeline().remove(SocksServerConnectHandler.this)
                                            .remove(SocksMessageEncoder.class);
                                    ctx.pipeline()
//                                            .addLast(new SkipSocksInBoundHandler())
                                            .addLast(new HttpRequestDecoder())
                                            .addLast(new HttpRequestHandler())
                                            .addLast(new HttpRequestEncoder());

//                                            .addLast(new LocalMsgEncrypt())//加密请求数据
//                                            .addLast(new LocalOutReplayHandler((SocketChannel) remoteInLocalBoundChannel));
                                    remoteInLocalBoundChannel.pipeline()
                                            .addLast(new RemoteMsgDecrypt())//解密remote 返回信息
                                            .addLast(new RemoteInReplayHandler((SocketChannel) ctx.channel()));//写到本地local channel
                                }
                            });
                }
            }
        });
        final Channel inBoundChannel = ctx.channel();
        bootstrap.group(inBoundChannel.eventLoop()).channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ClientChannelPromiseHandle(promise)); //
        //TODO 代理的地址和端口
        bootstrap.connect("45.63.104.54", 27).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    //回写local ss 连接失败信息
                    ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, SocksAddressType.IPv4));//TODO
                } else {
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
