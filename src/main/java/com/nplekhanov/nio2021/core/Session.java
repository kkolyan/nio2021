package com.nplekhanov.nio2021.core;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

final class Session implements Peer {
    ByteBuffer remainder;
    Queue<ByteBuffer> departure = new ArrayDeque<>();
    boolean wantToClose;
    SessionHandler sessionHandler;
    String address;

    @Override
    public void sendData(final ByteBuffer data) {
        departure.add(data);
    }

    @Override
    public void disconnect() {
        wantToClose = true;
    }

    @Override
    public String toString() {
        return "Session(0x" + Integer.toHexString(hashCode()) + ", " + address + ")";
    }
}
