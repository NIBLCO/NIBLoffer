package com.nibl.bot.service.autoadd;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BotDirectory {

    @JsonIgnore
    Path path;
    @JsonIgnore
    BasicFileAttributes attributes;

    String absolutePath;
    Map<String, BotDirectory> subDirectories = new HashMap<>();
    Map<String, BotFile> files = new HashMap<>();

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public BasicFileAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(BasicFileAttributes attributes) {
        this.attributes = attributes;
    }

    public Map<String, BotDirectory> getSubDirectories() {
        return subDirectories;
    }

    public void setSubDirectories(Map<String, BotDirectory> subDirectories) {
        this.subDirectories = subDirectories;
    }

    public Map<String, BotFile> getFiles() {
        return files;
    }

    public void setFiles(Map<String, BotFile> files) {
        this.files = files;
        for (String file : this.files.keySet()) {
            this.files.get(file).setDirectory(this);
        }
    }

}
