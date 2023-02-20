package cn.net.bhe.proxynetty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

/**
 * @author sxydh
 * @see <a href="https://developer.aliyun.com/article/769587">参考</a>
 * @see <a href="https://github.com/sinpolib/sokit">TCP/UDP测试工具</a>
 */
public class TcpServer {

    public static ChannelFuture build(EventLoopGroup bossGroup, EventLoopGroup workerGroup, int port) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new TcpHandler());
                    }
                });
        System.out.println("TcpServer准备就绪...");
        return bootstrap.bind(new InetSocketAddress(port)).sync();
    }

}
