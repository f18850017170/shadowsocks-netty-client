package cn.vonfly;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.handler.codec.socks.SocksMessage;
import io.netty.handler.codec.socks.SocksMessageEncoder;

@ChannelHandler.Sharable
public class AutoRemoveSocksMessageEncoder extends SocksMessageEncoder {
    @Override
    protected void encode(ChannelHandlerContext ctx, SocksMessage msg, ByteBuf out) throws Exception {
        super.encode(ctx, msg, out);
        if (msg instanceof SocksCmdResponse) {
            SocksCmdResponse socksCmdResponse = (SocksCmdResponse) msg;
            if (socksCmdResponse.cmdStatus() == SocksCmdStatus.SUCCESS || socksCmdResponse.cmdStatus() == SocksCmdStatus.FAILURE) {
                ctx.pipeline().remove(this);
            }
        }
    }
}
