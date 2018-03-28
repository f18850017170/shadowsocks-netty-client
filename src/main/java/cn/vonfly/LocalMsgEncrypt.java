package cn.vonfly;

import cn.vonfly.encryption.CryptFactory;
import cn.vonfly.encryption.ICrypt;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;


import java.io.ByteArrayOutputStream;
import java.util.List;

public class LocalMsgEncrypt extends MessageToMessageDecoder<ByteBuf> {
    private ICrypt iCrypt = CryptFactory.get("aes-256-cfb", "6P(g*(%gYDrBggFk");
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int len = msg.readableBytes();
        byte[] array = new byte[len];
        if (msg.hasArray()){
            array = msg.array();
        }else {//非数组支撑，是直接缓冲区
           msg.getBytes(msg.readerIndex(),array);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        iCrypt.encrypt(array, byteArrayOutputStream);
        out.add(byteArrayOutputStream.toByteArray());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }
}
