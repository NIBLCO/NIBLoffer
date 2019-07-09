package com.nibl.bot;

import javax.net.ssl.SSLSocketFactory;

import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.delay.AdaptingDelay;

import com.nibl.bot.listener.Listener;

public class BotConfiguration {

    private BotConfiguration() {

    }

    public static Configuration createConfiguration(Bot myBot) {

        Builder cb = new Builder();
        cb.setName("FileBoyz");
        cb.addAutoJoinChannel("#frog");
        cb.setVersion("GomuGomu");
        cb.setFinger("Watch where you are poking");
        cb.setLogin("FileBoy");
        cb.addServer("irc.rizon.net", 9999);
        cb.addListener(new Listener(myBot).getAdapter());

        cb.setMessageDelay(new AdaptingDelay(100, 500));
        cb.setAutoNickChange(true);
        cb.setAutoReconnect(true);
        cb.setSocketFactory(SSLSocketFactory.getDefault());
        // List<Integer> dccPorts = new ArrayList<>();
        // dccPorts.add(47470);
        // dccPorts.add(47471);
        // dccPorts.add(47472);
        // dccPorts.add(47473);
        // cb.setDccPorts(dccPorts);
        cb.setDccTransferBufferSize(1024);
        // try {
        // cb.setDccPublicAddress(InetAddress.getByName("116.203.134.128"));
        // } catch (UnknownHostException e) {
        // log.error("Unable to set DCC Public IP Address!!", e);
        // }

        return cb.buildConfiguration();
    }

}
