package loglang;

import nez.io.SourceContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by skgchxngsxyz-osx on 15/09/15.
 */
public class InputStreamContext extends SourceContext {
    static int DEFAULT_CAPACITY = 4096;
    static int DEFAULT_READ_SIZE = 256;

    private final InputStream input;

    private long mask = DEFAULT_CAPACITY - 1;

    private byte[] buffer = new byte[DEFAULT_CAPACITY];


    private int readOffset = 0;
    private int usedSize = 0;

    private long startPos = 0;

    /**
     * sum of read data size.
     */
    private long inputSize = 0;

    /**
     *
     * @param sourceName
     * @param input
     * must be utf8 string
     */
    public InputStreamContext(String sourceName, InputStream input) {
        super(Objects.requireNonNull(sourceName), 1);
        this.input = Objects.requireNonNull(input);
    }

    @Override
    public long length() {
        this.reserve(1);
        return this.inputSize;
    }

    /**
     *
     * @param pos
     * must be more than readOffset
     * @return
     */
    @Override
    public int byteAt(long pos) {
        this.reserve(1);
        return this.getByte((int) pos) & 0xff;
    }

    @Override
    public int EOF() {
        return 0;
    }

    @Override
    public byte[] subbyte(long startIndex, long endIndex) { //FIXME: range check
        int size = (int) (endIndex - startIndex);

        this.reserve(size);

        byte[] b = new byte[size];
        for(long index = startIndex; index < endIndex; index++) {   //FIXME: faster copy
            b[(int) (index - startIndex)] = this.getByte(index);
        }
        return b;
    }

    @Override
    public String substring(long startIndex, long endIndex) {
        return new String(this.subbyte(startIndex, endIndex));
    }

    @Override
    public long linenum(long pos) { //FIXME: line number table
        return 0;
    }

    @Override
    public boolean match(long pos, byte[] text) {   //FIXME: faster matching
        if(!this.reserve(text.length)) {
            return false;
        }

        for(int i = 0; i < text.length; i++) {
            if(text[i] != this.getByte(pos + i)) {
                return false;
            }
        }
        return true;
    }

    private byte getByte(long index) {
        return this.buffer[(int)(index & this.mask)];
    }

    private boolean reserve(int needSize) {
        final long afterPos = this.getPosition() + needSize;
        if(afterPos > this.inputSize) {
            // compute reading size
            int readingSize = DEFAULT_READ_SIZE;
            while(readingSize + this.inputSize < afterPos) {
                readingSize *= 2;
            }

            // check capacity
            this.expandBuf(readingSize);

            // read from input
            final int actualPos = (int)(this.getPosition() & this.mask);
            int readSize;
            if(actualPos + readingSize <= this.buffer.length) { // direct read
                try {
                    readSize = this.input.read(this.buffer, actualPos, readingSize);
                    if(readSize < 0) {
                        return false;
                    }
                    this.usedSize += readSize;
                    this.inputSize += readSize;
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    final int bottomReadSize = this.buffer.length - actualPos;
                    readSize = this.input.read(this.buffer, actualPos, bottomReadSize);
                    if(readSize < 0) {
                        return false;
                    }
                    if(readSize == bottomReadSize) {
                        readSize += this.input.read(this.buffer, 0, actualPos + readingSize - this.buffer.length);
                    }
                    this.usedSize += readSize;
                    this.inputSize += readSize;
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return readSize >= needSize;
        }
        return true;
    }

    private void expandBuf(int readingSize) {
        final int bufferSize = readingSize + this.usedSize;
        if(bufferSize > this.buffer.length) {
            // compute new buffer size
            int newSize = this.buffer.length;
            do {
                newSize *= 2;
            } while(newSize < bufferSize);

            // expand buffer
            byte[] newBuf = new byte[newSize];
            System.arraycopy(this.buffer, this.readOffset, newBuf, 0, this.buffer.length - this.readOffset);
            System.arraycopy(this.buffer, 0, newBuf, this.buffer.length - this.readOffset, this.readOffset);

            this.buffer = newBuf;
            this.readOffset = 0;
            this.mask = this.buffer.length - 1;
        }
    }

    public void trim() {
        final int remainSize = (int)(this.usedSize - (this.getPosition() - this.startPos));

        this.readOffset = (int)(this.getPosition() & this.mask);
        this.startPos = this.getPosition();
        this.usedSize = remainSize;
    }
}
