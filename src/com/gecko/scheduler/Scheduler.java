package com.gecko.scheduler;

import com.gecko.scheduler.recurrences.FixedDelayRecurrence;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A task scheduling subsystem
 * This module schedules tasks to be executed periodically.
 * It executes scheduled tasks with second precision, flooring all subsecond timing to 0.
 * <p>
 * Each scheduled task runs in its own thread. Due to this fact, any two tasks accessing
 * the same resources, especially
 * In task scheduling, try to avoid any potential
 * <p>
 * Par l'entremise du JVM (ver. 8+ implémentant JSR-310), cet ordonnanceur suit l'horloge & fuseau horaire du système.
 * L'un des avantage que cela pose est que tout changement du temps système sera réflété dans l'ordonnancement
 * des tâches, incluant le Daylight Saving Time (DST), les secondes intercalaires définies par IERS, etc.
 * <p>
 * Dû à comment JSR-310 définit les secondes (connoté "rubber seconds"), lors des jours avec des secondes intercalaires,
 * les secondes obtenues du JVM se diffèreront partiellement des secondes SI.
 * @author 9eck0
 * @see <a href="https://jcp.org/aboutJava/communityprocess/pfd/jsr310/JSR-310-guide.html">JCP-310</a>
 */
public class Scheduler {

    //region ================================ FIELDS ================================

//    private static Scheduler uniqueInstance = null;

    /**
     * List containing all scheduled tasks, with each single entry being an independent
     * {@link ScheduledExecutorService} with a core size of 1.
     */
    private final ArrayList<RecurringTaskContainer> recurringTasksList = new ArrayList<>();

    /**
     * The
     */
    private final ScheduledExecutorService singleExecutionTasksScheduler = new ScheduledThreadPoolExecutor(1);

    //endregion

    //region ================================ CONSTRUCTORS ================================

//    /*
//     * Default implementation of this scheduler module follows the singleton pattern.
//     * If more than a single instance is desired, comment out (or delete) everything
//     * inside this CONSTRUCTORS region.
//     */
//
//    private Scheduler() {
//    }
//
//    /**
//     * Obtains the global task scheduler instance.
//     * @return a shared scheduler instance
//     */
//    public static Scheduler getInstance() {
//        if (uniqueInstance == null) {
//            uniqueInstance = new Scheduler();
//        }
//        return uniqueInstance;
//    }

    //endregion

    //region ================================ METHODS (SCHEDULER) ================================

    public void addTask(RecurringTaskContainer task) {
        // TODO: collision detection...?
        task.start();
        recurringTasksList.add(task);
    }

    /**
     * Schedules a one-time execution {@linkplain Runnable task}.
     * @param name name of the task
     * @param task the task to execute
     * @param startTime date and time to begin execution
     */
    public void addTask(String name, Runnable task, ZonedDateTime startTime) {
        if (startTime.isBefore(ZonedDateTime.now())) {
            throw new IllegalArgumentException("'startTime' given is in the past.");
        }

        startTime = floorDateTime(startTime, TimeUnit.SECONDS);
        long delay = startTime.toEpochSecond() - ZonedDateTime.now().toEpochSecond();
        singleExecutionTasksScheduler.schedule(task, delay, TimeUnit.SECONDS);
    }

    /**
     * Schedules a {@linkplain Runnable task} with a weekly recurrence, executing on a specified set of {@linkplain DayOfWeek days of the week}.
     * @param name name of the task
     * @param task the task to execute
     * @param recurrence days of the week to execute the task
     * @param startTime the time within each day to begin execution
     */
    public void addTask(String name, Runnable task, HashSet<DayOfWeek> recurrence, LocalTime startTime)
    {
        RecurringTaskContainer conteneurTache = new RecurringTaskContainer(name, task, recurrence, startTime);
        addTask(conteneurTache);
    }

    /**
     * Schedules a task with a {@link FixedDelayRecurrence fixed delay recurrence}.
     * <br>
     * The delay represents the lapse of time between the end of a round of the task's execution and the start of next round.
     * @param name name of the task
     * @param task the task to execute
     * @param firstStartTime date and time of first execution
     * @param delay delay between the end of last execution and the start of next execution
     * @param delayUnit time unit of the delay parameter
     */
    public void addTask(String name, Runnable task, ZonedDateTime firstStartTime, long delay, ChronoUnit delayUnit)
    {
        firstStartTime = floorDateTime(firstStartTime, TimeUnit.SECONDS);
        RecurringTaskContainer conteneurTache = new RecurringTaskContainer(name, task, firstStartTime, delay, delayUnit);
        addTask(conteneurTache);
    }

    /**
     * Attempts to halt the execution of all running {@link Runnable tasks} and cancel any scheduled future execution,
     * by calling each task thread's {@link Thread#interrupt interrupt()} function.
     */
    public void stop() {
        singleExecutionTasksScheduler.shutdownNow();

        for (RecurringTaskContainer task : recurringTasksList) {
            task.shutdownNow();
        }
    }

    public void findTaskByName(String name) {

    }

    //endregion

    //region ================================ METHODS (STATIC) ================================

    /**
     * Finds the floor of a ZonedDateTime, with precision down to a given time unit.
     * <br>
     * The most granular precision is a second (i.e. anything more granular than a second will be set to 0).
     * @param time the original time to find floor
     * @param roundingTo the most granular unit of time to preserve
     * @return the floored time, down to the given time unit.
     */
    public static ZonedDateTime floorDateTime(ZonedDateTime time, TimeUnit roundingTo) {
        // If a time unit is smaller than roundingTo time unit, set the time unit to 0
        int hour = TimeUnit.HOURS.compareTo(roundingTo) < 0 ? 0 : time.getHour();
        int mins = TimeUnit.MINUTES.compareTo(roundingTo) < 0 ? 0 : time.getMinute();
        int secs = TimeUnit.SECONDS.compareTo(roundingTo) < 0 ? 0 : time.getSecond();

        return time.withHour(hour).withMinute(mins).withSecond(secs).withNano(0);
    }

    //endregion
}
