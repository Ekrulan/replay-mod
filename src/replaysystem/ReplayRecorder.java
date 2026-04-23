package replaysystem;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.io.SaveIO;

import java.util.concurrent.atomic.AtomicBoolean;

public class ReplayRecorder {
    public static final ReplayRecorder instance = new ReplayRecorder();

    private final AtomicBoolean recording = new AtomicBoolean(false);

    private final Seq<Jval> events = new Seq<>();
    private final ReplaySnapshotter snapshotter = new ReplaySnapshotter();

    private Fi currentFolder;

    public void start() {
        if (recording.get() || ReplayConfig.isReplaying) return;

        currentFolder = ReplayFile.createWorkDir();
        currentFolder.mkdirs();

        var initialSave = ReplayFile.createInitial(currentFolder);
        SaveIO.save(initialSave);

        events.clear();
        recording.set(true);
        Log.info("ReplayRecorder: start");
    }

    public void stop() {
        if (!recording.get() || events.isEmpty()) return;
        recording.set(false);

        var eventsFile = ReplayFile.createEvents(currentFolder);
        eventsFile.writeString(events.toString(), false);
//        eventsFile.writeString(array.toString(Jval.Jformat.formatted), false);

        Log.info("ReplayRecorder: saved (" + events.size + " events)");
        events.clear();
        currentFolder = null;
    }

    public void onUpdate() {
        if (!recording.get() || ReplayConfig.isReplaying) return;

        var maybeSnapshot = snapshotter.createSnapshot();
        if (maybeSnapshot != null) {
            events.add(maybeSnapshot);
            Log.info("event saved");
        }
    }

    public void recordBlock(ReplayFrame.Block block) {
        if (!recording.get() || ReplayConfig.isReplaying) return;
        snapshotter.recordBlock(block);
    }
}
