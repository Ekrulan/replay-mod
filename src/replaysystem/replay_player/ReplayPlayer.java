package replaysystem.replay_player;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.serialization.Jval;
import mindustry.Vars;
import replaysystem.ReplayConfig;
import replaysystem.ReplayData;
import replaysystem.ReplayFile;


public class ReplayPlayer {

    public interface SnapshotApplier {

        void applySnapshot(Jval snapshot);

        default void interpolate(Jval prevSnapshot, Jval curSnapshot) {
        }
    }

    private final Seq<SnapshotApplier> handlers;

    public static final ReplayPlayer instance = new ReplayPlayer(Seq.with(new ReplayUnit(), new ReplayBlock()));


    private ReplayPlayer(Seq<SnapshotApplier> hds) {
        this.handlers = hds;
    }

    private Seq<Jval> events = new Seq<>();

    private ReplayData currentReplay;
    private int snapshotCursor = 0;
    private boolean playing = false;

    private @Nullable Jval previousSnapshot = null;
    private @Nullable Jval currentSnapshot = null;


    public void start(ReplayData replay) {
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

        handlers.each((a) -> a.applySnapshot(e));

        previousSnapshot = currentSnapshot;
        currentSnapshot = e;

        snapshotCursor++;

        if (previousSnapshot != null) {
            handlers.each((a) -> a.interpolate(previousSnapshot, currentSnapshot));
        }
    }

    private void resetState() {
        snapshotCursor = 0;
        previousSnapshot = null;
        currentSnapshot = null;
    }
}