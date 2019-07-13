package com.nibl.bot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.service.ServiceManager;
import com.nibl.bot.service.autoadd.AutoaddService;
import com.nibl.bot.service.dcc.DCCActiveQueue;
import com.nibl.bot.service.dcc.DCCManager;

public class Bot {

	private Logger log = LoggerFactory.getLogger(Bot.class);

	private PircBotX pircBotX;

	private Properties properties;
	private ServiceManager serviceManager;

	public Bot(String configFile) throws IOException, IrcException, InterruptedException, ExecutionException {
		initializeResources(configFile);
		pircBotX.startBot();
	}

	private void initializeResources(String configFile)
			throws InterruptedException, ExecutionException, FileNotFoundException, IOException {
		Integer maxTransfers = 10;

		properties = new Properties();

		try {
			properties.load(new FileInputStream(new File(configFile)));
		} catch (FileNotFoundException e) {
			log.error("Config file not found.");
			throw e;
		}

		pircBotX = new PircBotX(BotConfiguration.createConfiguration(this));

		this.setServiceManager(new ServiceManager(this, maxTransfers, 10));

		// Run Autoadd once before startup
		log.info("Running Autoadd service once before startup");
		this.getServiceManager().addService(new AutoaddService(this, true)).get();

		// Add scheduled AutoaddService every 60s
		this.getServiceManager().addScheduledService(new AutoaddService(this), 60, 60);

		this.getServiceManager().addScheduledService(new DCCManager(this), 1, 1);

		this.getServiceManager().addScheduledService(new DCCActiveQueue(this), 1, 1);

	}

	public String getProperty(String key) {
		return this.properties.getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return this.properties.getProperty(key, defaultValue);
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
		if (args.length == 0) {
			System.out.println("need a path to config");
			System.exit(2);
		}
		new Bot(args[0]);
	}

}
