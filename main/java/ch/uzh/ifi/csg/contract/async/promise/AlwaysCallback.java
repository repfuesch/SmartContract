package ch.uzh.ifi.csg.contract.async.promise;

import org.jdeferred.Promise;

/**
 * Created by flo on 24.02.17.
 */

public interface AlwaysCallback<D> {
    void onAlways(final Promise.State state, final D resolved, final Throwable rejected);
}
