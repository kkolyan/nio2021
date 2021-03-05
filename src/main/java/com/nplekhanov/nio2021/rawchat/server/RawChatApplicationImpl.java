package com.nplekhanov.nio2021.rawchat.server;

import com.nplekhanov.nio2021.core.Peer;

import java.util.ArrayList;
import java.util.Collection;

public class RawChatApplicationImpl implements RawChatApplication {
    private final Collection<Peer> peers = new ArrayList<>();

    @Override
    public Collection<Peer> getPeers() {
        return peers;
    }
}
