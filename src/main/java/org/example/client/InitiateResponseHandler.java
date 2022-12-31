package org.example.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.example.node.Node;

// Sending orginal client message to Node
public class InitiateResponseHandler extends ChannelInitializer<SocketChannel> {

    private final Node node;

    // clientChannel = ReverseProxy(Client) and Node(server) connection
    private final Channel clientChannel;

    public InitiateResponseHandler(Node node, Channel channel) {
        this.clientChannel = channel;
        this.node = node;
    }



   // Send request message to ResponseHandler
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {

        // Creating pipeline
        var pipeline = channel.pipeline();


        // Add handler for ReverseProxy and Node channel
        // clientChannel = Channel between ReverseProxy and Node(server)
        pipeline.addLast(new ResponseHandler(node, clientChannel));
    }
}