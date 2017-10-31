package com.wade.client;

import com.wade.common.CustomHeartbeatHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by tingyun on 2017/10/31.
 */
public class ClientHandler extends CustomHeartbeatHandler {
    public ClientHandler() {
        super("client");
    }

    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        byte[] data= new byte[byteBuf.readableBytes() - 5];
        byteBuf.skipBytes(5);
        byteBuf.readBytes(data);
        String content = new String(data);
        System.out.println(name + " get content: " + content);
    }

    @Override
    protected void handleAllIdle(ChannelHandlerContext ctx) {
        super.handleAllIdle(ctx);
        //客户端调用该方法向服务端发送PING消息
        sendPingMsg(ctx);
    }
}
