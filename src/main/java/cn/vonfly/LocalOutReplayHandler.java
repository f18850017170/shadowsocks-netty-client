package cn.vonfly;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.net.util.Charsets;

import java.nio.charset.Charset;


/**
 * 读取pc 发送给local的信息，发送给remote
 */
public class LocalOutReplayHandler extends ChannelInboundHandlerAdapter {
    private Channel remoteSocketChannel;

    public LocalOutReplayHandler(Channel remoteSocketChannel) {
        this.remoteSocketChannel = remoteSocketChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        int len = byteBuf.readableBytes();
        byte[] array = new byte[len];
        if (byteBuf.hasArray()) {
            array = byteBuf.array();
        } else {//非数组支撑，是直接缓冲区
            //getxx操作不会移动 readIndex
            byteBuf.getBytes(byteBuf.readerIndex(), array);
        }
        System.out.println(new String(array));
        System.out.println(Thread.currentThread().getName() +"数据写入远程代理 remote-proxy-channel，length=" + len);
        remoteSocketChannel.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelReadComplete();
        remoteSocketChannel.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("pc 主动关闭了channel");
        ctx.close();
        cause.printStackTrace();
    }
}
