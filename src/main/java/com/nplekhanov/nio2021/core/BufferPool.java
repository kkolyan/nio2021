package com.nplekhanov.nio2021.core;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class BufferPool {

    private final List<Tier> pool = new ArrayList<>();
    private final int maxBufferSize;

    private static class Tier {
        final ArrayDeque<ByteBuffer> pool = new ArrayDeque<>();
        final int capacity;

        public Tier(final int capacity) {
            this.capacity = capacity;
        }
    }

    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    public BufferPool(final int minBufferSize, final int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
        generateTiers(minBufferSize, maxBufferSize);
    }

    public void release(final ByteBuffer buffer) {
        buffer.clear();

        Tier tier = getTier(buffer.capacity());
        tier.pool.add(buffer);
    }

    public ByteBuffer acquire(final int size) {
        Tier tier = getTier(size);
        ByteBuffer buffer = tier.pool.poll();
        if (buffer == null) {
            buffer = ByteBuffer.allocate(tier.capacity);
        }
        if (buffer.position() != 0 || buffer.limit() != buffer.capacity()) {
            // any released buffer is cleared, so this condition surely means that someone still use released buffer
            throw new IllegalStateException("buffer reference leak detected!");
        }
        return buffer;
    }

    private void generateTiers(final int minBufferSize, final int maxBufferSize) {
        int n = 1;
        while (n <= maxBufferSize) {
            if (n >= minBufferSize) {
                pool.add(new Tier(n));
            }
            n *= 2;
            if (n < 0) {
                throw new IllegalArgumentException("numeric overflow detected. invalid bounds.");
            }
        }
    }

    private Tier getTier(final int size) {
        for (Tier tier : pool) {
            if (tier.capacity < size) {
                continue;
            }
            return tier;
        }
        throw new IllegalStateException("size is too large: " + size);
    }

}
