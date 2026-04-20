package replaysystem;

public class ReplayState {
    public static boolean isReplaying = false;     // сейчас идёт просмотр
    public static boolean isLoadingReplay = false; // только во время SaveIO.load

    public static final int SNAPSHOT_INTERVAL = 2;

}