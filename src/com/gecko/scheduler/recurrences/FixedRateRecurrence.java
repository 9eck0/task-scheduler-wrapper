package com.gecko.scheduler.recurrences;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * A type of recurrence where the starting time between two executions
 * is constant.
 */
public class FixedRateRecurrence implements Recurrence {

    private final ZonedDateTime firstStartTime;
    private final ChronoUnit timeUnit;
    private final long recurrenceDelay;
    private long recurrenceCounter = 0;
    private long queryLimit = -1;
    private long timesQueried = 0;

    public FixedRateRecurrence(ZonedDateTime firstStart, long delay, ChronoUnit delayUnit, long queryLimit) {
        this.firstStartTime = firstStart;
        this.recurrenceDelay = delay;
        this.timeUnit = delayUnit;
        this.queryLimit = queryLimit;
    }

    public FixedRateRecurrence(ZonedDateTime firstStart, long delay, ChronoUnit delayUnit) {
        this(firstStart, delay, delayUnit, -1);
    }

    private boolean reachedQueryLimit() {
        return queryLimit > 0 && timesQueried >= queryLimit;
    }

    @Override
    public ZonedDateTime getNextExecutionTime() {
        if (reachedQueryLimit()) return null;
        timesQueried++;

        ZonedDateTime now = ZonedDateTime.now();
        if (firstStartTime.isAfter(now)) {
            return firstStartTime;
        } else {
            ZonedDateTime next = firstStartTime;
            while (next.isBefore(now)) {
                recurrenceCounter++;
                next = firstStartTime.plus(recurrenceDelay * recurrenceCounter, timeUnit);
            }
            return next;
        }
    }

}
