package com.nplekhanov.nio2021.core;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

final class Session implements Peer {
    private ByteBuffer remainder;
    private final Queue<ByteBuffer> departure = new ArrayDeque<>();
    private boolean closed;
    private SessionHandler sessionHandler;
    private String address;

    @Override
    public void sendData(final ByteBuffer data) {
        departure.add(data);
    }

    @Override
    public void disconnect() {
        closed = true;
    }

    @Override
    public String toString() {
        return "Session(0x" + Integer.toHexString(hashCode()) + ", " + address + ")";
    }

    public ByteBuffer getRemainder() {
        return remainder;
    }

    public void setRemainder(ByteBuffer remainder) {
        this.remainder = remainder;
    }

    public Queue<ByteBuffer> getDeparture() {
        return departure;
    }

    public boolean isClosed() {
        return closed;
    }

    public SessionHandler getSessionHandler() {
        return sessionHandler;
    }

    public void setSessionHandler(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
