package replaysystem.replayfmt.types;

import replaysystem.replayfmt.WriteToStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Block implements WriteToStream {
    public final int blockId;
    public final short x;
    public final short y;
    public final int rot;
    public final float health;
    public final int team;

    public Block(int blockId, short x, short y, int rot, float health, int team) {
        this.blockId = blockId;
        this.x = x;
        this.y = y;
        this.rot = rot;
        this.health = health;
        this.team = team;
    }

    public static Block fromInpStream(RandomAccessFile stream) throws IOException {
        var blockId = stream.readInt();
        var blockX = stream.readShort();
        var blockY = stream.readShort();
        var blockRot = stream.readInt();
        var blockHealth = stream.readFloat();
        var blockTeam = stream.readUnsignedByte();

        return new Block(blockId, blockX, blockY, blockRot, blockHealth, blockTeam);
    }

    @Override
    public void writeToStream(DataOutputStream stream) throws IOException {
        stream.writeInt(this.blockId);
        stream.writeShort(this.x);
        stream.writeShort(this.y);
        stream.writeInt(this.rot);
        stream.writeFloat(this.health);
        stream.writeByte((byte) this.team);
    }
}
