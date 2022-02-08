package com.gecko.scheduler.recurrences;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashSet;

/**
 * A type of recurrence where the execution time falls within a specified
 * set of {@link DayOfWeek}.
 * <p>
 * The {@linkplain LocalTime time in the day} when the execution happens
 * is definable and the same for every execution time.
 */
public class DayOfWeekRecurrence implements Recurrence {

    private final HashSet<DayOfWeek> recurrenceSet;
    private final LocalTime startTimeInDay;

    public DayOfWeekRecurrence(HashSet<DayOfWeek> recurrence) {
        this(recurrence, LocalTime.MIDNIGHT);
    }

    public DayOfWeekRecurrence(HashSet<DayOfWeek> recurrence, LocalTime timeToExecute) {
        this.recurrenceSet = recurrence;
        this.startTimeInDay = timeToExecute;
    }

    @Override
    public ZonedDateTime getNextExecutionTime() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime executionTimeIfToday = ZonedDateTime.of(now.toLocalDate(), startTimeInDay, now.getZone());
        ZonedDateTime next = executionTimeIfToday.plusDays(7);

        // Below loop finds the closest DayOfWeek from 'now' and updates 'next' accordingly:
        for (DayOfWeek day : recurrenceSet) {
            int differenceDays = day.getValue() - now.getDayOfWeek().getValue();
            if (differenceDays < 0) differenceDays += 7;

            ZonedDateTime potentialNext = executionTimeIfToday.plusDays(differenceDays);
            if (potentialNext.isAfter(now) && potentialNext.isBefore(next)) {
                next = potentialNext;
            }
        }

        return next;
    }

}
