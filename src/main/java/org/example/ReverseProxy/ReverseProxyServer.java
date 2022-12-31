package org.example.ReverseProxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.example.loadBalancer.RandomNodeAlgorithm;
import org.example.node.NodeHandler;

import java.util.Scanner;

public class ReverseProxyServer {

    private final NodeHandler nodeHandler;

    private final int port;
    private final EventLoopGroup adminGroup, workerGroup;

    public ReverseProxyServer(int port) {
        this.port = port;
        this.adminGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        // Choosing Load Balancing algorithm
        this.nodeHandler = new NodeHandler(this, new RandomNodeAlgorithm());
    }

    public void start() {

        // Starting connection between ReverseProxyServer and Original Client
        var bootstrap = new ServerBootstrap();

        try {
            var channel = bootstrap
                    .group(adminGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)

                    // Creating Initializer for incoming requests
                    .childHandler(new InitiateRequestHandler(this))
                    .bind(port)
                    .sync()
                    .channel();




            var input = new Scanner(System.in);
            while (!input.nextLine().equals("Quit")) {

            }


            // Closing the process
            nodeHandler.closeAll();
            channel.close();

            workerGroup.shutdownGracefully();
            adminGroup.shutdownGracefully();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public NodeHandler getNodeHandler() {
        return nodeHandler;
    }
}