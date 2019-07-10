package com.nibl.bot.service.dcc;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.Bot;
import com.nibl.bot.model.BotFile;
import com.nibl.bot.service.Service;

public class DCCTransfer extends Service {

	private Logger log = LoggerFactory.getLogger(DCCTransfer.class);

	private DCCUser user;
	private BotFile botFile;
	private Boolean isTransferring = false; // T ODO change to ENUM with failure, etc..
	private Date submittedOn = new Date();

	protected DCCTransfer(Bot myBot, DCCUser user, BotFile botFile) {
		super(myBot);
		this.user = user;
		this.botFile = botFile;
	}

	public DCCUser getDCCUser() {
		return user;
	}

	public BotFile getBotFile() {
		return botFile;
	}

	public Date getSubmittedOn() {
		return submittedOn;
	}

	public Boolean getIsTransferring() {
		return isTransferring;
	}

	public void setIsTransferring(Boolean isTransferring) {
		this.isTransferring = isTransferring;
	}

	@Override
	public void executeTask() {
		this.getDCCUser().sentTransfer();
		this.setIsTransferring(true);
		DCCServiceManager.addToActiveQueue(this);
		Integer sleepDuration = (new Random()).nextInt(15 - 5) + 5;
		this.getDCCUser().getUser().send().message("Simulating sending pack# " + this.getBotFile().getId() + " "
				+ this.getBotFile().getName() + ". Sleep " + sleepDuration);
		try {
			TimeUnit.SECONDS.sleep(sleepDuration);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.getDCCUser().getUser().send().message("Finished sending pack# " + this.getBotFile().getId());
		this.getDCCUser().getTransfers().remove(this);
		DCCServiceManager.removeFromActiveQueue(this);
//		try {
//			this.getDCCUser().getUser().send().dccFileAndTransfer(botFile.getPath().toFile());
//		} catch (DccException | IOException | InterruptedException e) {
//			this.getDCCUser().getUser().send()
//					.message("Sorry, I failed transferring " + botFile.getName() + "! Err: " + e.getMessage());
//			log.error("Failed transferring {} to {}", botFile.getName(), this.getDCCUser().getUser().getNick(), e);
//		}
	}

}
