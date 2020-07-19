package com.xdu.ichat.utils;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hujiaqi
 * @create 2020/6/26
 */
public class EnhancedThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolId = new AtomicInteger();

    private final AtomicInteger nextId = new AtomicInteger();
    private final String prefix;
    private final boolean daemon;
    private final int priority;

    public EnhancedThreadFactory(String poolName) {
        this(poolName, false, Thread.NORM_PRIORITY);
    }

    public EnhancedThreadFactory(String poolName, boolean daemon, int priority) {
        if (poolName == null) {
            throw new NullPointerException("poolName");
        }
        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException(
                    "priority: " + priority + " (expected: Thread.MIN_PRIORITY <= priority <= Thread.MAX_PRIORITY)");
        }

        prefix = poolName + '-' + poolId.incrementAndGet() + '-';
        this.daemon = daemon;
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = newThread(new EnhancedRunnableDecorator(r), prefix + nextId.incrementAndGet());
        try {
            if (t.isDaemon()) {
                if (!daemon) {
                    t.setDaemon(false);
                }
            } else {
                if (daemon) {
                    t.setDaemon(true);
                }
            }

            if (t.getPriority() != priority) {
                t.setPriority(priority);
            }
        } catch (Exception ignored) {
            // Doesn't matter even if failed to set.
        }
        return t;
    }

    protected Thread newThread(Runnable r, String name) {
        return new FastThreadLocalThread(r, name);
    }

    private static final class EnhancedRunnableDecorator implements Runnable {

        private final Runnable r;
        private final Map context;

        EnhancedRunnableDecorator(Runnable r) {
            this.r = r;
            this.context = MDC.getCopyOfContextMap();
        }

        @Override
        public void run() {
            try {
                if (context == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(context);
                }
                r.run();
            } finally {
                FastThreadLocal.removeAll();
            }
        }
    }
}
