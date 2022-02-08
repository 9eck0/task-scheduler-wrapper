package com.gecko.scheduler.recurrences;

import java.time.ZonedDateTime;

/**
 * Base interface to define recurrence provider used in task scheduling by the {@linkplain com.gecko.scheduler.Scheduler scheduler}.
 * <p>
 * A recurrence type is a timestamp provider using rules-based scheduling.
 * Recurrence providers must take into account the {@linkplain ZonedDateTime#now() current time} when calculating the
 * next scheduled
 * <br>
 * In recurrence types with predefined times of executions
 * (e.g. a {@linkplain java.util.Collection collection} of {@linkplain java.time.LocalDateTime DateTime}s),
 * the nearest future time will be returned by {@link #getNextExecutionTime()}.
 */
public interface Recurrence {

    /**
     * Obtains the next execution time from the moment this method is called.
     * <p>
     * If the query limit has been reached, this method returns {@code null}.
     * The query limit is the number of times this method has been called.
     * @return The next execution time of this recurrence. If unavailable, returns {@code null}.
     * @implNote Query limit is to be defined by each recurrence provider
     */
    ZonedDateTime getNextExecutionTime();
}
