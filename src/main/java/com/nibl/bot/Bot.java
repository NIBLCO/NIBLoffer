package com.nibl.bot;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.service.ServiceManager;
import com.nibl.bot.service.autoadd.AutoaddService;
import com.nibl.bot.service.dcc.DCCServiceManager;

public class Bot {

	private Logger log = LoggerFactory.getLogger(Bot.class);

	private PircBotX pircBotX = new PircBotX(BotConfiguration.createConfiguration(this));;

	private ServiceManager serviceManager;

	public Bot() throws IOException, IrcException, InterruptedException, ExecutionException {
		initializeResources();
		pircBotX.startBot();
	}

	private void initializeResources() throws InterruptedException, ExecutionException {
		Integer maxTransfers = 10;
		this.setServiceManager(new ServiceManager(this, maxTransfers, 2));

		// Run Autoadd once before startup
		log.info("Running Autoadd service once before startup");
		this.getServiceManager().addService(new AutoaddService(this, true)).get();

		// Add scheduled AutoaddService every 60s
		this.getServiceManager().addScheduledService(new AutoaddService(this), 60, 60);

		this.getServiceManager().addScheduledService(new DCCServiceManager(this), 1, 1);

	}

	public PircBotX getPircBotX() {
		return pircBotX;
	}

	public void setPircBotX(PircBotX pircBotX) {
		this.pircBotX = pircBotX;
	}

	public ServiceManager getServiceManager() {
		return serviceManager;
	}

	public void setServiceManager(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	public static void main(String[] args) throws Exception {
		new Bot();
	}

}
