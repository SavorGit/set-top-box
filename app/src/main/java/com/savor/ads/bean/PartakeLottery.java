package com.savor.ads.bean;

import java.io.Serializable;

public class PartakeLottery implements Serializable {

    private String dish_name;
    private String dish_image;

    public String getDish_name() {
        return dish_name;
    }

    public void setDish_name(String dish_name) {
        this.dish_name = dish_name;
    }

    public String getDish_image() {
        return dish_image;
    }

    public void setDish_image(String dish_image) {
        this.dish_image = dish_image;
    }
}
