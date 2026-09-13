package replaysystem.replayfmt.types;

import replaysystem.replayfmt.WriteToStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Tick implements WriteToStream {
    public final int tick;

    public Tick(int tick) {
        this.tick = tick;
    }


    public static Tick fromInpStream(RandomAccessFile stream) throws IOException {
        return new Tick(stream.readInt());
    }

    @Override
    public void writeToStream(DataOutputStream stream) throws IOException {
        stream.writeInt(this.tick);
    }
}
