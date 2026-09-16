package replaysystem.replayfmt.types;

import arc.util.Nullable;
import replaysystem.helpers.Coords2D;
import replaysystem.replayfmt.WriteToStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Unit implements WriteToStream {
    public final int id;
    public final short type;
    public final float x;
    public final float y;
    public final float rot;
    public final float health;
    public final int team;
    public final @Nullable Coords2D target;

    public Unit(int id, short type, float x, float y, float rot, float health, int team, Coords2D target) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.rot = rot;
        this.health = health;
        this.team = team;
        this.target = target;
    }

    public static Unit fromUnit(mindustry.gen.Unit unit) {
        Coords2D target = null;
        if (unit.isShooting) {
            target = new Coords2D(unit.aimX, unit.aimY);
        }
        return new Unit(unit.id, unit.type.id, unit.x, unit.y, unit.rotation, unit.health, unit.team.id, target);
    }

    public static Unit fromInpStream(RandomAccessFile stream) throws IOException {
        var unitId = stream.readInt();
        var unitType = stream.readShort();
        var unitX = stream.readFloat();
        var unitY = stream.readFloat();
        var unitRot = stream.readFloat();
        var unitHealth = stream.readFloat();
        var unitTeam = stream.readUnsignedByte();

        var isHasTarget = stream.readUnsignedByte();

        Coords2D target = null;

        if (isHasTarget != 0) {
            var targetX = stream.readFloat();
            var targetY = stream.readFloat();
            target = new Coords2D(targetX, targetY);
        }

        return new Unit(unitId, unitType, unitX, unitY, unitRot, unitHealth, unitTeam, target);
    }

    @Override
    public void writeToStream(DataOutputStream stream) throws IOException {
        stream.writeInt(this.id);
        stream.writeShort(this.type);
        stream.writeFloat(this.x);
        stream.writeFloat(this.y);
        stream.writeFloat(this.rot);
        stream.writeFloat(this.health);
        stream.writeByte((byte) this.team);
        if (this.target != null) {
            stream.writeByte(1);
            stream.writeFloat(this.target.x);
            stream.writeFloat(this.target.y);
        } else {
            stream.writeByte(0);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Unit: [id=%d, type=%d, x=%f, y=%f, rotation=%f, health=%f, team=%d, target=%s]", this.id, this.type, this.x, this.y,
                this.rot, this.health,
                this.team, this.target != null ? this.target.toString() : "null"
        );
    }
}
