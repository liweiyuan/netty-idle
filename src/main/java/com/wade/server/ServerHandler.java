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

    /**
     * 在这里面实现 EchoServer 的功能: 即收到客户端的消息后, 立即原封不动地将消息回复给客户端.
     * @param channelHandlerContext
     * @param byteBuf
     */
    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        byte[] data = new byte[byteBuf.readableBytes() - 5];
        ByteBuf responseBuf = Unpooled.copiedBuffer(byteBuf);
        byteBuf.skipBytes(5);
        byteBuf.readBytes(data);
        String content = new String(data);
        System.out.println(name + " get content: " + content);
        channelHandlerContext.write(responseBuf);

    }

    /**
     * 第二个重写的方法是 handleReaderIdle, 因为服务器仅仅对客户端的读 idle 感兴趣, 因此只重新了这个方法.
     * 若服务器在指定时间后没有收到客户端的消息, 则会触发 READER_IDLE 消息, 进而会调用 handleReaderIdle 这个方法.
     * 我们在前面提到, 客户端负责发送心跳的 PING 消息, 并且服务器的 READER_IDLE 的超时时间是客户端发送 PING
     * 消息的间隔的两倍, 因此当服务器 READER_IDLE 触发时, 就可以确定是客户端已经掉线了, 因此服务器直接关闭客户端连接即可.
     * @param ctx
     */
    @Override
    protected void handleReadIdle(ChannelHandlerContext ctx) {
        super.handleReadIdle(ctx);
        System.err.println("---client " + ctx.channel().remoteAddress().toString() + " reader timeout, close it---");
        ctx.close();
    }
}
