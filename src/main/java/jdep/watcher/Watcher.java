/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package jdep.watcher;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Class is analog to {@link WatchService}.
 * It provides {@link Event}s and monitor if the removed file a directory or not.
 */
public class Watcher implements Closeable {

    private final WatchService service;
    private final Map<WatchKey, Path> watchPaths = new HashMap<>();
    private final Set<Path> directories = new HashSet<>();
    private boolean closed;

    /**
     * Creates Watcher basing on default watch service of {@link FileSystems#getDefault()}
     */
    public Watcher() {
        try {
            service = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new IllegalStateException("failed to get file system watch service", e);
        }
    }

    public Watcher(WatchService service) {
        if (service == null) {
            throw new IllegalArgumentException("watch service is null");
        }
        this.service = service;
    }

    public Path resolveFullPath(WatchKey key, Path file) {
        return getKeyPath(key).resolve(file);
    }

    private Path getKeyPath(WatchKey key) {
        Path path = watchPaths.get(key);
        if (path == null) {
            throw new IllegalArgumentException("no watch directory found for key: " + key);
        }
        return path;
    }

    public void register(Path path, WatchEvent.Kind... kinds) throws IOException, WatcherClosedException {
        if (closed) {
            throw new WatcherClosedException("failed to register path: " + path);
        }
        registerPath(path, kinds);
    }

    private void registerPath(Path path, WatchEvent.Kind... kinds) throws IOException, WatcherClosedException {
        try {
            WatchKey key = path.register(service, kinds);
            watchPaths.put(key, path);
            for (Path dir : Files.newDirectoryStream(path, file -> Files.isDirectory(file))) {
                directories.add(dir);
            }
        } catch (ClosedWatchServiceException e) {
            closeWithException("failed to register path: " + path, e);
        }
    }

    public void registerRecursive(Path path, WatchEvent.Kind[] kinds) throws IOException, WatcherClosedException {
        if (closed) {
            throw new WatcherClosedException("failed to register path: " + path);
        }

        registerPath(path, kinds);
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerPath(dir, kinds);
                return super.preVisitDirectory(dir, attrs);
            }
        });
    }

    public boolean isClosed() {
        return closed;
    }

    public List<Event> pollEvents(WatchKey key) {
        List<WatchEvent<?>> events = key.pollEvents();
        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        key.reset();

        Path path = getKeyPath(key);

        List<Event> watchEvents = new ArrayList<>(events.size());
        for (WatchEvent event : events) {
            Path eventPath = path.resolve((Path) event.context());

            boolean isDir = false;
            if (event == StandardWatchEventKinds.ENTRY_CREATE) {
                isDir = Files.isDirectory(eventPath);
                if (isDir) {
                    directories.add(eventPath);
                }
            } else if (event == StandardWatchEventKinds.ENTRY_MODIFY) {
                isDir = Files.isDirectory(eventPath);
                if (isDir) {
                    directories.remove(eventPath);
                    directories.add(eventPath);
                }
            } else if (event == StandardWatchEventKinds.ENTRY_DELETE) {
                isDir = directories.remove(eventPath);
            }

            watchEvents.add(new Event(event.kind(), eventPath, isDir));
        }
        return watchEvents;
    }

    public List<Event> take() throws InterruptedException, WatcherClosedException {
        if (closed) {
            throw new WatcherClosedException("failed to take next event");
        }

        try {
            WatchKey key = service.take();
            return pollEvents(key);
        }catch (ClosedWatchServiceException e) {
            closeWithException("failed to take next event", e);
        }
        return Collections.emptyList();
    }

    public List<Event> poll() {
        if (closed) {
            return Collections.emptyList();
        }

        try {
            WatchKey key = service.poll();
            if (key == null) {
                return Collections.emptyList();
            }
            return pollEvents(key);
        } catch (ClosedWatchServiceException e) {
            closed = true;
            return Collections.emptyList();
        }
    }

    public void close() throws IOException {
        closed = true;
        service.close();
    }

    private void closeWithException(String msg, ClosedWatchServiceException e) throws WatcherClosedException{
        closed = true;
        throw new WatcherClosedException(msg, e);
    }

}
