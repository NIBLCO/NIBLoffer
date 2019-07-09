package com.nibl.bot.service.dcc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pircbotx.User;

public class DCCUser {

    private User user;
    private Date lastTransfer = new Date();
    private List<DCCTransfer> transfers = new ArrayList<>();

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

    public void addTransfer(DCCTransfer transfer) {
        transfers.add(transfer);
    }

    public List<DCCTransfer> getTransfers() {
        return transfers;
    }

}
