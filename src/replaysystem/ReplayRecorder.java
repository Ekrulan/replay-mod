package replaysystem;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.io.SaveIO;
import replaysystem.data.InfoFile;
import replaysystem.data.ReplayFile;

import java.util.concurrent.atomic.AtomicBoolean;

public class ReplayRecorder {
    public static final ReplayRecorder instance = new ReplayRecorder();

    private final AtomicBoolean recording = new AtomicBoolean(false);

    private final Seq<Jval> events = new Seq<>();
    private final ReplaySnapshotter snapshotter = new ReplaySnapshotter();

    private ReplayFile.Writer work_dir;

    public void start() {
        if (recording.get() || ReplayConfig.isReplaying) return;

        work_dir = new ReplayFile().new Writer();

        work_dir.saveMap();

        events.clear();
        recording.set(true);
        Log.info("ReplayRecorder: start");
    }

    public void stop() {
        if (!recording.get() || events.isEmpty()) return;
        recording.set(false);

        work_dir.writeEvent(events.toString());

        var meta = SaveIO.getMeta(work_dir.getFirstMap());

        var duration = events.get(events.size - 1).get(ReplayFrame.TICK).asInt() - events.get(0).get(ReplayFrame.TICK).asInt();

        work_dir.writeInfo(new InfoFile(meta.map.name(), duration, meta.timestamp, String.format("%dx%d", meta.map.width, meta.map.height)));

        Log.info("ReplayRecorder: saved (" + events.size + " events)");
        events.clear();
        work_dir = null;
    }

    public void onUpdate() {
        if (!recording.get() || ReplayConfig.isReplaying) return;

        var maybeSnapshot = snapshotter.createSnapshot();
        if (maybeSnapshot != null) {
            events.add(maybeSnapshot);
        }
    }

    public void recordBlock(ReplayFrame.Block block) {
        if (!recording.get() || ReplayConfig.isReplaying) return;
        snapshotter.recordBlock(block);
    }
}
