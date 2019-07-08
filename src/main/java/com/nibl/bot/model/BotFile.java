package com.nibl.bot.model;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BotFile {

    @JsonIgnore
    BotDirectory directory;
    @JsonIgnore
    Path path;
    @JsonIgnore
    BasicFileAttributes attributes;

    Integer id;
    String name;
    Integer gets = 0;

    public BotDirectory getDirectory() {
        return directory;
    }

    public void setDirectory(BotDirectory directory) {
        this.directory = directory;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Integer getGets() {
        return gets;
    }

    public void setGets(Integer gets) {
        this.gets = gets;
    }

}
