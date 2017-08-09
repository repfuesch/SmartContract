package ch.uzh.ifi.csg.contract.async.promise;

import org.jdeferred.Deferred;

/**
 * @see Deferred#resolve(Object)
 * @see SimplePromise#done(DoneCallback)
 *
 */
public interface DoneCallback<D> {
    public void onDone(final D result);
}
