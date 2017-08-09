package ch.uzh.ifi.csg.contract.async;

import org.jdeferred.DeferredManager;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromiseAdapter;

/**
 * Helper class that provides static methods to create Promise objects from a Callable object
 */

public class Async {

    private static DeferredManager deferredManager;
    private static ScheduledExecutorService scheduledExecutorService;

    static {
        scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 4);
        ExecutorService executorService =  Executors.newCachedThreadPool();
        deferredManager = new DefaultDeferredManager(executorService);
    }

    public static <T> SimplePromise<T> toPromise(Callable<T> callable)
    {
        final Promise<T, Throwable, Void> promise = deferredManager.when(callable);
        return makePromiseAdapter(promise);
    }

    public static SimplePromise<Void> run(Callable<Void> callable)
    {
        final Promise<Void, Throwable, Void> promise = deferredManager.when(callable);
        return makePromiseAdapter(promise);
    }

    private static <T> SimplePromise<T> makePromiseAdapter(Promise<T, Throwable, Void> promise)
    {
        final UUID id = UUID.randomUUID();
        SimplePromise<T> simplePromise = new SimplePromiseAdapter<>(promise, id);
        return simplePromise;
    }

    public static ScheduledExecutorService getScheduledExecutorService()
    {
        return scheduledExecutorService;
    }
}


