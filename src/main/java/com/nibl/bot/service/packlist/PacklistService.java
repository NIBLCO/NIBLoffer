package com.nibl.bot.service.packlist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.Bot;
import com.nibl.bot.service.Service;
import com.nibl.bot.service.autoadd.BotDirectory;
import com.nibl.bot.service.autoadd.BotFile;
import com.nibl.bot.service.autoadd.BotPacklist;
import com.nibl.bot.util.BotUtility;

public class PacklistService extends Service {

    private static Logger log = LoggerFactory.getLogger(PacklistService.class);

    private static BotPacklist packlist = new BotPacklist();

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
            packlist.getFiles().add(makePacklistfile());
        } catch (IOException e) {
            log.error("Failed writing packlist to disk!", e);
        }
    }

    private static BotFile makePacklistfile() throws IOException {
        Path path = Paths.get(myBot.getPircBotX().getNick() + ".txt");
        BotFile file = new BotFile();
        file.setName(path.getFileName().toString());
        file.setPath(Paths.get(file.getName()));
        file.setId(-1);
        file.setDirectory(null);
        file.setPath(path);
        file.setAttributes(Files.readAttributes(path, BasicFileAttributes.class));
        return file;
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
            totalOffered += file.getAttributes().size();
            output.append("#" + file.getId() + "  " + file.getGets() + "x [" + file.getFormattedSize() + "] "
                    + file.getName());
            output.append("\n");
        }
        output.append("Total Offered: " + BotUtility.bytesToHuman(totalOffered));
        output.append("\n");
        Files.write(Paths.get(myBot.getPircBotX().getNick() + ".txt"), output.toString().getBytes());
    }

    @Override
    public void executeTask() {
        // Put an HTTP server here for the packlist
    }

}
