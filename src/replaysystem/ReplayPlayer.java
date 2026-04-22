package replaysystem;

import arc.math.Mathf;
import arc.struct.IntSet;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Groups;

import static replaysystem.Util.safeFloat;

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

        var worldTick = (int) Vars.state.tick;

        if (worldTick % ReplayConfig.SNAPSHOT_INTERVAL != 0) return;

        var e = events.get(snapshotCursor);

        var eventTick = e.getInt(ReplayFrame.TICK, -1);

        applyUnitSnapshot(e);

        previousSnapshot = currentSnapshot;
        currentSnapshot = e;
        previousTick = currentSnapTick;
        currentSnapTick = eventTick;

        snapshotCursor++;

        interpolateUnits(worldTick);
    }

    private void applyUnitSnapshot(Jval snapshot) {
        var unitsArray = snapshot.get(ReplayFrame.UNITS);
        if (unitsArray == null || !unitsArray.isArray()) return;

        for (var u : unitsArray.asArray()) {

            var unitDt = ReplayFrame.Unit.fromJson(u);

            if (unitDt == null) {
                Log.warn("ReplayPlayer: invalid unit part: " + u);
                continue;
            }

            var unit = Groups.unit.find(unit2 -> unit2.id == unitDt.id);

            if (unit == null) {
                var unitType = Vars.content.units().find(ut -> ut.name.equals(unitDt.type));
                if (unitType == null) {
                    continue;
                }

                unit = unitType.create(Team.get(unitDt.team));
                unit.id = unitDt.id;
                unit.add();
            }
            unit.move(unitDt.x, unitDt.y);
            unit.rotation = unitDt.rot;
            unit.health = unitDt.health;
        }
    }

    private void interpolateUnits(int currentWorldTick) {
        if (previousSnapshot == null || currentSnapshot == null) return;

        var delta = currentSnapTick - previousTick;
        var t = (delta == 0) ? 1f : (currentWorldTick - previousTick) / (float) delta;
        t = Mathf.clamp(t, 0f, 1f);

        var prevUnits = previousSnapshot.get(ReplayFrame.UNITS);
        var currUnits = currentSnapshot.get(ReplayFrame.UNITS);
        if (prevUnits == null || currUnits == null) return;

        var prevIds = new IntSet();
        for (var pu : prevUnits.asArray()) {
            int id = pu.asArray().get(ReplayFrame.Unit.ID).asInt();
            if (id != -1) prevIds.add(id);
        }

        for (var cu : currUnits.asArray()) {
            var ut = ReplayFrame.Unit.fromJson(cu);
            assert ut != null;
            if (ut.id == -1) continue;

            prevIds.remove(ut.id);
            var unit = Groups.unit.find(u -> u.id == ut.id);
            if (unit == null || unit.dead()) continue;

            var pu = findUnitById(prevUnits, ut.id);
            if (pu == null) continue;

            var arr = pu.asArray();

            var prevX = safeFloat(arr.get(ReplayFrame.Unit. X));
            assert prevX != null;
            var prevY = safeFloat(arr.get(ReplayFrame.Unit. Y));
            assert prevY != null;
            var prevRot = safeFloat(arr.get(ReplayFrame.Unit.ROT));
            assert prevRot != null;

            unit.set(Mathf.lerp(prevX, ut.x, t), Mathf.lerp(prevY, ut.y, t));
            unit.rotation = lerpAngle(prevRot, ut.rot, t);
        }

        for (var it = prevIds.iterator(); it.hasNext; ) {
            var missingId = it.next();
            var unit = Groups.unit.find(u -> u.id == missingId);
            if (unit != null && !unit.dead()) unit.kill();
        }
    }


    private static float lerpAngle(float from, float to, float t) {
        var delta = ((to - from + 180f) % 360f) - 180f;
        return from + delta * t;
    }

    private Jval findUnitById(Jval unitsArray, int id) {
        if (!unitsArray.isArray()) return null;
        for (var u : unitsArray.asArray()) {
            if (u.asArray().get(ReplayFrame.Unit.ID).asInt() == id) return u;
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