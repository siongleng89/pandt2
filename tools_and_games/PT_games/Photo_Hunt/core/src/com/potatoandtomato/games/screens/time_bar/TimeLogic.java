package com.potatoandtomato.games.screens.time_bar;

import com.badlogic.gdx.utils.Disposable;
import com.potatoandtomato.common.GameCoordinator;
import com.potatoandtomato.common.utils.SafeThread;
import com.potatoandtomato.common.utils.Threadings;
import com.potatoandtomato.games.absintf.GameModelListener;
import com.potatoandtomato.games.assets.Sounds;
import com.potatoandtomato.games.enums.GameState;
import com.potatoandtomato.games.helpers.Logs;
import com.potatoandtomato.games.models.GameModel;
import com.potatoandtomato.games.models.Services;
import com.potatoandtomato.games.models.SimpleRectangle;
import com.potatoandtomato.games.statics.Global;

import java.util.ArrayList;

/**
 * Created by SiongLeng on 6/4/2016.
 */
public class TimeLogic implements Disposable {

    private Services services;
    private GameModel gameModel;
    private GameCoordinator gameCoordinator;
    private boolean paused;
    private boolean started;
    private ArrayList<SafeThread> timeThreads;
    private TimeActor timeActor;
    private KingLogic kingLogic;
    private CastleLogic castleLogic;
    private KnightLogic knightLogic;

    public TimeLogic(Services services, GameCoordinator gameCoordinator, KingLogic kingLogic,
                                CastleLogic castleLogic, KnightLogic knightLogic, GameModel gameModel) {
        this.services = services;
        this.gameCoordinator = gameCoordinator;
        this.gameModel = gameModel;
        this.kingLogic = kingLogic;
        this.castleLogic = castleLogic;
        this.knightLogic = knightLogic;
        this.timeThreads = new ArrayList();


        this.timeActor = new TimeActor(services, gameCoordinator);
        this.timeActor.populate(kingLogic.getKingActor(), castleLogic.getCastleActor(), knightLogic.getKnightActor());
        setListeners();
    }

    public void restart(){
        gameModel.setFreezingMiliSecs(0);
        stop();
        start();
    }

    public void startIfNotStarted(){
        if(!started){
            start();
        }
    }

    private void start(){
        started = true;
        final SafeThread timeThread = new SafeThread();
        timeThreads.add(timeThread);

        if(Global.REVIEW_MODE){
            return;
        }

        final int renderPeriodMiliSecs = 50;

        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if(timeThread.isKilled() || gameModel.getRemainingMiliSecs() <= 0) break;
                    else{

                        Threadings.sleep(renderPeriodMiliSecs);

                        if(paused) continue;

                        if (gameModel.getFreezingMiliSecs() > 0){
                            gameModel.setFreezingMiliSecs(gameModel.getFreezingMiliSecs() - renderPeriodMiliSecs);
                            continue;
                        }

                        gameModel.setRemainingMiliSecs(gameModel.getRemainingMiliSecs() - renderPeriodMiliSecs, true);
                    }
                }
            }
        });
    }

    public void setPause(boolean pause){
        this.paused = pause;
    }

    public void stop(){
        if(timeThreads.size() > 0){
            for(SafeThread safeThread : timeThreads){
                safeThread.kill();
            }
            timeThreads.clear();
        }
        setPause(true);
        started = false;
    }

    public void reduceTime(){
        if(gameModel.getFreezingMiliSecs() > 0){
            gameModel.setFreezingMiliSecs(0);
        }
        else{
            gameModel.setRemainingMiliSecs(gameModel.getRemainingMiliSecs() - 2000, true);
        }
    }

    public void setListeners(){
        gameModel.addGameModelListener(new GameModelListener() {
            @Override
            public void onStageNumberChanged(int newStageNumber) {

            }

            @Override
            public void onTimeFinished() {
                Threadings.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        knightLogic.updatePosition(0);
                    }
                });
                stop();
                Logs.show("Time up!");
            }

            @Override
            public void onCorrectClicked(SimpleRectangle rectangle, int remainingMiliSecsWhenClicked) {
                gameModel.addFreezeMiliSecs();
            }

            @Override
            public void onGameStateChanged(GameState oldState, GameState newState) {
                if(newState == GameState.Playing){
                    restart();
                    setPause(false);
                    services.getSoundsWrapper().playSounds(Sounds.Name.START_STAGE);
                }
                else if(newState == GameState.BeforeContinue){
                    setPause(false);
                }
                else if(newState == GameState.WaitingForNextStage){
                    stop();
                }
                else{
                    setPause(true);
                }
            }
        });
    }

    @Override
    public void dispose() {
        stop();
    }

    public TimeActor getTimeActor() {
        return timeActor;
    }

    public GameModel getGameModel() {
        return gameModel;
    }

    public KingLogic getKingLogic() {
        return kingLogic;
    }

    public CastleLogic getCastleLogic() {
        return castleLogic;
    }

    public KnightLogic getKnightLogic() {
        return knightLogic;
    }
}
