package org.example.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.node.Node;

// ResponseHandler => Client(ReverseProxy)
public class ResponseHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final Node node;

    // Client(ReverseProxy) channel
    private final Channel channel;

    public ResponseHandler(Node node, Channel channel) {
        this.node = node;
        this.channel = channel;
    }

    // Connecting ReverseProxy(Client) with Node(Server)
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Saving channel(connection) in Node
        node.addConnection(ctx.channel());
    }


    // Removes connection between Client and Node(Server)
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Removes connection from connection list
        node.endConnection(ctx.channel());
    }



    // Get Response message from Node(server)
    // Bytebuf = response message
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {

        // channel = ReverseProxy
        // Sending response back to ReverseProxy(Client)
        channel.writeAndFlush(buf.copy());


        // Increase RequestCounter +1
        node.addRequest();
        System.out.println(node.getPort());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        channel.close();
    }
}