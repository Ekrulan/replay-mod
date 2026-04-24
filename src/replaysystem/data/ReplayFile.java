package replaysystem.data;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.io.SaveIO;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplayFile {
    public static final Fi REPLAYS_DIR;

    static {
        Fi dir = Vars.dataDirectory.child("replays");
        dir.mkdirs();
        REPLAYS_DIR = dir;
    }

    private final Fi dir;

    private final Fi eventsDir;
    private final Fi mapsDir;

    private final AtomicInteger counter = new AtomicInteger();

    public ReplayFile() {
        this(System.currentTimeMillis() + "");
    }

    public ReplayFile(String name) {
        this.dir = REPLAYS_DIR.child(name);
        this.dir.mkdirs();
        this.eventsDir = dir.child("events");
        this.eventsDir.mkdirs();
        this.mapsDir = dir.child("maps");
        this.mapsDir.mkdirs();
    }


    public static Seq<ReplayFile.Reader> replays() {
        Seq<ReplayFile.Reader> list = new Seq<>();
        var entries = ReplayFile.REPLAYS_DIR.list();
        Arrays.stream(entries)
              .filter(Fi::isDirectory)
              .sorted(Comparator.comparingLong(f -> {
                  try {
                      return Long.parseLong(f.name());
                  } catch (NumberFormatException e) {
                      Log.err("ReplayFile: invalid replay name: " + f.name());
                      return 0L;
                  }
              }))
              .forEach(f -> {
                  var rf = new ReplayFile(f.name());
                  list.add(rf.new Reader());
              });
        return list;
    }

    public class Writer {

        private String nextName() {
            return String.valueOf(counter.getAndIncrement());
        }

        public void writeInfo(InfoFile info) {
            dir.child("info").writeString(info.toString());
        }

        public void saveMap() {
            SaveIO.save(mapsDir.child(nextName()));
        }

        public @Nullable Fi getFirstMap() {
            var l = mapsDir.list();
            if (l.length < 1) {
                return null;
            } else {
                return l[0];
            }
        }

        public void writeEvent(String ev) {
            eventsDir.child(nextName()).writeString(ev);
        }
    }

    public class Reader {
        private final Fi[] eventFiles = sortedOrEmpty(eventsDir.list());
        private final Fi[] mapFiles = sortedOrEmpty(mapsDir.list());
        private int eventIdx = 0;
        private int mapIdx = 0;

        private Fi[] sortedOrEmpty(Fi[] arr) {
            Arrays.sort(arr, Comparator.comparingInt(fi -> Integer.parseInt(fi.name())));
            return arr;
        }

        @Nullable
        public InfoFile readInfo() {
            return InfoFile.fromString(dir.child("info").readString());
        }

        public boolean loadNextMap() {
            if (mapIdx >= mapFiles.length) return false;
            SaveIO.load(mapFiles[mapIdx++]);
            return true;
        }

        @Nullable
        public String readNextEvent() {
            if (eventIdx >= eventFiles.length) return null;
            return eventFiles[eventIdx++].readString();
        }

        public void delete() {
            dir.deleteDirectory();
        }
    }
}
