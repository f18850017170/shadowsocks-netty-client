package cn.vonfly;

import cn.vonfly.encryption.CryptFactory;
import cn.vonfly.encryption.ICrypt;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.socks.SocksRequest;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class LocalMsgEncrypt extends MessageToMessageEncoder<ByteBuf>{
    private ICrypt iCrypt = CryptFactory.get("aes-256-cfb", "6P(g*(%gYDrBggFk");
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        msg.skipBytes(3);
        int len = msg.readableBytes();
        ByteBuf byteBuf = msg.readBytes(len);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        iCrypt.encrypt(byteBuf.array(), byteArrayOutputStream);
        out.add(byteArrayOutputStream.toByteArray());
    }
}
