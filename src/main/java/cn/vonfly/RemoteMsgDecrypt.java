package cn.vonfly;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.socks.SocksRequest;

import java.util.List;

public class RemoteMsgDecrypt extends MessageToMessageDecoder<SocksRequest>{


    protected void decode(ChannelHandlerContext ctx, SocksRequest msg, List<Object> out) throws Exception {

    }
}
