package com.nplekhanov.nio2021.core;

import java.nio.ByteBuffer;

public interface SessionHandler {

    void onReceive(ByteBuffer data);

    void onDisconnect();

}
