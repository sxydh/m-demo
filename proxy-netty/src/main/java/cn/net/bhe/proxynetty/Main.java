package cn.net.bhe.proxynetty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class Main {

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ChannelFuture webSocketFuture = WebSocketServer.build(bossGroup, workerGroup, 3010);
            ChannelFuture tcpFuture = TcpServer.build(bossGroup, workerGroup, 3020);
            webSocketFuture.channel().closeFuture().sync();
            tcpFuture.channel().closeFuture().sync();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
