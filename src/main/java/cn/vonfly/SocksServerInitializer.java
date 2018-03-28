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

    public SocksServerInitializer() {
        this.socksMessageEncoder = new SocksMessageEncoder();
        this.socksRequestHandle = new SocksRequestHandle();
    }

    protected void initChannel(SocketChannel ch) throws Exception {
            // inbound handle
            ch.pipeline()
                    .addLast(new SocksInitRequestDecoder())//socks 版本校验  验证通过时 返回SocksRequestType.INIT且移除该channelhandle
                    .addLast(socksRequestHandle);//处理 init 不验证 ；处理auth authentication success；处理cmd 移除该channelhandle 增加
            //outbound handle
            ch.pipeline().addFirst(socksMessageEncoder);//socks 格式信息转换为byte返回给pc

    }
}
