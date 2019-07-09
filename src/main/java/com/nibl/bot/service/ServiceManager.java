package com.nibl.bot.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.Bot;
import com.nibl.bot.BotInterface;

public class ServiceManager extends BotInterface {

    private Logger log = LoggerFactory.getLogger(ServiceManager.class);

    ExecutorService executor;
    ScheduledExecutorService scheduledExecutor;

    public ServiceManager(Bot myBot, Integer executorSize, Integer scheduledExecutorSize) {
        super(myBot);
        executor = Executors.newFixedThreadPool(executorSize);
        scheduledExecutor = Executors.newScheduledThreadPool(scheduledExecutorSize);
    }

    public void shutdownExecutor(Integer secondsToWait) {
        try {
            log.debug("Executor shutdown started");
            executor.shutdown();
            executor.awaitTermination(secondsToWait, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Executor tasks interrupted", e);
        } finally {
            if (!executor.isTerminated()) {
                log.debug("Executor tasks terminated after waiting {} seconds", secondsToWait);
            }
            executor.shutdownNow();
            log.debug("Executor shutdown finished");
        }
    }

    public void stopScheduledExecutor(Integer secondsToWait) {
        try {
            log.debug("Scheduled Executor shutdown started");
            scheduledExecutor.shutdown();
            scheduledExecutor.awaitTermination(secondsToWait, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Scheduled Executor tasks interrupted", e);
        } finally {
            if (!scheduledExecutor.isTerminated()) {
                log.debug("Scheduled Executor tasks terminated after waiting {} seconds", secondsToWait);
            }
            scheduledExecutor.shutdownNow();
            log.debug("Scheduled Executor shutdown finished");
        }
    }

    /**
     * 
     * @param Service service
     * @return Future
     * @throws ExecutionException
     * @throws RejectedExecutionException
     */
    public Future<?> addService(Service service) throws ExecutionException {
        if (executor.isShutdown()) {
            throw new ExecutionException("Executor is shutdown", null);
        }
        return executor.submit(service);
    }

    /**
     * 
     * @param Service service
     * @param Integer secondsToDelay
     * @param Integer secondsToIterate
     * @return ScheduledFuture
     * @throws ExecutionException
     */
    public ScheduledFuture<?> addScheduledService(Service service, Integer secondsToDelay, Integer secondsToIterate)
            throws ExecutionException {
        if (scheduledExecutor.isShutdown()) {
            throw new ExecutionException("Scheduled Executor is shutdown", null);
        }
        return scheduledExecutor.scheduleWithFixedDelay(service, secondsToDelay, secondsToIterate, TimeUnit.SECONDS);
    }
}
