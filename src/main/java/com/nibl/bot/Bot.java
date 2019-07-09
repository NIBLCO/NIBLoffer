package com.nibl.bot;

import java.io.IOException;
import javax.net.ssl.SSLSocketFactory;

import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.autoadd.AutoaddService;
import com.nibl.bot.packlist.PacklistService;

public class Bot {

	private Logger log = LoggerFactory.getLogger(Bot.class);

	private PircBotX pircBotX;

	// TODO change these to factory method
	private AutoaddService autoaddService = new AutoaddService(this);
	private PacklistService packlistService = new PacklistService(this);

	private void autoAddService() {
		new Thread(autoaddService).start();
	}

	public AutoaddService getAutoaddService() {
		return autoaddService;
	}

	public PacklistService getPacklistService() {
		return packlistService;
	}

	public Bot() throws IOException, IrcException {
		autoAddService();

		pircBotX = new PircBotX(this.createConfiguration());
		pircBotX.startBot();
	}

	public static void main(String[] args) {
		try {
			new Bot();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Configuration createConfiguration() {

		Builder cb = new Configuration.Builder();
		cb.setName("FileBoyz");
		cb.addAutoJoinChannel("#frog");
		cb.setVersion("GomuGomu");
		cb.setFinger("Watch where you are poking");
		cb.setLogin("FileBoy");
		cb.addServer("irc.rizon.net", 9999);
		cb.addListener(new Listener(this));
		cb.setMessageDelay(500);
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
//        try {
//            cb.setDccPublicAddress(InetAddress.getByName("116.203.134.128"));
//        } catch (UnknownHostException e) {
//            log.error("Unable to set DCC Public IP Address!!", e);
//        }

		return cb.buildConfiguration();
	}

	public PircBotX getBot() {
		return this.pircBotX;
	}

	public PircBotX getPircBotX() {
		return pircBotX;
	}

	public void setPircBotX(PircBotX pircBotX) {
		this.pircBotX = pircBotX;
	}

}
