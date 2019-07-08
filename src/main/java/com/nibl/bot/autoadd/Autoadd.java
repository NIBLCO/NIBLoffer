package com.nibl.bot.autoadd;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibl.bot.model.BotDirectory;
import com.nibl.bot.model.BotFile;
import com.nibl.bot.model.BotPacklist;

public class Autoadd implements Runnable {

    private Logger log = LoggerFactory.getLogger(Autoadd.class);

    private static Boolean isRunning = true;
    ObjectMapper mapper = new ObjectMapper();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
    Integer fileCounter = 0;
    BotDirectory directoryState = null;
    BotPacklist packlist = new BotPacklist();

    public static Boolean getIsRunning() {
        return isRunning;
    }

    public static void setIsRunning(Boolean isRunning) {
        Autoadd.isRunning = isRunning;
    }

    public BotPacklist getPacklist() {
        return packlist;
    }

    public void autoAddScan(Path path) throws IOException {
        if (null == directoryState) {
            try {
                log.debug("Load State from file");
                directoryState = mapper.readValue(new String(Files.readAllBytes(Paths.get("directoryState.json"))),
                        BotDirectory.class);
            } catch (NoSuchFileException e) {
                log.debug("No statefile found");
                directoryState = new BotDirectory();
            }
        }

        // Update top level
        directoryState.setAbsolutePath(path.toString());
        directoryState.setPath(path);
        directoryState.setAttributes(Files.readAttributes(path, BasicFileAttributes.class));
        fileCounter = 1;

        scanDirectory(directoryState);

        Files.write(Paths.get("directoryState.json"), mapper.writeValueAsString(directoryState).getBytes());

        makePacklist(directoryState);

        packlist.setFiles(packlist.getFiles().stream().sorted(Comparator.comparingInt(BotFile::getId))
                .collect(Collectors.toList()));

    }

    private void makePacklist(BotDirectory directory) {
        for (BotDirectory file : directory.getSubDirectories().values()) {
            makePacklist(file);
        }
        for (BotFile file : directory.getFiles().values()) {
            packlist.getFiles().add(file);
        }
    }

    private BotDirectory scanDirectory(BotDirectory directory) throws IOException {
        DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return (!entry.getName(entry.getNameCount() - 1).getFileName().toString().startsWith("."));
            }
        };

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory.getPath(), filter)) {

            List<String> directories = new ArrayList<>();
            List<String> files = new ArrayList<>();

            for (Path p : stream) {
                String name = p.getFileName().toString();
                BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);

                if (attr.isDirectory()) {

                    BotDirectory subDirectory = getBotDirectory(directory, p, attr);
                    directory.getSubDirectories().put(name, subDirectory);
                    directories.add(name);
                    scanDirectory(subDirectory);

                } else if (attr.isRegularFile()) {

                    BotFile file = getBotFile(directory, p, attr);
                    directory.getFiles().put(name, file);
                    files.add(file.getName());

                } else {
                    log.error("Unknown file type: {}", p.getFileName());
                }
            }
            removeStaleDirectoryEntries(directory, directories);
            removeStaleFileEntries(directory, files);
        }

        return directory;
    }

    private BotDirectory getBotDirectory(BotDirectory directory, Path p, BasicFileAttributes attr) {

        String directoryName = p.getFileName().toString();
        BotDirectory subDirectory = new BotDirectory();
        subDirectory.setAbsolutePath(p.toString());

        if (directory.getSubDirectories().containsKey(directoryName)) {
            subDirectory = directory.getSubDirectories().get(directoryName);
        } else {
            log.debug("Add new directory: {}", subDirectory.getAbsolutePath());
        }

        subDirectory.setPath(p);
        subDirectory.setAttributes(attr);

        return subDirectory;
    }

    private BotFile getBotFile(BotDirectory directory, Path p, BasicFileAttributes attr) {
        String filename = p.getFileName().toString();
        BotFile file = new BotFile();

        if (directory.getFiles().containsKey(filename)) {
            file = directory.getFiles().get(filename);
        } else {
            file.setName(filename);
            log.debug("Add new file: {}", directory.getAbsolutePath() + "/" + file.getName());
        }

        file.setId(fileCounter++);
        file.setDirectory(directory);
        file.setPath(p);
        file.setAttributes(attr);

        return file;
    }

    private void removeStaleDirectoryEntries(BotDirectory directory, List<String> directories) {
        Iterator<String> memoryDirIter = directory.getSubDirectories().keySet().iterator();
        while (memoryDirIter.hasNext()) {
            String memoryDir = memoryDirIter.next();
            if (!directories.contains(memoryDir)) {
                log.debug("Remove Directory: {}", directory.getSubDirectories().get(memoryDir).getAbsolutePath());
                memoryDirIter.remove();
            }
        }
    }

    private void removeStaleFileEntries(BotDirectory directory, List<String> files) {
        Iterator<String> memoryFileIter = directory.getFiles().keySet().iterator();
        while (memoryFileIter.hasNext()) {
            String memoryFile = memoryFileIter.next();
            if (!files.contains(memoryFile)) {
                log.debug("Remove File: {}/{}", directory.getAbsolutePath(), memoryFile);
                memoryFileIter.remove();
            }
        }
    }

    public void run() {

        while (Autoadd.isRunning) {
            try {
                long startTime = System.currentTimeMillis();

                autoAddScan(Paths.get("/mnt/unionfs"));
                long endTime = System.currentTimeMillis();

                long millis = (endTime - startTime);
                String hms = String.format("%02d:%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis)
                                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                        TimeUnit.MILLISECONDS.toSeconds(millis)
                                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
                        TimeUnit.MILLISECONDS.toMillis(millis)
                                - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millis)));

                log.debug("Iteration ran for {}", hms);
                log.debug("Sleep 60s");
                Thread.sleep(60000);
            } catch (Exception e) {
                log.error("Exception in Autoadd", e);
                Autoadd.setIsRunning(false);
            }
        }

    }

    // public static void main(String args[]) throws IOException, ParseException, InterruptedException {
    //
    // Autoadd autoAdd = new Autoadd();
    //
    // while (true) {
    // long startTime = System.currentTimeMillis();
    //
    // autoAdd.autoAddScan(Paths.get("/home/rob/Documents/repos/nibl/NIBLoffer/data/tv/Futurama"));
    // long endTime = System.currentTimeMillis();
    //
    // long millis = (endTime - startTime);
    // String hms = String.format("%02d:%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
    // TimeUnit.MILLISECONDS.toMinutes(millis)
    // - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
    // TimeUnit.MILLISECONDS.toSeconds(millis)
    // - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
    // TimeUnit.MILLISECONDS.toMillis(millis)
    // - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millis)));
    //
    // System.out.println("Iteration ran for " + hms);
    // System.out.println("Sleep 60s");
    // Thread.sleep(60000);
    //
    // }
    // }

}
