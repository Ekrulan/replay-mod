package replaysystem;

import mindustry.Vars;

public class ReplayConfig {
    public static boolean isReplaying = false;     // replay is currently being watched
    public static boolean isLoadingReplay = false; // only during SaveIO.load

    public static final int SNAPSHOT_INTERVAL = 5; // How many ticks between snapshots

    public static boolean shouldTakeSnapshot() {
        return (int) Vars.state.tick % SNAPSHOT_INTERVAL == 0;
    }
}