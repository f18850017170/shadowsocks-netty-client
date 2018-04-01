package cn.vonfly;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.util.ReferenceCountUtil;

public class DstAddressHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private ByteBuf dstAddress;//访问目标地址
    private Channel out2RemoteProxy;//
    private SocksCmdRequest socksCmdRequest;

    public DstAddressHandler(SocksCmdRequest msg, ByteBuf dstAddress, Channel out2RemoteProxy) {
        this.socksCmdRequest = msg;
        this.dstAddress = dstAddress;
        this.out2RemoteProxy = out2RemoteProxy;
        System.out.println(Thread.currentThread().getName() + ": construct DstAddressHandler," + this);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        int len = byteBuf.readableBytes();
        byte[] array = new byte[len];
        if (byteBuf.hasArray()) {
            array = byteBuf.array();
        } else {//非数组支撑，是直接缓冲区
            //getxx操作不会移动 readIndex
            byteBuf.getBytes(byteBuf.readerIndex(), array);
        }
//        System.out.println(new String(array));
        System.out.println(Thread.currentThread().getName() + "数据写入远程代理remote-proxy-channel[传输dest.addr信息=" + socksCmdRequest.host() + ":" + socksCmdRequest.port() + "]，length=" + len);
//        System.out.println(new String(array, Charsets.toCharset("UTF-8")));
        CompositeByteBuf compositeByteBuf = ctx.alloc().compositeBuffer();
        //dstAddr重复使用，所以需要retain
        compositeByteBuf.addComponents(dstAddress.duplicate(), Unpooled.copiedBuffer(array));
        //需要自己指定writeIndex,否则CompositeByteBuf的writeIndex=0；相应的readableBytes()=0
        //write的时候 readableBytes()=0，将不会写任何信息到channel中
        compositeByteBuf.writerIndex(compositeByteBuf.capacity());
        out2RemoteProxy.write(compositeByteBuf.retain());
        ctx.pipeline().remove(this);
        ctx.pipeline().addFirst(new LocalOutReplayHandler(out2RemoteProxy));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ReferenceCountUtil.safeRelease(dstAddress);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        out2RemoteProxy.flush();
    }
}
