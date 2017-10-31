package com.wade.client;

import com.wade.common.CustomHeartbeatHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Random;

/**
 * Created by tingyun on 2017/10/31.
 */
public class Client {
    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup(4);
        Random random = new Random(System.currentTimeMillis());
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline channelPipeline = socketChannel.pipeline();
                            channelPipeline.addLast(
                                    new IdleStateHandler(0, 0, 5));
                            channelPipeline.addLast(
                                    new LengthFieldBasedFrameDecoder(1024, 0, 4, -4, 0));
                            channelPipeline.addLast(new ClientHandler());
                        }
                    });
            Channel channel = bootstrap.remoteAddress("127.0.0.1", 12345).connect().sync().channel();
            for (int i = 0; i < 10; i++) {
                String message = "cleint msg " + i;
                ByteBuf byteBuf = channel.alloc().buffer();
                byteBuf.writeInt(5 + message.getBytes().length);
                byteBuf.writeByte(CustomHeartbeatHandler.CUSTOM_MSG);
                byteBuf.writeBytes(message.getBytes());
                channel.writeAndFlush(byteBuf);
                Thread.sleep(/*random.nextInt(20000)*/20000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
