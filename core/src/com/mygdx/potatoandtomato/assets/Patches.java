package com.mygdx.potatoandtomato.assets;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.potatoandtomato.common.absints.PTAssetsManager;
import com.potatoandtomato.common.assets.PatchAssets;

import java.util.HashMap;

/**
 * Created by SiongLeng on 9/2/2016.
 */
public class Patches extends PatchAssets{

    public Patches(PTAssetsManager assetsManager) {
        super(assetsManager);
    }

    public enum Name{
        CHAT_BOX, POPUP_BG,
        BTN_GREEN, BTN_BLUE, BTN_RED,
        WHITE_ROUNDED_BG, TRANS_BLACK_ROUNDED_BG, EXPANDABLE_TITLE_BG, TUTORIAL_BG, ORANGE_ROUNDED_BG, GREY_ROUNDED_BG,
        YELLOW_GRADIENT_BOX, YELLOW_GRADIENT_BOX_ROUNDED,
        SCROLLBAR_VERTICAL_HANDLE, WOOD_BG_SMALL_PATCH, WOOD_BG_FAT_PATCH,
        GAMELIST_BG, TEXT_FIELD_BG, INVITE_BG,
        INVITE_TAB_LEFT, INVITE_TAB_CENTER, INVITE_TAB_RIGHT, LEADERBOARD_ANIMATING_BASE,
        SHOP_WOOD_BTN, SHOP_WOOD_BTN_ONPRESS,
        CHATBOX_FOCUS, CHATBOX_UNFOCUS, CHAT_POPUP_BG,
        RIGHT_CURVE_TAB_HEADER, LEFT_CURVE_TAB_HEADER,
        BLUE_FRAME_PIXEL, GREEN_FRAME_PIXEL,
        INBOX_MSG_BG,
        SCROLLBAR_ORANGE_HANDLE, SCROLLBAR_ORANGE_BG,
        LEADERBOARD_FRIENDS_COLLAPSED_BG, LEADERBOARD_FRIENDS_BG, LEADERBOARD_FRIENDS_EXPANDED_BG
    }

}
