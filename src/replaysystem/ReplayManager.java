package replaysystem;

import arc.struct.Seq;

public class ReplayManager {
    public static final ReplayManager instance = new ReplayManager();

    public Seq<Replay> getAllReplays() {
        var list = new Seq<Replay>();
        for (var dir : ReplayFile.REPLAYS_DIR.list()) {
            if (dir.isDirectory() && dir.child(ReplayFile.INIT_MAP).exists()) {
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