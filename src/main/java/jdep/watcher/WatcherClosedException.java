package jdep.watcher;

public class WatcherClosedException extends RuntimeException {

    public WatcherClosedException(String msg) {
        super(msg);
    }

    public WatcherClosedException(Throwable e) {
        super(e);
    }

    public WatcherClosedException(String msg, Throwable e) {
        super(msg, e);
    }

}
