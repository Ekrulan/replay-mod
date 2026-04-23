package replaysystem;

import arc.files.Fi;

public class ReplayData {
    public final Fi folder;
    public final String name;
    public final long timestamp;

    public ReplayData(Fi folder) {
        this.folder = folder;
        this.timestamp = Long.parseLong(folder.name());
        this.name = "Replay " + new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new java.util.Date(timestamp));
    }
}