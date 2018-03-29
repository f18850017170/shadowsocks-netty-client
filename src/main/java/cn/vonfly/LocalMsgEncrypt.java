package cn.vonfly;

import cn.vonfly.encryption.CryptFactory;
import cn.vonfly.encryption.ICrypt;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.socks.SocksAddressType;


import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
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
        String host = null;
        int port = 0;
        ByteBuf buff = (ByteBuf) msg;

        if (buff.readableBytes() <= 0) {
            return;
        }
        ByteBuf dataBuff = buff.duplicate();
        int len = dataBuff.readableBytes();
        byte[] array = new byte[len];
        if (dataBuff.hasArray()){
            array = dataBuff.array();
        }else {
            dataBuff.getBytes(dataBuff.readerIndex(),array);
        }
        System.out.println(new String (array));
        int addressType = dataBuff.getUnsignedByte(0);
        if (addressType == SocksAddressType.IPv4.byteValue()) {
            if (dataBuff.readableBytes() < 7) {
                return;
            }
            dataBuff.readUnsignedByte();
            byte[] ipBytes = new byte[4];
            dataBuff.readBytes(ipBytes);
            host = InetAddress.getByAddress(ipBytes).toString().substring(1);
            port = dataBuff.readShort();
        } else if (addressType == SocksAddressType.DOMAIN.byteValue()) {
            int hostLength = dataBuff.getUnsignedByte(1);
            if (dataBuff.readableBytes() < hostLength + 4) {
                return;
            }
            dataBuff.readUnsignedByte();
            dataBuff.readUnsignedByte();
            byte[] hostBytes = new byte[hostLength];
            dataBuff.readBytes(hostBytes);
            host = new String(hostBytes);
            port = dataBuff.readShort();
        } else {
            throw new IllegalStateException("unknown address type: " + addressType);
        }
        System.out.println("addressType = " + addressType + ",host = " + host + ",port = " + port + ",dataBuff = "
                + dataBuff.readableBytes());
        super.channelRead(ctx, msg);
    }
}
