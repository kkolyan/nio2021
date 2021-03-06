package com.nplekhanov.nio2021.rawchat.server;

import com.nplekhanov.nio2021.core.BufferPool;
import com.nplekhanov.nio2021.core.Peer;
import com.nplekhanov.nio2021.core.SessionHandler;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class RawChatSessionHandler implements SessionHandler {

    private final Peer peer;
    private final RawChatApplication application;
    private final BufferPool bufferPool;

    public RawChatSessionHandler(
        final Peer peer,
        final RawChatApplication application,
        final BufferPool bufferPool
    ) {
        this.peer = peer;
        this.application = application;
        this.bufferPool = bufferPool;
        application.getPeers().add(peer);
        broadcast(peer + " joined");
    }

    @Override
    public void onDisconnect() {
        application.getPeers().remove(peer);
        broadcast(peer + " left");
    }

    @Override
    public void onReceive(final ByteBuffer data) {
        String inputMessage = tryReadMessage(data);
        if (inputMessage == null) {
            return;
        }
        if (inputMessage.equals("exit")) {
            peer.disconnect();
        }

        broadcast(peer + ": " + inputMessage);
    }

    private String tryReadMessage(final ByteBuffer data) {
        if (data.remaining() < Integer.BYTES) {
            return null;
        }
        int expected = data.getInt(data.position());
        if (data.remaining() < expected + Integer.BYTES) {
            return null;
        }
        data.getInt();

        byte[] bytes = new byte[expected];
        data.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void broadcast(final String text) {
        for (final Peer peer : application.getPeers()) {
            ByteBuffer out = bufferPool.acquire(text.length() + Integer.BYTES);
            putIntPrefixedUtf8(text, out);
            peer.sendData(out);
        }
    }

    private static void putIntPrefixedUtf8(final String s, final ByteBuffer data) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        data.putInt(bytes.length);
        data.put(bytes);
    }
}
