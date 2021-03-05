package com.nplekhanov.nio2021.rawchat.server;

import com.nplekhanov.nio2021.core.Peer;
import com.nplekhanov.nio2021.core.SessionHandler;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class RawChatSessionHandler implements SessionHandler {

    private final Peer peer;
    private final RawChatApplication application;

    public RawChatSessionHandler(final Peer peer, final RawChatApplication application) {
        this.peer = peer;
        this.application = application;
        application.getPeers().add(peer);
        broadcast("joined: " + peer);
    }

    @Override
    public void onDisconnect() {
        application.getPeers().remove(peer);
        broadcast("joined: " + "left: " + peer);
    }

    @Override
    public void onReceive(final ByteBuffer data) {
        String inputMessage = tryReadMessage(data);
        if (inputMessage == null) {
            return;
        }

        broadcast("broadcast by " + peer + ": " + inputMessage);
    }

    private String tryReadMessage(final ByteBuffer data) {
        if (data.remaining() < 4) {
            return null;
        }
        int expected = data.getInt(data.position());
        if (data.remaining() < expected + 4) {
            return null;
        }
        data.getInt();

        byte[] bytes = new byte[expected];
        data.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void broadcast(final String text) {
        for (final Peer peer : application.getPeers()) {
            peer.sendData(out -> putIntPrefixedUtf8(text, out));
        }
    }

    private static void putIntPrefixedUtf8(final String s, final ByteBuffer data) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        data.putInt(bytes.length);
        data.put(bytes);
    }
}
