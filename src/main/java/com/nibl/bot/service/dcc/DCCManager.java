package com.nibl.bot.service.dcc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.Bot;
import com.nibl.bot.service.Service;
import com.nibl.bot.service.packlist.PacklistService;
import com.nibl.bot.util.BotUtility;

public class DCCManager extends Service {

    private static List<DCCUser> waitingQueue = new ArrayList<>();

    private static Logger log = LoggerFactory.getLogger(DCCManager.class);

    public DCCManager(Bot myBot) {
        super(myBot);
    }

    public static void addRequest(PrivateMessageEvent event) {
        // Get the existing DCCUser or create a new one
        DCCUser dccUser = waitingQueue.stream().filter(u -> u.getUser().equals(event.getUser())).findFirst()
                .orElse(new DCCUser(event.getUser()));

        List<Integer> alreadyQueuedPackNumbers = new ArrayList<>();
        List<Integer> packNumbers = BotUtility.convertStringRangeToList(
                event.getMessage().replace("#", "").replace("xdcc batch", "").replace("xdcc send", ""));
        log.debug("Packs requested {}", packNumbers.stream().map(String::valueOf).collect(Collectors.joining(",")));

        // For each BotFile found, Add to the DCCUser if new
        PacklistService.getBotFilesByPackNumbers(packNumbers).forEach(botFile -> {
            if (!dccUser.getTransfers().stream().anyMatch(t -> t.getBotFile().equals(botFile))) {
                log.debug("Add new transfer for pack{}", botFile.getId());
                dccUser.addTransfer(new DCCRequest(myBot, dccUser, botFile));
            } else {
                alreadyQueuedPackNumbers.add(botFile.getId());
            }
            // Remove from requested packs so we can output a single "unknown" message
            packNumbers.remove(botFile.getId());
        });

        // Output already queued message
        if (!alreadyQueuedPackNumbers.isEmpty()) {
            dccUser.getUser().send()
                    .message("You are already queued for pack" + ((alreadyQueuedPackNumbers.size() > 1) ? "s" : "")
                            + " "
                            + alreadyQueuedPackNumbers.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }

        // Output unknown packs message
        if (!packNumbers.isEmpty()) {
            dccUser.getUser().send().message("Unknown pack" + ((packNumbers.size() > 1) ? "s" : "") + " "
                    + packNumbers.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }

        if (!waitingQueue.contains(dccUser)) {
            log.debug("Add new DCCUser {} to queue", dccUser.getUser().getNick());
            waitingQueue.add(dccUser);
        }

        dccUser.getUser().send().message("You are queued for " + dccUser.getTransfers().size() + " pack(s)");
        log.debug("{} has {} transfers queued", dccUser.getUser().getNick(), dccUser.getTransfers().size());
    }

    public static void removeTransfer(DCCRequest transfer) {
        Integer index = waitingQueue.indexOf(transfer.getDCCUser());
        if (index >= 0) {
            DCCUser user = waitingQueue.get(index);
            user.getTransfers().remove(transfer);
            if (user.getTransfers().isEmpty()) {
                log.debug("Queue finished for {}", user.getUser().getNick());
                waitingQueue.remove(user);
            }
        }
    }

    private static DCCRequest getNextForQueue() {

        // Get the user who hasn't transferred recently and has less than 2 transfers running
        Optional<DCCUser> dccUserOpt = waitingQueue.stream()
                .filter(dccUser -> dccUser.getTransfers().stream()
                        .filter(dccTransfer -> !dccTransfer.getStatus().equals(DCCRequest.Status.FINISHED)
                                && !dccTransfer.getStatus().equals(DCCRequest.Status.WAITING))
                        .count() < 2)
                .min(Comparator.comparing(DCCUser::getLastTransfer));
        if (!dccUserOpt.isPresent()) {
            throw new NoSuchElementException();
        }

        Optional<DCCRequest> dccTransferOpt = dccUserOpt.get().getTransfers().stream()
                .filter(tr -> tr.getStatus().equals(DCCRequest.Status.WAITING))
                .min(Comparator.comparing(DCCRequest::getSubmittedOn));

        if (!dccTransferOpt.isPresent()) {
            throw new NoSuchElementException();
        }

        return dccTransferOpt.get();
    }

    @Override
    public void executeTask() {
        if (DCCActiveQueue.getActiveQueue().size() < 10) {
            try {
                DCCActiveQueue.addToActiveQueue(getNextForQueue());
            } catch (NoSuchElementException e) {
                // Silently hide. No transfer to accommodate
            }
        }
    }

}
