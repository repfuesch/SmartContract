package ch.uzh.ifi.csg.contract.async.promise;


import java.util.UUID;

/**
 * Created by flo on 23.02.17.
 */

public interface SimplePromise<T>
{
    public SimplePromise<T> done(DoneCallback<T> callback);

    /**
     * This method will register {@link FailCallback} so that when a Deferred object
     * is rejected, {@link FailCallback} will be triggered.
     *
     * You can register multiple {@link FailCallback} by calling the method multiple times.
     * The order of callback trigger is based on the order you call this method.
     *
     * <pre>
     * <code>
     * promise.fail(new FaillCallback(){
     * 	 public void onFail(Object rejection) {
     *     ...
     *   }
     * });
     * </code>
     * </pre>
     *
     * @param callback
     * @return
     */
    public SimplePromise<T> fail(FailCallback callback);

    /**
     * This method will register {@link AlwaysCallback} so that when it's always triggered
     * regardless of whether the corresponding Deferred object was resolved or rejected.
     *
     * You can register multiple {@link AlwaysCallback} by calling the method multiple times.
     * The order of callback trigger is based on the order you call this method.
     *
     * <pre>
     * <code>
     * promise.always(new AlwaysCallback(){
     * 	 public void onAlways(State state, Object result, Object rejection) {
     *     if (state == State.RESOLVED) {
     *       // do something w/ result
     *     } else {
     *       // do something w/ rejection
     *     }
     *   }
     * });
     * </code>
     * </pre>
     *
     * @param callback
     * @return
     */
    public SimplePromise<T> always(AlwaysCallback<T> callback);

    public T get();

    public UUID getId();
}
