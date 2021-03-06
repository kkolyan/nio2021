package com.nplekhanov.nio2021.core;

import java.nio.ByteBuffer;

public interface Peer {

    /**
     *
     * @param data responsibility on releasing this buffer transferred to Peer after invocation,
     *             so caller of this method should not care
     */
    void sendData(ByteBuffer data);

    void disconnect();
}
