package ch.uzh.ifi.csg.contract.async;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.jdeferred.DeferredManager;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromiseAdapter;

/**
 * Created by flo on 23.02.17.
 */

public class Async {

    private static DeferredManager deferredManager;
    private static Map<UUID, SimplePromise> promiseMap;

    static {
        deferredManager = new DefaultDeferredManager();
        promiseMap = new HashMap<>();
    }

    public static <T> SimplePromise<T> toPromise(Callable<T> callable)
    {
        final Promise<T, Throwable, Void> promise = deferredManager.when(callable);
        final UUID id = UUID.randomUUID();
        SimplePromise<T> simplePromise = new SimplePromiseAdapter<>(promise, id);
        promiseMap.put(id, simplePromise);

        simplePromise.always(new AlwaysCallback<T>() {
            @Override
            public void onAlways(Promise.State state, T resolved, Throwable rejected) {
                promiseMap.remove(id);
            }
        });

        return simplePromise;
    }

    public static <T> SimplePromise<T> getPromiseById(UUID id)
    {
        if(promiseMap.containsKey(id))
        {
            SimplePromise<T> promise = promiseMap.get(id);
            promiseMap.remove(id);
            return promise;
        }

        return null;
    }

    public static List<SimplePromise> getPendingPromises()
    {
        return new ArrayList<>(promiseMap.values());
    }

    public static boolean hasPendingTransactions()
    {
        return !promiseMap.isEmpty();
    }
}


