package org.example.node;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NodeStartupHandler extends SimpleChannelInboundHandler<ByteBuf> {


    private final Node node;
    private final NodeHandler nodeHandler;

    public NodeStartupHandler(Node node, NodeHandler nodeHandler) {
        this.node = node;
        this.nodeHandler = nodeHandler;
    }



    // Method runs when ReverseProxy(Client) is connected to Node
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Moving Node from startingQue list to activeNodes list
        nodeHandler.addStartedNode(node);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    }

}
