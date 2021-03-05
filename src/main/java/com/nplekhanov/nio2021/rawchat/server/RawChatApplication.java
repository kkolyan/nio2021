package com.nplekhanov.nio2021.rawchat.server;

import com.nplekhanov.nio2021.core.Peer;

import java.util.Collection;

public interface RawChatApplication {
    Collection<Peer> getPeers();
}
