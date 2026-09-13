package replaysystem.data;

import arc.files.Fi;
import arc.files.ZipFi;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.io.SaveIO;
import replaysystem.helpers.ZipHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplayFile {
    private static final Fi REPLAYS_DIR;
    private static final Fi UNPACKED;

    static {
        UNPACKED = Vars.tmpDirectory.child("replays_unpacked");
        UNPACKED.file().deleteOnExit();

        REPLAYS_DIR = Vars.dataDirectory.child("replay_mod").child("replays");

        UNPACKED.mkdirs();
        REPLAYS_DIR.mkdirs();
    }

    private static final String EVENTS_NAME_DIR = "events";
    private static final String MAPS_NAME_DIR = "maps";
    private static final String INFO_NAME_FILE = "info";

    private final Fi zipFile;
    private final Fi workDir;
    private final Fi eventsDir;
    private final Fi mapsDir;

    private final AtomicInteger counter = new AtomicInteger();

    public ReplayFile() {
        this(String.valueOf(System.currentTimeMillis()));
        this.eventsDir.mkdirs();
        this.mapsDir.mkdirs();
    }

    public ReplayFile(String name) {
        this.zipFile = REPLAYS_DIR.child(name);
        this.workDir = UNPACKED.child(name);
        this.eventsDir = this.workDir.child(EVENTS_NAME_DIR);
        this.mapsDir = this.workDir.child(MAPS_NAME_DIR);
    }

    @Nullable
    public InfoFile getInfoFile() {
        if (!this.zipFile.exists()) {
            var infoFile = this.workDir.child(INFO_NAME_FILE);
            return infoFile.exists() ? InfoFile.fromString(infoFile.readString()) : null;
        }

        var zip = new ZipFi(this.zipFile);
        var infoEntry = zip.child(INFO_NAME_FILE);
        if (infoEntry.exists()) {
            return InfoFile.fromString(infoEntry.readString());
        }
        return null;
    }

    private void zip() {
        Log.info("ReplayFile: replay packaging");
        try {
            ZipHelper.zipFolder(this.workDir, this.zipFile);
        } catch (IOException e) {
            Log.err(e);
        }
    }

    private static void unzip(Fi zipFile, Fi toDir) {
        Log.info("ReplayFile: unpacking replay");
        var zip = new ZipFi(zipFile);
        for (var file : zip.list()) {
            file.copyTo(toDir);
        }
    }

    public static Seq<ReplayFile> replays() {
        var list = new Seq<ReplayFile>();
        var entries = REPLAYS_DIR.list();
        Arrays.stream(entries)
              .sorted(Comparator.comparingLong(f -> {
                  try {
                      return Long.parseLong(f.name());
                  } catch (NumberFormatException e) {
                      Log.err("ReplayFile: invalid replay name: " + f.name());
                      return 0L;
                  }
              }))
              .forEach(f -> list.add(new ReplayFile(f.name())));
        return list;
    }

    public Reader createReader() {
        return new Reader();
    }

    public void delete() {
        if (this.zipFile.exists()) {
            this.zipFile.delete();
        }
        if (this.workDir.exists()) {
            this.workDir.deleteDirectory();
        }
    }

    public class Writer {
        private String nextName() {
            return String.valueOf(counter.getAndIncrement());
        }

        public void writeInfo(InfoFile info) {
            workDir.child(INFO_NAME_FILE).writeString(info.toString());
        }

        public void saveMap() {
            SaveIO.save(mapsDir.child(nextName()));
        }

        public Fi getFirstMap() {
            var l = mapsDir.list();
            if (l.length == 0) {
                throw new NoSuchElementException("No maps available");
            } else {
                return l[0];
            }
        }

        public void writeEvent(String ev) {
            eventsDir.child(nextName()).writeString(ev);
        }

        public void zip() {
            ReplayFile.this.zip();
        }
    }

    public class Reader {
        private final Fi[] eventFiles;
        private final Fi[] mapFiles;
        private int eventIdx = 0;
        private int mapIdx = 0;

        public Reader() {
            if (!workDir.exists() && zipFile.exists()) {
                unzip(zipFile, workDir);
            }

            this.eventFiles = sorted(eventsDir.list());
            this.mapFiles = sorted(mapsDir.list());
        }

        private Fi[] sorted(Fi[] arr) {
            Arrays.sort(arr, Comparator.comparingInt(fi -> Integer.parseInt(fi.name())));
            return arr;
        }

        public InfoFile readInfo() {
            return InfoFile.fromString(workDir.child(INFO_NAME_FILE).readString());
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
    }
}
