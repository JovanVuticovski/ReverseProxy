package org.example.loadBalancer;
import org.example.node.Node;

import java.util.List;

public class RoundRobinAlgorithm implements LoadBalancer {

    private int nodeCounter = 0;


    // nodeWeight > nodes.size
    @Override
    public Node next(List<Node> nodes) {
        if (nodes.isEmpty())
            throw new RuntimeException("Sorry there is no active nodes.");

        nodeCounter++;
        if (nodeCounter >= nodes.size())
            nodeCounter = 0;

        return nodes.get(nodeCounter);
    }

}