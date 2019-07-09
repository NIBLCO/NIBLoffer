package com.nibl.bot;

public abstract class BotInterface {

    protected static Bot myBot = null;

    protected BotInterface(Bot myBot) {
        BotInterface.myBot = myBot;
    }

}
