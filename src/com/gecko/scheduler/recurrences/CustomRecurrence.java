package com.gecko.scheduler.recurrences;

import java.time.ZonedDateTime;

public class CustomRecurrence implements Recurrence {

    @Override
    public ZonedDateTime getNextExecutionTime() {
        return null;
    }

    /*
    Todo: patterned recurrence implementation
    - flexible patterns-inside-pattern
    - Passing test: definable leap year implementation
    - Goal: modern calendar custom recurrence specs
     */
}
