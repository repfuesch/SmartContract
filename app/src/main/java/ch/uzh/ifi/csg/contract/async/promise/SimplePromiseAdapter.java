package ch.uzh.ifi.csg.contract.async.promise;

import org.jdeferred.Promise;
import java.util.UUID;


/**
 * Class that wraps a {@link Promise} object and implments the {@link SimplePromise} interface
 */

public class SimplePromiseAdapter<T> implements SimplePromise<T>
{
    private Promise<T, Throwable, Void> promise;
    private T result;
    private UUID id;

    public SimplePromiseAdapter(Promise<T, Throwable, Void> promise, UUID id)
    {
        this.promise = promise;
        this.id = id;
    }


    @Override
    public SimplePromise<T> done(final DoneCallback<T> callback)
    {
        promise = promise.done(new org.jdeferred.DoneCallback<T>() {
            @Override
            public void onDone(T result) {
                callback.onDone(result);
            }
        });

        return this;
    }

    @Override
    public SimplePromise<T> fail(final FailCallback callback)
    {
        promise = promise.fail(new org.jdeferred.FailCallback<Throwable>() {
            @Override
            public void onFail(Throwable result) {
                callback.onFail(result);

            }
        });

        return this;
    }

    @Override
    public SimplePromise<T> always(final AlwaysCallback<T> callback)
    {
        promise = promise.always(new org.jdeferred.AlwaysCallback<T, Throwable>() {
            @Override
            public void onAlways(Promise.State state, T resolved, Throwable rejected) {
                callback.onAlways(state,resolved, rejected);
            }
        });

        return this;
    }

    public SimplePromise<T> then(final DoneCallback<T> callback)
    {
        promise = promise.then(new org.jdeferred.DoneCallback<T>() {
            @Override
            public void onDone(T result) {
                callback.onDone(result);
            }
        });

        return this;
    }

    public T get()
    {
        promise.always(new org.jdeferred.AlwaysCallback<T, Throwable>() {
            @Override
            public void onAlways(Promise.State state, T resolved, Throwable rejected) {

                if(state == Promise.State.REJECTED)
                {
                }
                result = resolved;
            }
        });
        try {
            promise.waitSafely();

        } catch (InterruptedException e)
        {
            return null;
        }

        return result;
    }

    @Override
    public UUID getId() {
        return id;
    }

}

