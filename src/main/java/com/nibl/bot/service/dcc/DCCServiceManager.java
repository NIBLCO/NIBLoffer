package com.nibl.bot.service.dcc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.Bot;
import com.nibl.bot.service.Service;
import com.nibl.bot.service.packlist.PacklistService;

public class DCCServiceManager extends Service {

	private static List<DCCUser> waitingQueue = new ArrayList<>();

	private static List<DCCTransfer> activeQueue = new ArrayList<>();

	private static ExecutorService executor = Executors.newCachedThreadPool();

	private static Logger log = LoggerFactory.getLogger(DCCServiceManager.class);

	public DCCServiceManager(Bot myBot) {
		super(myBot);
	}

	// TODO clean this method
	public static void addRequest(PrivateMessageEvent event) {
		// Get the existing DCCUser or create a new one
		DCCUser dccUser = waitingQueue.stream().filter(u -> u.getUser().equals(event.getUser())).findFirst()
				.orElse(new DCCUser(event.getUser()));

		List<Integer> alreadyQueuedPackNumbers = new ArrayList<>();
		// Convert the message into a list of packs
		List<Integer> packNumbers = convertInputToPackNumbers(event.getMessage());
		log.debug("Packs requested {}", packNumbers.stream().map(String::valueOf).collect(Collectors.joining(",")));

		// For each BotFile found, Add to the DCCUser if new
		PacklistService.getBotFilesByPackNumbers(packNumbers).forEach(botFile -> {
			if (!dccUser.getTransfers().stream().anyMatch(t -> t.getBotFile().equals(botFile))) {
				log.debug("Add new transfer for pack{}", botFile.getId());
				dccUser.addTransfer(new DCCTransfer(myBot, dccUser, botFile));
			} else {
				alreadyQueuedPackNumbers.add(botFile.getId());
			}
			// Remove from requested packs so we can output a single "unknown" message
			packNumbers.remove(botFile.getId());
		});

		// Output already queued message
		if (alreadyQueuedPackNumbers.size() > 0) {
			dccUser.getUser().send()
					.message("You are already queued for pack" + ((alreadyQueuedPackNumbers.size() > 1) ? "s" : "")
							+ " "
							+ alreadyQueuedPackNumbers.stream().map(String::valueOf).collect(Collectors.joining(",")));
		}

		// Output unknown packs message
		if (packNumbers.size() > 0) {
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

	public static void addToActiveQueue(DCCTransfer transfer) {
		activeQueue.add(transfer);
	}

	public static void removeFromActiveQueue(DCCTransfer transfer) {
//		Optional<DCCTransfer> activeTransferOpt = activeQueue.stream()
//				.filter(tr -> tr.getBotFile().getId() == transfer.getBotFile().getId() && tr.getDCCUser().getUser()
//						.getNick().equalsIgnoreCase(transfer.getDCCUser().getUser().getNick()))
//				.findFirst();
//		if (activeTransferOpt.isPresent()) {
//			log.debug("Transfer not found in active queue! pack# {} for user {}", transfer.getBotFile().getId(),
//					transfer.getDCCUser().getUser().getNick());
//			return;
//		}
//
//		DCCTransfer activeTransfer = activeTransferOpt.get();
		if (activeQueue.contains(transfer)) {
			activeQueue.remove(transfer);
			log.debug("Removing pack# {} for user {} from active queue", transfer.getBotFile().getId(),
					transfer.getDCCUser().getUser().getNick());
		} else {
			log.debug("Unknown pack# {} for user {} in active queue", transfer.getBotFile().getId(),
					transfer.getDCCUser().getUser().getNick());
		}

	}

	// TODO simplify & clarify this method
	private static List<Integer> convertInputToPackNumbers(String message) {
		List<Integer> dls = new ArrayList<>();
		String packs = message.replace("#", "").replace("xdcc batch", "").replace("xdcc send", "");
		String[] second = packs.replaceAll(" ", "").split(",");
		for (String third : second) {
			if (third.equals("-1")) {
				dls.add(Integer.parseInt(third));
			} else {
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
		return dls;
	}

	private static DCCTransfer getNextForQueue() {

		// Get the oldest user who does not have 2 transfers running
		Optional<DCCUser> dccUserOpt = waitingQueue.stream()
				.filter(wq -> activeQueue.stream().filter(aq -> aq.getDCCUser().equals(wq)).count() < 2)
				.min(Comparator.comparing(DCCUser::getLastTransfer));
		if (!dccUserOpt.isPresent()) {
			throw new NoSuchElementException();
		}

		DCCUser dccUser = dccUserOpt.get();
		return dccUser.getTransfers().stream().filter(t -> !t.getIsTransferring())
				.min(Comparator.comparing(DCCTransfer::getSubmittedOn)).get();
	}

	@Override
	public void executeTask() {
		log.debug("Entering DCCService Manager");

		if (activeQueue.size() < 3) {
			try {
				DCCTransfer transfer = DCCServiceManager.getNextForQueue();
				log.debug("Submitting transfer for pack# {} to user {}", transfer.getBotFile().getId(),
						transfer.getDCCUser().getUser().getNick());
				executor.submit(transfer);
			} catch (NoSuchElementException e) {
				// Nothing to do for queue
			}
		} else {
			log.debug("Active queue >= 10");
		}
	}

}
