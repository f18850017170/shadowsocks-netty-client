package cn.vonfly;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.net.util.Charsets;


/**
 * 读取pc 发送给local的信息，发送给remote
 */
public class LocalOutReplayHandler extends ChannelInboundHandlerAdapter {
    private SocketChannel remoteSocketChannel;

    public LocalOutReplayHandler(SocketChannel remoteSocketChannel) {
        this.remoteSocketChannel = remoteSocketChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        int len = byteBuf.readableBytes();
        byte[] array = new byte[len];
        if (byteBuf.hasArray()){
            array = byteBuf.array();
        }else {//非数组支撑，是直接缓冲区
            byteBuf.getBytes(byteBuf.readerIndex(),array);
        }
        System.out.println(new String(array, Charsets.toCharset("UTF-8")));
        remoteSocketChannel.write(((ByteBuf) msg).retain());
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        remoteSocketChannel.flush();
    }
}
