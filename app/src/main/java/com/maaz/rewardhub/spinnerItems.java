package com.maaz.rewardhub;

public class spinnerItems {
    private String amount;
    private int image;

    public spinnerItems(String amount, int image) {
        this.amount = amount;
        this.image = image;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}

