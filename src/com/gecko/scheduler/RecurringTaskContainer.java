package com.gecko.scheduler;

import com.gecko.scheduler.recurrences.DayOfWeekRecurrence;
import com.gecko.scheduler.recurrences.Recurrence;
import com.gecko.scheduler.recurrences.FixedRateRecurrence;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Un conteneur d'une tâche à être exécutée périodiquement.
 * @implNote inspiré de: <a href="https://stackoverflow.com/a/20388073">How to run certain task every day at a particular time using ScheduledExecutorService?</a>
 */
public class RecurringTaskContainer {

    //region ================================ FIELDS ================================

    public final String name;
    private final Runnable task;
    private final ScheduledExecutorService taskSchedulerService = new ScheduledThreadPoolExecutor(1);

    // Recurrence stopwatch
    private final Recurrence recurrence;
    // Day-of-week recurrence type
    //private HashSet<DayOfWeek> dayOfWeekRecurrence;
    //private LocalTime dayOfWeekRecurrenceStartTime;
    // Uniform recurrence type
    //private ZonedDateTime uniformRecurrenceFirstStartTime;
    //private ChronoUnit uniformRecurrenceUnit;
    //private long uniformRecurrenceDelay;

    private boolean started = false;
    private boolean shutdown = false;

    //endregion

    //region ================================ CONSTRUCTORS ================================

    public RecurringTaskContainer(String name, Runnable task, Recurrence recurrence) {
        if (task == null) {
            throw new NullPointerException("Illegal attempt to instantiate a recurring task with a null Runnable.");
        }
        if (recurrence == null) {
            throw new NullPointerException("Illegal attempt to instantiate a recurring task with a null recurrence.");
        }

        this.name = name;
        this.task = task;
        this.recurrence = recurrence;
    }

    public RecurringTaskContainer(String name, Runnable task, HashSet<DayOfWeek> daysToExecute, LocalTime timeToExecute) {
        this(name, task, new DayOfWeekRecurrence(daysToExecute, timeToExecute));
        //this.dayOfWeekRecurrence = daysToExecute;
        //this.dayOfWeekRecurrenceStartTime = timeToExecute;
    }

    public RecurringTaskContainer(String name, Runnable task, ZonedDateTime firstStart, long delay, ChronoUnit delayUnit) {
        this(name, task, new FixedRateRecurrence(firstStart, delay, delayUnit));
        //this.uniformRecurrenceFirstStartTime = firstStart;
        //this.uniformRecurrenceDelay = delay;
        //this.uniformRecurrenceUnit = delayUnit;
    }

    //endregion

    //region ================================ METHODS ================================

    public void start() {
        if (!shutdown) {
            taskSchedulerService.schedule(this::taskWithRecurrence, getNextDelayInSeconds(), TimeUnit.SECONDS);
            started = true;
        }
    }

    /**
     * Attendre la fin d'exécution de la tâche ordonnancée, et annuler tout ordonnancement prochain.
     */
    public void shutdown() {
        shutdown = true;
        taskSchedulerService.shutdown();
    }

    /**
     * Interrompre la tâche de force.
     */
    public void shutdownNow() {
        shutdown = true;
        taskSchedulerService.shutdownNow();
        started = false;
    }

    /**
     * Après avoir appelé shutdown() ou shutdownNow(), cette méthode bloque jusqu'à ce que la tâche est libérée de
     * l'ordonnanceur.
     * @param timeout le timeout d'attente
     * @param unit l'unité de temps du timeout
     * @return si la tâche a complété sans avoir dépassé le timeout (arrêt de force)
     * @throws InterruptedException si la tâche a été interrompue lors de l'attente du timeout
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        boolean result = this.taskSchedulerService.awaitTermination(timeout, unit);
        started = false;
        return result;
    }

    /**
     * Returns whether this RecurringTaskContainer has started running.
     */
    public boolean hasStarted() {
        return started;
    }

    /**
     * This method wrapper is invoked by {@linkplain ScheduledThreadPoolExecutor taskSchedulerService}
     * on each run.
     * Its purpose is to begin a new round of task scheduling after the current round is finished.
     */
    private void taskWithRecurrence() {
        task.run();
        start();
    }

    /*public ZonedDateTime getNextExecutionTime() {
        if (dayOfWeekRecurrence != null) {
            // Récurrence par jour de la semaine

            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime executionTimeIfToday = ZonedDateTime.of(now.toLocalDate(), dayOfWeekRecurrenceStartTime, now.getZone());
            ZonedDateTime next = executionTimeIfToday.plusDays(7);

            // Cette boucle cherche le DayOfWeek prochain le plus proche de now
            // et assigne next selon le résultat
            for (DayOfWeek day : dayOfWeekRecurrence) {
                int differenceDays = day.getValue() - now.getDayOfWeek().getValue();
                if (differenceDays < 0) differenceDays += 7;

                ZonedDateTime potentialNext = executionTimeIfToday.plusDays(differenceDays);
                if (potentialNext.isAfter(now) && potentialNext.isBefore(next)) {
                    next = potentialNext;
                }
            }

            return next;
        } else {
            // Récurrence uniforme

            ZonedDateTime now = ZonedDateTime.now();
            if (started || this.uniformRecurrenceFirstStartTime.isAfter(now)) {
                return now.plus(this.uniformRecurrenceDelay, uniformRecurrenceUnit);
            } else {
                return this.uniformRecurrenceFirstStartTime;
            }
        }
    }*/

    private long getNextDelayInSeconds() {
        ZonedDateTime next = recurrence.getNextExecutionTime();
        ZonedDateTime now = ZonedDateTime.now();
        Duration duration = Duration.between(now, next);
        return duration.toSeconds();
    }

    //endregion
}
