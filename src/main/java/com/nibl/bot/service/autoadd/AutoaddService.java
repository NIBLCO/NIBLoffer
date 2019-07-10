package com.nibl.bot.service.autoadd;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibl.bot.Bot;
import com.nibl.bot.service.Service;
import com.nibl.bot.service.packlist.PacklistService;

public class AutoaddService extends Service {

    private static Logger log = LoggerFactory.getLogger(AutoaddService.class);
    private static Boolean verbose = false;
    private static Path rootScanPath = Paths.get("/mnt/unionfs");
    // private static Path rootScanPath = Paths.get("D:\\Downlewds");
    private static Path stateFile = Paths.get("directoryState.json");
    private static ObjectMapper mapper = new ObjectMapper();
    private static Integer fileCounter = 0;
    private static BotDirectory directoryState = null;

    private static DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return (!entry.getName(entry.getNameCount() - 1).getFileName().toString().startsWith("."));
        }
    };

    public AutoaddService(Bot myBot) {
        super(myBot);
        AutoaddService.verbose = false;
    }

    public AutoaddService(Bot myBot, Boolean verbose) {
        super(myBot);
        AutoaddService.verbose = verbose;
    }

    public static void autoAddScan(Path path) throws IOException {
        if (null == directoryState) {
            try {
                log.debug("Load State from file");
                directoryState = mapper.readValue(new String(Files.readAllBytes(stateFile)), BotDirectory.class);
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

        log.debug("Start Scan of {}", directoryState.getAbsolutePath());

        scanDirectory(directoryState);

        PacklistService.makePacklist(directoryState);

        Files.write(stateFile, mapper.writeValueAsString(directoryState).getBytes());

    }

    private static BotDirectory scanDirectory(BotDirectory directory) throws IOException {
        if (AutoaddService.verbose) {
            log.debug("Scan {}", directory.getAbsolutePath());
        }

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
        } catch (IOException e) {
            log.error("Exception while scanning directory {}", directory.getAbsolutePath());
        }

        return directory;
    }

    private static BotDirectory getBotDirectory(BotDirectory directory, Path p, BasicFileAttributes attr) {

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

    private static BotFile getBotFile(BotDirectory directory, Path p, BasicFileAttributes attr) {
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

    private static void removeStaleDirectoryEntries(BotDirectory directory, List<String> directories) {
        Iterator<String> memoryDirIter = directory.getSubDirectories().keySet().iterator();
        while (memoryDirIter.hasNext()) {
            String memoryDir = memoryDirIter.next();
            if (!directories.contains(memoryDir)) {
                log.debug("Remove Directory: {}", directory.getSubDirectories().get(memoryDir).getAbsolutePath());
                memoryDirIter.remove();
            }
        }
    }

    private static void removeStaleFileEntries(BotDirectory directory, List<String> files) {
        Iterator<String> memoryFileIter = directory.getFiles().keySet().iterator();
        while (memoryFileIter.hasNext()) {
            String memoryFile = memoryFileIter.next();
            if (!files.contains(memoryFile)) {
                log.debug("Remove File: {}/{}", directory.getAbsolutePath(), memoryFile);
                memoryFileIter.remove();
            }
        }
    }

    private static String runtime(long millis) {
        return String.format("%02d:%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
                TimeUnit.MILLISECONDS.toMillis(millis)
                        - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millis)));
    }

    @Override
    public void executeTask() {
        try {
            long startTime = System.currentTimeMillis();
            autoAddScan(rootScanPath);
            long endTime = System.currentTimeMillis();
            String totalTime = runtime(endTime - startTime);
            log.debug("Iteration ran for {}", totalTime);
        } catch (Exception e) {
            log.error("Exception in Autoadd", e);
        }
    }

}
