package com.mygdx.potatoandtomato.scenes.prerequisite_scene;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.potatoandtomato.PTScreen;
import com.mygdx.potatoandtomato.absintflis.OnQuitListener;
import com.mygdx.potatoandtomato.absintflis.databases.DatabaseListener;
import com.mygdx.potatoandtomato.absintflis.gamingkit.JoinRoomListener;
import com.mygdx.potatoandtomato.absintflis.gamingkit.RoomInfoListener;
import com.mygdx.potatoandtomato.absintflis.scenes.LogicAbstract;
import com.mygdx.potatoandtomato.absintflis.scenes.SceneAbstract;
import com.mygdx.potatoandtomato.enums.AnalyticEvent;
import com.mygdx.potatoandtomato.enums.RoomUserState;
import com.mygdx.potatoandtomato.enums.SceneEnum;
import com.mygdx.potatoandtomato.helpers.Analytics;
import com.mygdx.potatoandtomato.services.Texts;
import com.mygdx.potatoandtomato.models.Game;
import com.mygdx.potatoandtomato.models.Room;
import com.mygdx.potatoandtomato.models.Services;
import com.potatoandtomato.common.enums.Status;

/**
 * Created by SiongLeng on 15/12/2015.
 */
public class PrerequisiteLogic extends LogicAbstract {

    PrerequisiteScene _scene;
    Game _game;
    Texts _texts;
    JoinType _joinType;
    String _roomId;
    Room _joiningRoom;
    boolean _roomInfoRetrieved;

    public PrerequisiteLogic(PTScreen screen, Services services, Object... objs) {
        super(screen, services, objs);
        setSaveToStack(false);
        _texts = _services.getTexts();
        _scene = new PrerequisiteScene(services, screen);
        _game = (Game) objs[0];
        _joinType = (JoinType) objs[1];
        if(objs.length > 2) _roomId = (String) objs[2];
    }

    @Override
    public void onQuit(OnQuitListener listener) {
        _services.getGamingKit().leaveRoom();
        super.onQuit(listener);
    }

    @Override
    public void onInit() {
        super.onInit();
        _services.getAutoJoiner().stopAutoJoinRoom();
        restart();
    }

    public void restart(){
        if(_joinType == JoinType.CREATING){
            Analytics.log(AnalyticEvent.CreatingGame);
        }
        else if(_joinType == JoinType.JOINING){
            Analytics.log(AnalyticEvent.JoiningGame);
        }
        else if(_joinType == JoinType.CONTINUING){
            Analytics.log(AnalyticEvent.ContinuingGame);
        }

        _roomInfoRetrieved = false;
        _services.getDatabase().clearAllOnDisconnectListenerModel();
        if(_joinType == JoinType.CREATING){
            createRoom();
        }
        else{
            joinRoom();
        }
    }

    public void createRoom(){
        _scene.changeMessage(_texts.lookingForServer());

        _services.getDatabase().getGameSimpleByAbbr(_game.getAbbr(), new DatabaseListener<Game>(Game.class) {
            @Override
            public void onCallback(Game obj, Status st) {
                if(st == Status.SUCCESS){
                    _game = obj;

                    _services.getGamingKit().addListener(getClassTag(), new JoinRoomListener() {
                        @Override
                        public void onRoomJoined(String roomId) {
                            if(isDisposing()) return;
                            createRoomSuccess(roomId);
                        }

                        @Override
                        public void onJoinRoomFailed() {
                            joinRoomFailed(0);
                        }
                    });
                    _services.getGamingKit().createAndJoinRoom();
                }
                else{
                    joinRoomFailed(0);
                }
            }
        });


    }

