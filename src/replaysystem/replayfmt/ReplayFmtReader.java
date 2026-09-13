package replaysystem.replayfmt;

import arc.files.Fi;
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
    private Object nextElement = null;
    private boolean endOfFile = false;

    public ReplayFmtReader(Fi inputFile) {
        try {
            this.replayFile = new RandomAccessFile(inputFile.file(), "r");
            this.indexFile = new RandomAccessFile(inputFile.sibling(".index").file(), "r");
        } catch (Exception e) {
            throw new RuntimeException("Failed to open replay file", e);
        }
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                if (nextElement == null && !endOfFile) {
                    nextElement = readNextElement();
                }
                return nextElement != null;
            }

            @Override
            public Object next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                var element = nextElement;
                nextElement = null;
                return element;
            }
        };
    }

    private @Nullable Object readNextElement() throws IllegalArgumentException {
        try {
            int type = replayFile.read();
            if (type == -1) {
                close();
                endOfFile = true;
                return null;
            }

            return switch (type) {
                case 1 -> // TICK
                        Tick.fromInpStream(replayFile);
                case 2 -> // UNITS
                        Unit.fromInpStream(replayFile);
                case 3 -> // BLOCKS
                        Block.fromInpStream(replayFile);
                default -> {
                    close();
                    throw new IllegalStateException("Unknown data type: " + type);
                }
            };
        } catch (IOException e) {
            close();
            throw new RuntimeException("Error reading replay stream", e);
        }
    }

    /// The function takes the tick index and moves the cursor according to that position. This is needed so that the replay can be scrubbed.
    public void seekTickByIndex(int index) {
        try {
            var position = index * Integer.BYTES;

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
        try {
            if (replayFile != null) replayFile.close();
        } catch (IOException ignored) {
        }
        try {
            if (indexFile != null) indexFile.close();
        } catch (IOException ignored) {
        }
    }
}
