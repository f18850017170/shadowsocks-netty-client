package cn.vonfly;

import io.netty.buffer.ByteBuf;
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
        ByteBuf byteBuf = (ByteBuf) msg;
        int len = byteBuf.readableBytes();
        byte[] array = new byte[len];
        if (byteBuf.hasArray()){
            array = byteBuf.array();
        }else {//非数组支撑，是直接缓冲区
            byteBuf.getBytes(byteBuf.readerIndex(),array);
        }
        System.out.println(Thread.currentThread().getName() +"接受远程代理返回信息，即将写入ss local channel,len="+((ByteBuf) msg).readableBytes());
        localSocketChannel.write(((ByteBuf) msg).retain());
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelReadComplete();
        ctx.flush();
        localSocketChannel.flush();
    }
}
