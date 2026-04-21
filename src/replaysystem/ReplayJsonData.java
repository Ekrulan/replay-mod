package replaysystem;

import arc.util.Nullable;
import arc.util.serialization.Jval;
import mindustry.gen.Unit;

import static replaysystem.Util.safeFloat;

public class ReplayJsonData {

    // Make sure the markings don't match.
    public static final String TICK = "t";
    public static final String EVENT_TYPE = "t2";


    public static class UnitSnapshot {
        public final int id;
        public final String type;
        public final float x;
        public final float y;
        public final float rot;
        public final float health;
        public final int team;

        // Make sure the markings don't match.
        public static final String ID = "i";
        public static final String TYPE = "t";
        public static final String X = "x";
        public static final String Y = "y";
        public static final String ROT = "r";
        public static final String HEALTH = "h";
        public static final String TEAM = "t1";

        public static final String UNITS = "u";

        public UnitSnapshot(int id, String type, float x, float y, float rot, float health, int team) {
            this.id = id;
            this.type = type;
            this.x = x;
            this.y = y;
            this.rot = rot;
            this.health = health;
            this.team = team;
        }

        public static UnitSnapshot fromUnit(Unit unit) {
            return new UnitSnapshot(unit.id, unit.type.name, unit.x, unit.y, unit.rotation, unit.health, unit.team.id);
        }

        public static @Nullable UnitSnapshot fromJson(Jval vl) {
            int id = vl.getInt(ID, -1);
            if (id == -1) return null;

            String typeName = vl.getString(TYPE);
            if (typeName == null) return null;

            Float x = safeFloat(vl, X);
            if (x == null) return null;

            Float y = safeFloat(vl, Y);
            if (y == null) return null;

            Float rot = safeFloat(vl, ROT);
            if (rot == null) return null;

            Float health = safeFloat(vl, HEALTH);
            if (health == null) return null;

            int teamId = vl.getInt(TEAM, -1);
            if (teamId == -1) return null;

            return new UnitSnapshot(id, typeName, x, y, rot, health, teamId);
        }

        public Jval toJson() {
            Jval u = Jval.newObject();
            u.put(ID, this.id);
            u.put(TYPE, this.type);
            u.put(X, this.x);
            u.put(Y, this.y);
            u.put(ROT, this.rot);
            u.put(HEALTH, this.health);
            u.put(TEAM, this.team);
            return u;
        }
    }

}
