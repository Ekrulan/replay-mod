package replaysystem.replayfmt.helpers;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Coords2D implements WriteToStream {

    public float x;
    public float y;


    public Coords2D(float x, float y) {
        this.x = x;
        this.y = y;
    }


    @Override
    public String toString() {
        return String.format("[Coords2D]: x=%f, y=%f", this.x, this.y);
    }


    public static Coords2D fromInpStream(RandomAccessFile stream) throws IOException {
        var targetX = stream.readFloat();
        var targetY = stream.readFloat();
        return new Coords2D(targetX, targetY);
    }

    @Override
    public void writeToStream(DataOutputStream stream) throws IOException {
        stream.writeFloat(this.x);
        stream.writeFloat(this.y);
    }
}
