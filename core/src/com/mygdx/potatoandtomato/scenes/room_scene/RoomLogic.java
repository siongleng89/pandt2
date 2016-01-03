package com.mygdx.potatoandtomato.scenes.room_scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.potatoandtomato.PTScreen;
import com.mygdx.potatoandtomato.absintflis.ConfirmResultListener;
import com.mygdx.potatoandtomato.absintflis.OnQuitListener;
import com.mygdx.potatoandtomato.absintflis.databases.DatabaseListener;
import com.mygdx.potatoandtomato.absintflis.downloader.DownloaderListener;
import com.mygdx.potatoandtomato.absintflis.gamingkit.UpdateRoomMatesCode;
import com.mygdx.potatoandtomato.absintflis.gamingkit.UpdateRoomMatesListener;
import com.mygdx.potatoandtomato.absintflis.push_notifications.PushCode;
import com.mygdx.potatoandtomato.absintflis.scenes.LogicAbstract;
import com.mygdx.potatoandtomato.absintflis.scenes.SceneAbstract;
import com.mygdx.potatoandtomato.enums.SceneEnum;
import com.mygdx.potatoandtomato.helpers.utils.SafeThread;
import com.mygdx.potatoandtomato.helpers.utils.Threadings;
import com.mygdx.potatoandtomato.models.*;
import com.potatoandtomato.common.BroadcastEvent;
import com.potatoandtomato.common.Broadcaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SiongLeng on 16/12/2015.
 */
public class RoomLogic extends LogicAbstract {

    RoomScene _scene;
    Room _room;
    GameClientChecker _gameClientChecker;
    HashMap<String, String> _noGameClientUsers;
    int _currentPercentage, _previousSentPercentage;
    SafeThread _downloadThread, _countDownThread;
    boolean _readyToStart;
    boolean _forceQuit;

    public Room getRoom() {
        return _room;
    }

    public RoomLogic(PTScreen screen, Services services, Object... objs) {
        super(screen, services, objs);

        _room = (Room) objs[0];
        _scene = new RoomScene(services, screen, _room);
        _noGameClientUsers = new HashMap();



    }

