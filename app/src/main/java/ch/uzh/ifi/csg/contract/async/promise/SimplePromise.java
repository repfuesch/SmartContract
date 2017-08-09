package ch.uzh.ifi.csg.contract.async.promise;


import org.jdeferred.Deferred;
import org.jdeferred.Promise;

import java.util.UUID;

/**
 * Interface that wraps the {@link Promise} interface of the JDeferred library
 */

public interface SimplePromise<T>
{
    /**
     * This method will register {@link DoneCallback} so that when a Deferred object
     * is resolved ({@link Deferred#resolve(Object)}), {@link DoneCallback} will be triggered.
     *
     * You can register multiple {@link DoneCallback} by calling the method multiple times.
     * The order of callback trigger is based on the order you call this method.
     *
     * <pre>
     * <code>
     * promise.done(new DoneCallback(){
     * 	 public void onDone(Object done) {
     *     ...
     *   }
     * });
     * </code>
     * </pre>
     *
     * @param callback
     * @return
     */
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

    /**
     * Equivalent to {@link #done(DoneCallback)}
     *
     * @param doneCallback {@link #done(DoneCallback)}
     * @return
     */
    public SimplePromise<T> then(DoneCallback<T> doneCallback);


    /**
     * Blocks the calling  thread and waits for the operation to complete. If the operation fails,
     * this method returns null
     *
     * @return the result of the operation
     */
    public T get();

    /**
     * Returns the UUID assigned to this Promise object
     *
     * @return the UUID
     */
    public UUID getId();
}
