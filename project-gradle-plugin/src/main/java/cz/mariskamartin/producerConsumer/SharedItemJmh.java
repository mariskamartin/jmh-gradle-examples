package cz.mariskamartin.producerConsumer;

import java.util.concurrent.atomic.AtomicLong;

public class SharedItemJmh {
    private final String msg;
    private final AtomicLong eventCount;

    public SharedItemJmh(String s, AtomicLong eventCount) {
        this.msg = s;
        this.eventCount = eventCount;
    }

    public String getMsg() {
        return msg;
    }

    public AtomicLong getEventCount() {
        return eventCount;
    }
}
