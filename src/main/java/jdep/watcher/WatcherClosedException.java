/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package jdep.watcher;

/**
 * Class is analog to {@link java.nio.file.ClosedWatchServiceException}.
 * The main idea is to provide exception from the same package where {@link Watcher} is from.
 */
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
