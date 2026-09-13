package replaysystem.replayplayer;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.game.Team;
import replaysystem.ReplayConfig;
import replaysystem.data.InfoFile;
import replaysystem.data.ReplayFile;
import replaysystem.replayplayer.replayers.ReplayBlock;
import replaysystem.replayplayer.replayers.ReplayUnit;

import java.util.function.Consumer;


public class ReplayPlayer {

    public interface SnapshotApplier {

        void applySnapshot(Jval snapshot);

        default void interpolate(Jval prevSnapshot, Jval curSnapshot) {
        }
    }

    public final Seq<Consumer<ReplayPlayer>> listeners = new Seq<>();

    private final Seq<SnapshotApplier> handlers;

    public static final ReplayPlayer instance = new ReplayPlayer(Seq.with(new ReplayUnit(), new ReplayBlock()));


    public ReplayPlayer(Seq<SnapshotApplier> hds) {
        this.handlers = hds;
    }

    private Seq<Jval> events = new Seq<>();

    private ReplayFile.Reader currentReplay;

    public InfoFile replayInfoFile;

    public int snapshotCursor = 0;

    private boolean playing = false;

    private @Nullable Jval previousSnapshot = null;
    private @Nullable Jval currentSnapshot = null;


    public void start(ReplayFile.Reader replay) {
        if (playing) stop();

        this.currentReplay = replay;
        this.replayInfoFile = replay.readInfo();

        ReplayConfig.isReplaying = true;

        Vars.player.team(Team.derelict);

        resetState();

        playing = true;
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

    private boolean loadEvents() {

        try {
            var json = currentReplay.readNextEvent();
            if (json != null) {
                var root = Jval.read(json);
                assert root.isArray();
                events = root.asArray();
                Log.info("ReplayPlayer: load " + events.size + " events");
                return true;
            }

            Log.info("end replay");
            playing = false;

        } catch (Exception e) {
            Log.err("ReplayPlayer: error reading events", e);
        }
        return false;
    }

    public void onUpdate() {
        if (!playing) return;

        if (snapshotCursor >= events.size) {
            if (!loadEvents()) {
                return;
            }
            resetState();
        }

        listeners.each(fn -> fn.accept(this));

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