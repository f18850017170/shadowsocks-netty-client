package cn.vonfly;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.socks.SocksRequest;

import java.util.List;

public class LocalMsgEncrypt extends MessageToMessageEncoder<SocksRequest>{

    protected void encode(ChannelHandlerContext ctx, SocksRequest msg, List<Object> out) throws Exception {

    }
}
