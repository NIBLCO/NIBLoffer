package com.nibl.bot.service;

import com.nibl.bot.Bot;
import com.nibl.bot.BotInterface;

public abstract class Service extends BotInterface implements Runnable {

    protected Service(Bot myBot) {
        super(myBot);
    }

    @Override
    public void run() {
        executeTask();
    }

    public abstract void executeTask();

}
