package cn.net.bhe.proxynetty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.net.InetSocketAddress;

/**
 * @author sxydh
 * @see <a href="https://cloud.tencent.com/developer/article/1944498">参考</a>
 */
public class WebSocketServer {

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
                                .addLast(new HttpServerCodec())
                                .addLast(new ChunkedWriteHandler())
                                .addLast(new HttpObjectAggregator(1024 * 1024 * 1024))
                                .addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65535))
                                .addLast(new WebSocketHandler());
                    }
                });
        System.out.println("WebSocketServer准备就绪...");
        return bootstrap.bind(new InetSocketAddress(port)).sync();
    }

}
