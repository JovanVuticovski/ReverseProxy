package org.example.loadBalancer;

import org.example.node.Node;

import java.util.List;
import java.util.Random;

public class RandomAlgorithm implements LoadBalancer {

    Random random = new Random();
    @Override
   public Node next(List<Node> nodes) {

        if (nodes.isEmpty())
            throw new RuntimeException("Sorry there is no active nodes.");

        int index = random.nextInt(nodes.size());
        return nodes.get(index);

    }
}
