package org.corefine.test.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * @author Fe by 2022/1/11 14:37
 */
public class TcpHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String data = (String) msg;
        System.out.println("rec：" + data);
        // 向客户端发送消息
        ByteBuf byteBuf = Unpooled.copiedBuffer(("rec@" + data.length() + "\r\n").getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(byteBuf);
    }

}
