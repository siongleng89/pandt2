package com.potatoandtomato.games.screens.time_bar;

import com.potatoandtomato.common.GameCoordinator;
import com.potatoandtomato.common.utils.Threadings;
import com.potatoandtomato.games.absintf.GameModelListener;
import com.potatoandtomato.games.assets.Sounds;
import com.potatoandtomato.games.enums.GameState;
import com.potatoandtomato.games.enums.KnightState;
import com.potatoandtomato.games.enums.StageType;
import com.potatoandtomato.games.models.GameModel;
import com.potatoandtomato.games.models.Services;

/**
 * Created by SiongLeng on 12/4/2016.
 */
public class KnightLogic {

    private GameModel gameModel;
    private Services services;
    private GameCoordinator gameCoordinator;
    private KnightActor knightActor;
    private float totalMiliSecs;
    private float totalMovingMiliSecs;
    private float totalAtkMiliSecs;
    private float totalDistance;
    private boolean freezed;
    private int playingMusic;

    public KnightLogic(GameModel gameModel, Services services, GameCoordinator gameCoordinator) {
        this.gameModel = gameModel;
        this.services = services;
        this.gameCoordinator = gameCoordinator;
        this.totalDistance = 450;
        this.playingMusic = -1;

        this.knightActor = new KnightActor(services, totalDistance);
        reset();
        setListeners();
    }

    public void reset(){
        //gameModel.setCastleAttackedCount(0);
        this.totalMiliSecs = gameModel.getThisStageTotalMiliSecs();
        this.totalMovingMiliSecs = gameModel.getThisStageTotalMovingMiliSecs();
        this.totalAtkMiliSecs = gameModel.getThisStageTotalAtkMiliSecs();
        knightActor.setKnightAtkSpeed(totalMiliSecs / (3000 * 1000));
        knightActor.changeState(KnightState.Walk, false);
        setFreezed(false);
        knightActor.setKnightPositionX(totalDistance, false, false);
        stopAllMusic();
    }

    public void updatePosition(float remainingMiliSecs){
        float distance = getRemainingDistanceByRemainingTime(remainingMiliSecs);
        knightActor.setKnightPositionX(distance, true, true);

        if(gameModel.getStageType() != StageType.Bonus){
            startMusic(distance <= 50);
        }
    }

    public void kingCapture(){
        knightActor.changeState(KnightState.Won, true);
    }

    public void setFreezed(boolean freezed) {
        if(this.freezed != freezed){
            this.freezed = freezed;
            knightActor.setFreeze(freezed);
        }
    }

    public void setPause(boolean pause){
        if(pause){
            knightActor.stopAnimation();
            stopAllMusic();
        }
        else{
            knightActor.continueAnimation();
        }
    }

    public float getRemainingDistanceByRemainingTime(float remainingMiliSecs){
        float remainingDistance = 0;
        if(totalMiliSecs - remainingMiliSecs <= totalMovingMiliSecs){
            float percent =  (remainingMiliSecs - totalAtkMiliSecs) / totalMovingMiliSecs;
            remainingDistance = totalDistance * percent;
            if(remainingDistance < 0) remainingDistance = 0;
        }
        return remainingDistance;
    }

    public void stopAllMusic(){
        services.getSoundsWrapper().stopMusic(Sounds.Name.ATTACKING_CASTLE_MUSIC);
        services.getSoundsWrapper().stopMusic(Sounds.Name.GAME_PLAYING_MUSIC);
        playingMusic = -1;
    }

    public void startMusic(boolean isDanger){
        if(isDanger && playingMusic != 2){
            stopAllMusic();
            services.getSoundsWrapper().playMusic(Sounds.Name.ATTACKING_CASTLE_MUSIC);
            playingMusic = 2;
        }
        else if(!isDanger && playingMusic != 1){
            stopAllMusic();
            services.getSoundsWrapper().playMusic(Sounds.Name.GAME_PLAYING_MUSIC);
            playingMusic = 1;
        }
    }

    public void setListeners(){
        gameModel.addGameModelListener(new GameModelListener() {

            @Override
            public void onStageNumberChanged(int newStageNumber) {
                Threadings.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        reset();
                    }
                });
            }

            @Override
            public void onRemainingMiliSecsChanged(final int remainingMiliSecs) {
                Threadings.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if(remainingMiliSecs != gameModel.getThisStageTotalMiliSecs()){
                            updatePosition(remainingMiliSecs);
                        }
                    }
                });
            }

            @Override
            public void onFreezingMiliSecsChanged(final int remainingMiliSecs) {
                setFreezed(remainingMiliSecs > 0);
            }

            @Override
            public void onGameStateChanged(GameState oldState, final GameState newState) {
                Threadings.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if(newState == GameState.Playing){
                            setPause(false);
                        }
                        else if(newState == GameState.Lose){
                            setPause(true);
                            kingCapture();
                        }
                        else if(newState == GameState.Won || newState == GameState.WonWithoutContributions){
                            setPause(true);
                        }
                        else if(newState == GameState.BeforeNewGame){
                            setPause(true);
                        }
                    }
                });
            }
        });

        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                knightActor.getKnightAtkAnimator().callBackOnIndex(17, new Runnable() {
                    @Override
                    public void run() {
                        knightActor.popStars();
                        gameModel.setCastleAttackedCount(gameModel.getCastleAttackedCount() + 1);
                    }
                });
            }
        });


    }

    public KnightActor getKnightActor() {
        return knightActor;
    }

    public void setKnightActor(KnightActor knightActor) {
        this.knightActor = knightActor;
    }
}
