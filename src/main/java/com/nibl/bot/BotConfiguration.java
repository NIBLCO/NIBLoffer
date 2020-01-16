package com.nibl.bot;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.delay.AdaptingDelay;
import org.pircbotx.delay.StaticDelay;

import com.nibl.bot.listener.Listener;

public class BotConfiguration {

    private BotConfiguration() {

    }

    public static Configuration createConfiguration(Bot myBot) throws UnknownHostException {

        Builder cb = new Builder();
        cb.setShutdownHookEnabled(true);
        cb.addListener(new Listener(myBot).getAdapter());
        cb.setMessageDelay(new AdaptingDelay(100, 500));

        cb.setName(myBot.getProperty("name"));
        cb.setLogin(myBot.getProperty("login", "GomuGomu"));
        cb.setVersion(myBot.getProperty("version", "1"));

        for (String channel : myBot.getProperty("channels", "#nibl").split(",")) {
            String[] keyedChannel = channel.split(":");
            if (keyedChannel.length > 1) {
                cb.addAutoJoinChannel(keyedChannel[0].trim(), keyedChannel[1].trim());
            } else {
                cb.addAutoJoinChannel(keyedChannel[0].trim());
            }
        }

        if (Boolean.valueOf(myBot.getProperty("use_ssl", "true"))) {
            cb.setSocketFactory(SSLSocketFactory.getDefault());
        }

        String nsPass = myBot.getProperty("nickserv_password");
        if (null != nsPass && !nsPass.isEmpty()) {
            cb.setNickservPassword(nsPass);
        }

        String[] server = myBot.getProperty("server", "irc.rizon.net:9999").split(":");
        cb.addServer(server[0], Integer.parseInt(server[1]));

        cb.setAutoNickChange(Boolean.valueOf(myBot.getProperty("auto_nick_change", "true")));
        cb.setAutoReconnect(Boolean.valueOf(myBot.getProperty("auto_reconnect", "true")));
        cb.setAutoReconnectDelay(new StaticDelay(Integer.valueOf(myBot.getProperty("auto_reconnect_delay", "1000"))));
        cb.setAutoReconnectAttempts(Integer.valueOf(myBot.getProperty("auto_reconnect_attempts", "3")));

        String portsConfig = myBot.getProperty("dcc_ports");
        if (portsConfig != null && !portsConfig.isEmpty()) {
            List<Integer> dccPorts = new ArrayList<>();
            for (String port : portsConfig.split(",")) {
                dccPorts.add(Integer.valueOf(port));
            }
            if (!dccPorts.isEmpty()) {
                cb.setDccPorts(dccPorts);
            }
        }

        String inetAddress = myBot.getProperty("public_address");
        if (inetAddress != null && !inetAddress.isEmpty()) {
            cb.setDccPublicAddress(InetAddress.getByName(inetAddress));
        }

        return cb.buildConfiguration();
    }

}
