package org.example;

import org.example.ReverseProxy.ReverseProxyServer;

public class Main {
    public static void main(String[] args) {

        new ReverseProxyServer( 3000).start();
    }
}