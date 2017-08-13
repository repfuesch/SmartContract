package ch.uzh.ifi.csg.smartcontract.library.async.promise;

import org.jdeferred.Deferred;

/**
 * @see Deferred#reject(Object)
 * @see SimplePromise#fail(FailCallback)
 *
 */
public interface FailCallback {
    void onFail(final Throwable result);
}
