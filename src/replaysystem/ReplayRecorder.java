package replaysystem;

import arc.util.Log;
import mindustry.io.SaveIO;
import replaysystem.data.InfoFile;
import replaysystem.data.ReplayFile;
import replaysystem.replayfmt.types.Block;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReplayRecorder {

    private final AtomicBoolean recording = new AtomicBoolean(false);

    //    private final Seq<Jval> events = new Seq<>();
    private static final ReplaySnapshotter snapshotter = new ReplaySnapshotter();


    private ReplayFile.Writer workDir;

    public void start() {
        if (recording.get() || ReplayConfig.isReplaying) return;

        workDir = new ReplayFile().new Writer();

        workDir.saveMap();

//        events.clear();
        recording.set(true);
        Log.info("ReplayRecorder: start");
    }

    public void stop() {
        if (!recording.get()) return;
        recording.set(false);

//        workDir.writeEvent(events.toString());

        var meta = SaveIO.getMeta(workDir.getFirstMap());

        String mapName = null;
        int width = -1;
        int height = -1;

        if (meta.map != null) {
            mapName = meta.map.name();
            width = meta.map.width;
            height = meta.map.height;
        } else {

            for (var entry : meta.tags.iterator()) {
                switch (entry.key) {
                    case "mapname":
                        mapName = entry.value;
                        break;
                    case "width":
                        width = Integer.parseInt(entry.value);
                        break;
                    case "height":
                        height = Integer.parseInt(entry.value);
                        break;
                }
            }

            if (mapName == null || width < 0 || height < 0) {
                throw new IllegalStateException("missing map metadata");
            }
        }

        var duration = snapshotter.duration();

        workDir.writeInfo(new InfoFile(mapName, duration, Instant.now().getEpochSecond(), String.format("%dx%d", width, height)));

//        Log.info("ReplayRecorder: saved (" + events.size + " events)");
//        events.clear();
        workDir.zip();
        workDir = null;
    }

    public boolean isRecording() {
        return recording.get() && !ReplayConfig.isReplaying;
    }

    public void onUpdate() {

        if (!this.isRecording()) {
            return;
        }

        var maybeSnapshot = snapshotter.createSnapshot();
        if (maybeSnapshot != null) {
//            Log.info("snapshot: " + maybeSnapshot);
            // We iterate in reverse order, because tick must come first, but in the snapshot it’s added last.
            for (var i = maybeSnapshot.size - 1; i >= 0; i--) {
                workDir.write(maybeSnapshot.get(i));
            }
        }
    }

    public void recordBlock(Block block) {
//        Log.info("recordBlock: " + block);
        snapshotter.recordBlock(block);
    }
}
