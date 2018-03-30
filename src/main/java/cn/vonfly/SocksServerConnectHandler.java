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

@ChannelHandler.Sharable
public final class SocksServerConnectHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {
    private Bootstrap bootstrap = new Bootstrap();

    protected void channelRead0(final ChannelHandlerContext ctx, final SocksCmdRequest msg) throws Exception {
        Promise<Channel> promise = ctx.executor().newPromise();
        ByteBuf socksCmdReqMsg = ctx.alloc().buffer();
        msg.encodeAsByteBuf(socksCmdReqMsg);
        socksCmdReqMsg.skipBytes(3);//跳过版本号(1byte)，cmd命名(1byte),RSV保留字段(1byte)
        byte addressType = socksCmdReqMsg.readByte();
        SocksAddressType socksAddressType = SocksAddressType.valueOf(addressType);
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
        final ByteBuf dstAddr = Unpooled.buffer(bytesLen);
        socksCmdReqMsg.resetReaderIndex();
        socksCmdReqMsg.skipBytes(3);
        socksCmdReqMsg.readBytes(dstAddr, bytesLen);
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
//                                            .addLast(new LocalMsgEncrypt())//加密请求数据
                                            .addLast(new LocalOutReplayHandler(dstAddr, (SocketChannel) remoteInLocalBoundChannel));
                                    remoteInLocalBoundChannel.pipeline()
//                                            .addLast(new RemoteMsgDecrypt())//解密remote 返回信息
                                            .addLast(new RemoteInReplayHandler((SocketChannel) ctx.channel()));//写到本地local channel
                                }
                            });
                }
            }
        });
        final Channel inBoundChannel = ctx.channel();
        bootstrap.group(inBoundChannel.eventLoop()).channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ClientChannelPromiseHandle(promise)); //
        //TODO 代理的地址和端口
        bootstrap.connect("127.0.0.1", 2081).addListener(new ChannelFutureListener() {
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
