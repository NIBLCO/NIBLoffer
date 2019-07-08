package com.nibl.bot;

import java.io.File;
import java.io.IOException;

import org.pircbotx.exception.DccException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.model.BotFile;

public class Listener extends ListenerAdapter {

    private Logger log = LoggerFactory.getLogger(Listener.class);

    private Bot bot;

    public Listener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onConnect(ConnectEvent event) {
        log.info("{} connected to {} on port {}", event.getBot().getNick(), event.getBot().getServerHostname(),
                event.getBot().getServerPort());
    }

    @Override
    public void onMessage(MessageEvent event) {
        log.info("[MESSAGE] {}: {}", event.getUser().getNick(), event.getMessage());
    }

    @Override
    public void onNotice(NoticeEvent event) {
        log.info("[NOTICE] {}: {}", event.getUser().getNick(), event.getMessage());
    }

    @Override
    public void onPrivateMessage(PrivateMessageEvent event) {
        log.info("[PMESSAGE] {}: {}", event.getUser().getNick(), event.getMessage());
        if (event.getMessage().startsWith("xdcc send")) {
            Integer packNumber = Integer.parseInt(event.getMessage().replace("xdcc send", "").trim());
            log.info("Sending pack#{}", packNumber);

            if (bot.getAutoadd().getPacklist().getFiles().size() >= packNumber) {
                BotFile botFile = bot.getAutoadd().getPacklist().getFiles().get(packNumber);
                try {
                    event.getUser().send().dccFileAndTransfer(
                            new File(bot.getAutoadd().getPacklist().getFiles().get(packNumber).getPath().toString()));
                } catch (DccException | IOException | InterruptedException e) {
                    log.error("Failed transferring {} to {}", botFile.getName(), event.getUser().getNick(), e);
                }

            }
        }
    }

    @Override
    public void onIncomingChatRequest(IncomingChatRequestEvent event) {
        log.info("[CHAT] from {}", event.getUser().getNick());
    }

}
