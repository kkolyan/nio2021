package com.nplekhanov.nio2021.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NonBlockingServer {
    public static void run(int port, SessionHandlerFactory sessionHandlerFactory) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.bind(new InetSocketAddress(port));
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while (selector.isOpen()) {

            int select = selector.select(1000);
            if (select <= 0) {
                continue;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            for (Iterator<SelectionKey> iterator = selectedKeys.iterator(); iterator.hasNext(); ) {
                SelectionKey key = iterator.next();
                iterator.remove();

                SelectableChannel channel = key.channel();

                if (channel instanceof ServerSocketChannel) {
                    doAccept(sessionHandlerFactory, selector, (ServerSocketChannel) channel);

                } else if (channel instanceof SocketChannel) {
                    doIo(key, (SocketChannel) channel);

                } else {
                    throw new IllegalStateException("channel of unsupported type: " + channel);
                }
            }
        }
    }

    private static void doAccept(
        final SessionHandlerFactory sessionHandlerFactory,
        final Selector selector,
        final ServerSocketChannel channel
    ) throws IOException {
        SocketChannel accepted = channel.accept();
        if (accepted == null) {
            throw new IllegalStateException("WTF");
        }
        accepted.configureBlocking(false);
        SelectionKey socketKey = accepted.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        Session session = new Session();
        session.remoteHost = accepted.getRemoteAddress().toString();
        socketKey.attach(session);
        session.sessionHandler = sessionHandlerFactory.createSessionHandler(session);
    }

    private static void doIo(
        final SelectionKey key,
        final SocketChannel channel
    ) {
        Session session = (Session) key.attachment();
        try {
            if (key.isReadable()) {
                ByteBuffer data = session.arrival;
                channel.read(data);
                data.flip();
                session.sessionHandler.onReceive(data);
                data.compact();
            }
            if (key.isWritable()) {
                ByteBuffer data = session.departure;
                data.flip();
                channel.write(data);
                data.compact();
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace(System.out);
            session.wantToClose = true;
        }
        if (session.wantToClose) {
            key.cancel();
            try {
                channel.close();
            } catch (IOException ignored) {
            }
            session.sessionHandler.onDisconnect();
        }
    }
}
