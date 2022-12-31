package org.example.loadBalancer;
import org.example.node.Node;

import java.util.List;

// Possible to expand Load Balancing algorithm options
public interface LoadBalancer {

    Node next(List<Node> nodes);
}