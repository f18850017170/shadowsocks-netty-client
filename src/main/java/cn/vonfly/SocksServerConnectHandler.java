package cn.vonfly;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.util.Date;

@ChannelHandler.Sharable
public final class SocksServerConnectHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {
    private Bootstrap bootstrap = new Bootstrap();

    protected void channelRead0(final ChannelHandlerContext ctx, final SocksCmdRequest msg) throws Exception {
        Promise<Channel> promise = ctx.executor().newPromise();
        ByteBuf socksCmdReqMsg = ctx.alloc().buffer();
        msg.encodeAsByteBuf(socksCmdReqMsg);
        socksCmdReqMsg.skipBytes(3);//跳过版本号(1byte)，cmd命名(1byte),RSV保留字段(1byte)
        byte addressType = socksCmdReqMsg.readByte();
        final SocksAddressType socksAddressType = SocksAddressType.valueOf(addressType);
        int bytesLen = 1;//1byte socksAddressType
        switch (socksAddressType) {
            case IPv4:
                bytesLen += 4;
                break;
            case DOMAIN:
                bytesLen += 1;
                bytesLen += socksCmdReqMsg.readByte();
                break;
            case IPv6:
                bytesLen += 16;
                break;
        }
        bytesLen += 2;//port
        final ByteBuf dstAddr = ctx.alloc().buffer(bytesLen);
        socksCmdReqMsg.resetReaderIndex();
        socksCmdReqMsg.skipBytes(3);
        socksCmdReqMsg.readBytes(dstAddr, bytesLen);
        //proxy server shake hand成功时promise成功
        promise.addListener(new GenericFutureListener<Future<Channel>>() {
            public void operationComplete(Future<Channel> future) throws Exception {
                final Channel remoteToLocalInBoundChannel = future.getNow();//连接到远程代理服务器的channel
                if (future.isSuccess()) {
                    ctx.pipeline()
//                          .addLast(new LocalMsgEncrypt())//加密请求数据
                            .addLast(new LocalOutReplayHandler(remoteToLocalInBoundChannel));
                    //TODO 会被执行到多次，需要再确认
                    ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, SocksAddressType.IPv4))//返回成功 标志着socks4次认证完成 所以移除掉相应的channleHandle
                            .addListener(new ChannelFutureListener() {
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    if (null != ctx.pipeline().get(SocksMessageEncoder.class)) {
                                        ctx.pipeline().remove(SocksMessageEncoder.class);
                                    }
                                    System.out.println(future.channel() + "socks5 四次握手协议完成，返回成功");
                                }
                            });
                } else {
                    Throwable cause = future.cause();
                    cause.printStackTrace();
                    ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, socksAddressType));
                }
            }
        });
        final Channel local2ClientChannel = ctx.channel();
        bootstrap.group(local2ClientChannel.eventLoop()).channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new RemoteProxyShakeHandHandler(dstAddr, promise, (SocketChannel) local2ClientChannel));
                    }
                }); //
        //TODO 代理的地址和端口
        bootstrap.connect("127.0.0.1", 2081).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    //回写local ss 连接失败信息
                    local2ClientChannel.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, SocksAddressType.IPv4));//TODO
                } else {
                    System.out.println(future.channel() + "连接到远程代理成功,DST.ADDRESS=" + msg.host() + ":" + msg.port() + new Date());
                }
            }
        });
        //TODO 不要在异步任务中移除其他handler，可能会出现handler不存在问题
        ctx.pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
