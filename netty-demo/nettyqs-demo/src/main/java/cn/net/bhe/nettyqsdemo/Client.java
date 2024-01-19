package cn.net.bhe.nettyqsdemo;

import cn.net.bhe.mutil.CpUtils;
import cn.net.bhe.mutil.DtUtils;
import cn.net.bhe.nettyqsdemo.helper.ThreadPool;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.log4j.Log4j2;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Log4j2
public class Client {

    public static void main(String[] args) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline()
                                    .addLast(new LengthFieldPrepender(4))
                                    .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                    .addLast(new MsgHandler());
                        }
                    });
            ChannelFuture future = bootstrap.connect("localhost", 10010).sync();
            sendToServer(future.channel());
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private static void sendToServer(Channel channel) {
        ThreadPool.submit(() -> {
            try {
                int size = 100000000;
                while (--size >= 0) {
                    String msg = CpUtils.ranChnCp();
                    ByteBuf byteBuf = Unpooled.copiedBuffer(msg, StandardCharsets.UTF_8);
                    channel.writeAndFlush(byteBuf);
                    Thread.sleep(100);
                }
            } catch (Throwable e) {
                log.error(e.getLocalizedMessage(), e);
            }
        });
    }

    public static class MsgHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof ByteBuf) {
                ByteBuf byteBuf = (ByteBuf) msg;
                String msgString = byteBuf.toString(Charset.defaultCharset());
                System.out.printf("[%s] [%s] Received - %s\r\n", DtUtils.format(), ctx.channel().remoteAddress(), msgString);
            }
        }

    }

}
