package cn.vonfly;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socks.SocksInitRequestDecoder;

/**
 * socks服务处理
 */
public final class ClientServerInitializer extends ChannelInitializer<SocketChannel> {
    private Local2ClientChannelInitializer socksRequestHandle;

    public ClientServerInitializer() {
        this.socksRequestHandle = new Local2ClientChannelInitializer();
    }

    protected void initChannel(SocketChannel ch) throws Exception {
        // inbound handle
        ch.pipeline()
                .addLast(new SocksInitRequestDecoder())//socks 版本校验  验证通过时 返回SocksRequestType.INIT且移除该channelhandle
                .addLast(socksRequestHandle);//处理 init 不验证 ；处理auth authentication success；处理cmd 移除该channelhandle 增加
        //outbound handle
        ch.pipeline().addFirst(new AutoRemoveSocksMessageEncoder());//socks 格式信息转换为byte返回给pc

    }
}
