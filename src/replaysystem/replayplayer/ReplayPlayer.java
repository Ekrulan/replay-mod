package replaysystem.replayplayer;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.game.Team;
import replaysystem.ReplayConfig;
import replaysystem.data.InfoFile;
import replaysystem.data.ReplayFile;
import replaysystem.replayfmt.ReplayFmtMap;
import replaysystem.replayplayer.replayers.ReplayBlock;
import replaysystem.replayplayer.replayers.ReplayUnit;

import java.util.function.Consumer;


public class ReplayPlayer {

    public interface SnapshotApplier {

        void applySnapshot(ReplayFmtMap snapshot);

        default void interpolate(ReplayFmtMap prevSnapshot, ReplayFmtMap curSnapshot) {
        }
    }

    public final Seq<Consumer<ReplayPlayer>> listeners = new Seq<>();

    private final Seq<SnapshotApplier> handlers;

//    public static final ReplayPlayer instance = new ReplayPlayer(Seq.with(new ReplayUnit(), new ReplayBlock()));

    public static ReplayPlayer withDefaultHandlers() {
        return new ReplayPlayer(Seq.with(new ReplayUnit(), new ReplayBlock()));
    }

    public ReplayPlayer(Seq<SnapshotApplier> hds) {
        this.handlers = hds;
    }

    private ReplayFmtMap snapshot;

    private ReplayFile.Reader currentReplay;

    public InfoFile replayInfoFile;

    public int snapshotCursor = 0;

    private boolean playing = false;

    private @Nullable ReplayFmtMap previousSnapshot = null;


    public void start(ReplayFile.Reader replay) {
        if (playing) stop();

        this.currentReplay = replay;
        this.replayInfoFile = replay.readInfo();

        ReplayConfig.isReplaying = true;

        Vars.player.team(Team.derelict);

//        resetState();

        playing = true;
    }

    public void stop() {
        if (!playing) return;

        playing = false;
        ReplayConfig.isReplaying = false;
        snapshot.clear();
        previousSnapshot = null;
        snapshotCursor = 0;

        Log.info("ReplayPlayer: stopped");
    }

    private boolean loadSnapshot() {

        try {
            var snapshot = currentReplay.readNextSnapshot();
            if (snapshot != null) {
                this.previousSnapshot = this.snapshot;
                this.snapshot = snapshot;
//                Log.info("ReplayPlayer: load " + this.snapshot);
                return true;
            }

            Log.info("end replay");
            playing = false;

        } catch (Exception e) {
            Log.err("ReplayPlayer: error reading events", e);
        }
        currentReplay.close(); // TODO
        return false;
    }

    public void onUpdate() {
        if (!playing) return;

        if (!loadSnapshot()) {
            return;
        }

//        listeners.each(fn -> fn.accept(this)); // TODO

//        var worldTick = (int) Vars.state.tick;
//
//        if (worldTick % ReplayConfig.SNAPSHOT_INTERVAL != 0) return;


        handlers.each((a) -> a.applySnapshot(this.snapshot));

//        snapshotCursor++;

        if (previousSnapshot != null) {
            handlers.each((a) -> a.interpolate(previousSnapshot, this.snapshot));
        }
    }

//    private void resetState() {
//        snapshotCursor = 0;
//        previousSnapshot = null;
//    }
}