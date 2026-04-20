package replaysystem;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

public class ReplayPlayer {

    public static final ReplayPlayer instance = new ReplayPlayer();

    private Seq<Jval> events = new Seq<>();
    private Replay currentReplay;
    private int snapshotCursor = 0;
    private boolean playing = false;

    private @Nullable Jval previousSnapshot = null;
    private @Nullable Jval currentSnapshot = null;
    private int previousTick = 0;
    private int currentSnapTick = 0;

    public void start(Replay replay) {
        if (playing) stop();

        this.currentReplay = replay;
        ReplayConfig.isReplaying = true;

        if (Vars.player.unit() != null) {
            Vars.player.unit().kill();
        }
        Vars.player.clearUnit();

        loadEvents();
        resetState();

        playing = true;
        Log.info("ReplayPlayer: events size: " + events.size);
    }

    public void stop() {
        if (!playing) return;

        playing = false;
        ReplayConfig.isReplaying = false;
        events.clear();
        previousSnapshot = null;
        currentSnapshot = null;
        snapshotCursor = 0;

        Log.info("ReplayPlayer: stopped");
    }

    private void loadEvents() {
        var file = ReplayFile.createEvents(currentReplay.folder);
        if (!file.exists()) {
            Log.warn("events.json not found");
            return;
        }

        try {
            var json = file.readString();
            var root = Jval.read(json);
            if (root.isArray()) {
                events = root.asArray();
                Log.info("ReplayPlayer: load " + events.size + " events");
            }
        } catch (Exception e) {
            Log.err("ReplayPlayer: error reading events.json", e);
        }
    }

    public void onUpdate() {
        if (!playing || events.isEmpty()) return;

        if (snapshotCursor >= events.size) {
            Log.info("end replay");
            playing = false;
            return;
        }

        var worldTick = (int)Vars.state.tick;

        if (worldTick % ReplayConfig.SNAPSHOT_INTERVAL != 0) return;

        var e = events.get(snapshotCursor);
        if (!"snapshot".equals(e.getString("type", ""))) {
            snapshotCursor++;
            return;
        }

        var eventTick = e.getInt("tick", -1);

        applyUnitSnapshot(e);

        previousSnapshot = currentSnapshot;
        currentSnapshot = e;
        previousTick = currentSnapTick;
        currentSnapTick = eventTick;

        snapshotCursor++;

        interpolateUnits(worldTick);
    }

    private void applyUnitSnapshot(Jval snapshot) {
        var unitsArray = snapshot.get("units");
        if (unitsArray == null || !unitsArray.isArray()) return;

        Log.info("g unit: " + Groups.unit.size() + " a unit: " + unitsArray.asArray().size);

        for (var u : unitsArray.asArray()) {
            var id = u.getInt("id", -1);
            if (id == -1) continue;

            var typeName = u.getString("type");
            var x = safeFloat(u, "x", -1f);
            var y = safeFloat(u, "y", -1f);
            var rot = safeFloat(u, "rot", -1f);
            var health = safeFloat(u, "health", -1f);
            var teamId = u.getInt("team", -1);

            var unit = Groups.unit.find(unit2 -> unit2.id == id);

            if (unit == null) {
                var unitType = Vars.content.units().find(ut -> ut.name.equals(typeName));
                if (unitType == null) {
                    continue;
                }

                unit = unitType.create(Team.get(teamId));
                unit.id = id;
                unit.add();
            }


            unit.move(x, y);
            unit.rotation = rot;
            unit.health = health;
        }
    }

    private void interpolateUnits(int currentWorldTick) {
        if (previousSnapshot == null || currentSnapshot == null) return;

        var delta = currentSnapTick - previousTick;
        var t = (delta == 0) ? 1f : (currentWorldTick - previousTick) / (float) delta;
        t = Mathf.clamp(t, 0f, 1f);

        var prevUnits = previousSnapshot.get("units");
        var currUnits = currentSnapshot.get("units");
        if (prevUnits == null || currUnits == null) return;

        for (var cu : currUnits.asArray()) {
            var id = cu.getInt("id", -1);
            if (id == -1) continue;

            Unit unit = Groups.unit.find(u -> u.id == id);
            if (unit == null || unit.dead()) continue;

            Jval pu = findUnitById(prevUnits, id);
            if (pu == null) continue;

            var prevX = safeFloat(pu, "x", unit.x);
            var prevY = safeFloat(pu, "y", unit.y);
            var prevRot = safeFloat(pu, "rot", unit.rotation);

            var targetX = safeFloat(cu, "x", unit.x);
            var targetY = safeFloat(cu, "y", unit.y);
            var targetRot = safeFloat(cu, "rot", unit.rotation);

            unit.set(
                    Mathf.lerp(prevX, targetX, t),
                    Mathf.lerp(prevY, targetY, t)
            );

            unit.rotation = lerpAngle(prevRot, targetRot, t);
        }
    }

    private static float safeFloat(Jval obj, String key, float defaultValue) {
        var v = obj.get(key);
        if (v == null) return defaultValue;

        try {
            return v.asFloat();
        } catch (Exception ignored) {
            try {
                return Float.parseFloat(v.asString());
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }

    private static float lerpAngle(float from, float to, float t) {
        var delta = ((to - from + 180f) % 360f) - 180f;
        return from + delta * t;
    }

    private Jval findUnitById(Jval unitsArray, int id) {
        if (!unitsArray.isArray()) return null;
        for (var u : unitsArray.asArray()) {
            if (u.getInt("id", -1) == id) return u;
        }
        return null;
    }

    private void resetState() {
        snapshotCursor = 0;
        previousSnapshot = null;
        currentSnapshot = null;
        previousTick = 0;
        currentSnapTick = 0;
    }
}