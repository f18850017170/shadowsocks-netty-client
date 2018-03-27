package cn.vonfly;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;


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
        remoteSocketChannel.write(msg);
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
