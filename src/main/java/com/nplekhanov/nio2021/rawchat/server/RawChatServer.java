package com.nplekhanov.nio2021.rawchat.server;

import com.nplekhanov.nio2021.core.NonBlockingServer;

import java.io.IOException;

public class RawChatServer {
    public static void main(String[] args) throws IOException {

        RawChatApplication application = new RawChatApplicationImpl();

        NonBlockingServer.run(8080, peer ->
            new RawChatSessionHandler(peer, application)
        );
    }
}
