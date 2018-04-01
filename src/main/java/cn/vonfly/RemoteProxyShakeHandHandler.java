package cn.vonfly;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Promise;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 远程代理握手认证
 * 1、TODO 账号确认
 * 2、传递新连接的dst.address
 */
public class RemoteProxyShakeHandHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final byte SHAKE_HAND_SUCC = 0x07;
    //传递dstAddress信息
    private ByteBuf dstAddress;
    private Promise<Channel> promise;
    private SocketChannel local2ClientChannel;//本地到客户端代理的channel
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    public RemoteProxyShakeHandHandler(ByteBuf dstAddress, Promise<Channel> promise, SocketChannel local2ClientChannel) {
        this.dstAddress = dstAddress;
        this.promise = promise;
        this.local2ClientChannel = local2ClientChannel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte code = msg.readByte();
        if (code == SHAKE_HAND_SUCC) {
            ctx.pipeline().remove(RemoteProxyShakeHandHandler.class);
            //.addLast(new RemoteMsgDecrypt())//解密remote 返回信息
            ctx.pipeline().addLast(new RemoteInReplayHandler(local2ClientChannel));
            System.out.println(this + " promise set succ,count=" + atomicInteger.getAndIncrement());
            promise.setSuccess(ctx.channel());
        } else {
            msg.resetReaderIndex();
            byte[] tip;
            if (msg.hasArray()) {
                tip = msg.array();
            } else {
                int len = msg.readableBytes();
                tip = new byte[len];
                msg.getBytes(msg.readerIndex(), tip);
            }
            promise.setFailure(new Throwable("shake hand with remote proxy fail:" + new String(tip, Charset.forName("UTF-8"))));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //发送dstaddress到代理服务器
        ctx.writeAndFlush(dstAddress);
    }
}
