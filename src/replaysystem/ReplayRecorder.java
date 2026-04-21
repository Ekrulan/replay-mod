package replaysystem;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.io.SaveIO;

import java.util.concurrent.atomic.AtomicBoolean;


public class ReplayRecorder {
    public static final ReplayRecorder instance = new ReplayRecorder();

    private Fi currentFolder;
    private final Seq<Jval> events = new Seq<>();
    private final AtomicBoolean recording = new AtomicBoolean(false);


    public void start() {
        if (recording.get() || ReplayConfig.isReplaying) {
            return;
        }

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
        var array = Jval.newArray();
        events.each(array::add);
        eventsFile.writeString(array.toString(), false);
//        eventsFile.writeString(array.toString(Jval.Jformat.formatted), false);

        Log.info("ReplayRecorder: saved (" + events.size + " events)");
        events.clear();
        currentFolder = null;
    }

    public void onUpdate() {
        if (!recording.get() || ReplayConfig.isReplaying) return;
        var currentTick = (int) Vars.state.tick;
        if (currentTick % ReplayConfig.SNAPSHOT_INTERVAL == 0) {
            recordSnapshot(currentTick);
        }
    }

    private void recordSnapshot(int currentTick) {
        var snapshot = Jval.newObject();
        snapshot.put(ReplayJsonData.TICK, currentTick);
        snapshot.put(ReplayJsonData.EVENT_TYPE, "snapshot");

        var unitsArray = Jval.newArray();
        Groups.unit.each(unit -> {
            if (unit == null || !unit.isAdded()) return;
            var u = ReplayJsonData.UnitSnapshot.fromUnit(unit).toJson();
            unitsArray.add(u);
        });

        snapshot.put(ReplayJsonData.UnitSnapshot.UNITS, unitsArray);

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