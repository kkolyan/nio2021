package com.nplekhanov.nio2021.core;

public interface SessionHandlerFactory {

    SessionHandler createSessionHandler(Peer peer, BufferPool bufferPool);
}
