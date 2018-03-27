package cn.vonfly;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(1),new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .localAddress(1081)
                .childHandler(new SocksServerInitializer());
        try {
            serverBootstrap.bind().sync().channel().closeFuture().sync();
            System.out.println("local server start ");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
