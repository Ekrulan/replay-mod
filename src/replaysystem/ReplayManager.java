package replaysystem;

import arc.struct.Seq;
import arc.files.Fi;
import mindustry.Vars;

public class ReplayManager {
    public static final ReplayManager instance = new ReplayManager();
    final Fi replayDir;

    private ReplayManager() {
        replayDir = Vars.dataDirectory.child("replays");
        if (!replayDir.exists()) replayDir.mkdirs();
    }

    public Seq<Replay> getAllReplays() {
        Seq<Replay> list = new Seq<>();
        for (Fi dir : replayDir.list()) {
            if (dir.isDirectory() && dir.child("initial.msav").exists()) {
                list.add(new Replay(dir));
            }
        }
        list.sort(r -> -r.timestamp);
        return list;
    }

    public void delete(Replay replay) {
        replay.folder.deleteDirectory();
    }
}