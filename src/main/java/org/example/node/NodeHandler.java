package org.example.node;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.example.loadBalancer.LoadBalancer;
import org.example.ReverseProxy.ReverseProxyServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NodeHandler {

    private final ReverseProxyServer server;

    // LoadBalancer from interface class
    private final LoadBalancer balancer;

    private final ReentrantReadWriteLock lock;

    // Managing if node status is active or not
    // Make sure Thread stops when program end
    private boolean nodeActive = true;

    private int portCounter;
    private final List<Node> activeNodes;
    private final List<Node> startingQueue;
    private final List<Node> closingQueue;

    private final int minAmountNodes;

    public NodeHandler(ReverseProxyServer server, LoadBalancer balancer) {
        this.server = server;
        this.balancer = balancer;
        this.activeNodes = new ArrayList<>();
        this.startingQueue = new ArrayList<>();
        this.closingQueue = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
        this.minAmountNodes = 3;
        this.portCounter = 8080;

        this.start(minAmountNodes);
        this.startThread();
    }

    //------------------------------------------------------------------------------------------------------------------

    // START NODE
    public void addStartedNode(Node node) {
        try {
            lock.writeLock().lock();

            // DEBUGGING In Node(Springboot application)
            // allows to view output in Node(spring boot application)
            var input = node.getProcess().getInputStream();
            var bytes = new byte[10000];
            var read = input.read(bytes);
            System.out.println(new String(bytes, 0, read));


            // Move Node from startingQue to activeNode list
            startingQueue.remove(node);
            activeNodes.add(node);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------------------

    // Choose Load Balancer and Node (Server)
    public Node chooseBalancer() {
        try {
            lock.readLock().lock();
            return balancer.next(activeNodes);
        } finally {
            lock.readLock().unlock();
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------------

    // START Process for Nodes
    private void start(int amount) {
        System.out.println("Starting " + amount + " nodes.");
        for (int i = 0; i < amount; i++) {
            var node = new Node(portCounter++);
            startingQueue.add(node);
            // startingQue iniate starting process of node

            try {
                node.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



                              // Node DEPLOYMENT
// --------------------------------------------------------------------------------------------------------------------------------------

   // Method check status for startingQue, closingQue and calculation of Req/seconds
    private void statusChecker() {
        // Iterator = list of elements(nodes)
        // Iterate over Nodes in startingQue list
        var iterator = startingQueue.iterator();


        // If iterator contains nodes(elements)
        while (iterator.hasNext()) {
            // Return the nodes(elements)
            var node = iterator.next();


            // Connecting to Node(Server)
            try {
                var bootstrap = new Bootstrap();
                bootstrap
                        .group(server.getWorkerGroup())
                        .channel(NioSocketChannel.class)

                        // Allows node(server) to start same time as ReverseProxy(client)
                        // Stops 3 s before moving Node from startingQue to activeNode list
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                        .handler(new InitiateNodeStartup(node, this))
                        .connect("localhost", node.getPort())
                        .sync();

            } catch (Exception ignored) {

            }
        }


        // Loop through closingQue list
        iterator = closingQueue.iterator();
        while (iterator.hasNext()) {
            var node = iterator.next();

            // Checks there is no clients connected to Node
            if (!node.getConnections().isEmpty()) continue;

            node.stop();

            // Removes Node from closingQue list
            iterator.remove();
        }


        // Counting amount Requests/second the Nodes(Servers) handle
        var requestCounter = 0.0;
        for (var node : activeNodes) {
            requestCounter+= node.getRequestCounter();

            // resets requestCounter to 0
            node.resetRequests();
        }

        //Calculating average Req/second
        requestCounter = requestCounter / (double) activeNodes.size();

        System.out.println("R/s: " + requestCounter);


        // If Req/second is less than 0.5 Req/second close node
        // Stops when minAmountOfNodes is reached
        if (requestCounter < 0.5 && activeNodes.size() > minAmountNodes) {
            closeOne();


            // When Req/second is more than 6 add new node to startingQue
        } else if (requestCounter >= 6.0) {
            int amount = (int) (requestCounter / 6.0);
            amount -= startingQueue.size();

            if (amount > 0)
                start(amount);
        }
    }


    //------------------------------------------------------------------------------------------------------------------
                                      // CLOSE NODES

    private void closeOne() {
        System.out.println("Closing  node");

        // Get last Node
        var i = activeNodes.size() - 1;
        var node = activeNodes.get(i);

        // removes node from activeNodes(list)
        activeNodes.remove(i);

        // Move Node to closingQue list
        // prevents stoping Node while handling active Request
        closingQueue.add(node);
    }

    // Ending all node connections
    public void closeAll() {
        try {
            lock.writeLock().lock();

            // Stoping Thread
            nodeActive = false;

            // end program
            activeNodes.forEach(Node::stop);
            closingQueue.forEach(Node::stop);
            startingQueue.forEach(Node::stop);
        } finally {
            lock.writeLock().unlock();
        }
    }



                                     // THREADS
//------------------------------------------------------------------------------------------------------------------
    // Starting Thread
    private void startThread() {
        var thread = new Thread(this::runThread);

        thread.start();
    }



    // startThread calls on runThread method to start
    private void runThread() {
        try {
            // locks thread(security reasons)
            lock.writeLock().lock();
            // Thread method runs until nodeActive(boolean) = false
            if (!nodeActive) {
                return;
            }

            statusChecker();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }

        // Stop Thread 2.5 sec
        try {
            // Checks nodes and que status every 2.5 sec
            Thread.sleep(2500);
        } catch(Exception e) {
            e.printStackTrace();
        }

        // Continue running Thread
        runThread();
    }
}
