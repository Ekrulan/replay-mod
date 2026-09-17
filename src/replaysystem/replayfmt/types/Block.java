package replaysystem.replayfmt.types;

import arc.util.Log;
import arc.util.Nullable;
import mindustry.game.EventType;
import replaysystem.replayfmt.helpers.WriteToStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Block implements WriteToStream {
    public final short x;
    public final short y;
    public final @Nullable Build build;

    public Block(short x, short y, @Nullable Build build) {
        this.x = x;
        this.y = y;
        this.build = build;
    }

    public static Block fromInpStream(RandomAccessFile stream) throws IOException {
        var blockX = stream.readShort();
        var blockY = stream.readShort();

        var isHasBuild = stream.readUnsignedByte();

        Build b = null;

        if (isHasBuild != 0) {
            b = Build.fromInpStream(stream);
        }
        return new Block(blockX, blockY, b);
    }

    @Override
    public void writeToStream(DataOutputStream stream) throws IOException {
        stream.writeShort(this.x);
        stream.writeShort(this.y);
        if (this.build != null) {
            stream.writeByte(1);
            this.build.writeToStream(stream);
        } else {
            stream.writeByte(0);
        }
    }

    public static Block destroyBlock(short x, short y) {
        return new Block(x, y, null);
    }

    public static Block fromEvent(EventType.BlockBuildEndEvent e) {
        var build = e.tile.build;

        Build content = null;

        if (build != null && build.tile.blockID() != 5) { // block id 5 - build1, placed when a building is destroyed
            content = new Build(build.block.id, build.rotation, build.health, build.team.id);
        }
        return new Block(e.tile.x, e.tile.y, content);

    }

    @Override
    public String toString() {
        return String.format("Block: [x=%d, y=%d, build=%s]", this.x, this.y, this.build != null ? this.build.toString() : "null");
    }


    public static class Build implements WriteToStream {
        public final int blockId;
        public final int rot;
        public final float health;
        public final int team;

        public Build(int blockId, int rot, float health, int team) {
            this.blockId = blockId;
            this.rot = rot;
            this.health = health;
            this.team = team;
        }

        public static Build fromInpStream(RandomAccessFile stream) throws IOException {
            var blockId = stream.readInt();
            var blockRot = stream.readUnsignedByte();
            var blockHealth = stream.readFloat();
            var blockTeam = stream.readUnsignedByte();
            return new Build(blockId, blockRot, blockHealth, blockTeam);
        }

        @Override
        public void writeToStream(DataOutputStream stream) throws IOException {
            stream.writeInt(this.blockId);
            stream.writeByte((byte) this.rot);
            stream.writeFloat(this.health);
            stream.writeByte((byte) this.team);
        }

        @Override
        public String toString() {
            return String.format("Content: [id=%d, rotation=%d,  health=%f, team=%d]", this.blockId, this.rot, this.health, this.team);
        }
    }
}
