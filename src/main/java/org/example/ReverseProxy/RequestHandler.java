package org.example.ReverseProxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.example.client.InitiateResponseHandler;

// ReverseProxy = client
// ReverseProxyServer(Client) -->  Node(spring boot application)


// Handles all incoming request msg from Original client
public class RequestHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final ReverseProxyServer server;

    // channel = ReverseProxy(Client) and Node(Server)
    private Channel channel;

    public RequestHandler(ReverseProxyServer server) {
        this.server = server;
    }


    // Getting request message from Original Client(Postman)
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {

        // Channel connection between ReverseProxyServer(Client) and Node(Server)
        var bootstrap = new Bootstrap();

        // Selecting Node
        var node = server.getNodeHandler().chooseBalancer();


        try {
            this.channel = bootstrap
                    // Get workerGroup from server (ReverseProxyServer)
                    .group(server.getWorkerGroup())
                    .channel(NioSocketChannel.class)

                    // Creating Initialiser for response messages
                    // ctx.channel = Channel for ReverseProxy(Client) and Node(Server)
                    .handler(new InitiateResponseHandler(node, ctx.channel()))
                    .connect("localhost", node.getPort())
                    .sync()
                    .channel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Bytebuf = actual Request message
        // Send Request message to Node(server)
        channel.writeAndFlush(buf.copy());
    }



    // ERROR handling
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // IF error happends close all connections
        cause.printStackTrace();
        ctx.channel().close();
        channel.close();
    }
}