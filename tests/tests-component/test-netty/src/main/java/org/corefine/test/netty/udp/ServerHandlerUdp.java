package org.corefine.test.netty.udp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.nio.charset.StandardCharsets;

public class ServerHandlerUdp extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        String data = packet.content().toString(StandardCharsets.UTF_8);
        System.out.println("rec：" + data);
        // 向客户端发送消息
        ByteBuf byteBuf = Unpooled.copiedBuffer(("rec@" + data.length()).getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(new DatagramPacket(byteBuf, packet.sender()));
    }
}