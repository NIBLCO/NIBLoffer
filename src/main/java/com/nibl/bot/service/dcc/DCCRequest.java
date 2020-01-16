package com.nibl.bot.service.dcc;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.pircbotx.dcc.SendFileTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nibl.bot.Bot;
import com.nibl.bot.service.Service;
import com.nibl.bot.service.autoadd.BotFile;

public class DCCRequest extends Service {

    private Logger log = LoggerFactory.getLogger(DCCRequest.class);

    public enum Status {
        WAITING, QUEUED, TRANSFERRING, FINISHED, ERROR
    }

    private DCCUser user;
    private BotFile botFile;
    private SendFileTransfer dccTransfer;
    private Date submittedOn = new Date();
    private Status status = Status.WAITING;

    protected DCCRequest(Bot myBot, DCCUser user, BotFile botFile) {
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

    public SendFileTransfer getFileTransfer() {
        return dccTransfer;
    }

    public Date getSubmittedOn() {
        return submittedOn;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    private void messageUserStart() {
        this.getDCCUser().sendUserNotice(String.format("Sending you #%s [%s] %s", this.getBotFile().getId(),
                this.getBotFile().getFormattedSize(), this.getBotFile().getName()));
    }

    private void messageUserFinish() {
        this.getDCCUser()
                .sendUserNotice(String.format(
                        "Finished sending %s. Problems? Let us know: https://github.com/NIBLCO/NIBLoffer/issues",
                        this.getBotFile().getName()));
        // If credit line, send?
    }

    private void messageUserError() {
        this.getDCCUser().sendUserNotice(String.format(
                "Failed sending %s. This is new software, Jenga will review the logs.", this.getBotFile().getName()));
    }

    @Override
    public void executeTask() {

        try {
            this.getDCCUser().sentTransfer();
            messageUserStart();

            this.setStatus(Status.TRANSFERRING);
            dccTransfer = this.getDCCUser().getUser().send().dccFile(botFile.getPath().toFile());

            while (!dccTransfer.getFileTransferStatus().isFinished()) {
                TimeUnit.SECONDS.sleep(1);
            }

            this.setStatus(Status.FINISHED);
            messageUserFinish();

        } catch (Exception e) {
            log.error("Failed transferring {} to {}", botFile.getName(), this.getDCCUser().getUser().getNick(), e);
            messageUserError();
            this.setStatus(Status.ERROR);
        } finally {
            DCCManager.removeTransfer(this);
        }

    }

}
