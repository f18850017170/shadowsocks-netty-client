package cn.vonfly;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;

//读取remote 返回的信息，写入到local channel
public class RemoteInReplayHandler extends ChannelInboundHandlerAdapter{
    private SocketChannel localSocketChannel;

    public RemoteInReplayHandler(SocketChannel socketChannel) {
        this.localSocketChannel = socketChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        localSocketChannel.write(msg);
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
