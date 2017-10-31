package com.wade.server;

import com.wade.common.CustomHeartbeatHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by tingyun on 2017/10/31.
 */
public class ServerHandler extends CustomHeartbeatHandler {
    public ServerHandler() {
        super("server");
    }

    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        byte[] data = new byte[byteBuf.readableBytes() - 5];
        ByteBuf responseBuf = Unpooled.copiedBuffer(byteBuf);
        byteBuf.skipBytes(5);
        byteBuf.readBytes(data);
        String content = new String(data);
        System.out.println(name + " get content: " + content);
        channelHandlerContext.write("传给客户端: " + responseBuf);

    }

    @Override
    protected void handleReadIdle(ChannelHandlerContext ctx) {
        super.handleReadIdle(ctx);
        System.err.println("---client " + ctx.channel().remoteAddress().toString() + " reader timeout, close it---");
        ctx.close();
    }
}
