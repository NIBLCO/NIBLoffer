package com.nibl.bot.service.dcc;

import java.io.IOException;
import java.util.Date;

import org.pircbotx.User;
import org.pircbotx.exception.DccException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.Bot;
import com.nibl.bot.model.BotFile;
import com.nibl.bot.service.Service;

public class DCCTransfer extends Service {

    private Logger log = LoggerFactory.getLogger(DCCTransfer.class);

    private User user;
    private BotFile botFile;
    private Date submittedOn;

    protected DCCTransfer(Bot myBot, User user, BotFile botFile) {
        super(myBot);
        this.user = user;
        this.botFile = botFile;
    }

    public User getUser() {
        return user;
    }

    public BotFile getBotFile() {
        return botFile;
    }

    public Date getSubmittedOn() {
        return submittedOn;
    }

    @Override
    public void executeTask() {
        try {
            user.send().dccFileAndTransfer(botFile.getPath().toFile());
        } catch (DccException | IOException | InterruptedException e) {
            user.send().message("Sorry, I failed transferring " + botFile.getName() + "! Err: " + e.getMessage());
            log.error("Failed transferring {} to {}", botFile.getName(), user.getNick(), e);
        }
    }

}
