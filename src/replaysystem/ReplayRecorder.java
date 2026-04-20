package replaysystem;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.game.EventType.*;
import mindustry.gen.Groups;
import mindustry.io.SaveIO;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplayRecorder {
    public static final ReplayRecorder instance = new ReplayRecorder();

    private Fi currentFolder;
    private final Seq<Jval> events = new Seq<>();
    private final AtomicInteger tick = new AtomicInteger(0);
    private final AtomicBoolean recording = new AtomicBoolean(false);

    private static final int SNAPSHOT_INTERVAL = 16;


    public void start() {
        if (recording.get() || ReplayState.isReplaying) {
            return;
        }

        currentFolder = ReplayManager.instance.replayDir.child("replay-" + System.currentTimeMillis());
        currentFolder.mkdirs();

        var initialSave = currentFolder.child("initial.msav");
        SaveIO.save(initialSave);

        events.clear();
        tick.set(0);
        recording.set(true);
        Log.info("ReplayRecorder: start");
    }

    public void stop() {
        if (!recording.get()) return;
        recording.set(false);

        var eventsFile = currentFolder.child("events.json");
        var array = Jval.newArray();
        events.each(array::add);
//        eventsFile.writeString(array.toString(), false);
        eventsFile.writeString(array.toString(Jval.Jformat.formatted), false);

        Log.info("ReplayRecorder: saved (" + events.size + " events)");
        events.clear();
        currentFolder = null;
    }

    public void onUpdate() {
        if (!recording.get() || ReplayState.isReplaying) return;
        var currentTick = tick.getAndIncrement();
        if (currentTick % SNAPSHOT_INTERVAL == 0) {
            recordSnapshot(currentTick);
            tick.set(0);
        }
    }

    private void recordSnapshot(int currentTick) {
        var snapshot = Jval.newObject();
        snapshot.put("tick", currentTick);
        snapshot.put("type", "snapshot");

        var unitsArray = Jval.newArray();
        Groups.unit.each(unit -> {
            if (unit == null || !unit.isAdded()) return;
            var u = Jval.newObject();
            u.put("id", unit.id);
            u.put("type", unit.type.name);
            u.put("x", unit.x);
            u.put("y", unit.y);
            u.put("rot", unit.rotation);
            u.put("health", unit.health);
            u.put("team", unit.team.id);
            unitsArray.add(u);
        });

        snapshot.put("units", unitsArray);
        events.add(snapshot);
    }

//    public void recordEvent(String type, Object eventData) {
//        if (!recording.get() || ReplayState.isReplaying) return;
//        var json = Jval.newObject();
//        json.put("tick", tick.get());
//        json.put("type", type);
//        json.put("data", eventData.toString());
//        events.add(json);
//    }
}