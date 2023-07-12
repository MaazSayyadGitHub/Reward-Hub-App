package com.maaz.rewardhub;

public class User {
    private String userName;
    private long coins = 0;

    public User(){

    }

    public User(String userName){
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }
}
