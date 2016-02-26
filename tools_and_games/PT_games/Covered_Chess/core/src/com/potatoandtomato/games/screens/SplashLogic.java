package com.potatoandtomato.games.screens;

import com.potatoandtomato.common.GameCoordinator;
import com.potatoandtomato.common.Threadings;
import com.potatoandtomato.games.assets.Sounds;
import com.potatoandtomato.games.models.Services;
import com.potatoandtomato.games.statics.Global;

/**
 * Created by SiongLeng on 24/2/2016.
 */
public class SplashLogic {

    private SplashActor _splashActor;
    private Services _services;

    public SplashLogic(GameCoordinator coordinator, Services services) {
        this._services = services;
        _splashActor = new SplashActor(coordinator, services.getAssets(), services.getTexts());

        _services.getSoundsWrapper().playSounds(Sounds.Name.START_GAME);
        startFadeOutThread();
    }

    public void startFadeOutThread(){
        Threadings.delay(!Global.NO_ENTRANCE ? 7000 : 0, new Runnable() {
            @Override
            public void run() {
                _splashActor.fadeOutActor(new Runnable() {
                    @Override
                    public void run() {
                        _splashActor.remove();
                        _services.getSoundsWrapper().playTheme();
                    }
                });
            }
        });
    }

    public SplashActor getSplashActor() {
        return _splashActor;
    }
}