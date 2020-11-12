package jdep.watcher;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class Event {

    private final WatchEvent.Kind kind;
    private final Path path;
    private final boolean isDirectory;

    public Event(WatchEvent.Kind kind, Path path, boolean isDir) {
        this.kind = kind;
        this.path = path;
        this.isDirectory = isDir;
    }

    public WatchEvent.Kind getKind() {
        return kind;
    }

    public Path getPath() {
        return path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

}
