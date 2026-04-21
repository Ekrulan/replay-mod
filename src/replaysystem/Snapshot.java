package replaysystem;

import arc.util.Nullable;
import arc.util.serialization.Jval;

import static replaysystem.Util.safeFloat;

public class Snapshot {

    // Make sure the markings don't match.
    public static final String TICK = "t";
    public static final String UNITS = "u";
    public static final String BLOCKS = "b";


    public static class Unit {
        public final int id;
        public final String type;
        public final float x;
        public final float y;
        public final float rot;
        public final float health;
        public final int team;

        public static final int ID = 0;
        public static final int TYPE = 1;
        public static final int X = 2;
        public static final int Y = 3;
        public static final int ROT = 4;
        public static final int HEALTH = 5;
        public static final int TEAM = 6;

        public Unit(int id, String type, float x, float y, float rot, float health, int team) {
            this.id = id;
            this.type = type;
            this.x = x;
            this.y = y;
            this.rot = rot;
            this.health = health;
            this.team = team;
        }

        public static Unit fromUnit(mindustry.gen.Unit unit) {
            return new Unit(unit.id, unit.type.name, unit.x, unit.y, unit.rotation, unit.health, unit.team.id);
        }

        public static @Nullable Snapshot.Unit fromJson(Jval.JsonArray vl) {

            if (vl.size != 7) {
                return null;
            }

            int id = vl.get(ID).asInt();
            if (id == -1) return null;

            String typeName = vl.get(TYPE).asString();
            if (typeName == null) return null;

            Float x = safeFloat(vl.get(X));
            if (x == null) return null;

            Float y = safeFloat(vl.get(Y));
            if (y == null) return null;

            Float rot = safeFloat(vl.get(ROT));
            if (rot == null) return null;

            Float health = safeFloat(vl.get(HEALTH));
            if (health == null) return null;

            int teamId = vl.get(TEAM).asInt();
            if (teamId == -1) return null;

            return new Snapshot.Unit(id, typeName, x, y, rot, health, teamId);
        }

        public static @Nullable Snapshot.Unit fromJson(Jval vl) {
            return vl.isArray() ? fromJson(vl.asArray()) : null;
        }

        public Jval toJson() {
            var u = Jval.newArray();
            u.add(this.id);
            u.add(this.type);
            u.add(this.x);
            u.add(this.y);
            u.add(this.rot);
            u.add(this.health);
            u.add(this.team);
            return u;
        }
    }


    public static class Block {

        public final int build_id;
        public final short x;
        public final short y;
        public final int rot;
        public final float health;
        public final int team;


        public static final int BUILD_ID = 0;
        public static final int X = 1;
        public static final int Y = 2;
        public static final int ROT = 3;
        public static final int HEALTH = 4;
        public static final int TEAM = 5;

        public Block(int buildId, short x, short y, int rot, float health, int team) {
            this.x = x;
            this.y = y;
            this.build_id = buildId;
            this.rot = rot;
            this.health = health;
            this.team = team;
        }

        public static Snapshot.Block destroy(short x, short y) {
            return new Snapshot.Block(5, x, y, 0, 0, -1);
        }

        public static @Nullable Snapshot.Block fromJson(Jval.JsonArray vl) {
            if (vl.size != 6) {
                return null;
            }
            int build_id = vl.get(BUILD_ID).asInt();
            if (build_id == -1) return null;

            short x = (short) vl.get(X).asInt();

            short y = (short) vl.get(Y).asInt();

            int rot = vl.get(ROT).asInt();

            Float health = safeFloat(vl.get(HEALTH));
            if (health == null) return null;

            int teamId = vl.get(TEAM).asInt();
            if (teamId == -1) return null;

            return new Snapshot.Block(build_id, x, y, rot, health, teamId);
        }

        public static @Nullable Snapshot.Block fromJson(Jval vl) {
            return vl.isArray() ? fromJson(vl.asArray()) : null;
        }

        public Jval toJson() {
            var u = Jval.newArray();
            u.add(this.build_id);
            u.add(this.x);
            u.add(this.y);
            u.add(this.rot);
            u.add(this.health);
            u.add(this.team);
            return u;
        }
    }
}
