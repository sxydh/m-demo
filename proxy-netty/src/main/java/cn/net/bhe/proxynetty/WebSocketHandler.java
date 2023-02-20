package cn.net.bhe.proxynetty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

    static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.printf("cn.net.bhe.proxynetty.WebSocketHandler.handlerAdded, %s\n", ctx.channel());
        channelGroup.add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
    }

}
