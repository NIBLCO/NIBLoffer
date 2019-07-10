package com.nibl.bot.service.packlist;

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
import com.nibl.bot.service.Service;

public class PacklistService extends Service {

	private static Logger log = LoggerFactory.getLogger(PacklistService.class);

	private static BotPacklist packlist = new BotPacklist();
//	private static BotFile packlistFile = new BotFile();

	public PacklistService(Bot myBot) {
		super(myBot);
	}

	public static BotPacklist getPacklist() {
		return packlist;
	}

	public static void makePacklist(BotDirectory directory) {
		packlist.setFiles(orderPacklist(recurseDirectory(directory, new BotPacklist())));
		try {
			outputPacklist();
			BotFile file = new BotFile();
			file.setName(myBot.getPircBotX().getNick() + ".txt");
			file.setPath(Paths.get(file.getName()));
			file.setId(-1);
			packlist.getFiles().add(file);
		} catch (IOException e) {
			log.error("Failed writing packlist to disk!", e);
		}
	}

	private static BotPacklist recurseDirectory(BotDirectory directory, BotPacklist packlist) {
		for (BotDirectory file : directory.getSubDirectories().values()) {
			recurseDirectory(file, packlist);
		}
		for (BotFile file : directory.getFiles().values()) {
			packlist.getFiles().add(file);
		}
		return packlist;
	}

	private static List<BotFile> orderPacklist(BotPacklist packlist) {
		return packlist.getFiles().stream().sorted(Comparator.comparingInt(BotFile::getId))
				.collect(Collectors.toList());
	}

	public static List<BotFile> getBotFilesByPackNumbers(List<Integer> packNumbers) {
		return packlist.getFiles().stream().filter(f -> packNumbers.contains(f.getId())).collect(Collectors.toList());
	}

	private static void outputPacklist() throws IOException {
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

	@Override
	public void executeTask() {

	}

}
