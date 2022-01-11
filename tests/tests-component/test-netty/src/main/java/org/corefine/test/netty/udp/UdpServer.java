package org.corefine.test.netty.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * @author Fe by 2022/1/11 10:21
 */
public class UdpServer {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    // 主线程处理
                    .channel(NioDatagramChannel.class)
                    // 广播
                    .option(ChannelOption.SO_BROADCAST, true)
                    // 设置读缓冲区为2M
                    .option(ChannelOption.SO_RCVBUF, 2048 * 1024)
                    // 设置写缓冲区为1M
                    .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        protected void initChannel(NioDatagramChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            //udp 无法进行分包
//                            pipeline.addLast(new LineBasedFrameDecoder(1024));
//                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new ServerHandlerUdp());
                        }
                    });

            ChannelFuture f = bootstrap.bind(9500).sync();
            System.out.println("服务器正在监听......");
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
