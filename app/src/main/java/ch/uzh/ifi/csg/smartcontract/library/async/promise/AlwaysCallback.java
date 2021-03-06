package ch.uzh.ifi.csg.smartcontract.library.async.promise;

import org.jdeferred.Promise;

/**
 * @see SimplePromise#always(AlwaysCallback)
 *
 */
public interface AlwaysCallback<D> {
    void onAlways(final Promise.State state, final D resolved, final Throwable rejected);
}
