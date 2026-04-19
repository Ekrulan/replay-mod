package replaysystem;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.game.EventType.*;
import mindustry.io.SaveIO;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplayRecorder {
    public static final ReplayRecorder instance = new ReplayRecorder();

    private Fi currentFolder;
    private final Seq<Jval> events = new Seq<>();
    private final AtomicInteger tick = new AtomicInteger(0);
    private final AtomicBoolean recording = new AtomicBoolean(false);

    private ReplayRecorder() {}

    public void start() {
        if (recording.get()) return;

        currentFolder = ReplayManager.instance.replayDir.child("replay-" + System.currentTimeMillis());
        currentFolder.mkdirs();

        var initialSave = currentFolder.child("initial.msav");
        SaveIO.save(initialSave);

        events.clear();
        tick.set(0);
        recording.set(true);
        Log.info("ReplayRecorder: replay recording has started");
    }

    public void stop() {
        if (!recording.get()) return;
        recording.set(false);

        Fi eventsFile = currentFolder.child("events.json");
        var array = Jval.newArray();
        events.each(array::add);
        eventsFile.writeString(array.toString(Jval.Jformat.formatted), false);

        Log.info("ReplayRecorder: replay saved to " + currentFolder);
        events.clear();
        currentFolder = null;
    }

    public void onUpdate() {
        if (!recording.get()) return;
        tick.getAndIncrement();

    }

    public void recordEvent(String type, Object eventData) {
        if (!recording.get()) return;

        Jval json = Jval.newObject();
        json.put("tick", tick.get());
        json.put("type", type);
        json.put("data", eventData.toString());
        events.add(json);
    }
}