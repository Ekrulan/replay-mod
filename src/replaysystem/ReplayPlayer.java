package replaysystem;

import arc.util.Log;
import arc.struct.Seq;
import arc.util.serialization.Jval;
import mindustry.gen.Groups;
import mindustry.Vars;

public class ReplayPlayer {
    public static final ReplayPlayer instance = new ReplayPlayer();

    private Seq<Jval> events = new Seq<>();
    private Replay currentReplay;
    private int currentTick = 0;
    private int snapshotCursor = 0;
    private boolean playing = false;

    public void start(Replay replay) {
        if (playing) stop();

        ReplayState.isReplaying = true;
        currentReplay = replay;
        loadEvents();
        currentTick = 0;
        snapshotCursor = 0;
        playing = true;

        if (Vars.player.unit() != null) {
            Vars.player.unit().kill();
        }
        Vars.player.clearUnit();

        Log.info("ReplayPlayer: start");
    }

    public void stop() {
        if (!playing) return;
        playing = false;
        ReplayState.isReplaying = false;
        events.clear();
        currentReplay = null;
        Log.info("ReplayPlayer: stop");
    }

    private void loadEvents() {
        var file = currentReplay.folder.child("events.json");
        if (!file.exists()) return;

        try {
            var json = file.readString();
            var root = Jval.read(json);
            if (root.isArray()) {
                events = root.asArray();
                Log.info("ReplayPlayer: загружено " + events.size + " событий");
            }
        } catch (Exception e) {
            Log.err("ReplayPlayer: ошибка чтения events.json", e);
        }
    }

    public void onUpdate() {
        if (!playing) return;

        currentTick++;

        applyNextSnapshot();
    }

    private void applyNextSnapshot() {
        while (snapshotCursor < events.size) {
            var e = events.get(snapshotCursor);
            if (!"snapshot".equals(e.getString("type"))) {
                snapshotCursor++;
                continue;
            }

            var eventTick = e.getInt("tick", 0);
            if (eventTick > currentTick) break;

            var unitsArray = e.get("units");
            if (unitsArray != null && unitsArray.isArray()) {
                unitsArray.asArray().each(u -> {
                    var id = u.getInt("id", -1);
                    if (id == -1) return;

                    var unit = Groups.unit.find(unit2 -> unit2.id == id);
                    if (unit != null && unit.isAdded()) {
                        unit.set(u.getFloat("x", 0), u.getFloat("y", 0));
                        unit.rotation = u.getFloat("rot", 0);
                        unit.health = Math.min(u.getFloat("health", 0), unit.maxHealth());
                    }
                });
            }

            snapshotCursor++;
        }
    }
}