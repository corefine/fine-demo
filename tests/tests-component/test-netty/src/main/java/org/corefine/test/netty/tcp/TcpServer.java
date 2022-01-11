package org.corefine.test.netty.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class TcpServer {
    public void run() throws Exception{
        //NioEventLoopGroup是用来处理IO操作的多线程事件循环器
        EventLoopGroup bossGroup  = new NioEventLoopGroup();  // 用来接收进来的连接
        EventLoopGroup workerGroup  = new NioEventLoopGroup();// 用来处理已经被接收的连接
        try{
            ServerBootstrap server =new ServerBootstrap();//是一个启动NIO服务的辅助启动类
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)  // 这里告诉Channel如何接收新的连接
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LineBasedFrameDecoder(1024 * 1024 * 4));
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new TcpHandler());
                        }
                    });
            server.option(ChannelOption.SO_BACKLOG,128);
            server.childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = server.bind(9500).sync();// 绑定端口，开始接收进来的连接
            System.out.println("服务端启动成功...");
            // 监听服务器关闭监听
            f.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully(); //关闭EventLoopGroup，释放掉所有资源包括创建的线程
            workerGroup.shutdownGracefully();
        }

        // 服务器绑定端口监听


    }
    public static void main(String[] args) throws Exception {
        new TcpServer().run();
    }
}