package com.nibl.bot.service.dcc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pircbotx.User;

public class DCCUser {

    private User user;
    private Date lastTransfer = new Date();
    private List<DCCRequest> transfers = new ArrayList<>();

    public DCCUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Date getLastTransfer() {
        return lastTransfer;
    }

    public void sentTransfer() {
        this.lastTransfer = new Date();
    }

    public void addTransfer(DCCRequest transfer) {
        transfers.add(transfer);
    }

    public List<DCCRequest> getTransfers() {
        return transfers;
    }

    public void sendUserMessage(String message) {
        this.getUser().send().message(message);
    }

    public void sendUserNotice(String message) {
        this.getUser().send().notice(message);
    }

}
