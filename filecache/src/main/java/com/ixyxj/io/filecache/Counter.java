package com.ixyxj.io.filecache;

import android.annotation.TargetApi;
import android.os.Build;

import java.util.*;

/**
 * For more information, you can visit https://github.com/xieyangxuejun,
 * or contact me by xieyangxuejun@gmail.com
 *
 * @author silen on 2019/3/20 1:11
 * Copyright (c) 2019 in FORETREE
 */
public class Counter {
    private static final Counter ROOT = new Counter("<root>", null);
    private static final ThreadLocal<Counter> CURRENT = new ThreadLocal<>();

    private final Map<String, Counter> children = new HashMap<>();
    private final String name;
    private final Counter parent;
    private int count = 0;
    private float time = 0;


    public Counter(String name, Counter counter) {
        this.name = name;
        this.parent = counter;
    }

    public static Scope time(String name) {
        Counter counter = CURRENT.get();
        if (counter == null) {
            counter = ROOT.child("Thread " + Thread.currentThread().getName());
            CURRENT.set(counter);
        }
        return new Scope(counter.child(name));
    }

    public static String collect(boolean reset) {
        StringBuilder sb = new StringBuilder();
        synchronized (ROOT) {
            for (Counter value : ROOT.children.values()) {
                value.write(sb, 0);
            }
        }

        if (reset) {
            ROOT.reset();
        }
        return sb.toString();



    }

    private synchronized Counter child(String name) {
        Counter counter = children.get(name);
        if (counter == null) {
            counter = new Counter(name, this);
            children.put(name, counter);
        }
        return counter;
    }

    private void exit(float duration) {
        synchronized (this) {
            time += duration;
            count++;
        }
        CURRENT.set(this);
    }

    private void enter() {
        CURRENT.set(this);
    }

    private synchronized void reset() {
        children.clear();
        count = 0;
        time = 0;
    }

    private static void indent(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append("    ");
        }
    }

    private  void write(StringBuilder sb, int depth) {
        if (count > 0) {
            indent(sb, depth);
            if (parent.time != 0) {
                sb.append((100 * time / parent.time))
                        .append("% ");
            }
            sb.append(name).append(" ")
                    .append("[total: ").append(time)
                    .append(" count: ").append(count)
                    .append(" average: ").append(time/count)
                    .append("]\n");
        } else {
            sb.append("\n");
        }

        if (children.size() > 0) {
            List<Counter> sorted = new ArrayList<>(children.values());
            Collections.sort(sorted, new Comparator<Counter>() {
                @Override
                public int compare(Counter o1, Counter o2) {
                    return Float.compare(o1.time, o2.time);
                }
            });
            for (Counter counter : sorted) {
                counter.write(sb, depth);
            }
            float other = time;
            for (Counter value : children.values()) {
                other -= value.time;
            }
            if (other > 0) {
                indent(sb, depth);
                sb.append((100 * other / time)).append("% <other>\n");
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static class Scope implements AutoCloseable {
        private final long start;
        private final Counter counter;

        private Scope(Counter counter) {
            this.start = System.nanoTime();
            this.counter = counter;
            counter.enter();
        }


        @Override
        public void close() {
            long end = System.nanoTime();
            counter.exit((end - start) / 1000000000.0f);
        }
    }
}
