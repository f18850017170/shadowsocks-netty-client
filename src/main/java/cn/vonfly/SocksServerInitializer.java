package cn.vonfly;

import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socks.SocksInitRequestDecoder;
import io.netty.handler.codec.socks.SocksMessageEncoder;

/**
 * socks服务处理
 */
public final class SocksServerInitializer extends ChannelInitializer<SocketChannel>{
    private SocksMessageEncoder socksMessageEncoder;
    private SocksRequestHandle socksRequestHandle;

    public SocksServerInitializer(ChannelConfig channelConfig) {
        this.socksMessageEncoder = new SocksMessageEncoder();
        this.socksRequestHandle = new SocksRequestHandle(channelConfig);
    }

    protected void initChannel(SocketChannel ch) throws Exception {
            // inbound handle
            ch.pipeline()
                    .addLast(new SocksInitRequestDecoder())
                    .addLast(socksRequestHandle);
            //outbound handle
            ch.pipeline().addFirst(socksMessageEncoder);

    }
}
