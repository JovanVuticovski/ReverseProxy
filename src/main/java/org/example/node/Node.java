package org.example.node;

import io.netty.channel.Channel;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Getter
public class Node {

    // ReentrantReadWriteLock = lock for Threads
    // Security for Threads
    private final ReentrantReadWriteLock lock;

    private final int port;
    private Process process;
    private int requestCounter;

   // private int maxWeight;

    // List of connections between ReverseProxy(Clients) and Nodes(Server)
    private List<Channel> connections;

    public Node(int port) {
        this.port = port;
        this.requestCounter = 0;
        this.connections = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
       // this.maxWeight = 5;
    }

    public void start() throws IOException {
        var args = new String[] {
                "java",
                "-jar",
                "spring.jar",
                "--server.port=" + port
        };

        this.process = Runtime
                .getRuntime()
                .exec(args);

        System.out.println("Started node: " + process.pid());
    }

    public void stop() {
        if (process == null)
            return;

        process.destroyForcibly();
        System.out.println("Stoping node: " + process.pid());
        process = null;
    }




                  // Connections between ReverseProxy(Client) and Node(Server)
    //------------------------------------------------------------------------------------------------------------------

    // Adding connection to channel between client and Node(Server)
    public void addConnection(Channel channel) {
        try {
            lock.writeLock().lock();
            this.connections.add(channel);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Removes connection from channel between ReverseProxy and Node
    public void endConnection(Channel channel) {
        try {
            lock.writeLock().lock();
            this.connections.remove(channel);
        } finally {
            lock.writeLock().unlock();
        }
    }




                                           //COUNT REQUEST
//----------------------------------------------------------------------------------------------------------------------
    // Method runns when receiving Request
    public void addRequest() {
        try {
            lock.writeLock().lock();
            // Increasing requestCounter by 1
            this.requestCounter++;
        } finally {
            lock.writeLock().unlock();
        }
    }


                               // RESET REQUEST COUNTER 
    //-------------------------------------------------------------------------------------------------------------------

    // Method is used to read and calculate how many Request/second
    // Rest request directly after adding new request
    public void resetRequests() {
        try {
            lock.writeLock().lock();
            // Setting requestCounter = 0
            this.requestCounter = 0;
        } finally {
            lock.writeLock().unlock();
        }
    }
}