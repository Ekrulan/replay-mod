package replaysystem;

import arc.util.Nullable;
import arc.util.serialization.Jval;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import replaysystem.helpers.Coords2D;


import static replaysystem.helpers.Util.safeFloat;

public class ReplayFrame {

    // Make sure the markings don't match.
    public static final String TICK = "t";
    public static final String UNITS = "u";
    public static final String BLOCKS = "b";


    public static class Unit {
        public final int id;
        public final short type;
        public final float x;
        public final float y;
        public final float rot;
        public final float health;
        public final int team;
        public final @Nullable Coords2D target;

        public static final int ID = 0;
        public static final int TYPE = 1;
        public static final int X = 2;
        public static final int Y = 3;
        public static final int ROT = 4;
        public static final int HEALTH = 5;
        public static final int TEAM = 6;
        public static final int TARGET = 7;

        public Unit(int id, short typeId, float x, float y, float rot, float health, int team, @Nullable Coords2D target) {
            this.id = id;
            this.type = typeId;
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

        public static @Nullable ReplayFrame.Unit fromJson(Jval.JsonArray vl) {

            if (vl.size != 8) {
                return null;
            }

            int id = vl.get(ID).asInt();
            if (id == -1) return null;

            short typeId = (short) vl.get(TYPE).asInt();
            if (typeId == -1) return null;

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

            var targetRaw = vl.get(TARGET);

            var target = Coords2D.fromJval(targetRaw);

            return new ReplayFrame.Unit(id, typeId, x, y, rot, health, teamId, target);
        }

        public static @Nullable ReplayFrame.Unit fromJson(Jval vl) {
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
            if (this.target != null) {
                u.add(this.target.toJval());
            } else {
                u.add(Jval.NULL);
            }
            return u;
        }
    }


    public static class Block {

        public final int blockId;
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

        public Block(int blockId, short x, short y, int rot, float health, int team) {
            this.blockId = blockId;
            this.x = x;
            this.y = y;
            this.rot = rot;
            this.health = health;
            this.team = team;
        }

        public static ReplayFrame.Block destroyBlock(short x, short y) {
            return new ReplayFrame.Block(Blocks.air.id, x, y, 0, 0, -1);
        }

        public static ReplayFrame.Block fromEvent(EventType.BlockBuildEndEvent e) {
            var build = e.tile.build;
            if (build != null && build.block.id != 5) {
                return new ReplayFrame.Block(build.block.id, e.tile.x, e.tile.y, build.rotation, build.health, build.team.id);
            } else {
                return destroyBlock(e.tile.x, e.tile.y);
            }
        }

        public static @Nullable ReplayFrame.Block fromJson(Jval.JsonArray vl) {
            if (vl.size != 6) return null;
            int build_id = vl.get(BUILD_ID).asInt();

            short x = (short) vl.get(X).asInt();

            short y = (short) vl.get(Y).asInt();

            int rot = vl.get(ROT).asInt();

            Float health = safeFloat(vl.get(HEALTH));
            if (health == null) return null;


            int teamId = vl.get(TEAM).asInt();

            return new ReplayFrame.Block(build_id, x, y, rot, health, teamId);
        }

        public static @Nullable ReplayFrame.Block fromJson(Jval vl) {
            return vl.isArray() ? fromJson(vl.asArray()) : null;
        }

        public Jval toJson() {
            var u = Jval.newArray();
            u.add(this.blockId);
            u.add(this.x);
            u.add(this.y);
            u.add(this.rot);
            u.add(this.health);
            u.add(this.team);
            return u;
        }
    }
}