    @Override
    public void onInit() {
        super.onInit();

        _scene.populateGameDetails(_room.getGame());

        _room.addRoomUser(_services.getProfile());
        userJustJoinedRoom(_services.getProfile());
        openRoom();
        hostSendUpdateRoomStatePush();

        if(!isHost()) flushRoom(true, null);

        refreshRoomDesign();

        _services.getDatabase().monitorRoomById(_room.getId(), new DatabaseListener<Room>(Room.class) {
            @Override
            public void onCallback(Room obj, Status st) {
                if(st == Status.SUCCESS){
                    if(_countDownThread != null) _countDownThread.kill();
                    ArrayList<RoomUser> justJoinedUsers = _room.getJustJoinedUsers(obj);
                    ArrayList<RoomUser> justLeftUsers = _room.getJustLeftUsers(obj);

                    for(RoomUser u : justJoinedUsers){
                        userJustJoinedRoom(u.getProfile());
                    }
                    for(RoomUser u : justLeftUsers){
                        userJustLeftRoom(u.getProfile());
                    }

                    if(justJoinedUsers.size() > 0){
                        stopGameStartCountDown(justJoinedUsers.get(0).getProfile());
                    }
                    else if(justLeftUsers.size() > 0){
                        stopGameStartCountDown(justLeftUsers.get(0).getProfile());
                    }

                    _room = obj;
                    _services.getChat().setRoom(_room);
                    refreshRoomDesign();
                    checkHostInRoom();
                    if(justJoinedUsers.size() > 0 || justLeftUsers.size() > 0){
                        hostSendUpdateRoomStatePush();
                    }
                }
                else{
                    errorOccured();
                }
            }
        });

        _services.getDatabase().removeUserFromRoomOnDisconnect(_room, _services.getProfile(), new DatabaseListener<String>() {
            @Override
            public void onCallback(String obj, Status st) {
                if(st == Status.FAILED){
                    errorOccured();
                }
            }
        });

        _services.getGamingKit().addListener(new UpdateRoomMatesListener() {
            @Override
            public void onUpdateRoomMatesReceived(int code, String msg, String senderId) {
                receivedUpdateRoomMates(code, msg, senderId);
            }
        });

        _gameClientChecker = new GameClientChecker(_room.getGame(), _services.getPreferences(), _services.getDownloader(), new DownloaderListener() {
            @Override
            public void onCallback(byte[] bytes, Status st) {

            }
            @Override
            public void onStep(double percentage) {
                super.onStep(percentage);
                _currentPercentage = (int) percentage;
                downloadingGameNotify();
            }
        });

        _scene.getHostLeftConfirm().setListener(new ConfirmResultListener() {
            @Override
            public void onResult(Result result) {
                _forceQuit = true;
            }
        });

        _scene.getErrorConfirm().setListener(new ConfirmResultListener() {
            @Override
            public void onResult(Result result) {
                _forceQuit = true;
            }
        });

        _scene.getStartButton().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                hostSendStartGame();
            }
        });

        _scene.getInviteButton().addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                _screen.toScene(SceneEnum.INVITE, _room);
            }
        });

    }

    @Override
    public void onQuit(final OnQuitListener listener) {
        if(!_forceQuit){
            _scene.getLeaveRoomConfirm().setListener(new ConfirmResultListener() {
                @Override
                public void onResult(Result result) {
                    if(result == Result.YES){
                        _forceQuit = true;
                        leaveRoom();
                        listener.onResult(OnQuitListener.Result.YES);
                    }
                    else{
                        listener.onResult(OnQuitListener.Result.NO);
                    }
                }
            });
            _scene.showLeaveRoomConfirm(isHost());
        }
        else{
            leaveRoom();
            super.onQuit(listener);
        }
    }

    public void refreshRoomDesign(){
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                _scene.updateRoom(_room);
                int i = 0;
                for(Table t : _scene.getTeamTables()){
                    final int finalI = i;
                    t.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            super.clicked(event, x, y);
                            if(_room.changeTeam(finalI, _services.getProfile())){
                                refreshRoomDesign();
                                flushRoom(true, null);
                            }
                        }
                    });
                    i++;
                }
            }
        });

    }

    public void downloadingGameNotify(){
        if(_downloadThread == null){
            _downloadThread = new SafeThread();

            Threadings.runInBackground(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        if(_previousSentPercentage != _currentPercentage){
                            sendUpdateRoomMates(UpdateRoomMatesCode.UPDATE_DOWNLOAD, String.valueOf(_currentPercentage));
                            _previousSentPercentage = _currentPercentage;
                        }
                        Threadings.sleep(2000);
                        if(_downloadThread.isKilled()){
                            _gameClientChecker.killDownloads();
                            _previousSentPercentage = _currentPercentage = 0;
                            return;
                        }
                        if(_previousSentPercentage >= 100) return;
                    }
                }
            });

        }
    }

    public void sendUpdateRoomMates(int code, String msg){
        _services.getGamingKit().updateRoomMates(code, msg);
    }

    public void receivedUpdateRoomMates(int code, final String msg, final String senderId){
        if(code == UpdateRoomMatesCode.UPDATE_DOWNLOAD){
            if(Integer.valueOf(msg) < 100){
                _noGameClientUsers.put(senderId, msg);
            }
            else{
                _noGameClientUsers.remove(senderId);
            }
            if(_noGameClientUsers.size() > 0){
                Map.Entry<String, String> entry = _noGameClientUsers.entrySet().iterator().next();  //first item
                stopGameStartCountDown(_room.getProfileByUserId(entry.getKey()));
            }
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    _scene.updateDownloadPercentage(senderId, Integer.valueOf(msg));
                }
            });
        }
        else if(code == UpdateRoomMatesCode.START_GAME){
            startGame();
        }
    }

    public void userJustJoinedRoom(Profile user){
        Broadcaster.getInstance().broadcast(BroadcastEvent.CHAT_NEW_MESSAGE,
                        new ChatMessage(String.format(_services.getTexts().userHasJoinedRoom(), user.getDisplayName()), ChatMessage.FromType.SYSTEM, null));
    }

    public void userJustLeftRoom(Profile user){
        Broadcaster.getInstance().broadcast(BroadcastEvent.CHAT_NEW_MESSAGE,
                        new ChatMessage(String.format(_services.getTexts().userHasLeftRoom(), user.getDisplayName()), ChatMessage.FromType.SYSTEM, null));
    }

    public void openRoom(){
        _room.setOpen(true);
        _room.setPlaying(false);
        flushRoom(false, null);
    }

    public void leaveRoom(){
        if(isHost()){
            _room.setOpen(false);
        }
        _room.getRoomUsers().remove(_services.getProfile().getUserId());
        flushRoom(true, null);

        _services.getGamingKit().leaveRoom();
        Broadcaster.getInstance().broadcast(BroadcastEvent.DESTROY_ROOM);
    }

    public void flushRoom(boolean force, DatabaseListener listener){
        if(isHost() || force){
            _services.getDatabase().saveRoom(_room, listener);      //only host can save room
        }
    }

    public boolean checkHostInRoom(){
        if(_forceQuit) return false;

        boolean found = false;
        for(RoomUser roomUser : _room.getRoomUsers().values()){
            if(roomUser.getProfile().equals(_room.getHost())){
                found = true;
                break;
            }
        }
        if(!found) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    _scene.getHostLeftConfirm().setListener(new ConfirmResultListener() {
                        @Override
                        public void onResult(Result result) {
                            _forceQuit = true;
                            _screen.back();
                        }
                    });
                    _scene.hostLeft();
                }
            });
        }
        return found;
    }

    public void hostSendUpdateRoomStatePush(){
        if(isHost()){       //only host can send push notification to update room state
            boolean canStart = (startGameCheck(false) == 0);
            PushNotification push = new PushNotification();
            push.setId(PushCode.UPDATE_ROOM);
            push.setSticky(true);
            push.setTitle(_texts.PUSHRoomUpdateTitle());
            push.setMessage(String.format(_texts.PUSHRoomUpdateContent(), _room.getRoomUsersCount(), _room.getGame().getMaxPlayers()));
            push.setSilentNotification(true);
            push.setSilentIfInGame(false);

            for(RoomUser roomUser : _room.getRoomUsers().values()){
                if(!roomUser.getProfile().equals(_services.getProfile())){  //not host
                    _services.getGcmSender().send(roomUser.getProfile(), push);
                }
            }

            if(canStart){
                push.setTitle(_texts.PUSHRoomUpdateGameReadyTitle());
                push.setSilentIfInGame(true);
                push.setSilentNotification(false);
            }
            _services.getGcmSender().send(_services.getProfile(), push);
        }
    }

    public void hostSendGameStartedPush(){
        if(isHost()){       //only host can send push notification to update room state
            PushNotification push = new PushNotification();
            push.setId(PushCode.UPDATE_ROOM);
            push.setSticky(true);
            push.setTitle(_texts.PUSHRoomUpdateGameStartedTitle());
            push.setMessage(String.format(_texts.PUSHRoomUpdateContent(), _room.getRoomUsersCount(), _room.getGame().getMaxPlayers()));
            push.setSilentNotification(false);
            push.setSilentIfInGame(true);
            for(RoomUser roomUser : _room.getRoomUsers().values()){
                if(!roomUser.getProfile().equals(_services.getProfile())){  //not host
                    _services.getGcmSender().send(roomUser.getProfile(), push);
                }
            }
        }
    }

    public void errorOccured(){
        if(_forceQuit) return;
        else{
            _scene.getErrorConfirm().setListener(new ConfirmResultListener() {
                @Override
                public void onResult(Result result) {
                    _forceQuit = true;
                    _screen.back();
                }
            });
            _scene.showError();
        }
    }

    public int startGameCheck(boolean showMessage){
        if(!_room.checkAllTeamHasMinPlayers()){
            if(showMessage){
                _scene.showMessage(String.format(_services.getTexts().notEnoughPlayers(), _room.getGame().getTeamMinPlayers()));
            }
            return 1;
        }
        else if(_noGameClientUsers.size() > 0){
            if(showMessage){
                _scene.showMessage(_services.getTexts().stillDownloadingClient());
            }
            return 2;
        }
        else{
            return 0;
        }
    }

    public void hostSendStartGame(){
        if(startGameCheck(true) == 0){
             sendUpdateRoomMates(UpdateRoomMatesCode.START_GAME, "");
        }
    }

    public void startGame(){
        _countDownThread = new SafeThread();
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                int i = 3;
                _services.getChat().show();
                _services.getChat().expand();
                while(i > 0){
                    _services.getChat().add(new ChatMessage(String.format(_texts.gameStartingIn(), i), ChatMessage.FromType.IMPORTANT, null));
                    Threadings.sleep(1500);
                    i--;
                    if(_countDownThread.isKilled()){
                        _countDownThread = null;
                        return;
                    }
                }
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        gameStarted();
                    }
                });
            }
        });
    }

    public void stopGameStartCountDown(Profile profile){
        if(_countDownThread != null){
            _countDownThread.kill();
            if(profile != null){
                _services.getChat().add(new ChatMessage(String.format(_texts.gameStartStop(),
                        profile.getDisplayName()), ChatMessage.FromType.SYSTEM, null));
            }
        }
    }


    public void gameStarted(){
        _room.setOpen(false);
        _room.setPlaying(true);
        _room.setRoundCounter(_room.getRoundCounter()+1);
        flushRoom(false, null);
        _services.getDatabase().savePlayedHistory(_services.getProfile(), _room, null);
        hostSendGameStartedPush();
        _services.getChat().add(new ChatMessage(_texts.gameStarted(), ChatMessage.FromType.SYSTEM, null));
        _services.getChat().hide();
        _screen.toScene(SceneEnum.GAME_SANDBOX, _room);
    }

    public void gameFinished(){
        openRoom();
        hostSendUpdateRoomStatePush();
    }

    private boolean isHost(){
        return _room.getHost().equals(_services.getProfile());
    }

    @Override
    public SceneAbstract getScene() {
        return _scene;
    }

    @Override
    public void onShow() {
        super.onShow();
        _services.getChat().setRoom(_room);
        _services.getChat().show();
        _services.getChat().expand();
    }

    @Override
    public void dispose() {
        super.dispose();
        _services.getChat().hide();
        _services.getChat().resetChat();
    }
}