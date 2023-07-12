package com.maaz.rewardhub;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class WithdrawRequest {
    private String userId;
    private String emailId;
    private String requestedBy;
    private long coins;

    // with this it will get firebase server time./ not system/device
    @ServerTimestamp
    private Date createdAt;

    public WithdrawRequest(){

    }

    public WithdrawRequest(String userId, String emailId, String requestedBy, long coins) {
        this.userId = userId;
        this.emailId = emailId;
        this.requestedBy = requestedBy;
        this.coins = coins;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }

    // date getter setter
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
