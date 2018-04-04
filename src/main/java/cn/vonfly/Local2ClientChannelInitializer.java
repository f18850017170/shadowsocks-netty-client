package cn.vonfly;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socks.SocksInitRequestDecoder;

/**
 * local-client-channel 服务处理
 */
public final class Local2ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private Local2ClientSocksCmdRequestHandle local2ClientSocksCmdRequestHandle;

    public Local2ClientChannelInitializer() {
        this.local2ClientSocksCmdRequestHandle = new Local2ClientSocksCmdRequestHandle();
    }

    protected void initChannel(SocketChannel ch) throws Exception {
        // inbound handle
        ch.pipeline()
                .addLast(new SocksInitRequestDecoder())//socks 版本校验  验证通过时 返回SocksRequestType.INIT且移除该channelhandle
                .addLast(local2ClientSocksCmdRequestHandle);//处理 init 不验证 ；处理auth authentication success；处理cmd 移除该channelhandle 增加
        //outbound handle
        ch.pipeline().addFirst(new AutoRemoveSocksMessageEncoder());//socks 格式信息转换为byte返回给pc

    }
}
