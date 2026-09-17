package replaysystem.replayfmt;

import arc.files.Fi;
import arc.util.Nullable;
import replaysystem.replayfmt.helpers.Types;
import replaysystem.replayfmt.helpers.Util;
import replaysystem.replayfmt.types.Block;
import replaysystem.replayfmt.types.Tick;
import replaysystem.replayfmt.types.Unit;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class ReplayFmtReader implements Iterable<Object>, AutoCloseable {
    private final RandomAccessFile replayFile;
    private final RandomAccessFile indexFile;

    private final ReplayFmtIterator globalIterator = new ReplayFmtIterator();

    public ReplayFmtReader(Fi inputFile) {
        try {
            this.replayFile = new RandomAccessFile(inputFile.file(), "r");
            this.indexFile = new RandomAccessFile(Util.dotIndex(inputFile).file(), "r");
        } catch (Exception e) {
            throw new RuntimeException("Failed to open replay file", e);
        }
    }

    public class ReplayFmtIterator implements Iterator<Object> {

        private Object nextElement = null;

        @Override
        public boolean hasNext() {
            if (this.nextElement == null) {
                this.nextElement = readNextElement();
            }
            return this.nextElement != null;
        }

        public Object peek() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return this.nextElement;
        }

        @Override
        public Object next() {
            var element = this.peek();
            this.resetNextElement();
            return element;
        }

        public void resetNextElement() {
            this.nextElement = null;
        }
    }

    @Override
    public ReplayFmtIterator iterator() {
        return this.globalIterator;
    }

    public @Nullable ReplayFmtMap readSnapshot() {
        var iter = this.iterator();

        if (!iter.hasNext()) {
            return null;
        }

        var map = new ReplayFmtMap();
        var hasTick = false;

        while (iter.hasNext()) {
            var obj = iter.peek();
            if (obj instanceof Tick) {
                if (hasTick) {
                    break;
                }
                hasTick = true;
                map.add(Types.TICK, iter.next());
                continue;
            }
            if (obj instanceof Unit) {
                map.add(Types.UNIT, obj);
            } else if (obj instanceof Block) {
//                Log.info("readSnapshot, block: " + obj);
                map.add(Types.BLOCK, obj);
            } else {
                throw new IllegalArgumentException("invalid object type: " + obj);
            }
            iter.next();
        }
        return map;
    }

    private @Nullable Object readNextElement() {
        try {
            int type = this.replayFile.readUnsignedByte();

            return switch (type) {
                case Types.TICK -> Tick.fromInpStream(this.replayFile);
                case Types.UNIT -> Unit.fromInpStream(this.replayFile);
                case Types.BLOCK -> Block.fromInpStream(this.replayFile);
                default -> throw new IllegalStateException("Unknown data type: " + type);
            };
        } catch (EOFException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Error reading replay stream", e);
        }
    }

    public void seekTickByIndex(int index) {
        try {
            var position = (long) index * Integer.BYTES;

            if (position < 0 || position > this.indexFile.length()) {
                throw new IllegalArgumentException("Index out of bounds: " + index);
            }

            this.indexFile.seek(position);
            var tickPosition = this.indexFile.readInt();

            this.replayFile.seek(tickPosition);

            this.globalIterator.resetNextElement();
        } catch (IOException e) {
            throw new RuntimeException("Error seeking to tick index: " + index, e);
        }
    }

    @Override
    public void close() {
        IOException exception = null;

        if (this.replayFile != null) {
            try {
                this.replayFile.close();
            } catch (IOException e) {
                exception = e;
            }
        }
        if (this.indexFile != null) {
            try {
                this.indexFile.close();
            } catch (IOException e) {
                if (exception == null) exception = e;
            }
        }

        if (exception != null) {
            throw new RuntimeException("Failed to close files safely", exception);
        }
    }
}
