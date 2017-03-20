package ch.uzh.ifi.csg.contract.async.promise;

/**
 * Created by flo on 24.02.17.
 */

public interface DoneCallback<D> {
    public void onDone(final D result);
}
