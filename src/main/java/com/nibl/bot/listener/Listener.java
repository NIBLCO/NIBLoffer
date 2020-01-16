package com.nibl.bot.listener;

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.IncomingChatRequestEvent;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.UnknownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.Bot;
import com.nibl.bot.BotInterface;
import com.nibl.bot.service.dcc.DCCManager;

public class Listener extends BotInterface {

    private Logger log = LoggerFactory.getLogger(Listener.class);

    public static FileChannel blah;

    public Listener(Bot myBot) {
        super(myBot);
    }

    public Adapter getAdapter() {
        return new Adapter();
    }

    class Adapter extends ListenerAdapter {

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
            if (event.getMessage().toLowerCase().startsWith("xdcc")) {
                DCCManager.addRequest(event);
            }
        }

        @Override
        public void onIncomingChatRequest(IncomingChatRequestEvent event) {
            log.info("[CHAT] from {}", event.getUser().getNick());
        }

        @Override
        public void onIncomingFileTransfer(IncomingFileTransferEvent event) throws Exception {
//            try {
//                super.onIncomingFileTransfer(event);
//                Path path = Paths.get(myBot.getProperty("data_directory") + File.separator + event.getSafeFilename());
//                if (path.toFile().exists()) {
//                    BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
//                    log.info("Resume from position {} for file {}", attr.size(), event.getSafeFilename());
//                    event.acceptResume(path.toFile(), attr.size());
//                } else {
//                    log.info("Accept file {}", event.getSafeFilename());
//                    event.accept(path.toFile());
//                }
//            } catch (Exception e) {
//                log.error("Exception on incoming file", e);
//            }
        }

        @Override
        public void onUnknown(UnknownEvent event) throws Exception {
            super.onUnknown(event);
            log.error("Unknown Event {}", event.getCommand());
        }
    }

}
