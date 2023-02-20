package cn.net.bhe.proxynetty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class TcpHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.printf("cn.net.bhe.proxynetty.TcpHandler.channelRead, %s\n", msg);
        WebSocketHandler.channelGroup.writeAndFlush(new TextWebSocketFrame(msg.toString()));
    }

}
