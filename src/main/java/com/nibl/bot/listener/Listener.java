package com.nibl.bot.listener;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nibl.bot.Bot;
import com.nibl.bot.BotInterface;
import com.nibl.bot.service.dcc.DCCManager;

public class Listener extends BotInterface {

    private Logger log = LoggerFactory.getLogger(Listener.class);

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
    }

}
