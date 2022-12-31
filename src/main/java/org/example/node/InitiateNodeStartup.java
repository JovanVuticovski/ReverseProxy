
package org.example.node;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class InitiateNodeStartup extends ChannelInitializer<SocketChannel> {

    private final Node node;
    private final NodeHandler nodeHandler;

    public InitiateNodeStartup(Node node, NodeHandler nodeHandler) {
        this.node = node;
        this.nodeHandler = nodeHandler;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }


    // Connecting ReverseProxy(Client) with Node(Server
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel
                .pipeline()
                .addLast(new NodeStartupHandler(node, nodeHandler) {
                });
    }
}
