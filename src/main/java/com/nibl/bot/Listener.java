package com.nibl.bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.pircbotx.exception.DccException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.model.BotFile;

public class Listener extends ListenerAdapter {

	private Logger log = LoggerFactory.getLogger(Listener.class);

	private Bot myBot;

	public Listener(Bot myBot) {
		this.myBot = myBot;
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

	// TODO fix all of this
	@Override
	public void onPrivateMessage(PrivateMessageEvent event) {
		log.info("[PMESSAGE] {}: {}", event.getUser().getNick(), event.getMessage());
		List<Integer> dls = new ArrayList<Integer>();
		if (event.getMessage().toLowerCase().startsWith("xdcc send")) {
			dls.add(Integer.parseInt(event.getMessage().replace("#", "").replace("xdcc send", "").trim()));
		} else if (event.getMessage().toLowerCase().startsWith("xdcc batch")) {
			String packs = event.getMessage().replace("#", "").replace("xdcc batch", "");
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
		}
		for (Integer packNumber : dls) {
			log.info("Sending pack#{}", packNumber);

			BotFile file = myBot.getPacklistService().getBotFileByPackNumber(packNumber);

			if (null == file) {
				event.getUser().send().message("I don't have pack #" + packNumber);
				return;
			}

			try {
				event.getUser().send().dccFileAndTransfer(file.getPath().toFile());
			} catch (DccException | IOException | InterruptedException e) {
				event.getUser().send()
						.message("Sorry, I failed transferring " + file.getName() + "! Err: " + e.getMessage());
				log.error("Failed transferring {} to {}", file.getName(), event.getUser().getNick(), e);
			}
		}

	}

	@Override
	public void onIncomingChatRequest(IncomingChatRequestEvent event) {
		log.info("[CHAT] from {}", event.getUser().getNick());
	}

}
