package com.nplekhanov.nio2021.core;

import java.nio.ByteBuffer;

class Session implements Peer {
    ByteBuffer arrival = ByteBuffer.allocate(1024 * 1024);
    ByteBuffer departure = ByteBuffer.allocate(1024 * 1024);
    boolean wantToClose;
    SessionHandler sessionHandler;
    String address;

    @Override
    public void sendData(final OutputOp outputOp) {
        outputOp.doOutputOp(departure);
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
