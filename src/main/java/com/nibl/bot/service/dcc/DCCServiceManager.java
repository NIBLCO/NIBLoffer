package com.nibl.bot.service.dcc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.pircbotx.User;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.Bot;
import com.nibl.bot.model.BotFile;
import com.nibl.bot.service.Service;
import com.nibl.bot.service.packlist.PacklistService;

public class DCCServiceManager extends Service {

    private static Map<User, DCCUser> waitingQueue = new HashMap<>();

    private static List<DCCTransfer> activeQueue = new ArrayList<>();

    private static ExecutorService executor = Executors.newCachedThreadPool();

    private static Logger log = LoggerFactory.getLogger(DCCServiceManager.class);

    protected DCCServiceManager(Bot myBot) {
        super(myBot);
    }

    public static void addRequest(PrivateMessageEvent event) {
        User user = event.getUser();
        if (!waitingQueue.containsKey(user)) {
            waitingQueue.put(user, new DCCUser(user));
        }

        List<Integer> dls = convertInputToPackNumbers(event.getMessage());
        for (Integer packNumber : dls) {
            log.info("Sending pack#{}", packNumber);

            BotFile file = PacklistService.getBotFileByPackNumber(packNumber);
            if (null == file) {
                user.send().message("Pack #" + packNumber + " not found");
                continue;
            }
            waitingQueue.get(user).addTransfer(new DCCTransfer(myBot, user, file));
        }
    }

    // TODO simplify & clarify this method
    private static List<Integer> convertInputToPackNumbers(String message) {
        List<Integer> dls = new ArrayList<>();
        String packs = message.replace("#", "").replace("xdcc batch", "").replace("xdcc send", "");
        String[] second = packs.replaceAll(" ", "").split(",");
        for (String third : second) {
            String[] fourth = third.split("-");
            if (fourth.length > 1) {
                for (int i = Integer.parseInt(fourth[0]); i <= Integer.parseInt(fourth[1]); i++) {
                    dls.add(i);
                }
            } else {
                dls.add(Integer.parseInt(fourth[0]));
            }
        }
        return dls;
    }

    private static DCCTransfer getNextForQueue() {
        DCCUser user = waitingQueue.values().stream().min(Comparator.comparing(DCCUser::getLastTransfer)).get();
        return user.getTransfers().stream().min(Comparator.comparing(DCCTransfer::getSubmittedOn)).get();
    }

    @Override
    public void executeTask() {
        executor
    }

}
