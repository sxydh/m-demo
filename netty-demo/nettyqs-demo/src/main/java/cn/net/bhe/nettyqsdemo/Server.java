package cn.net.bhe.nettyqsdemo;

import cn.net.bhe.mutil.DtUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.log4j.Log4j2;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Log4j2
public class Server {

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) {
                            channel.pipeline()
                                    // 防止粘包
                                    .addLast(new LengthFieldPrepender(4))
                                    .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                    .addLast(new MsgHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(10010).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static class MsgHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msgObj) {
            if (msgObj instanceof ByteBuf) {
                ByteBuf byteBuf = (ByteBuf) msgObj;
                String msg = byteBuf.toString(Charset.defaultCharset());
                System.out.printf("[%s] [%s] Received - %s\r\n", DtUtils.format(), ctx.channel().remoteAddress(), msg);

                // 业务处理
                // 耗时操作必须在新的线程处理
                // 严禁阻塞 IO 线程

                byteBuf = Unpooled.copiedBuffer(String.valueOf(msg.length()), StandardCharsets.UTF_8);
                ctx.channel().writeAndFlush(byteBuf);
            }
        }

    }

}
