package ch.uzh.ifi.csg.contract.async.promise;

/**
 * Created by flo on 12.06.17.
 */

public interface ContinueCallback<T, U> {
    public SimplePromise<U> onContinue(final T result);
}
