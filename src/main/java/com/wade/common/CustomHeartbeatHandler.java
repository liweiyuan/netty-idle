package com.wade.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by tingyun on 2017/10/31.
 * 负责心跳的发送与接收
 * https://segmentfault.com/a/1190000006931568
 */
public abstract class CustomHeartbeatHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public static final byte PING_MSG = 1;
    public static final byte PONG_MSG = 2;
    public static final byte CUSTOM_MSG = 3;
    protected String name;
    private int heartbeatCount = 0;

    public CustomHeartbeatHandler(String name) {
        this.name = name;
    }

    /**
     * 我们接下来看看数据处理部分:
     *
     * @param ctx
     * @param byteBuf
     * @throws Exception +--------+-----+---------------+
     *                   | Length |Type |   Content     |
     *                   |   17   |  1  |"HELLO, WORLD" |
     *                   +--------+-----+---------------+
     *                   来判断当前的报文类型, 如果是 PING_MSG 则表示是服务器收到客户端的 PING 消息,
     *                   此时服务器需要回复一个 PONG 消息, 其消息类型是 PONG_MSG.
     *                   扔报文类型是 PONG_MSG, 则表示是客户端收到服务器发送的 PONG 消息, 此时打印一个 log 即可.
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                ByteBuf byteBuf) throws Exception {
        if (byteBuf.getByte(4) == PING_MSG) {
            sendPongMsg(ctx);
        } else if (byteBuf.getByte(4) == PONG_MSG) {
            System.out.println(name + " get pong msg from " + ctx.channel().remoteAddress());
        } else {
            handleData(ctx, byteBuf);
        }
    }

    public void sendPingMsg(ChannelHandlerContext ctx) {
        ByteBuf byteBuf = ctx.alloc().buffer(5);
        byteBuf.writeInt(5);
        byteBuf.writeByte(PING_MSG);
        ctx.writeAndFlush(byteBuf);
        heartbeatCount++;
        System.out.println(name + " sent ping msg to " + ctx.channel().remoteAddress() + ", count: " + heartbeatCount);
    }

    private void sendPongMsg(ChannelHandlerContext ctx) {
        ByteBuf byteBuf = ctx.alloc().buffer(5);
        byteBuf.writeInt(5);
        byteBuf.writeByte(PONG_MSG);
        ctx.channel().writeAndFlush(byteBuf);
        heartbeatCount++;
        System.out.println(name + " sent pong msg to " + ctx.channel().remoteAddress() + ", count: " + heartbeatCount);
    }


    protected abstract void handleData(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf);

    /**
     * <p></p>类 CustomHeartbeatHandler 负责心跳的发送和接收, 我们接下来详细地分析一下它的作用. 我们在前面提到,
     * IdleStateHandler 是实现心跳的关键, 它会根据不同的 IO idle 类型来产生不同的 IdleStateEvent 事件,
     * 而这个事件的捕获, 其实就是在 userEventTriggered 方法中实现的.<p></p>
     *
     * @param ctx
     * @param evt
     * @throws Exception 实例化一个 IdleStateHandler 需要提供三个参数:
     *                   <p>
     *                   readerIdleTimeSeconds, 读超时. 即当在指定的时间间隔内没有从 Channel 读取到数据时, 会触发一个 READER_IDLE 的 IdleStateEvent 事件.
     *                   writerIdleTimeSeconds, 写超时. 即当在指定的时间间隔内没有数据写入到 Channel 时, 会触发一个 WRITER_IDLE 的 IdleStateEvent 事件.
     *                   allIdleTimeSeconds, 读/写超时. 即当在指定的时间间隔内没有读或写操作时, 会触发一个 ALL_IDLE 的 IdleStateEvent 事件.
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // IdleStateHandler 所产生的 IdleStateEvent 的处理逻辑.
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case READER_IDLE:
                    handleReadIdle(ctx);
                    break;
                case WRITER_IDLE:
                    handleWriteIdle(ctx);
                    break;
                case ALL_IDLE:
                    handleAllIdle(ctx);
                    break;
                default:
                    break;
            }
        }
    }

    protected void handleReadIdle(ChannelHandlerContext ctx) {
        System.err.println("---READER_IDLE---");
    }

    protected void handleWriteIdle(ChannelHandlerContext ctx) {
        System.err.println("---WRITER_IDLE---");
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {
        System.err.println("---ALL_IDLE---");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("---" + ctx.channel().remoteAddress() + " is active---");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("---" + ctx.channel().remoteAddress() + " is inactive---");
    }
}
