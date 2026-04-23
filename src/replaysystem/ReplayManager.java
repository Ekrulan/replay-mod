package replaysystem;

import arc.struct.Seq;

public class ReplayManager {
    public static final ReplayManager instance = new ReplayManager();

    public Seq<ReplayData> getAllReplays() {
        var list = new Seq<ReplayData>();
        for (var dir : ReplayFile.REPLAYS_DIR.list()) {
            if (dir.isDirectory() && dir.child(ReplayFile.INIT_MAP).exists()) {
                list.add(new ReplayData(dir));
            }
        }
        list.sort(r -> -r.timestamp);
        return list;
    }

    public void delete(ReplayData replay) {
        replay.folder.deleteDirectory();
    }
}