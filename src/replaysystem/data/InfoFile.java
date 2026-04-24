package replaysystem.data;


public class InfoFile {
    public final String mapName;
    public final int duration;
    public final long timestamp;
    public final String mapSize;


    public InfoFile(String mapName, int duration, long timestamp, String mapSize) {
        this.mapName = mapName;
        this.duration = duration;
        this.timestamp = timestamp;
        this.mapSize = mapSize;
    }

    @Override
    public String toString() {
        return String.format("%s;%d;%d;%s", mapName, duration, timestamp, mapSize);
    }

    public static InfoFile fromString(String s) throws IllegalArgumentException {
        var parts = s.split(";", 4);

        if (parts.length < 4) {
            throw new IllegalArgumentException(String.format("invalid info file content: '%s'", s));
        }
        return new InfoFile(parts[0], Integer.parseInt(parts[1]), Long.parseLong(parts[2]), parts[3]);
    }
}