    public void joinRoom(){
        _scene.changeMessage(_texts.locatingRoom());

        _services.getDatabase().getRoomById(_roomId, new DatabaseListener<Room>(Room.class) {
            @Override
            public void onCallback(Room obj, Status st) {
                if(st == Status.SUCCESS && obj != null){
                    if(isDisposing()) return;

                    int minusMe = (obj.getRoomUserByUserId(_services.getProfile().getUserId()) == null) ? 0 : 1;

                    if(obj.getRoomUsersCount() - minusMe >= Integer.valueOf(obj.getGame().getMaxPlayers())){
                        joinRoomFailed(1);
                        return;
                    }

                    if(!obj.isOpen() && _joinType == JoinType.JOINING){
                        joinRoomFailed(2);
                        return;
                    }

                    if(_joinType == JoinType.CONTINUING && !obj.canContinue(_services.getProfile())){
                        joinRoomFailed(3);
                        return;
                    }

                    _joiningRoom = obj;

                    _services.getGamingKit().addListener(getClassTag(), new JoinRoomListener() {
                        @Override
                        public void onRoomJoined(String roomId) {
                            _scene.changeMessage(_texts.joiningRoom());
                            _services.getGamingKit().addListener(getClassTag(), new RoomInfoListener(roomId, getClassTag()) {
                                @Override
                                public void onRoomInfoRetrievedSuccess(String[] inRoomUserIds) {
                                    if(isDisposing()) return;

                                    if(!_roomInfoRetrieved){
                                        _roomInfoRetrieved = true;
                                        if(inRoomUserIds.length == 1){  //only one people and thats myself, error
                                            joinRoomFailed(4);
                                        }
                                        else{
                                            joinRoomSuccess();
                                        }
                                    }
                                }

                                @Override
                                public void onRoomInfoFailed() {
                                    if(!_roomInfoRetrieved){
                                        _roomInfoRetrieved = true;
                                        joinRoomFailed(4);
                                    }
                                }
                            });
                            _services.getGamingKit().getRoomInfo(_joiningRoom.getWarpRoomId(), getClassTag());
                        }

                        @Override
                        public void onJoinRoomFailed() {
                            joinRoomFailed(4);
                        }
                    });

                    _services.getGamingKit().joinRoom(_joiningRoom.getWarpRoomId());
                }
                else{
                    joinRoomFailed(0);
                }
            }
        });
    }

    public void joinRoomFailed(final int reason){
        if(isDisposing()) return;

        if(reason == 0){    //general msg
            _scene.failedMessage(_texts.joinRoomFailed(), true);
        }
        else if(reason == 1){    //full room
            _scene.failedMessage(_texts.roomIsFull(), true);
        }
        else if(reason == 2){    //room is not open
            _scene.failedMessage(_texts.roomNotAvailable(), false);
        }
        else if(reason == 3){   //cannot continue game
            _scene.failedMessage(_texts.cannotContinue(), false);
        }
        else if(reason == 4){     //room no user anymore
            _scene.failedMessage(_texts.roomNotAvailable(), false);
            _services.getDatabase().updateRoomPlayingAndOpenState(_joiningRoom, false, false, null);
        }

        _services.getGamingKit().leaveRoom();

    }

    public void createRoomSuccess(final String roomId){
        if(isDisposing()) return;

        _scene.changeMessage(_texts.joiningRoom());
        _joiningRoom = new Room();
        _joiningRoom.setWarpRoomId(roomId);
        _joiningRoom.setGame(_game);
        _joiningRoom.setOpen(false);
        _joiningRoom.setHost(_services.getProfile());
        _joiningRoom.setPlaying(false);
        _joiningRoom.setRoundCounter(0);
        _joiningRoom.addRoomUser(_services.getProfile(), RoomUserState.NotReady);
        _services.getDatabase().saveRoom(_joiningRoom, true, new DatabaseListener<String>() {
            @Override
            public void onCallback(String obj, Status st) {
                if(isDisposing()) return;

                if (st == Status.SUCCESS) {
                    _screen.toScene(SceneEnum.ROOM, _joiningRoom, false);
                } else {
                    joinRoomFailed(0);
                }
            }
        });
    }

    public void joinRoomSuccess(){
        if(isDisposing()) return;
        _screen.toScene(SceneEnum.ROOM, _joiningRoom, _joinType == JoinType.CONTINUING);
    }


    @Override
    public void setListeners() {
        super.setListeners();
        _scene.getRetryButton().addListener((new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                restart();     //retry whole process
            }
        }));

        _scene.getQuitButton().addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                _screen.back();
            }
        });

    }

    @Override
    public SceneAbstract getScene() {
        return _scene;
    }

    public Room getJoiningRoom() {
        return _joiningRoom;
    }

    public enum JoinType{
        CREATING, JOINING, CONTINUING
    }



}
