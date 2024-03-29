package com.potatoandtomato.common.models;

import com.badlogic.gdx.graphics.Color;
import com.potatoandtomato.common.utils.ColorUtils;
import com.shaded.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by SiongLeng on 25/12/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {

    public String name;
    public String userId;
    public String country;
    public boolean isHost;
    public int slotIndex;
    public Color userColor;

    public Player() {
    }

    public Player(String name, String userId, String country, boolean isHost, int slotIndex) {
        this.name = name;
        this.userId = userId;
        this.country = country;
        this.isHost = isHost;
        this.slotIndex = slotIndex;
        this.userColor = ColorUtils.getUserColorByIndex(slotIndex);
    }

    public boolean getIsHost() {
        return isHost;
    }

    public void setIsHost(boolean isHost) {
        this.isHost = isHost;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Color getUserColor() {
        return userColor;
    }

    public void setUserColor(Color userColor) {
        this.userColor = userColor;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
