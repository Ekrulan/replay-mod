package replaysystem.replayfmt;

import arc.files.Fi;
import replaysystem.replayfmt.types.Block;
import replaysystem.replayfmt.types.Tick;
import replaysystem.replayfmt.types.Unit;

import java.io.*;

public class ReplayFmtWriter implements AutoCloseable {
    private final FileOutputStream replayRawStream;
    private final DataOutputStream replayFileStream;
    private final DataOutputStream indexFileStream;

    public ReplayFmtWriter(Fi outputFile) {
        try {
            this.replayRawStream = new FileOutputStream(outputFile.file());
            this.replayFileStream = new DataOutputStream(new BufferedOutputStream(replayRawStream));
            this.indexFileStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Util.dotIndex(outputFile).file())));
        } catch (Exception e) {
            throw new RuntimeException("Failed to open replay file for writing", e);
        }
    }

    public void write(Object obj) {
        try {
            if (obj instanceof Tick tick) {
                this.replayFileStream.flush();


                this.replayFileStream.writeByte(Types.TICK);
                tick.writeToStream(this.replayFileStream);

                long seek = this.replayRawStream.getChannel().position();

//                Log.info("ReplayFmtWriter: tick seek: " + seek);

                this.indexFileStream.writeInt((int) seek);
                this.indexFileStream.flush();

            } else if (obj instanceof Unit unit) {
                this.replayFileStream.writeByte(Types.UNIT);
                unit.writeToStream(this.replayFileStream);
            } else if (obj instanceof Block block) {
                this.replayFileStream.writeByte(Types.BLOCK);
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
        try (replayFileStream; indexFileStream) {
            if (replayFileStream != null) replayFileStream.flush();
            if (indexFileStream != null) indexFileStream.flush();
        } catch (IOException ignored) {
        }
    }
}
