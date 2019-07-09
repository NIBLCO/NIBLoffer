package com.nibl.bot.packlist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.Bot;
import com.nibl.bot.model.BotDirectory;
import com.nibl.bot.model.BotFile;
import com.nibl.bot.model.BotPacklist;

public class PacklistService {

	private Logger log = LoggerFactory.getLogger(PacklistService.class);

	private Bot myBot;
	private static BotPacklist packlist = new BotPacklist();
	private static BotFile packlistFile = new BotFile();

	public PacklistService(Bot myBot) {
		this.myBot = myBot;
	}

	public BotPacklist getPacklist() {
		return packlist;
	}

	public void makePacklist(BotDirectory directory) {
		packlist.setFiles(orderPacklist(recurseDirectory(directory, new BotPacklist())));
		try {
			outputPacklist();
			BotFile file = new BotFile();
			file.setPath(Paths.get(myBot.getPircBotX().getNick() + ".txt"));
			packlistFile = file;
		} catch (IOException e) {
			log.error("Failed writing packlist to disk!", e);
		}
	}

	private BotPacklist recurseDirectory(BotDirectory directory, BotPacklist packlist) {
		for (BotDirectory file : directory.getSubDirectories().values()) {
			recurseDirectory(file, packlist);
		}
		for (BotFile file : directory.getFiles().values()) {
			packlist.getFiles().add(file);
		}
		return packlist;
	}

	private List<BotFile> orderPacklist(BotPacklist packlist) {
		return packlist.getFiles().stream().sorted(Comparator.comparingInt(BotFile::getId))
				.collect(Collectors.toList());
	}

	public BotFile getBotFileByPackNumber(Integer packNumber) {
		if (packNumber == -1) {
			return packlistFile;
		}
		if (packlist.getFiles().size() <= packNumber) {
			return packlist.getFiles().get(packNumber);
		} else {
			return null;
		}
	}

	private void outputPacklist() throws IOException {
		long totalOffered = 0;
		StringBuilder output = new StringBuilder();
		output.append("** To request a file, type \"/MSG " + myBot.getPircBotX().getNick() + " XDCC SEND x\" **");
		output.append("\n");

		for (BotFile file : packlist.getFiles()) {
			long size = file.getAttributes().size();
			totalOffered += size;
			output.append("#" + file.getId() + "  " + file.getGets() + "x [" + FileUtils.byteCountToDisplaySize(size)
					+ "] " + file.getName());
			output.append("\n");
		}
		output.append("Total Offered: " + FileUtils.byteCountToDisplaySize(totalOffered));
		Files.write(Paths.get(myBot.getPircBotX().getNick() + ".txt"), output.toString().getBytes());
	}

}
