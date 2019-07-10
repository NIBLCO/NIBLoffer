package com.nibl.bot.service.dcc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.Bot;
import com.nibl.bot.service.Service;

public class DCCActiveQueue extends Service {

    private static Logger log = LoggerFactory.getLogger(DCCActiveQueue.class);

    private static Boolean isRunning = true;
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private static BlockingQueue<DCCRequest> activeQueue = new LinkedBlockingQueue<>();

    public DCCActiveQueue(Bot myBot) {
        super(myBot);
    }

    public static void setIsRunning(Boolean isRunning) {
        DCCActiveQueue.isRunning = isRunning;
    }

    public static void addToActiveQueue(DCCRequest transfer) {
        if (!isRunning) {
            log.warn("Queue is not running.  No transfers will be sent!");
        }

        log.debug("Adding pack# {} for user {} to active queue", transfer.getBotFile().getId(),
                transfer.getDCCUser().getUser().getNick());
        transfer.setStatus(DCCRequest.Status.QUEUED);
        activeQueue.add(transfer);
    }

    public static BlockingQueue<DCCRequest> getActiveQueue() {
        return activeQueue;
    }

    @Override
    public void executeTask() {
        while (isRunning) {
            log.debug("Poll active queue");
            try {
                DCCRequest transfer = activeQueue.take();
                log.debug("Submitting transfer for pack# {} to user {}", transfer.getBotFile().getId(),
                        transfer.getDCCUser().getUser().getNick());
                executor.submit(transfer);
            } catch (InterruptedException e) {
                log.warn("Active queue interrupted!");
            }

        }
    }

}
