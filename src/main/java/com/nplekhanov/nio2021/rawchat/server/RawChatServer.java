package com.nplekhanov.nio2021.rawchat.server;

import com.nplekhanov.nio2021.core.NonBlockingServer;

public class RawChatServer {

    private RawChatServer() {
    }

    public static void main(final String[] args) {

        RawChatApplication application = new RawChatApplicationImpl();

        NonBlockingServer server = new NonBlockingServer(8080, (peer, bufferPool) ->
            new RawChatSessionHandler(peer, application, bufferPool)
        );
        server.run();
    }
}
