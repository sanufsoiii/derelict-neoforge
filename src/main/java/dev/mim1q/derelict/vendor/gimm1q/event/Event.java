package dev.mim1q.derelict.vendor.gimm1q.event;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Minimal replacement for Fabric API's array-backed event (loader-independent).
 * Listeners are invoked in registration order through the invoker built by the factory.
 */
public final class Event<T> {
    private final Class<T> type;
    private final Function<T[], T> invokerFactory;
    private T[] listeners;
    private volatile T invoker;

    @SuppressWarnings("unchecked")
    Event(Class<T> type, Function<T[], T> invokerFactory) {
        this.type = type;
        this.invokerFactory = invokerFactory;
        this.listeners = (T[]) Array.newInstance(type, 0);
        this.invoker = invokerFactory.apply(this.listeners);
    }

    public synchronized void register(T listener) {
        if (listener == null) throw new NullPointerException("listener");
        listeners = Arrays.copyOf(listeners, listeners.length + 1);
        listeners[listeners.length - 1] = listener;
        invoker = invokerFactory.apply(listeners);
    }

    public T invoker() {
        return invoker;
    }
}
