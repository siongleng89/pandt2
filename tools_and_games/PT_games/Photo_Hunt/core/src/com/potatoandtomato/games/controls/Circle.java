package com.potatoandtomato.games.controls;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.potatoandtomato.common.GameCoordinator;
import com.potatoandtomato.games.assets.Textures;
import com.potatoandtomato.games.models.Services;

/**
 * Created by SiongLeng on 13/4/2016.
 */
public class Circle extends Image {

    private Services services;
    private GameCoordinator gameCoordinator;
    private Color userColor;

    public Circle(GameCoordinator gameCoordinator, Services services, String userId) {
        this.services = services;
        this.gameCoordinator = gameCoordinator;
        this.userColor = gameCoordinator.getPlayerByUserId(userId).getUserColor();

        this.setDrawable(new TextureRegionDrawable(services.getAssets().getTextures().get(Textures.Name.CIRCLE)));
        this.setColor(userColor);
    }
}