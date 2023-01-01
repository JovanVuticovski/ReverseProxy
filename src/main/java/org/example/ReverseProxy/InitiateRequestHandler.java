package org.example.ReverseProxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

// Creating a handler class to handle incoming requests
// In coming requests = Original Client
public class InitiateRequestHandler extends ChannelInitializer<SocketChannel> {

    // Inject ReverseProxyServer class to access Eventloop workerGroup
    // workerGroup allows creating  Boostrap
    private final ReverseProxyServer server;

    public InitiateRequestHandler(ReverseProxyServer server) {
        this.server = server;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {

        // Original Client sends Request to pipeline
        // Pipeline redirects request to RequestHandler
        var pipeline = channel.pipeline();


        // Add handler for request message from Original Client
        // Sending in server to get WorkerGroup
        pipeline.addLast(new RequestHandler(server));
    }
}