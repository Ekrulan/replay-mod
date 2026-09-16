package replaysystem.replayfmt;

import arc.files.Fi;
import arc.util.Log;
import arc.util.Nullable;
import replaysystem.replayfmt.types.Block;
import replaysystem.replayfmt.types.Tick;
import replaysystem.replayfmt.types.Unit;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class ReplayFmtReader implements Iterable<Object>, AutoCloseable {
    private final RandomAccessFile replayFile;
    private final RandomAccessFile indexFile;

    private final ReplayFmtIterator globalIterator = new ReplayFmtIterator();

    private Object nextElement = null;

    public ReplayFmtReader(Fi inputFile) {
        try {
            this.replayFile = new RandomAccessFile(inputFile.file(), "r");
            this.indexFile = new RandomAccessFile(Util.dotIndex(inputFile).file(), "r");
        } catch (Exception e) {
            throw new RuntimeException("Failed to open replay file", e);
        }
    }

    public class ReplayFmtIterator implements Iterator<Object> {
        @Override
        public boolean hasNext() {
            if (nextElement == null) {
                nextElement = readNextElement();
            }
            return nextElement != null;
        }

        public Object peek() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return nextElement;
        }

        @Override
        public Object next() {
            var element = this.peek();
            nextElement = null;
            return element;
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

    private @Nullable Object readNextElement() throws IllegalArgumentException {
        try {
            int type = replayFile.read();
            if (type == -1) {
                return null;
            }

            return switch (type) {
                case Types.TICK -> Tick.fromInpStream(replayFile);
                case Types.UNIT -> Unit.fromInpStream(replayFile);
                case Types.BLOCK -> Block.fromInpStream(replayFile);
                default -> throw new IllegalStateException("Unknown data type: " + type);
            };
        } catch (IOException e) {
            throw new RuntimeException("Error reading replay stream", e);
        }
    }

    public void seekTickByIndex(int index) {
        try {
            var position = (long) index * Integer.BYTES;

            if (position < 0 || position > indexFile.length()) {
                throw new IllegalArgumentException("Index out of bounds: " + index);
            }

            indexFile.seek(position);
            var tickPosition = indexFile.readInt();

            replayFile.seek(tickPosition);

            nextElement = null;
        } catch (IOException e) {
            throw new RuntimeException("Error seeking to tick index: " + index, e);
        }
    }

    @Override
    public void close() {
        IOException exception = null;

        if (replayFile != null) {
            try {
                replayFile.close();
            } catch (IOException e) {
                exception = e;
            }
        }
        if (indexFile != null) {
            try {
                indexFile.close();
            } catch (IOException e) {
                if (exception == null) exception = e;
            }
        }

        if (exception != null) {
            throw new RuntimeException("Failed to close files safely", exception);
        }
    }
}
