package com.nplekhanov.nio2021.core;

public interface Peer {

    void sendData(OutputOp outputOp);

    void disconnect();
}
