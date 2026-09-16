package replaysystem;

import mindustry.Vars;

public class ReplayConfig {
    public static boolean isReplaying = false;     // сейчас идёт просмотр
    public static boolean isLoadingReplay = false; // только во время SaveIO.load

    public static final int SNAPSHOT_INTERVAL = 5; // раз в сколько тиков делать снапшот.

    public static boolean shouldTakeSnapshot() {
        return (int) Vars.state.tick % SNAPSHOT_INTERVAL == 0;
    }
}