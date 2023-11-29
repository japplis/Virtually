/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2023 Japplis.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.japplis.virtually;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A ReadableByteChannel that isn't 'synchronized' when reading the input stream but that uses a ReentrantLock to be virtual thread friendly.
 *
 * @author Anthony Goubard - Japplis
 */
public class ReadByteChannel implements ReadableByteChannel {

    private boolean isOpen = true;
    private final InputStream inputStream;
    private final ReentrantLock channelLock = new ReentrantLock();

    public ReadByteChannel(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int read(ByteBuffer dest) throws IOException {
        if (dest.isReadOnly()) {
            throw new IllegalArgumentException("The byte buffer should not be read only");
        }
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        try {
            channelLock.lockInterruptibly();
        } catch (InterruptedException ex) {
            close();
            throw new ClosedByInterruptException();
        }
        int byteRead = 0;
        int totalRead = 0;
        try {
            while (byteRead >= 0) {
                if (!isOpen()) {
                    throw new AsynchronousCloseException();
                }
                int destBufferSize = dest.remaining();
                if (destBufferSize <= 0) break;
                byte[] readBuffer = new byte[destBufferSize];
                byteRead = inputStream.read(readBuffer);
                if (byteRead > 0) {
                    totalRead += byteRead;
                    dest.put(readBuffer, 0, byteRead);
                }
            }
        } finally {
            channelLock.unlock();
        }
        return totalRead;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void close() throws IOException {
        isOpen = false;
    }
}
