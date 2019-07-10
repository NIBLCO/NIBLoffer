package com.nibl.bot.service.autoadd;

import java.util.ArrayList;
import java.util.List;

public class BotPacklist {

    private List<BotFile> files = new ArrayList<>();

    public List<BotFile> getFiles() {
        return files;
    }

    public void setFiles(List<BotFile> files) {
        this.files = files;
    }

}
