package replaysystem.replayfmt;

import arc.files.Fi;
import replaysystem.replayfmt.types.Block;
import replaysystem.replayfmt.types.Tick;
import replaysystem.replayfmt.types.Unit;

import java.io.*;

public class ReplayFmtWriter implements AutoCloseable {
    private final DataOutputStream replayFileStream;
    private final DataOutputStream indexFileStream;

    public ReplayFmtWriter(Fi outputFile) {
        try {
            this.replayFileStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile.file())));
            this.indexFileStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile.sibling(".index").file())));
        } catch (Exception e) {
            throw new RuntimeException("Failed to open replay file for writing", e);
        }
    }

    public void write(Object obj) {
        try {
            if (obj instanceof Tick tick) {
                this.replayFileStream.writeByte(1); // TICK

                // TODO если файл будет слишокм большим, может произойти переполнение int.
                var seek = this.replayFileStream.size();
                this.indexFileStream.writeInt(seek);

                tick.writeToStream(this.replayFileStream);
            } else if (obj instanceof Unit unit) {
                this.replayFileStream.writeByte(2); // UNITS
                unit.writeToStream(this.replayFileStream);
            } else if (obj instanceof Block block) {
                this.replayFileStream.writeByte(3); // BLOCKS
                block.writeToStream(this.replayFileStream);
            } else {
                throw new IllegalArgumentException("Unknown object type for replay: " + obj.getClass().getName());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing to replay stream", e);
        }
    }

    @Override
    public void close() {
        try {
            if (replayFileStream != null) {
                replayFileStream.flush();
                replayFileStream.close();
            }
        } catch (IOException ignored) {
        }
    }
}
