package replaysystem;

import arc.files.Fi;
import mindustry.Vars;

public class ReplayFile {

    public static final String INIT_MAP = "initial.msav";
    public static final String EVENTS = "events.json";


    public static final Fi REPLAYS_DIR;

    static {
        var dir = Vars.dataDirectory.child("replays");
        dir.mkdirs();
        REPLAYS_DIR = dir;
    }

    public static Fi createWorkDir() {
        var dir = REPLAYS_DIR.child(System.currentTimeMillis() + "");
        dir.mkdirs();
        return dir;
    }

    public static Fi createInitial(Fi dir) {
        return dir.child(INIT_MAP);
    }

    public static Fi createEvents(Fi dir) {
        return dir.child(EVENTS);
    }
}
