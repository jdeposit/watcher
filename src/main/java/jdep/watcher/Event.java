/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package jdep.watcher;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * Class describes watcher event.
 * Except kind of an event and file path it knows if the path contains file or directory.
 * It can be useful when event is about removing a file, as far as event comes after removing
 * there is no chance to check if path is directory or not, but this object will know it.
 */
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
