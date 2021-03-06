package com.nplekhanov.nio2021.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public final class NonBlockingServer implements Runnable {
    private final int port;
    private final SessionHandlerFactory sessionHandlerFactory;
    private BufferPool bufferPool;
    private ByteBuffer sharedArrival;

    public NonBlockingServer(
        final int port,
        final SessionHandlerFactory sessionHandlerFactory
    ) {
        this.port = port;
        this.sessionHandlerFactory = sessionHandlerFactory;
    }

    @Override
    public void run() {
        try (Selector selector = Selector.open()) {

            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            bufferPool = new BufferPool(64, 1024 * 1024);
            sharedArrival = ByteBuffer.allocate(bufferPool.getMaxBufferSize());

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
                        doAccept((ServerSocketChannel) channel, selector);

                    } else if (channel instanceof SocketChannel) {
                        doIo(key, (SocketChannel) channel);

                    } else {
                        throw new IllegalStateException("channel of unsupported type: " + channel);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void doAccept(final ServerSocketChannel channel, final Selector selector) throws IOException {
        SocketChannel accepted = channel.accept();
        if (accepted == null) {
            throw new IllegalStateException("WTF");
        }
        accepted.configureBlocking(false);
        SelectionKey socketKey = accepted.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        Session session = new Session();
        session.setAddress(accepted.getRemoteAddress().toString());
        socketKey.attach(session);
        session.setSessionHandler(sessionHandlerFactory.createSessionHandler(session, bufferPool));
    }

    private void doIo(final SelectionKey key, final SocketChannel channel) {
        Session session = (Session) key.attachment();
        try {
            if (key.isReadable()) {
                sharedArrival.clear();
                if (session.getRemainder() != null) {
                    sharedArrival.put(session.getRemainder());
                    bufferPool.release(session.getRemainder());
                    session.setRemainder(null);
                }
                channel.read(sharedArrival);
                sharedArrival.flip();
                session.getSessionHandler().onReceive(sharedArrival);

                if (sharedArrival.hasRemaining()) {
                    ByteBuffer remainder = bufferPool.acquire(sharedArrival.remaining());
                    remainder.put(sharedArrival);
                    remainder.flip();
                    session.setRemainder(remainder);
                }
            }
            if (key.isWritable()) {
                while (true) {
                    ByteBuffer data = session.getDeparture().peek();
                    if (data == null) {
                        break;
                    }
                    data.flip();
                    channel.write(data);
                    if (data.hasRemaining()) {
                        data.compact();
                        break;
                    }
                    session.getDeparture().remove();
                    bufferPool.release(data);
                }
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace(System.out);
            session.disconnect();
        }
        if (session.isClosed()) {
            key.cancel();
            try {
                channel.close();
            } catch (IOException ignored) {
            }
            session.getSessionHandler().onDisconnect();
        }
    }
}
